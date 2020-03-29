package io.youi.component.support

import io.youi.component.Component
import io.youi.component.feature.ContainerFeature

trait ContainerSupport extends Component {
  val children: ContainerFeature = new ContainerFeature(this)
}