package io.youi.component

import com.outr.pixijs.PIXI
import io.youi._
import io.youi.style.Theme

class HTMLComponent[C <: hypertext.Component](val component: C) extends Component {
  override protected[component] lazy val instance: PIXI.Container = new PIXI.Sprite(PIXI.Texture.EMPTY)

  override protected def defaultTheme: Theme = HTMLComponent

  size.measured.width := component.size.actual.width
  size.measured.height := component.size.actual.height

  override def update(delta: Double): Unit = {
    super.update(delta)

    val a = instance.worldTransform.a
    val b = instance.worldTransform.b
    val c = instance.worldTransform.c
    val d = instance.worldTransform.d
    val tx = instance.worldTransform.tx
    val ty = instance.worldTransform.ty
    component.element.style.transform = s"matrix($a, $b, $c, $d, $tx, $ty)"
  }
}

object HTMLComponent extends Theme(Theme)