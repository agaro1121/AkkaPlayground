package http

import akka.actor.ActorSystem
import akka.event.{LoggingAdapter, Logging}
import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.{HttpResponse, HttpRequest}
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.{ActorMaterializer, Materializer}
import akka.stream.scaladsl.{Flow, Sink, Source}
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import java.io.IOException
import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.math._
import spray.json.DefaultJsonProtocol

/**
  * Created by Hierro on 5/4/16.
  */
trait AkkaHttpMicroservice {

  val routes = {
    pathPrefix("v1") {
      pathPrefix("v2") {
        (get & path("hello" / "one")) {
          get {
            complete {
              ToResponseMarshallable("Saluton Mondo !") // -> localhost:9000/v1/v2/hello/one
            }
          }
        }
      }
    }
  }

}

object Main extends App with AkkaHttpMicroservice {
  implicit val system = ActorSystem()
  implicit val executor = system.dispatcher
  implicit val materializer = ActorMaterializer()

    val logger = Logging(system, getClass)

  Http().bindAndHandle(routes, "localhost", 9000)
}