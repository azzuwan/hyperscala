package org.hyperscala.examples.basic

import org.hyperscala.examples.Example
import org.hyperscala.html._

/**
 * @author Matt Hicks <matt@outr.com>
 */
class ConnectedExample extends Example {
  connected[tag.HTML] {
    case html => html.head.title := "Connected Example - Modified the Title"
  }

  contents += new tag.P {
    contents += "Uses Markup.connected[HTML] to invoke a function (in this case, changing the title of the page) " +
      "when HTML is available as an ancestor in the tree. This allows modular components to be created and access " +
      "the elements higher in the hierarchical structure when it is connected."
  }
}
