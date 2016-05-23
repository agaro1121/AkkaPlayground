package http

import akka.actor.ActorSystem
import akka.http.scaladsl._
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import akka.http.scaladsl.server.Directives._
import akka.stream.scaladsl.{Sink, Source}
import akka.util.ByteString

import scala.concurrent.Future
import scala.io.StdIn
import scala.util.Random

/**
  * Created by Hierro on 5/22/16.
  */
object CommonReqs {
  implicit val actorSystem = ActorSystem("QuickStartGuide")
  implicit val actorMaterializer = ActorMaterializer()
  implicit val executionContext = actorSystem.dispatcher

}


object Introduction extends App {

  import CommonReqs._

  val route =
    path("hello") {
      get {
        complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<h1>Say hello to akka-http</h1>"))
      }
    }

  val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)

  println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
  StdIn.readLine() // let it run until user presses return
  bindingFuture
    .flatMap(_.unbind()) // trigger unbinding from the port
    .onComplete(_ ⇒ actorSystem.terminate()) // and shutdown when done
}

/**
  * curl --limit-rate 50b 127.0.0.1:8080/random #slow HTTP Client - Signals back pressure
  **/
object IntroductionWithStreams extends App {

  import CommonReqs._

  val numbers = Source.fromIterator(() =>
    Iterator.continually(Random.nextInt()))

  val route =
    path("random") {
      get {
        complete(
          HttpEntity(
            ContentTypes.`text/plain(UTF-8)`,
            // transform each number to a chunk of bytes
            numbers.map(n => ByteString(s"$n\n"))
          )
        )
      }
    }

  val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)
  println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
  StdIn.readLine() // let it run until user presses return
  bindingFuture
    .flatMap(_.unbind()) // trigger unbinding from the port
    .onComplete(_ => actorSystem.terminate()) // and shutdown when done
}

object IntroductionWithHttpClientAndPrintsResponseStuff extends App {

  import CommonReqs._

  val responseFuture: Future[HttpResponse] =
      Http().singleRequest(HttpRequest(uri = "http://akka.io"))
//    Http().singleRequest(HttpRequest(uri = "http://localhost:8080/hello"))

  /*responseFuture.onComplete {
    println
  }*/
  responseFuture.onComplete { x => x.map(r => r.headers.foreach(h => println(s"name=${h.name()} value=${h.value()}"))) }
  responseFuture.onComplete { x =>
    println(x.map(r =>
      r.entity
        .getContentType()
        .getCharsetOption
        .orElse(HttpCharset("")(List[String]()))
    ))
  }
  /*responseFuture.onComplete { httpResponse => //prints body
    httpResponse.map { r =>
      import scala.concurrent.duration._
      r.entity.toStrict(1 second).foreach(e => println(s"contentType=${e.contentType} data=${e.data.utf8String}"))
    }
  }*/

  responseFuture.onComplete{ t => //prints body in a stream-safe way
     t.map{ r =>
       r.entity.dataBytes.map(_.utf8String).runWith(Sink.foreach(println))
     }
  }
}

object RoutingDSLSimpleServer extends App {

  import CommonReqs._

  val route =
    path("hello") {
      get {
        complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<h1>Say hello to akka-http</h1>"))
      }
    }

  val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)

  println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
  StdIn.readLine() // let it run until user presses return
  bindingFuture
    .flatMap(_.unbind()) // trigger unbinding from the port
    .onComplete(_ ⇒ actorSystem.terminate()) // and shutdown when done
}
object RoutingDSLSimpleServerWithMatchers1 extends App {

  import CommonReqs._

  /*
    http://localhost:8080/foo/bar/X42/edit
    http://localhost:8080/foo/bar/X42/create
    http://localhost:8080/foo/bar/X/create
    http://localhost:8080/foo/bar/X/edit
   */
  val route =
    path("foo"/"bar"/"X" ~ IntNumber.? / ("edit" | "create") ) { x =>
      get {
        complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<h1>Say hello to akka-http</h1>"))
      }
    }

  val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)

  println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
  StdIn.readLine() // let it run until user presses return
  bindingFuture
    .flatMap(_.unbind()) // trigger unbinding from the port
    .onComplete(_ ⇒ actorSystem.terminate()) // and shutdown when done
}
object RoutingDSLSimpleServerWithMatchers2 extends App {

  import CommonReqs._

  case class Color(name: String, red: Int, green: Int, blue: Int)

  //http://localhost:8080/color/anthony?r=1&g=2&b=3
  val route =
    (path("color" / Segment) & parameters('r.as[Int], 'g.as[Int], 'b.as[Int]))
      .as(Color) { color =>
        println(color)
        complete("Your color: " + color)
      }

  val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)

  println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
  StdIn.readLine() // let it run until user presses return
  bindingFuture
    .flatMap(_.unbind()) // trigger unbinding from the port
    .onComplete(_ ⇒ actorSystem.terminate()) // and shutdown when done
}
object RoutingDSLSimpleServerWithMatchers2WithValidation extends App {

  import CommonReqs._

  case class Color(name: String, red: Int, green: Int, blue: Int) {
    require(!name.isEmpty, "color name must not be empty")
    require(0 <= red && red <= 255, "red color component must be between 0 and 255")
    require(0 <= green && green <= 255, "green color component must be between 0 and 255")
    require(0 <= blue && blue <= 255, "blue color component must be between 0 and 255")
  }

  //http://localhost:8080/color/anthony?r=1&g=2&b=3
  val route =
    (path("color" / Segment) & parameters('r.as[Int], 'g.as[Int], 'b.as[Int]))
      .as(Color) { color =>
        println(color)
        complete("Your color: " + color)
      }

  val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)

  println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
  StdIn.readLine() // let it run until user presses return
  bindingFuture
    .flatMap(_.unbind()) // trigger unbinding from the port
    .onComplete(_ ⇒ actorSystem.terminate()) // and shutdown when done
}

