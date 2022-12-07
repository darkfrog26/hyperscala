package io.youi.client

import cats.effect.IO

import java.io.IOException
import java.net.URI
import java.nio.ByteBuffer
import io.undertow.protocols.ssl.UndertowXnioSsl
import io.undertow.server.DefaultByteBufferPool
import io.undertow.util.Headers
import io.undertow.websockets.client.WebSocketClient.ConnectionBuilder
import io.undertow.websockets.client.{WebSocketClientNegotiation, WebSocketClient => UndertowWebSocketClient}
import io.undertow.websockets.core._
import io.youi.http.{ByteBufferData, ConnectionStatus, WebSocket}
import io.youi.net.URL
import io.youi.server.KeyStore
import io.youi.server.util.SSLUtil
import io.youi.util.Time
import org.xnio.{IoFuture, OptionMap, Options, Xnio}
import reactify.Var
import scribe.Execution.global

import scala.concurrent.duration._
import scala.concurrent.{Future, Promise}
import scala.jdk.CollectionConverters._

class WebSocketClient(url: URL,
                      keyStore: Option[KeyStore] = None,
                      autoReconnect: Boolean = true,
                      reconnectDelay: FiniteDuration = 5.seconds,
                      directBuffer: Boolean = false,
                      bufferSize: Int = 2048,
                      workerThreads: Int = 2,
                      highWater: Int = 1000000,
                      lowWater: Int = 1000000,
                      coreThreads: Int = 30,
                      maxThreads: Int = 30,
                      noDelay: Boolean = true,
                      buffered: Boolean = true,
                      authorization: => Option[String] = None) extends WebSocket {
  private lazy val worker = Xnio.getInstance().createWorker(OptionMap.builder()
    .set(Options.KEEP_ALIVE, true)
    .set(Options.WORKER_IO_THREADS, workerThreads)
    .set(Options.CONNECTION_HIGH_WATER, highWater)
    .set(Options.CONNECTION_LOW_WATER, lowWater)
    .set(Options.WORKER_TASK_CORE_THREADS, coreThreads)
    .set(Options.WORKER_TASK_MAX_THREADS, maxThreads)
    .set(Options.TCP_NODELAY, noDelay)
    .set(Options.CORK, buffered)
    .getMap
  )
  private lazy val bufferPool = new DefaultByteBufferPool(directBuffer, bufferSize)
  private lazy val connectionBuilder: ConnectionBuilder = {
    val builder = UndertowWebSocketClient.connectionBuilder(worker, bufferPool, new URI(url.toString))
      .setClientNegotiation(new WebSocketClientNegotiation(null, null) {
        override def beforeRequest(headers: java.util.Map[String, java.util.List[String]]): Unit = {
          authorization.foreach { auth =>
            headers.put(Headers.AUTHORIZATION_STRING, List(auth).asJava)
          }
          headers.put(Headers.UPGRADE_STRING, List("websocket").asJava)
          headers.put(Headers.CONNECTION_STRING, List("Upgrade").asJava)
        }
      })
    keyStore.foreach { ks =>
      val sslContext = SSLUtil.createSSLContext(ks.location, ks.password)
      builder.setSsl(new UndertowXnioSsl(Xnio.getInstance(), OptionMap.EMPTY, sslContext))
    }
    builder
  }

  private val _channel = Var[Option[WebSocketChannel]](None)
  def channel: WebSocketChannel = _channel.get.getOrElse(throw new RuntimeException("No connection has been established."))

  private var backlog = List.empty[AnyRef]

  def connect(): IO[ConnectionStatus] = if (_channel.get.isEmpty) {
    val promise = Promise[ConnectionStatus]()
    connectionBuilder.connect().addNotifier(new IoFuture.HandlingNotifier[WebSocketChannel, Any] {
      override def handleDone(data: WebSocketChannel, attachment: Any): Unit = {
        _channel @= Some(data)
        channel.resumeReceives()

        // Receive messages
        channel.getReceiveSetter.set(new AbstractReceiveListener {
          override def onFullTextMessage(channel: WebSocketChannel, message: BufferedTextMessage): Unit = {
            val data = message.getData
            receive.text @= data
          }

          override def onError(channel: WebSocketChannel, error: Throwable): Unit = {
            super.onError(channel, error)
            disconnect()
          }
        })

        // Send messages
        send.text.attach { message =>
          checkBacklog()
          sendMessage(message)
        }
        send.binary.attach {
          case ByteBufferData(message) => {
            checkBacklog()
            sendMessage(message)
          }
        }
        _status @= ConnectionStatus.Open
        scribe.info(s"Connected to $url successfully")

        checkBacklog()

        promise.success(ConnectionStatus.Open)
      }

      override def handleFailed(exception: IOException, attachment: Any): Unit = {
        _channel @= None
        if (autoReconnect) {
          scribe.warn(s"Connection closed or unable to connect to $url (${exception.getMessage}). Trying again in ${reconnectDelay.toSeconds} seconds...")
          IO.sleep(reconnectDelay).map(_ => connect())
        } else {
          scribe.warn("Connection closed or unable to connect.")
        }
      }
    }, None.orNull)
    promise.future
  } else {
    Future.successful(status())
  }

  def disconnect(): Unit = if (status() == ConnectionStatus.Open) {
    channel.close()
    _status @= ConnectionStatus.Closed
  }

  def dispose(): Unit = {
    disconnect()
    worker.shutdown()
  }

  private def checkBacklog(): Unit = {
    synchronized {
      if (backlog.nonEmpty) {
        val messages = backlog
        backlog = Nil
        messages.foreach {
          case text: String => sendMessage(text)
          case binary: ByteBuffer => sendMessage(binary)
        }
      }
    }
  }

  private def sendMessage(message: String): Unit = {
    WebSockets.sendText(message, channel, new WebSocketCallback[Void] {
      override def complete(channel: WebSocketChannel, context: Void): Unit = {
        // Successfully sent
      }

      override def onError(channel: WebSocketChannel, context: Void, throwable: Throwable): Unit = WebSocketClient.this synchronized {
        backlog = message :: backlog
        disconnect()
      }
    })
  }

  private def sendMessage(message: ByteBuffer): Unit = {
    WebSockets.sendBinary(message, channel, new WebSocketCallback[Void] {
      override def complete(channel: WebSocketChannel, context: Void): Unit = {
        // Successfully sent
      }

      override def onError(channel: WebSocketChannel, context: Void, throwable: Throwable): Unit = WebSocketClient.this synchronized {
        backlog = message :: backlog
        disconnect()
      }
    })
  }

  status.attach {
    case ConnectionStatus.Closed => {
      _channel @= None

      if (autoReconnect) {
        connect()
      }
    }
    case _ => // Ignore others
  }
}
