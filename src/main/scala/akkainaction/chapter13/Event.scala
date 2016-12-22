package akkainaction.chapter13

import java.time.ZonedDateTime

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
  def apply(s: String): State = s match {
    case "ok"       ⇒ OK
    case "error"    ⇒ ERROR
    case "warning"  ⇒ WARNING
    case "critical" ⇒ CRITICAL
  }
}

case class Event(
  host: String,
  service: String,
  state: State,
  time: ZonedDateTime,
  description: String,
  tag: Option[String] = None,
  metric: Option[Double] = None
)
