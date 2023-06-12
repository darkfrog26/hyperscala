package io.youi

import org.scalajs.dom.{Event, PopStateEvent, document, html, window}
import reactify.{Channel, Val, Var}
import spice.net.{URLPath, Protocol, URL}

import scala.scalajs.js

/**
  * Convenience functionality for working with browser history.
  */
object History {
  /**
    * If set to true calling push and replace will always result in the same functionality as set. This is a toggle to
    * be able to disable single-application mode with one switch.
    *
    * Defaults to false.
    */
  val alwaysReload: Var[Boolean] = Var(false)
  private val currentURL = Var[URL](URL.parse(document.location.href))
  val url: Val[URL] = Val(currentURL)
  val stateChange: Channel[HistoryStateChange] = Channel[HistoryStateChange]

  def isSecure: Boolean = url().protocol == Protocol.Https

  window.addEventListener("popstate", (evt: PopStateEvent) => {
    val urlString = document.location.href
    val newURL = URL.parse(urlString)
    if (newURL != url()) {
      currentURL @= newURL
      stateChange @= HistoryStateChange(newURL, StateType.Pop, evt.state)
    }
  })

  def update(change: HistoryStateChange): Unit = change.stateType match {
    case StateType.Push => push(change.url, change.state)
    case StateType.Replace => replace(change.url, change.state)
    case StateType.Set => set(change.url)
    case StateType.Pop => back()
  }

  def setPath(path: URLPath, keepParams: Boolean = false): Unit = {
    set(url.withPath(path) match {
      case u if keepParams => u
      case u => u.clearParams()
    })
  }

  def set(url: URL, target: String = "_self"): Unit = {
    window.open(url.toString, target)
    stateChange @= HistoryStateChange(url, StateType.Set, null)
  }

  def pushPath(path: URLPath, keepParams: Boolean = false, state: js.Any = null): Unit = {
    push(url.withPath(path) match {
      case u if keepParams => u
      case u => u.clearParams()
    }, state)
  }

  def push(url: URL, state: js.Any = null): Unit = if (alwaysReload()) {
    set(url)
  } else if (url != this.url()) {
    val urlString = url.toString
    window.history.pushState(state, urlString, urlString)
    currentURL @= url
    stateChange @= HistoryStateChange(url, StateType.Push, state)
  }

  def replacePath(path: URLPath, keepParams: Boolean = false, state: js.Any = null): Unit = {
    replace(url.withPath(path) match {
      case u if keepParams => u
      case u => u.clearParams()
    }, state)
  }

  def replace(url: URL, state: js.Any): Unit = if (alwaysReload()) {
    set(url)
  } else if (url != this.url()) {
    val urlString = url.toString
    window.history.replaceState(state, urlString, urlString)
    currentURL @= url
    stateChange @= HistoryStateChange(url, StateType.Replace, state)
  }

  def back(delta: Int = 1): Unit = window.history.go(-delta)

  def forward(delta: Int = 1): Unit = window.history.go(delta)

  def reload(): Unit = window.location.reload()

  /**
    * Updates all anchors on the page to internal links to push history instead of loading another page. Essentially
    * converts all links to be single-page-app compliant.  May be run multiple times and will only change new links.
    */
  def fixAnchors(): Unit = dom.byTag[html.Anchor]("a").foreach(fixAnchor)

  def fixAnchor(anchor: html.Anchor): html.Anchor = {
    if (Option(anchor.onclick).isEmpty && linkType(anchor.href) == LinkType.Internal) {
      this.anchor(anchor, anchor.href)
    } else {
      anchor
    }
  }

  def anchor(anchor: html.Anchor, location: String): html.Anchor = {
    anchor.href = location
    anchor.onclick = (evt: Event) => {
      evt.preventDefault()
      evt.stopPropagation()

      push(URL.parse(anchor.href))
    }
    anchor
  }

  def linkType(href: String): LinkType = if (href.trim.isEmpty) {
    LinkType.Empty
  } else if (href.contains("#")) {
    LinkType.Hash
  } else if (href.startsWith("javascript:")) {
    LinkType.JavaScript
  } else if (href.startsWith(url().base) || href.startsWith("/")) {
    LinkType.Internal
  } else {
    LinkType.External
  }
}