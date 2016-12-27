package akkainaction.chapter13

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer

object LogProcessorHttp extends App {

  implicit val system = ActorSystem("http-server")
  implicit val ex = system.dispatcher
  implicit val actorMaterializer = ActorMaterializer()


  //  val api = new LogsApi()
  //  val api = new LogsApiWithCustomUnmarshallar()
  //  val api = new LogsApiWithCustomUnmarshallarAndMarshaller()
//  val api = new LogsApiWithFanOut()
  val api = new LogsApiWithFanIn()

  Http().bindAndHandle(api.route, "localhost", 9000)

  println("System Ready...")
  readLine()
  system.terminate().onComplete(println)
}
