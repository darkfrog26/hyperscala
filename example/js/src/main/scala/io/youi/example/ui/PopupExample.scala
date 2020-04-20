package io.youi.example.ui

import io.youi._
import io.youi.component._
import io.youi.component.support.{MeasuredSupport, PositionSupport}
import io.youi.component.types.PositionType
import io.youi.easing.Linear
import io.youi.event.EventSupport
import io.youi.example.screen.UIExampleScreen
import io.youi.font.GoogleFont
import io.youi.net._
import reactify._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class PopupExample extends UIExampleScreen {
  override def title: String = "Popup Example"
  override def path: Path = path"/examples/popup.html"

  private val text = new TextView() with PositionSupport with MeasuredSupport with EventSupport {
    content @= "Show Popup"
    font.size @= 64.px
    color @= Color.DarkBlue
    position.`type` @= PositionType.Absolute
    position.center := ui.size.center
    position.middle := ui.size.middle
  }

  override def createUI(): Future[Unit] = for {
    fnt <- GoogleFont.`Lobster`.load()
  } yield {
    text.font.family @= fnt.family
    container.children += text

    val popup = new Popup(showGlassPane = true) with EventSupport
    popup.backgroundColor @= Color.Green.withBrightness(0.5)
    popup.init()
    popup.easing @= Linear
    container.children += popup

    text.event.click.on {
      popup.show()
    }
  }
}