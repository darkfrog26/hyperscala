package io.youi.app

import io.youi.ajax.AjaxRequest
import io.youi.app.screen.ScreenManager
import io.youi.{History, JavaScriptError, JavaScriptLog, LocalStorage}
import io.youi.app.sourceMap.ErrorTrace
import org.scalajs.dom._
import io.youi.dom._
import io.youi.net.URL
import profig.JsonUtil
import scribe.{Level, LogRecord}
import scribe.output.LogOutput
import scribe.writer.Writer

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.scalajs.js
import scala.scalajs.js.|

trait ClientApplication extends YouIApplication with ScreenManager {
  ClientApplication.instance = this

  def remoteHost: String = window.location.host

  addScript("/source-map.min.js")

  override def isClient: Boolean = true

  override def isServer: Boolean = false

  // Configure communication end-points
  private var configuredConnectivity: Map[ApplicationConnectivity, ClientConnectivity] = Map.empty

  def clientConnectivity(connectivity: ApplicationConnectivity): ClientConnectivity = configuredConnectivity(connectivity)

  private val errorFunction: js.Function5[String, String, Int, Int, Throwable | js.Error, Unit] = (message: String, source: String, line: Int, column: Int, err: Throwable | js.Error) => {
    err match {
      case null => ErrorTrace.toError(message, source, line, column, None).map(ClientApplication.sendError)
      case t: Throwable => ErrorTrace.toError(message, source, line, column, Some(t)).map(ClientApplication.sendError)
      case e: js.Error => ErrorTrace.toError(message, source, line, column, Some(js.JavaScriptException(e))).map(ClientApplication.sendError)
    }
    ()
  }

  if (logJavaScriptErrors) {
    js.Dynamic.global.window.onerror = errorFunction
    scribe.Logger.root.withHandler(writer = ErrorTrace).replace()
  }

  connectivityEntries.attachAndFire { entries =>
    entries.foreach { connectivity =>
      if (!configuredConnectivity.contains(connectivity)) {
        configuredConnectivity += connectivity -> new ClientConnectivity(connectivity, this)
      }
    }
  }

  // Client-side management and caching of URL-based session id
  LocalStorage.get[String]("sessionId") match {
    case Some(sessionId) => if (History.url().param("sessionId").contains(sessionId)) {
      // Already set, nothing needed
    } else {
      // Redirect to stored session id
      val url = History.url().withParam("sessionId", sessionId, append = false)
      History.set(url)
    }
    case None => History.url.param("sessionId").foreach { sessionId =>
      // Store the session id in local storage
      LocalStorage("sessionId") = sessionId
    }
  }

  def reconnectStrategy: ReconnectStrategy = ReconnectStrategy.Reload

  def reConnect(): Unit = {
    configuredConnectivity.values.foreach(_.disconnect())
    configuredConnectivity = Map.empty

    connectivityEntries.foreach { connectivity =>
      if (!configuredConnectivity.contains(connectivity)) {
        configuredConnectivity += connectivity -> new ClientConnectivity(connectivity, this)
      }
    }

    reConnected()
  }

  protected def reConnected(): Unit = {}

  override def cached(url: URL): String = url.asPath()
}

object ClientApplication {
  def logWriter(maximumBytes: Long = -1L,
                maximumRecords: Int = -1,
                maximumErrors: Int = -1): Writer = new Writer {
    private var bytesWritten = 0L
    private var recordsWritten = 0
    private var errorsWritten = 0
    private var enabled = true

    override def write[M](record: LogRecord[M], output: LogOutput): Unit = if (enabled) {
      val text = output.plainText
      bytesWritten += text.length
      recordsWritten += 1
      if (record.level >= Level.Error) errorsWritten += 1
      sendLog(JavaScriptLog(text))
      if (maximumBytes != -1L && bytesWritten >= maximumBytes) {
        enabled = false
      } else if (maximumRecords != -1 && recordsWritten >= maximumRecords) {
        enabled = false
      } else if (maximumErrors != -1 && errorsWritten >= maximumErrors) {
        enabled = false
      }
    }
  }

  private var instance: ClientApplication = _

  def sendError(throwable: Throwable): Future[XMLHttpRequest] = {
    ErrorTrace.toError(throwable).flatMap(sendError)
  }

  def sendError(error: JavaScriptError): Future[XMLHttpRequest] = {
    val formData = new FormData
    val jsonString = JsonUtil.toJsonString(error)
    formData.append("error", jsonString)
    val request = new AjaxRequest(History.url().replacePathAndParams(instance.logPath), data = Some(formData))
    request.send()
  }

  def sendError(event: ErrorEvent): Future[XMLHttpRequest] = {
    ErrorTrace.toError(event).flatMap(sendError)
  }

  def sendLog(log: JavaScriptLog): Future[XMLHttpRequest] = {
    val formData = new FormData
    val jsonString = JsonUtil.toJsonString(log)
    formData.append("message", jsonString)
    val request = new AjaxRequest(History.url().replacePathAndParams(instance.logPath), data = Some(formData))
    request.send()
  }
}