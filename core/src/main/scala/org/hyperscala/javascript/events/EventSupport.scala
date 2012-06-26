package com.outr.webframework.javascript.events

import com.outr.webframework.{WebAttribute, BodyContent}

/**
 * @author Matt Hicks <mhicks@sgine.org>
 */
trait EventSupport extends BodyContent {
  object event {
    val afterPrint = WebAttribute[String]("onbeforeprint")
    val beforePrint = WebAttribute[String]("onbeforeprint")
    val beforeOnLoad = WebAttribute[String]("onbeforeonload")
    val hasChange = WebAttribute[String]("onhaschange")
    val load = WebAttribute[String]("onload")
    val message = WebAttribute[String]("onmessage")
    val offline = WebAttribute[String]("onoffline")
    val online = WebAttribute[String]("ononline")
    val pageHide = WebAttribute[String]("onpagehide")
    val pageShow = WebAttribute[String]("onpageshow")
    val popState = WebAttribute[String]("onpopstate")
    val redo = WebAttribute[String]("onredo")
    val resize = WebAttribute[String]("onresize")
    val storage = WebAttribute[String]("onstorage")
    val undo = WebAttribute[String]("onundo")
    val unLoad = WebAttribute[String]("onunload")
    val blur = WebAttribute[String]("onblur")
    val change = WebAttribute[String]("onchange")
    val contextMenu = WebAttribute[String]("oncontextmenu")
    val focus = WebAttribute[String]("onfocus")
    val formChange = WebAttribute[String]("onformchange")
    val formInput = WebAttribute[String]("onforminput")
    val input = WebAttribute[String]("oninput")
    val invalid = WebAttribute[String]("oninvalid")
    val reset = WebAttribute[String]("onreset")
    val select = WebAttribute[String]("onselect")
    val submit = WebAttribute[String]("onsubmit")
    val keyDown = WebAttribute[String]("onkeydown")
    val keyPress = WebAttribute[String]("onkeypress")
    val keyUp = WebAttribute[String]("onkeyup")
    val click = WebAttribute[String]("onclick")
    val doubleClick = WebAttribute[String]("ondblclick")
    val drag = WebAttribute[String]("ondrag")
    val dragEnd = WebAttribute[String]("ondragend")
    val dragEnter = WebAttribute[String]("ondragenter")
    val dragLeave = WebAttribute[String]("ondragleave")
    val dragOver = WebAttribute[String]("ondragover")
    val dragStart = WebAttribute[String]("ondragstart")
    val drop = WebAttribute[String]("ondrop")
    val mouseDown = WebAttribute[String]("onmousedown")
    val mouseMove = WebAttribute[String]("onmousemove")
    val mouseOut = WebAttribute[String]("onmouseout")
    val mouseOver = WebAttribute[String]("onmouseover")
    val mouseUp = WebAttribute[String]("onmouseup")
    val mouseWheel = WebAttribute[String]("onmousewheel")
    val scroll = WebAttribute[String]("onscroll")
    val abort = WebAttribute[String]("onabort")
    val canPlay = WebAttribute[String]("oncanplay")
    val canPlayThrough = WebAttribute[String]("oncanplaythrough")
    val durationChange = WebAttribute[String]("ondurationchange")
    val emptied = WebAttribute[String]("onemptied")
    val ended = WebAttribute[String]("onended")
    val error = WebAttribute[String]("onerror")
    val loadedData = WebAttribute[String]("onloadeddata")
    val loadedMetaData = WebAttribute[String]("onloadedmetadata")
    val loadStart = WebAttribute[String]("onloadstart")
    val pause = WebAttribute[String]("onpause")
    val play = WebAttribute[String]("onplay")
    val playing = WebAttribute[String]("onplaying")
    val progress = WebAttribute[String]("onprogress")
    val rateChange = WebAttribute[String]("onratechange")
    val readyStateChange = WebAttribute[String]("onreadystatechange")
    val seeked = WebAttribute[String]("onseeked")
    val seeking = WebAttribute[String]("onseeking")
    val stalled = WebAttribute[String]("onstalled")
    val suspend = WebAttribute[String]("onsuspend")
    val timeUpdate = WebAttribute[String]("ontimeupdate")
    val volumeChange = WebAttribute[String]("onvolumechange")
    val waiting = WebAttribute[String]("onwaiting")
  }
}