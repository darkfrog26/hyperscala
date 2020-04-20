package io.youi.drawable

trait Drawable extends Modifiable {
  def draw(context: Context, x: Double, y: Double): Unit
}

object Drawable {
  object None extends Drawable {
    override def draw(context: Context, x: Double, y: Double): Unit = {}
  }
}