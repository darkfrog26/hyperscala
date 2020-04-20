package io.youi.example

import io.youi.server.session.InMemorySessionManager
import reactify.Var

import scala.concurrent.Future

class MySession {
  val created: Long = System.currentTimeMillis()
  val username: Var[Option[String]] = Var(None)
}

object MySession extends InMemorySessionManager[MySession] {
  override protected def applyToURL: Boolean = true

  override protected def create(sessionId: String): Future[MySession] = Future.successful(new MySession)
}