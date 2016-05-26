package http

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import akka.http.scaladsl.model.HttpMethods._

import scala.concurrent.Future
import scala.util.Try

/**
  * Created by Hierro on 5/22/16.
  */
object LowLevelApI extends App {

  val serverSource: Source[Http.IncomingConnection, Future[Http.ServerBinding]] =
    Http().bind(interface = "localhost", port = 8080)

  val bindingFuture: Future[Http.ServerBinding] =
    serverSource.to(Sink.foreach { connection =>
      println("Accepted new connection from " + connection.remoteAddress)
    }).run()
}

object LowLevelAPIWithRoutes extends App {

  val serverSource = Http().bind(interface = "localhost", port = 8080)

  val requestHandler: HttpRequest => HttpResponse = {
    case HttpRequest(GET, Uri.Path("/"), _, _, _) =>
      HttpResponse(entity = HttpEntity(ContentTypes.`text/html(UTF-8)`,
        "<html><body>Hello world!</body></html>"))

    case HttpRequest(GET, Uri.Path("/ping"), _, _, _) =>
      HttpResponse(entity = "PONG!")

    case HttpRequest(GET, Uri.Path("/crash"), _, _, _) =>
      sys.error("BOOM!")

    case _: HttpRequest =>
      HttpResponse(404, entity = "Unknown resource!")
  }

  val bindingFuture: Future[Http.ServerBinding] =
    serverSource.to(Sink.foreach { connection =>
      println("Accepted new connection from " + connection.remoteAddress)

      connection handleWithSyncHandler requestHandler
      // this is equivalent to
      // connection handleWith { Flow[HttpRequest] map requestHandler }
    }).run()
}

object ConnectionPoolFail extends App {

  val poolClientFlow = Http().cachedHostConnectionPool[Int]("akka.io") //host being contacted
  val responseFuture: Future[(Try[HttpResponse], Int)] =
    Source.single(HttpRequest(uri = "/") -> 42)
      .via(poolClientFlow)
      .runWith(Sink.head)

  responseFuture.onComplete{ t =>
    t.map{ r =>
      println(s"random T=${r._2}")
      r._1.foreach { response =>
        response.entity.dataBytes.map(_.utf8String).runWith(Sink.foreach(println))
      }
    }
  }
}