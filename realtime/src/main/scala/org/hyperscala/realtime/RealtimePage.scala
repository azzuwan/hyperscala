package org.hyperscala.realtime

import org.hyperscala.web.Webpage
import org.hyperscala.html._
import org.hyperscala.javascript.JavaScriptContent
import org.powerscala.hierarchy.event.Descendants
import org.powerscala.Priority
import org.powerscala.log.Logging
import org.hyperscala.css.{Style, StyleSheetAttribute, StyleSheet}
import org.powerscala.property.Property
import org.hyperscala._
import org.hyperscala.svg.{Svg, SVGTag}
import org.powerscala.hierarchy.ChildLike
import org.hyperscala.connect.{Message, Connection, Connect}
import argonaut.{Json, CodecJson}
import argonaut.Argonaut._
import org.powerscala.property.event.PropertyChangeEvent
import org.powerscala.hierarchy.event.ChildAddedEvent
import scala.Some
import org.powerscala.hierarchy.event.ChildRemovedEvent
import com.outr.net.http.session.Session

/**
 * @author Matt Hicks <matt@outr.com>
 */
class RealtimePage[S <: Session] private(page: Webpage[S]) extends Logging {
  def connections = Connect.connections(page)(page.website.manifest)

  // Configure Listeners
  page.childAdded.listen(Priority.Normal, Descendants) {
    case evt => childAdded(evt)
  }
  page.childRemoved.listen(Priority.Normal, Descendants) {
    case evt => childRemoved(evt)
  }
  page.head.styleSpaces.removed.on {
    case evt => styleSheetRemoved(evt.value)
  }
  page.listen[PropertyChangeEvent[_], Unit, Unit]("change", Priority.Normal, Descendants) {
    case evt => propertyChanged(evt)
  }

  def send(event: String, message: Json, sendWhenConnected: Boolean) = synchronized {
    connections.send2Client(event, message, sendWhenConnected)
  }

  /*@tailrec
  private def sendRecursive(event: String, message: String, connections: List[Connection]): Unit = {
    if (connections.nonEmpty) {
      val c = connections.head
      c.send(event, message)
      sendRecursive(event, message, connections.tail)
    }
  }*/

  /*protected[realtime] def connectionCreated(connection: Connection) = synchronized {
    _connections = connection :: _connections
    heardFromListener = connection.heardFrom.on {
      case time => page.checkIn()       // Check in with the page to keep it from timing out.
    }
  }*/

//  private var heardFromListener: Listener[Long, Unit] = _

//  protected[realtime] def connectionConnected(connection: Connection) = synchronized {
//    sendBacklog()
//  }

  protected[realtime] def received(connection: Connection[S], message: Message) = {
    synchronized {
      val json = message.data.obj.getOrElse(throw new RuntimeException(s"Data is not a JSON object: ${message.data}"))
      val id = json.string("id")
      val t = id match {
        case null | "" => Some(page.body)
        case _ => page.html.byId[IdentifiableTag](id)
      }
      t match {
        case Some(element) => element.receive(message.event, json)
        case None => warn(s"Unable to find tag by id: $id to fire event: ${message.event} for message: ${message.data}")
      }
//      val eventType = content("eventType").asInstanceOf[String]

//      val t = id match {
//        case null => Some(page.body)
//        case _ => page.html.byId[IdentifiableTag](id)
//      }
//      t match {
//        case Some(element) => element.receive(message.event, content)
//        case None => warn(s"Unable to find tag by id: $id to fire event: $eventType for message: $content")
//      }
    }
  }

/*
  /**
   * Executes the event in another thread to keep from blocking data receiving.
   *
   * @param element the element the event is received for
   * @param eventType the type of event
   * @param responseMessage the response message of the event
   */
  private def asynchronousReceive(element: IdentifiableTag, eventType: String, responseMessage: ResponseMessage) = {
    val context = Website().requestContext          // Get the context for the current thread
    val f = () => {
      Website().contextualize(context) {
        Page.instance.set(Webpage())
        element.receive(eventType, responseMessage)
      }
    }
    actor ! f     // Process receives one at a time via actor
  }

  protected[realtime] def connectionDisposed(connection: Connection) = synchronized {
    _connections = _connections.filterNot(c => c == connection)
    connection.heardFrom -= heardFromListener
  }*/

