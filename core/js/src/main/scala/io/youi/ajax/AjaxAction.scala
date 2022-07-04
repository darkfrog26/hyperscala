package io.youi.ajax

import org.scalajs.dom.XMLHttpRequest
import reactify._

import scala.scalajs.concurrent.JSExecutionContext.queue
import scala.concurrent.Future

class AjaxAction(request: AjaxRequest) {
  lazy val future: Future[XMLHttpRequest] = request.promise.future
  private[ajax] val _state = Var[ActionState](ActionState.New)
  def state: Val[ActionState] = _state
  def loaded: Val[Double] = request.loaded
  def total: Val[Double] = request.total
  def percentage: Val[Int] = request.percentage
  def cancelled: Val[Boolean] = request.cancelled

  private[ajax] def start(manager: AjaxManager): Unit = {
    if (!cancelled()) {
      _state @= ActionState.Running
      future.onComplete { _ =>
        _state @= ActionState.Finished
        manager.remove(this)
      }(queue)
      request.send()
    } else {
      manager.remove(this)
    }
  }

  // TODO: dequeue if not already running
  def cancel(): Unit = request.cancel()     // TODO: does cancel fire onComplete with a failure?
}