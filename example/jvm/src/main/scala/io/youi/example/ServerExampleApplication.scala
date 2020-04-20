package io.youi.example

import io.youi.app._
import io.youi.http._
import io.youi.http.content.Content
import io.youi.net._
import io.youi.server.WebSocketListener
import io.youi.server.dsl._
import io.youi.server.handler.{CachingManager, LanguageSupport}
import io.youi.upload.UploadManager
import profig.JsonUtil
import scribe.Execution.global

import scala.concurrent.Future

object ServerExampleApplication extends ExampleApplication with ServerConnectedApplication[ExampleConnection] {
  val generalPages: Page = page(GeneralPages)
  val uploadManager: UploadManager = UploadManager()

  case class Greeting(message: String, name: String)

  override def getOrCreateConnection(listener: WebSocketListener): ExampleConnection = new ServerConnection

  override protected def init(): Future[Unit] = {
    uploadManager.received.attach { file =>
      scribe.info(s"File received: ${file.getAbsolutePath}")
    }
    super.init().map { _ =>
      // TODO: add support to `Connection` to add deltas so they may all be processed at the end
      proxies += ProxyHandler(path.exact("/proxy.html")) { url =>
        URL("http://google.com").copy(path = url.path)
      }
      handler(
        filters(
          path"/" / redirect(path"/ui-examples.html"),
          path"/hello.txt" / CachingManager.MaxAge(120L) / "Hello, World!".withContentType(ContentType.`text/plain`),
          path"/hello.json" / Content.json(JsonUtil.toJson(Greeting("Hello", "World"))),
          combined.any(
            path.exact(path"/courio.html"),
            path.matches("/examples/.*[.]html"),
            path.exact("/ui-examples.html")
          ) / Application / ServerApplication.AppTemplate,
          path"/cookies.html" / CookiesExample,
          path"/session.html" / SessionExample,
          uploadManager,
          ClassLoaderPath(pathTransform = (path: String) => s"content$path") / CachingManager.LastModified(),
          path.startsWith("/app") / ClassLoaderPath()
        )
      )
      handlers += new LanguageSupport()
    }
  }
}