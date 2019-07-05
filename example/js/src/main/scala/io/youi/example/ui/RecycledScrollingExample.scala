package io.youi.example.ui

import io.youi.component.recycled.{BatchedData, RecycledRenderer, RecycledScroller}
import io.youi.{Color, Key}
import io.youi.component.{Container, HTMLTextInput, HTMLTextView}
import io.youi.example.screen.UIExampleScreen
import io.youi.net._
import io.youi.style.{HTMLBorder, HTMLBorderStyle, InputType, Position}
import reactify._

import scala.concurrent.Future

class RecycledScrollingExample extends UIExampleScreen {
  override def title: String = "Recycled Scrolling Example"
  override def path: Path = path"/examples/recycled-scrolling.html"

  override def createUI(): Future[Unit] = {
    val scroller = new RecycledScroller[Int, NumberComponent](10, NumberComponentRenderer) {
      pane1.background := Color.LightBlue
      pane2.background := Color.LightPink
      pane3.background := Color.LightGreen

      position.center := container.size.center
      position.middle := container.size.middle
      size.width := 1000.0
      size.height := 500.0
      background := Color.LightGray
      batch.data := BatchedData((0 until 10000).toList)
    }

    val inputSlider: HTMLTextInput = new HTMLTextInput {
      `type` := InputType.Range
      min := "0"
      max := scroller.batch.total().toString
      position.left := scroller.position.left
      position.bottom := scroller.position.top
      size.width := 200.0
      size.height := 25.0

      var modifying = false
      scroller.batch.position.attachAndFire { p =>
        if (!modifying) {
          modifying = true
          value := p.toString
          modifying = false
        }
      }

      value.attach { v =>
        if (!modifying) {
          modifying = true
          scroller.batch.position := v.toInt
          modifying = false
        }
      }
    }
    val textTotal = new HTMLTextView {
      position.right := scroller.position.right
      position.bottom := scroller.position.top
      font.size := 24.0
      value := s"of ${scroller.batch.total()}"
    }
    val inputPosition = new HTMLTextInput {
      position.right := textTotal.position.left - 10.0
      position.bottom := scroller.position.top - 5.0
      size.width := 50.0
      size.height := 25.0
      scroller.batch.position.attachAndFire { p =>
        value := p.toString
      }

      event.key.up.attach { evt =>
        if (evt.key == Key.Enter) {
          if (value().nonEmpty && value().forall(_.isDigit)) {
            scroller.batch.position.static(value().toInt)
          }
        }
      }
    }
    container.children ++= List(scroller, inputSlider, textTotal, inputPosition)

    Future.successful(())
  }

  class NumberComponent extends Container {
    val value: Var[Int] = Var(0)

    position.`type` := Position.Absolute
    size.width := 1000.0
    size.height := 50.0
    htmlBorder.radius := 5.0
    htmlBorder := HTMLBorder(1.0, HTMLBorderStyle.Dashed, Color.DarkRed)

    val label = new HTMLTextView
    label.position.left := 20.0
    label.position.middle := size.middle
    label.value := s"Number: ${value() + 1} with extra text"
    label.font.size := 42.0
    children += label
  }

  object NumberComponentRenderer extends RecycledRenderer[Int, NumberComponent] {
    override def createComponent: NumberComponent = new NumberComponent

    override def setData(data: Int, component: NumberComponent): Unit = component.value := data

    override def getData(component: NumberComponent): Int = component.value()

    override def loading(component: NumberComponent): Unit = component.value := -1
  }
}