  private def childAdded(evt: ChildAddedEvent) = synchronized {
    if (!RealtimePage.ignoringStructureChanges) {
      val parent = evt.parent.asInstanceOf[IdentifiableTag with Container[IdentifiableTag]]
      evt.child match {
        case child: IdentifiableTag => {
          // Verifies the parent and child have an id
          parent.identity
          child.identity

          val index = parent.contents.indexOf(child)
          val variable = child match {
            case tag: SVGTag if !tag.isInstanceOf[Svg] => "parseSVG(content)"
            case _ => "content"
          }
          child match {
            case t: tag.Script => send(JavaScriptMessage(t.contents.map(c => c.content).mkString("\r\n")))
            case _ => {
              val instruction = if (index == parent.contents.length - 1) {    // Append to the end
                "$('#%s').append(%s);".format(parent.id(), variable)
              } else if (index == 0) {                                   // Append before
              val after = parent.contents(1)
                "$('#%s').before(%s);".format(after.id(), variable)
              } else {
                val before = parent.contents(index - 1)
                "$('#%s').after(%s);".format(before.id(), variable)
              }
              val content = child.outputString
              send(JavaScriptMessage(instruction, Some(content)))
              if (page.rendered) {                            // Only do this if the page has already rendered
                Markup.rendered(child)                        // Mark child tag as rendered
                child.byTag[HTMLTag].foreach(Markup.rendered) // Mark all tags children of child tag as rendered
              }
            }
          }
        }
        case js: JavaScriptContent => send(JavaScriptMessage(js.content))
      }
    }
  }

  private def childRemoved(evt: ChildRemovedEvent) = synchronized {
    if (!RealtimePage.ignoringStructureChanges) evt.child match {
      case text: tag.Text => {
        val parent = evt.parent.asInstanceOf[HTMLTag with Container[HTMLTag]]
        val index = parent.contents.indexOf(text)
        val instruction = "$('#%s').contents().eq(%s).remove();".format(parent.id(), index)
        send(JavaScriptMessage(instruction))
      }
      case tag: IdentifiableTag => send(JavaScriptMessage("$('#%s').remove();".format(tag.id())))
      case js: JavaScriptContent => // TODO: evaluate? - Previously nothing was needed here
    }
  }

  private def styleSheetRemoved(styleSheet: StyleSheet) = synchronized {
    val selector = styleSheet.selectorString
    send(JavaScriptMessage(s"$$.stylesheet('$selector').css(null)"))
  }

  private def propertyChanged(evt: PropertyChangeEvent[_]) = synchronized {
    if (FormField.changingProperty == evt.property && FormField.changingValue == evt.newValue) {
      // Ignore a change initialized by this connector (avoid recursive changes)
      debug("Ignoring change being applied: %s".format(FormField.changingValue))
    } else if (RealtimePage._ignoringChangeProperty.get() == evt.property && RealtimePage._ignoringChangeValue.get() == evt.newValue) {
      RealtimePage._ignoringChangeProperty.remove()
      RealtimePage._ignoringChangeValue.remove()
      debug(s"Ignoring change to property! ${evt.newValue}")
    } else {
      evt.property match {
        case property: PropertyAttribute[_] => ChildLike.parentOf(property) match {
          case t: IdentifiableTag => tagPropertyChanged(t, property, evt.oldValue, evt.newValue)
          case styleSheet: StyleSheet => {
            val ssa = property.asInstanceOf[StyleSheetAttribute[_]]
            styleSheet.hierarchicalParent match {
              case styleSpaces: StyleSpaces => styleSheetChanged(styleSheet.selectorString, ssa.style, evt.newValue.asInstanceOf[AnyRef])
              case t: IdentifiableTag => styleChanged(s"#${t.identity}", ssa.style, evt.newValue.asInstanceOf[AnyRef])
            }
          }
        }
        case _ => // Ignore non-PropertyAttributes
      }
    }
  }

