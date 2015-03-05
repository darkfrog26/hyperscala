package org.hyperscala.fabricjs

import org.hyperscala.javascript.{JavaScriptString, JavaScriptContent}
import org.hyperscala.module.Module
import org.hyperscala.realtime.Realtime
import org.hyperscala.web.{Website, Webpage}
import com.outr.net.http.session.Session
import org.powerscala.event.Listenable
import org.powerscala.property.Property
import org.powerscala.{Color, Unique, Version}
import org.hyperscala.html._

/**
 * @author Matt Hicks <matt@outr.com>
 */
object FabricJS extends Module {
  val name = "fabric.js"
  val version = Version(1, 4, 0)

  override def dependencies = List(Realtime)

  override def init[S <: Session](website: Website[S]) = {
    website.register("/js/hyperscala-fabric.js", "hyperscala-fabric.js")
  }

  override def load[S <: Session](webpage: Webpage[S]) = {
    webpage.head.contents += new tag.Script(src = "http://cdnjs.cloudflare.com/ajax/libs/fabric.js/1.4.0/fabric.min.js")
    webpage.head.contents += new tag.Script(src = "/js/hyperscala-fabric.js")
  }
}

class ObjectProperty[T](val name: String, o: Object)(implicit manifest: Manifest[T]) extends Property[T](default = None)(o, manifest)