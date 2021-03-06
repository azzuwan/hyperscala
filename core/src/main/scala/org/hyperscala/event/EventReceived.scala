package org.hyperscala.event

import argonaut.JsonObject

/**
 * EventReceived is fired on an IdentifiableTag if no other internal receiving support is found.
 *
 * If this event is handled externally Routing.Stop should be returned by the handler.
 *
 * @author Matt Hicks <matt@outr.com>
 */
case class EventReceived(event: String, json: JsonObject)