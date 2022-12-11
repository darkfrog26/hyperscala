package io.youi.upload

import cats.effect.IO
import io.youi.History
import io.youi.ajax.AjaxManager
import io.youi.component.FileInput
import org.scalajs.dom.{File, FormData}
import reactify.{Val, Var}
import spice.http.{Headers, HttpMethod}
import spice.net._

import scala.util.{Failure, Success}

/**
  * UploadManager represents a system to support uploads to the server and should be coupled with the JVM UploadManager
  * counter-part.
  *
  * Must point to the same path as the server is assigned.
  *
  * @param url the URL to upload to - defaults to the current URL with "/upload" as the path
  * @param chunkSize the size to slice large files to (defaults to 50 meg)
  * @param timeout the timeout in seconds (defaults to 0 - no timeout)
  * @param headers headers to be passed (defaults to empty)
  * @param withCredentials whether credentials should be passed (defaults to false)
  * @param ajaxManager an AJAXManager to use (defaults to a new instance with four maximum concurrent)
  */
case class UploadManager(url: URL = History.url.withPath(path"/upload"),
                         chunkSize: Long = 50L * 1000L * 1000L,
                         timeout: Int = 0,
                         headers: Headers = Headers.empty,
                         withCredentials: Boolean = false,
                         ajaxManager: AjaxManager = new AjaxManager(4)) {
  private var verifier: File => IO[Boolean] = (_: File) => IO.pure(true)
  private var action: Uploading => Unit = (_: Uploading) => ()
  private val fileInput = new FileInput {
    files.attach { files =>
      if (files.nonEmpty) {
        verifyAndUpload(files)
        reset()
      }
    }
  }

  private def verifyAndUpload(files: List[File]): IO[Unit] = if (files.nonEmpty) {
    val file = files.head
    verifier(file).flatMap { verified =>
      if (verified) upload(file)
      verifyAndUpload(files.tail)
    }
  } else {
    IO.unit
  }

  def select(multiple: Boolean = false,
             verifier: File => IO[Boolean] = _ => IO.pure(true))
            (action: Uploading => Unit): Unit = {
    this.verifier = verifier
    this.action = action
    fileInput.multiple @= multiple
    fileInput.click()
  }

  def upload(file: File): Uploading = {
    val headersMap = headers.map.map {
      case (key, values) => key -> values.head
    }
    val progress = Var[Long](0)
    val percentage = Val(progress / file.size)
    val io = uploadSlice(
      file = file,
      offset = 0L,
      total = file.size.toLong,
      progress = progress,
      headers = headersMap
    )
    val uploading = Uploading(file, progress, percentage, io)
    action(uploading)
    uploading
  }

  private def uploadSlice(file: File,
                          offset: Long,
                          total: Long,
                          progress: Var[Long],
                          headers: Map[String, String],
                          slices: Set[String] = Set.empty): IO[String] = {
    val start = offset
    val end = math.min(total, offset + chunkSize)
    val sliced = file.slice(start.toDouble, end.toDouble)
    val sliceName = file.name
    val action = ajaxManager.enqueue(
      url = url,
      method = HttpMethod.Post,
      data = Some(new FormData {
        append("type", "upload")
        append("file", sliced, sliceName)
      }),
      timeout = timeout,
      headers = headers,
      withCredentials = withCredentials
    )
    val reaction = action.loaded.attach(d => progress @= math.min(offset + d.toLong, total))
    action.io.flatMap { t =>
      action.loaded.reactions -= reaction
      t match {
        case Success(request) => {
          val partName = request.response.toString
          if (end == total) {
            if (offset == 0L) {   // Only one slice, no need to merge
              IO.pure(partName)
            } else {
              mergeSlices(file.name, headers, slices + partName)
            }
          } else {
            uploadSlice(file, end, total, progress, headers, slices + partName)
          }
        }
        case Failure(exception) => IO.raiseError(exception)
      }
    }
  }

  private def mergeSlices(fileName: String, headers: Map[String, String], slices: Set[String]): IO[String] = {
    val action = ajaxManager.enqueue(
      url = url,
      data = Some(new FormData {
        append("type", "mergeSlices")
        append("fileName", fileName)
        append("slices", slices.mkString(","))
      }),
      headers = headers
    )
    action.io.map(_.get.response.toString)
  }
}