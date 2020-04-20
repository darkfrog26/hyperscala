package io.youi.component.types

import reactify.Var

import scala.util.matching.Regex

class SizeProperty(get: => String, set: String => Unit, callbacks: (() => Unit)*) extends Var[Double](-1.0) {
  val `type`: Var[SizeType] = Var(SizeType.Auto)

  refresh()

  private var changed = false

  attach { d =>
    if (d != -1.0 && `type`() == SizeType.Auto && !changed) {
      `type` @= SizeType.Pixel
      changed = true
    }
    set()
    callbacks.foreach(_())
  }
  `type`.on(set())

  def set(value: Double, `type`: => SizeType): Unit = {
    this.`type` := `type`
    set(value)
  }

  private def set(): Unit = {
    val t = `type`() match {
      case SizeType.Auto => ""
      case t => t.name
    }
    val value = if (`type`.includeNumeric) {
      val d = apply()
      s"$d$t"
    } else {
      t
    }
    set(value)
  }

  def refresh(): Unit = get match {
    case null | "" | "auto" => {
      this @= -1.0
      `type` @= SizeType.Auto
    }
    case "initial" => {
      this @= -1.0
      `type` @= SizeType.Initial
    }
    case "inherit" => {
      this @= -1.0
      `type` @= SizeType.Inherit
    }
    case SizeProperty.ValueRegex(number, unit) => {
      this @= number.toDouble
      `type` @= SizeType(unit)
    }
  }
}

object SizeProperty {
  val ValueRegex: Regex = """([0-9.]+)(ch|em|ex|rem|vh|vw|vmin|vmax|px|cm|mm|in|pc|pt)""".r
}