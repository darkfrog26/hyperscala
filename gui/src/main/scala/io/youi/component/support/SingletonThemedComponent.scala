package io.youi.component.support

import io.youi.component.Component
import io.youi.theme.Theme

/**
  * SingleThemedComponent is useful for components that have exactly one instance and exposes a `theme` that can be used
  * for stylization.
  */
trait SingletonThemedComponent {
  this: Component =>

  protected val className: String = getClass.getSimpleName.replace("$", "")

  val theme: Theme = Theme.byClassName(className)

  classes += className
}