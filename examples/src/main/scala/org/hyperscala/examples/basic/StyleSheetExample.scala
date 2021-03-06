package org.hyperscala.examples.basic

import org.hyperscala.html._
import org.hyperscala.web.{Website, Webpage}
import org.powerscala.Color
import org.hyperscala.realtime.Realtime
import org.hyperscala.selector._
import org.hyperscala.selector.Selector._
import org.hyperscala.css.attributes.Decoration
import com.outr.net.http.session.Session

/**
 * @author Matt Hicks <matt@outr.com>
 */
class StyleSheetExample[S <: Session](website: Website[S]) extends Webpage(website) {
  require(Realtime)
  Realtime.connectStandard(this)

  val allStyle = head.selector(all)
  allStyle.fontFamily := "sans-serif"

  val buttonStyle = head.selector(element[tag.Button])
  buttonStyle.color := Color.Blue
  buttonStyle.fontSize := 18.pt

  val colorButtonStyle = head.selector(id("colorButton"))
  colorButtonStyle.color := Color.Red

  val h1HoverStyle = head.selector(pseudo(PseudoClass.Hover, Some(element[tag.H1])))
  h1HoverStyle.fontSize := 28.pt

  val h1 = new tag.H1(content = "Colored Red") {
    style.color := Color.Red
    style.textDecoration := Decoration.Underline
    style.textDecoration.important := false
  }
  body.contents += h1

  val b = new tag.Button(id = "colorButton", content = "Change Color") {
    clickEvent.on {
      case evt => {
        h1.style.color := Color.random
        if (colorButtonStyle.color() != null) {
          colorButtonStyle.color := null
        } else {
          colorButtonStyle.color := Color.random
        }
      }
    }
  }
  body.contents += b
  body.contents += new tag.Button(id = "removeButton", content = "Remove Hover Style") {
    clickEvent.on {
      case evt => head.deleteSelector(h1HoverStyle.selector)
    }
  }

  val idStyle = head.selector(b)
  idStyle.backgroundColor := Color.Green
}
