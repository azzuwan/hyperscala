package org.hyperscala.site

import org.hyperscala.web.Website
import org.hyperscala.examples.helloworld.HelloWorldPage
import org.hyperscala.web.Scope
import org.hyperscala.examples.basic._
import org.hyperscala.examples.ui._
import org.hyperscala.examples.todomvc.TodoMVC
import org.hyperscala.examples.svg.{DynamicSVGExample, SVGShapesExample, BasicSVGExample}
import org.hyperscala.examples.comparison.PlayHelloWorldPage
import org.powerscala.log.{Logging, Level, Logger}
import java.io.File
import org.powerscala.log.writer.FileWriter
import org.powerscala.log.formatter.Formatter
import org.hyperscala.examples.snapsvg.SnapSVGExample
import com.outr.net.http.session.MapSession
import com.outr.net.http.jetty.JettyApplication
import com.outr.net.http.request.HttpRequest
import org.hyperscala.examples.connect.ConnectExample
import org.hyperscala.examples.service.TestService
import com.outr.net.http.filter.PathFilter
import org.hyperscala.hello.HelloSite
import org.hyperscala.site.extra.HyperscalaGenerator

/**
 * @author Matt Hicks <matt@outr.com>
 */
object HyperscalaSite extends Website[MapSession] with JettyApplication {
  // Setup file logging
  Logger.configure("root") {
    case l => {
      val directory = new File("logs")
      val writer = new FileWriter(directory, FileWriter.Daily("hyp"), append = true)
      l.withHandler(Formatter.Default, Level.Info, writer)
    }
  }
  // Configure System.out and System.err to go to logger
  Logging.configureSystem()

  override def init() = {
    super.init()
    HelloSite.initialize()    // Make sure HelloSite is initialized
  }

  override def defaultPort = 8889

