package io.youi.component

import io.youi.component.extras.HTMLComponent
import io.youi.dom
import io.youi.theme.VideoViewTheme
import org.scalajs.dom.html
import reactify._

class VideoView(override protected val element: html.Span = dom.create[html.Span]("span")) extends HTMLComponent[html.Span] with VideoViewTheme {
  parentTheme := Some(VideoView)

  override def componentType: String = "VideoView"

  def play(): Unit = video.play()
  def pause(): Unit = video.pause()
  def isPaused: Boolean = video.isPaused
  def isEnded: Boolean = video.isEnded

  override protected def measuredWidth: Double = video.width.toDouble

  override protected def measuredHeight: Double = video.height.toDouble
}

object VideoView extends VideoViewTheme