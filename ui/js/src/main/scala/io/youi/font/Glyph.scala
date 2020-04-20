package io.youi.font

trait Glyph {
  def font: Font
  def char: Char
  def path: Path
  def sizedPath(size: Double): Path
  def width(size: Double): Double
  def actualWidth(size: Double): Double
  def draw(context: Context, x: Double, y: Double, size: Double): Unit
}