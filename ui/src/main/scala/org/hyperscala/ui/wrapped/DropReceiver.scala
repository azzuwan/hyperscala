package org.hyperscala.ui.wrapped

import org.hyperscala.module.Module
import org.powerscala.{Version, StorageComponent}
import org.hyperscala.html._
import org.hyperscala.jquery.jQuery
import org.hyperscala.realtime.Realtime
import org.hyperscala.web._
import org.hyperscala.web.WrappedComponent
import org.powerscala.event.processor.UnitProcessor
import org.powerscala.event.{Intercept, Listenable}
import com.outr.net.http.session.Session

/**
 * @author Matt Hicks <matt@outr.com>
 */
class DropReceiver private(val wrapped: HTMLTag, val autoInit: Boolean = true) extends WrappedComponent[HTMLTag] with Listenable {
  initReceiver()

  private def initReceiver() = {
    wrapped.eventReceived.on {
      case evt if evt.event == "dropped" => {
        val types = evt.json.strings("types")
        val data = evt.json.stringMap("data").map {
          case (key, value) => DropData(key, value.toString)
        }.toList
        dropped.fire(DropReceived(types, data))

        Intercept.Stop
      }
      case _ => Intercept.Continue
    }
  }

  protected def initializeComponent(values: Map[String, Any]) = wrapped.connected[Webpage[Session]] {
    case webpage => {
      Realtime.sendJavaScript(webpage, s"createDropReceiver('${wrapped.identity}');", onlyRealtime = false)
      values.foreach {
        case (key, value) => modify(key, value)
      }
    }
  }

  /**
   * Invoked when a a drop is received in the browser.
   */
  val dropped = new UnitProcessor[DropReceived]("dropped")

  /**
   * Receive Types defines the types of data that is acceptable during a drop.
   *
   * Defaults to List("text/plain", "text/html", "text/uri-list")
   */
  val receiveTypes = wrapped.dataWrapper[List[String]]("receive-types", List("text/plain", "text/html", "text/uri-list")) {
    case list => list.mkString(", ")
  }

  protected def modify(key: String, value: Any) = println(s"*** Modify: $key = $value")
}

object DropReceiver extends Module with StorageComponent[DropReceiver, HTMLTag] {
  def name = "DropReceiver"
  def version = Version(1)

  override def dependencies = List(jQuery.LatestWithDefault, Realtime)

  override def init[S <: Session](website: Website[S]) = {
    website.register("/js/drop_receiver.js", "drop_receiver.js")
  }

  override def load[S <: Session](webpage: Webpage[S]) = {
    webpage.head.contents += new tag.Script(mimeType = "text/javascript", src = "/js/drop_receiver.js")
  }

  override def apply(t: HTMLTag) = {
    t.require(this)
    super.apply(t)
  }

  protected def create(t: HTMLTag) = new DropReceiver(t)
}

case class DropReceived(types: List[String], data: List[DropData]) {
  def get(mimeType: String) = data.find(dd => dd.mimeType == mimeType).map(dd => dd.data)
}

case class DropData(mimeType: String, data: String)