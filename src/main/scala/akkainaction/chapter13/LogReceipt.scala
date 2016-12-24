package akkainaction.chapter13

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.DefaultJsonProtocol

case class LogReceipt(logId: String, count: Long)
object LogReceipt extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val json = jsonFormat2(LogReceipt.apply)
}