  val site = new {
    val about = page(new HyperscalaAbout, Scope.Request, "/about.html", "/")
    val examples = page(new HyperscalaExamples, Scope.Request, "/examples.html")
    val generator = page(new HyperscalaGenerator, Scope.Page, "/generator.html")
    val documentation = page(new HyperscalaDocumentation, Scope.Request, "/documentation.html")
  }
  val examples = new {
    val hello = page(new HyperscalaExample(new HelloWorldPage), Scope.Request, "/example/hello.html")
//    val numberGuess = page(new HyperscalaExample(new NumberGuess), Scope.Page, "/example/number_guess.html")
    val scoped = page(new HyperscalaExample(ScopedExample), Scope.Application, "/example/scoped.html")
    val framed = page(new HyperscalaExample(new FramedExample), Scope.Page, "/example/framed.html")
    val connected = page(new HyperscalaExample(new ConnectedExample), Scope.Page, "/example/connected.html")
    val style = page(new StyleSheetExample(HyperscalaSite.this), Scope.Request, "/example/style.html")
    val userAgent = page(new HyperscalaExample(new UserAgentExample), Scope.Request, "/example/user_agent.html")
    val large = page(new LargePageExample(HyperscalaSite.this), Scope.Request, "/example/large.html")
    val static = page(new HyperscalaExample(new StaticHTMLExample), Scope.Request, "/example/static.html")
    val form = page(new HyperscalaExample(new FormExample), Scope.Session, "/example/form.html")
    val realTime = page(new HyperscalaExample(new RealtimeExample), Scope.Session, "/example/realtime.html")
    val realTimeWebpage = page(new HyperscalaExample(new RealtimeWebpageExample), Scope.Session, "/example/realtime_webpage.html")
    val realTimeForm = page(new HyperscalaExample(new RealtimeFormExample), Scope.Page, "/example/realtime_form.html")
    val visual = page(new HyperscalaExample(new VisualExample), Scope.Session, "/example/visual.html")
    val visualize = page(new HyperscalaExample(new VisualizeExample), Scope.Session, "/example/visualize.html")
    val visualizeAdvanced = page(new HyperscalaExample(new VisualizeAdvancedExample), Scope.Session, "/example/visualize_advanced.html")
    val autoComplete = page(new HyperscalaExample(new AutoCompleteExample), Scope.Page, "/example/autocomplete.html")
    val tabs = page(new HyperscalaExample(new TabsExample), Scope.Page, "/example/tabs.html")
    val tree = page(new HyperscalaExample(new TreeExample), Scope.Page, "/example/tree.html")
    val richEditor = page(new HyperscalaExample(new RichEditorExample), Scope.Page, "/example/richeditor.html")
    val editableContent = page(new HyperscalaExample(new EditableContentExample), Scope.Page, "/example/editablecontent.html")
    val nivoSlider = page(new HyperscalaExample(new NivoSliderExample), Scope.Page, "/example/nivoslider.html")
    val gritter = page(new HyperscalaExample(new GritterExample), Scope.Page, "/example/gritter.html")
    val spectrum = page(new HyperscalaExample(new SpectrumExample), Scope.Page, "/example/spectrum.html")
    val colorPicker = page(new HyperscalaExample(new ColorPickerExample), Scope.Page, "/example/colorpicker.html")
    val visualSearch = page(new HyperscalaExample(new VisualSearchExample), Scope.Page, "/example/visualsearch.html")
    val dialog = page(new HyperscalaExample(new DialogExample), Scope.Page, "/example/dialog.html")
    val spinner = page(new HyperscalaExample(new SpinnerExample), Scope.Page, "/example/spinner.html")
    val busyDialog = page(new HyperscalaExample(new BusyDialogExample), Scope.Page, "/example/busy_dialog.html")
    val draggable = page(new HyperscalaExample(new DraggableExample), Scope.Page, "/example/draggable.html")
    val droppable = page(new HyperscalaExample(new DroppableExample), Scope.Page, "/example/droppable.html")
    val dropReceiver = page(new HyperscalaExample(new DropReceiverExample), Scope.Page, "/example/drop_receiver.html")
    val clipboard = page(new HyperscalaExample(new ClipboardExample), Scope.Page, "/example/clipboard.html")
    val confirmDialog = page(new HyperscalaExample(new ConfirmDialogExample), Scope.Page, "/example/confirm_dialog.html")
    val dynamic = page(new HyperscalaExample(new DynamicContentExample), Scope.Page, "/example/dynamic.html")
    val dynamicPage = page(new DynamicPageExample(HyperscalaSite.this), Scope.Page, "/example/dynamic_page.html")
    val chat = page(new HyperscalaExample(new ChatExample), Scope.Page, "/example/chat.html")
    val pageChange = page(new HyperscalaExample(new PageChangeWarningExample), Scope.Page, "/example/page_change.html")
    val pageLoader = page(new PageLoaderExample(HyperscalaSite.this), Scope.Page, "/example/page_loader.html")
    val fileUploader = page(new HyperscalaExample(new FileUploaderExample), Scope.Page, "/example/file_upload.html")
    val dynamicURL = page(new HyperscalaExample(new DynamicURLExample), Scope.Page, "/example/dynamic_url.html")
    val multiSelect = page(new HyperscalaExample(new MultiSelectExample), Scope.Page, "/example/multi_select.html")
    val select2 = page(new HyperscalaExample(new Select2Example), Scope.Page, "/example/select2.html")
    val caseForm = page(new HyperscalaExample(new CaseFormExample), Scope.Page, "/example/case_form.html")
    val jQueryEvents = page(new HyperscalaExample(new jQueryEventsExample), Scope.Request, "/example/jquery_events.html")
    val selectWrapper = page(new HyperscalaExample(new SelectWrapperExample), Scope.Page, "/example/select_wrapper.html")
    val dsl = page(new HyperscalaExample(new RealtimeDSLExample), Scope.Page, "/example/dsl.html")
    val history = page(new HyperscalaExample(new HistoryExample), Scope.Page, "/example/history.html")
    val monitor = page(new HyperscalaExample(new MonitorExample), Scope.Page, "/example/monitor.html")
    val changeable = page(new HyperscalaExample(new ChangeableExample), Scope.Page, "/example/changeable.html")
    val compliance = page(new HyperscalaExample(new ComplianceExample), Scope.Page, "/example/compliance.html")
    val coordinates = page(new HyperscalaExample(new CoordinatesExample), Scope.Page, "/example/coordinates.html")
    val jsrequest = page(new HyperscalaExample(new JSRequestExample), Scope.Page, "/example/jsrequest.html")
    val bounding = page(new HyperscalaExample(new BoundingExample), Scope.Page, "/example/bounding.html")
    val jCanvas = page(new HyperscalaExample(new jCanvasExample), Scope.Page, "/example/jcanvas.html")
    val connect = page(new HyperscalaExample(new ConnectExample), Scope.Page, "/example/connect.html")
    val externalStyle = page(new HyperscalaExample(new ExternalStyleExample), Scope.Page, "/example/external_style.html")
    val headScript = page(new HyperscalaExample(new HeadScriptExample), Scope.Page, "/example/head_script.html")
    val basketJS = page(new HyperscalaExample(new BasketJSExample), Scope.Page, "/example/basketjs.html")
    val svg = new {
      val basic = page(new HyperscalaExample(new BasicSVGExample), Scope.Page, "/example/svg/basic.html")
      val shapes = page(new HyperscalaExample(new SVGShapesExample), Scope.Page, "/example/svg/shapes.html")
      val dynamic = page(new HyperscalaExample(new DynamicSVGExample), Scope.Page, "/example/svg/dynamic.html")
    }
    val snapsvg = page(new HyperscalaExample(new SnapSVGExample), Scope.Page, "/example/snapsvg.html")
    val playComparison = page(new PlayHelloWorldPage(HyperscalaSite.this), Scope.Page, "/example/comparison/play_hello_world.html")
  }
  val todoMVC = page(new TodoMVC(HyperscalaSite.this), Scope.Session, "/todomvc.html")

  handlers.add(PathFilter("/hello", HelloSite))

  TestService.register(this)    // Register the test service

  addClassPath("/images/", "images/")
  addClassPath("/css/", "css/")
  addClassPath("/js/", "js/")

  protected def createSession(request: HttpRequest, id: String) = new MapSession(id, this)

  def run() = main(Array.empty)
}
