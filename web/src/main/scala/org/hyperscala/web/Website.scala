package org.hyperscala.web

import com.outr.net.http.WebApplication
import org.powerscala.MapStorage
import com.outr.net.http.session.Session
import org.powerscala.reflect._
import com.outr.net.http.handler.CachedHandler
import org.powerscala.log.Logging
import org.powerscala.event.processor.UnitProcessor
import com.outr.net.http.request.HttpRequest
import com.outr.net.http.response.{HttpResponseStatus, HttpResponse}
import com.outr.net.http.content.StringContent

/**
 * @author Matt Hicks <matt@outr.com>
 */
abstract class Website[S <: Session](implicit val manifest: Manifest[S]) extends WebApplication[S] {
  /**
   * Application stores content that should be persistent throughout the life of the website.
   */
  val application = new MapStorage[Any, Any]
  /**
   * Invoked when a page throws an error.
   */
  val pageError = new UnitProcessor[(Webpage[S], Throwable)]("pageError")
  /**
   * Keeps a reference to all currently loaded pages referenced by id.
   */
  private[web] val _pages = new MapStorage[String, Webpage[S]]
  /**
   * Access to all currently loaded pages.
   */
  lazy val pages = new Pages[S](this)

  def init(): Unit = {
    handlers += CachedHandler               // Add caching support
    handlers += new ErrorHandler(this)      // Add error handling support - calls errorPage if there's an error with no content
    pageError.on {                          // Default logging of errors on a page
      case (page, t) => error(s"An exception was thrown on ${request.url} (page ${page.getClass.getSimpleName}).", t)
    }
  }

  def page(creator: => Webpage[S], scope: Scope, uris: String*) = {
    val handler = new WebpageHandler(() => creator, scope, this, uris.toList)
    if (uris.nonEmpty) {
      addHandler(handler, uris: _*)
    }
    handler
  }

  def errorThrown(t: Throwable) = {
    error(s"Error occurred on URL: ${request.url}.", t)
  }

  def errorPage(request: HttpRequest,
                response: HttpResponse,
                status: HttpResponseStatus = HttpResponseStatus.InternalServerError): HttpResponse = {
    response.copy(status = status, content = StringContent("An error occurred!"))
  }

  override def dispose() = {
    super.dispose()

    application.clear()     // Clear out the application storage at dispose time
  }

  override def update(delta: Double) = {
    super.update(delta)

    try {
      pages.foreach {
        case page => page.update(delta)
      }
    } catch {
      case t: Throwable => error("Exception thrown while updating pages.", t)
    }
  }
}

class Pages[S <: Session](website: Website[S]) extends Iterable[Webpage[S]] with Logging {
  def ids = website._pages.map.keys

  def byId[W <: Webpage[S]](pageId: String) = website._pages.get[W](pageId)

  // TODO: this is broken currently - references that website.session references the current context session
//  def bySession(session: Session) = website._pages.values.filter(wp => wp.website.session eq session).toList

  // TODO: is there a better way to avoid updating duplicates of the same page?
  def iterator = website._pages.values.toSet[Webpage[S]].iterator

  def remove(page: Webpage[S]) = synchronized {
    debug(s"Removing webpage: ${page.pageId} (${page.getClass.getSimpleName})")
    website._pages.map.foreach {
      case (id, p) if p eq page => website._pages.remove(id)      // Clear all references of the page
      case _ => // Ignore others
    }
  }

//  def removeBySession(session: Session) = synchronized {
//    // TODO: restrict this to Scopes that don't span sessions (everything but Application)
//    bySession(session).foreach(remove)
//  }

  def apply[W <: Webpage[S]](implicit manifest: Manifest[W]) = iterator.collect {
    case w if w.getClass.hasType(manifest.runtimeClass) => w.asInstanceOf[W]
  }
}