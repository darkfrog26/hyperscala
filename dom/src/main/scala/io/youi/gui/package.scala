package io.youi

import org.scalajs.dom.html

import scala.language.implicitConversions

package object gui {
  implicit def component2Element(component: Component): html.Element = component.element
}