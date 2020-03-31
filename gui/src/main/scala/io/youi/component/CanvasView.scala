package io.youi.component

import io.youi._
import io.youi.component.types.Prop
import io.youi.drawable.Context
import org.scalajs.dom.html

abstract class CanvasView(canvas: html.Canvas = dom.create.canvas) extends Component(canvas) {
  protected lazy val context: Context = new Context(canvas, ratio)

  lazy val width: Prop[Double] = new Prop[Double](0, d => canvas.style.width = s"${d}px", measure.trigger, render)
  lazy val height: Prop[Double] = new Prop[Double](0, d => canvas.style.height = s"${d}px", measure.trigger, render)

  protected def draw(content: Context): Unit

  def render(): Unit = {
    context.reset()

    canvas.width = math.ceil(width * ratio).toInt
    canvas.height = math.ceil(height * ratio).toInt
    draw(context)
  }
}