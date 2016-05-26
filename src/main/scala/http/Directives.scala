package http

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes
import akka.stream.ActorMaterializer
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Directive1, PathMatchers, ValidationRejection}

import scala.io.StdIn

/**
  * Created by Hierro on 5/25/16.
  */
/*object CommonReqs {
  implicit val actorSystem = ActorSystem("QuickStartGuide")
  implicit val actorMaterializer = ActorMaterializer()
  implicit val executionContext = actorSystem.dispatcher

}*/

object Directives extends App {

  val route =
    path("foo") {
      get {
        failWith(new RuntimeException("Oops! :-) "))
      }
    }

  val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)
  println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")

  /**
    * WITHOUT THIS, THE FLATMAP WILL COMPLETE RIGHT AWAY
    **/
  StdIn.readLine() // let it run until user presses return


  bindingFuture.flatMap { t =>
    t.unbind()
  }

}

object DirectivesWithRedirect extends App {

  val route =
    pathPrefix("foo") {
      pathSingleSlash {
        complete("yes")
      } ~
        pathEnd {
          redirect("/foo/", StatusCodes.PermanentRedirect)
        }
    }

  val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)
  println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")

  /**
    * WITHOUT THIS, THE FLATMAP WILL COMPLETE RIGHT AWAY
    **/
  StdIn.readLine() // let it run until user presses return


  bindingFuture.flatMap { t =>
    t.unbind()
  }
}

object DirectivesWithReject extends App {
  val route =
    path("a") {
      reject // don't handle here, continue on
    } ~
      path("a") { //this overrides the first route with path=a
        complete("foo")
      } ~
      path("b") {
        // trigger a ValidationRejection explicitly
        // rather than through the `validate` directive
        reject(ValidationRejection("Restricted!"))
      }

  runSingleRequestServer(route)
}

object DirectivesWithTFlatMap extends App {
  val intParameter: Directive1[Int] = parameter("a".as[Int])

  val myDirective: Directive1[Int] =
    intParameter.flatMap {
      case a if a > 0 => provide(2 * a)
      case _          => reject
    }

  val route =
    myDirective { n =>
      complete(n.toString)
    }

  runSingleRequestServer(route)
}