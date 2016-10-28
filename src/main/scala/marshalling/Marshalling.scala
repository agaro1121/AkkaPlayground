package marshalling

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives
import org.scalatest.Matchers
import spray.json.DefaultJsonProtocol

import scala.concurrent.{Await, duration}
import duration._
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by Hierro on 6/12/16.
  */
object Marshalling1 extends App with Matchers {

  val string = "Yeah"
  val entityFuture = Marshal(string).to[MessageEntity]
  val entity = Await.result(entityFuture, 1.second) // don't block in non-test code!
  println(entity.contentType)
  entity.contentType shouldEqual ContentTypes.`text/plain(UTF-8)`

}

object Marshalling2 extends App with Matchers {
  val errorMsg = "Easy, pal!"
  val responseFuture = Marshal(420 -> errorMsg).to[HttpResponse]
  val response = Await.result(responseFuture, 1.second) // don't block in non-test code!
  println(response.status)
  println(response.entity.contentType)
  response.status shouldEqual StatusCodes.EnhanceYourCalm
  response.entity.contentType shouldEqual ContentTypes.`text/plain(UTF-8)`
}

object Marshalling3 extends App with Matchers {
  val request = HttpRequest(headers = List(headers.Accept(MediaTypes.`application/json`)))
  val responseText = "Plaintext"
  val respFuture = Marshal(responseText).toResponseFor(request) // with content negotiation!
  a[Marshal.UnacceptableResponseContentTypeException] should be thrownBy {
    Await.result(respFuture, 1.second) // client requested JSON, we only have text/plain!
  }
}

object MarshallingWithSprayJson extends App with SprayJsonSupport with DefaultJsonProtocol with Directives {

  final case class Item(name: String, id: Long)

  final case class Order(items: List[Item])

  implicit val itemFormat = jsonFormat2(Item)
  implicit val orderFormat = jsonFormat1(Order) // contains List[Item]

  val itemJson =
    """{
  "name": "Anthony",
  "id": 42
}"""

  val item = as[Item]
  println(item)

}

