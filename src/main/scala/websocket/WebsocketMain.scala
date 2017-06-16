package websocket

import akka.Done
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ws.{Message, TextMessage, WebSocketRequest}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Keep, Sink, Source}

import scala.concurrent.{Future, Promise}

object WebsocketMain extends App {

  implicit val actorSystem = ActorSystem("Websockets")
  implicit val mat = ActorMaterializer()
  import actorSystem.dispatcher

  // print each incoming strict text message
  val printSink: Sink[Message, Future[Done]] =
    Sink.foreach {
      case message: TextMessage.Strict =>
        println(message.text)
    }


  val flow: Flow[Message, Message, Promise[Option[Message]]] =
    Flow.fromSinkAndSourceMat(
      Sink.foreach(println),
      Source.maybe[Message]
    )(Keep.right)

  private val (_, promise) = Http().singleWebSocketRequest(
    WebSocketRequest("wss://api.gemini.com/v1/marketdata/btcusd"),
    flow
  )

  promise.future.foreach(maybeMessage => maybeMessage.map(msg => msg.asTextMessage.getStrictText))

}
