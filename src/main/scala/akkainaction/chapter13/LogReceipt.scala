package akkainaction.chapter13

import spray.json.DefaultJsonProtocol

case class LogReceipt(logId: String, count: Long)
object LogReceipt extends DefaultJsonProtocol {
  implicit val json = jsonFormat2(LogReceipt.apply)
}
