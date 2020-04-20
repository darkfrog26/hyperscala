package io.youi.component.feature

import io.youi.component.Component
import io.youi.component.types.{PositionType, Prop, SizeProperty}
import io.youi.theme.Theme
import reactify._

import scala.scalajs.js.|
import scala.util.Try

class PositionFeature(override val parent: FeatureParent) extends Feature {
  lazy val x: SizeProperty = new SizeProperty(parent.css.left, parent.css.left_=)
  lazy val y: SizeProperty = new SizeProperty(parent.css.top, parent.css.top_=)
  lazy val z: Prop[Int] = new Prop[Int](Try(parent.css.zIndex.toInt).getOrElse(0), i => parent.css.zIndex = i.toString)

  val `type`: Prop[PositionType] = {
    val p = new Prop[PositionType](
      getter = PositionType(parent.css.position),
      setter = pt => {
        parent.css.position = pt.name
        parent.css.left = x.toString
        parent.css.top = y.toString
      }
    )
    x.and(y).on {
      if (p() == PositionType.Static) {
        p @= PositionType.Absolute
      }
    }
    p
  }

  lazy val left: Var[Double] = x
  lazy val center: Dep[Double, Double] = componentOption.flatMap(Component.width) match {
    case Some(w) => Dep(left)(_ + (w / 2.0), _ - (w / 2.0))
    case None => Dep(left)(identity, identity)
  }
  lazy val right: Dep[Double, Double] = componentOption.flatMap(Component.width) match {
    case Some(w) => Dep(left)(_ + w, _ - w)
    case None => Dep(left)(identity, identity)
  }

  lazy val top: Var[Double] = y
  lazy val middle: Dep[Double, Double] = componentOption.flatMap(Component.height) match {
    case Some(h) => Dep(top)(_ + (h / 2.0), _ - (h / 2.0))
    case None => Dep(top)(identity, identity)
  }
  lazy val bottom: Dep[Double, Double] = componentOption.flatMap(Component.height) match {
    case Some(h) => Dep(top)(_ + h, _ - h)
    case None => Dep(top)(identity, identity)
  }

  lazy val depth: Var[Int] = z
}