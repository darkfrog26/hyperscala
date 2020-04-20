package io.youi.component

import io.youi._
import io.youi.component.feature.{FeatureParent, HeightFeature, WidthFeature}
import io.youi.component.types.Prop
import io.youi.drawable.Context
import io.youi.theme.Theme
import org.scalajs.dom.html

import scala.scalajs.js.|

abstract class CanvasView(canvas: html.Canvas = dom.create.canvas) extends Component(canvas) with WidthFeature with HeightFeature {
  protected lazy val context: Context = new Context(canvas, ratio)

  override def parent: FeatureParent = this

  override lazy val width: Prop[Double] = new Prop[Double](0, d => canvas.style.width = s"${d}px", measureComponent, render)
  override lazy val height: Prop[Double] = new Prop[Double](0, d => canvas.style.height = s"${d}px", measureComponent, render)

  protected def draw(content: Context): Unit

  def render(): Unit = {
    context.reset()

    canvas.width = math.ceil(width * ratio).toInt
    canvas.height = math.ceil(height * ratio).toInt
    draw(context)
  }
}