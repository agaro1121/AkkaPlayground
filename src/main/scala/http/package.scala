import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer

import scala.io.StdIn

/**
  * Created by Hierro on 5/25/16.
  */
package object http {
  implicit val actorSystem = ActorSystem("QuickStartGuide")
  implicit val actorMaterializer = ActorMaterializer()
  implicit val executionContext = actorSystem.dispatcher

  def runSingleRequestServer(route: Route) = {
    val bindingFuture = Http().bindAndHandle(route,"localhost",8080)
    println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")

    /**
      * WITHOUT THIS, THE FLATMAP WILL COMPLETE RIGHT AWAY
      * */
    StdIn.readLine() // let it run until user presses return


    bindingFuture.flatMap{ t =>
      t.unbind()
    }
  }

}
