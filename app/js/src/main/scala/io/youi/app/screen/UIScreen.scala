package io.youi.app.screen

import io.youi.component.Container
import io.youi.component.support.{MarginSupport, SizeSupport}
import io.youi.component.types.Display
import io.youi.ui

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait UIScreen extends Screen with PathActivation {
  protected lazy val container: Container with SizeSupport with MarginSupport = {
    val c = new Container with SizeSupport with MarginSupport
    c.id @= title
    c
  }

  override protected def init(): Future[Unit] = super.init().flatMap { _ =>
    container.size.width := ui.size.width
    container.size.height := ui.size.height

    ui.children += container
    createUI()
  }

  def createUI(): Future[Unit]

  override protected def activate(): Future[Unit] = super.activate().map(_ => container.display @= Display.Block)

  override protected def deactivate(): Future[Unit] = super.deactivate().map(_ => container.display @= Display.None)
}