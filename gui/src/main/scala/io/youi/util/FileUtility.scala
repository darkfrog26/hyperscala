package io.youi.util

import org.scalajs.dom.Event
import org.scalajs.dom.raw.{File, FileReader, URL}
import scribe.Execution.global

import scala.concurrent.{Future, Promise}

object FileUtility {
  def loadDataURL(file: File, useFileReader: Boolean = false): Future[String] = if (useFileReader) {
    val reader = new FileReader
    val promise = Promise[String]()
    reader.addEventListener("load", (_: Event) => {
      promise.success(reader.result.asInstanceOf[String])
    })
    reader.readAsDataURL(file)
    val future = promise.future
    future.failed.foreach(scribe.error(_))
    future
  } else {
    Future.successful(URL.createObjectURL(file))
  }

  def loadText(file: File): Future[String] = {
    val reader = new FileReader
    val promise = Promise[String]()
    reader.addEventListener("load", (_: Event) => {
      promise.success(reader.result.asInstanceOf[String])
    })
    reader.readAsText(file)
    val future = promise.future
    future.failed.foreach(scribe.error(_))
    future
  }
}