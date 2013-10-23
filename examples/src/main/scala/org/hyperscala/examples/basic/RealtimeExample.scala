package org.hyperscala.examples.basic

import org.hyperscala.web.Webpage
import org.hyperscala.html._
import org.hyperscala.realtime.{RealtimeEvent, Realtime}
import language.reflectiveCalls

/**
 * @author Matt Hicks <mhicks@outr.com>
 */
class RealtimeExample extends Webpage {
  require(Realtime)

  body.contents += new tag.Button(content = "Click Me!") {
    clickEvent := RealtimeEvent()

    clickEvent.on {
      case evt => info("I've been clicked!")
    }
  }
}
