package org.hyperscala.examples.snapsvg

import org.hyperscala.examples.Example
import org.hyperscala.html._
import org.hyperscala.svg._
import org.hyperscala.snapsvg.{SnapElement, Snap}
import org.hyperscala.javascript.dsl._
import org.hyperscala.realtime.Realtime
import org.hyperscala.realtime.dsl._
import org.powerscala.Color
import org.hyperscala.web._

import scala.language.implicitConversions
import com.outr.net.http.session.Session

/**
 * @author Matt Hicks <mhicks@outr.com>
 */
class SnapSVGExample extends Example {
  this.require(Snap)
  this.require(Realtime)

  implicit def color2Statement(c: Color) = s(c)
  implicit def element2Statement(e: SnapElement) = s(e)

  val svg = new Svg(id = "svg", width = 300.px, height = 300.px)
  contents += svg

  val js = new JavaScriptContext {
    var snap = Snap(svg)
    var bigCircle = snap.circle(150, 150, 100)
    bigCircle.attr(fill = Color.immutable("#bada55"), stroke = Color.immutable("#000"), strokeWidth = 5)
    var smallCircle = snap.circle(100, 150, 70)
    var discs = snap.group(smallCircle, snap.circle(200, 150, 70))
    discs.attr(fill = Color.White)
    bigCircle.attr(mask = discs)
  }
  println(js.toJS())
  connected[Webpage[Session]] {
    case webpage => js.send(webpage)
  }
}
