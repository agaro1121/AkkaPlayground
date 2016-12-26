package akkainaction.chapter13


import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.{DefaultJsonProtocol, JsString, JsValue, JsonFormat, RootJsonFormat, deserializationError}

/* Sample Input
 * my-host-1 | web-app | ok    | 2015-08-12T12:12:00.127Z | 5 tickets sold.||
 * my-host-2 | web-app | ok    | 2015-08-12T12:12:01.127Z | 3 tickets sold.||
 * my-host-1 | web-app | ok    | 2015-08-12T12:12:02.127Z | 1 tickets sold.||
 * my-host-2 | web-app | error | 2015-08-12T12:12:03.127Z | exception!!||
*/

sealed trait State

case object OK       extends State
case object WARNING  extends State
case object ERROR    extends State
case object CRITICAL extends State

object State {

  implicit val stateFormat = new JsonFormat[State] {
    def write(state: State) = JsString(state.toString.toLowerCase)
    def read(value: JsValue): State = value match {
      case JsString("ok") => OK
      case JsString("warning") => WARNING
      case JsString("error") => ERROR
      case JsString("critical") => CRITICAL
      case js =>
        val msg = s"Could not deserialize $js to State."
        throw LogParseException(msg)
    }
  }

  def apply(s: String): Option[State] = s.toLowerCase match {
    case "ok"       ⇒ Some(OK)
    case "error"    ⇒ Some(ERROR)
    case "warning"  ⇒ Some(WARNING)
    case "critical" ⇒ Some(CRITICAL)
    case _ => None
  }
}

case class Event(
  host: String,
  service: String,
  state: State,
  time: String,
  description: String,
  tag: Option[String] = None,
  metric: Option[Double] = None
)

object Event extends DefaultJsonProtocol with SprayJsonSupport {

  implicit val json: RootJsonFormat[Event] = jsonFormat7(Event.apply)

  def apply(s: String): Option[Event] = {
    val parts: Array[String] = s.split("\\|")
    if(parts.length == 5) {
      Some(Event(parts(0), parts(1), State(parts(2).replaceAll("\\s","").trim).get, parts(3), parts(4)))
    } else {
      println(s"**** could not parse $s")
      None
    }
  }
}