  private def tagPropertyChanged(t: IdentifiableTag, property: PropertyAttribute[_], oldValue: Any, newValue: Any) = {
    debug("propertyChanged: %s.%s from %s to %s".format(t.xmlLabel, property.name, oldValue, newValue))
    if (t.root[Webpage[_]].nonEmpty && !property.isInstanceOf[StyleSheetAttribute[_]] && !t.isInstanceOf[tag.Text]) {
      if (property == t.id && oldValue == null) {
        // Ignore initial id change as it is sent when added
      } else {
        page.intercept.renderAttribute.fire(property) match {
          case Some(pa) => t match {
            case title: tag.Title if property.name == "content" => send(JavaScriptMessage("document.title = content;", Some(property.attributeValue)))
            case textual: Textual if property.name == "content" => textual match {
              case option: tag.Option => send(JavaScriptMessage("$('#%s').html(content);".format(t.id()), Some(property.attributeValue)))
              case _ => send(JavaScriptMessage("$('#%s').val(content);".format(t.id()), Some(property.attributeValue)))
            }
            case input: tag.Input if property.name == "value" => send(JavaScriptMessage("$('#%s').val(content);".format(t.id()), Some(property.attributeValue)))
            case input: tag.Input if property.name == "checked" => send(JavaScriptMessage(s"$$('#${t.identity}').prop('checked', ${property()});"))
            case option: tag.Option if property.name == "selected" => {
              if (option.selected()) {
                val select = option.parent.asInstanceOf[tag.Select]
                if (select.multiple()) {
                  throw new RuntimeException("Multiple Select Currently not supported!")
                } else {
                  send(JavaScriptMessage(s"$$('#${select.identity}').val(content);", content = Option(option.value())))
                }
              }
            }
            //            case option: tag.Option if (property.name == "selected") => if (property() == true) send(JavaScriptMessage(s"$$('#${t.id()}').attr('${property.name}', ${property()});"))
            case _ if property() == false => send(JavaScriptMessage("$('#%s').removeAttr('%s');".format(t.id(), property.name)))
            //            case _ if (property() == true) => send(JavaScriptMessage(s"$$('#${t.id()}').attr('${property.name}', '${property.name}');"))
            case _ => send(JavaScriptMessage("$('#%s').attr('%s', content);".format(t.id(), property.name), Some(property.attributeValue)))
          }
          case None => // Attribute shouldn't render so we ignore it
        }
      }
    }
  }

  private def styleSheetChanged(selector: String, style: Style[_], value: AnyRef) = {
    val anyStyle = style.asInstanceOf[Style[AnyRef]]
    val cssName = style.cssName
    val cssValue = value match {
      case null => ""
      case _ => anyStyle.persistence.toString(value, cssName, value.getClass)
    }
    send(JavaScriptMessage(s"$$.stylesheet('$selector', '$cssName', content)", Option(cssValue)))
  }

  private def styleChanged(selector: String, style: Style[_], value: AnyRef) = {
    val anyStyle = style.asInstanceOf[Style[AnyRef]]
    val cssName = style.cssName
    val cssValue = if (value != null) anyStyle.persistence.toString(value, cssName, value.getClass) else null
    send(JavaScriptMessage("$('%s').css('%s', content);".format(selector, cssName), Option(cssValue)))
  }

  private def send(js: JavaScriptMessage): Unit = {
    val message = EvalMessage(js.instruction, js.content)
    val data = message.asJson
    connections.send2Client("eval", data, sendWhenConnected = false)
  }
}

case class EvalMessage(instruction: String, content: Option[String])

object EvalMessage {
  implicit def EvalMessageCodecJson: CodecJson[EvalMessage] = casecodec2(EvalMessage.apply, EvalMessage.unapply)("instruction", "content")
}

object RealtimePage {
  private val _ignoringChangeProperty = new ThreadLocal[Property[_]]
  private val _ignoringChangeValue = new ThreadLocal[Any]
  private val _ignoringStructureChanges = new ThreadLocal[Boolean] {
    override def initialValue() = false
  }
  def ignoreStructureChanges[T](f: => T): T = {     // TODO: verify this will work with multiple connections to the same page
  val set = _ignoringStructureChanges.get()
    _ignoringStructureChanges.set(true)
    try {
      f
    } finally {
      if (!set) {
        _ignoringStructureChanges.set(false)
      }
    }
  }
  def ignoringStructureChanges = _ignoringStructureChanges.get()
  def ignoringChange[T](property: Property[T], value: T) = {
    _ignoringChangeProperty.set(property)
    _ignoringChangeValue.set(value)
    try {
      property := value
    } finally {
      _ignoringChangeProperty.remove()
      _ignoringChangeValue.remove()
    }
  }

  def apply[S <: Session](page: Webpage[S]) = synchronized {
    if (page == null) {
      throw new NullPointerException("Page cannot be null!")
    }
    page.store.getOrSet("realtime", new RealtimePage(page))
  }
}