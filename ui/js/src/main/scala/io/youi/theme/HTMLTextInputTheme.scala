package io.youi.theme

trait HTMLTextInputTheme extends HTMLComponentTheme with HTMLFontTheme {
  protected var valueChanging = false

  lazy val value: StyleProp[String] = style[String]("value", "", Some(new StyleConnect[String] {
    override def init(theme: Theme, v: StyleProp[String], name: String): Unit = withElement(theme) { e =>
      val value = v.option.map(valueOption => valueOption.getOrElse(v.value()))
      value.attachAndFire { s =>
        if (!valueChanging) {
          e.asInstanceOf[html.Input].value = s
        }
      }
    }
  }))
  lazy val placeholder: StyleProp[String] = style[String]("placeholder", "", StyleConnect.field[String])
  lazy val `type`: StyleProp[InputType] = style[InputType]("type", InputType.Text, StyleConnect.field[InputType])
  lazy val min: StyleProp[String] = style[String]("min", "", StyleConnect.field[String])
  lazy val max: StyleProp[String] = style[String]("max", "", StyleConnect.field[String])
  lazy val step: StyleProp[String] = style[String]("step", "", StyleConnect.field[String])
}