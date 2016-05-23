package streams

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, Graph}
import akka.stream.scaladsl.{Sink, Source}
import akka.util.Timeout

import scala.concurrent.duration._
import akka.actor.ActorDSL._

/**
  * Created by Hierro on 5/11/16.
  */
object StreamsPractice extends App {
  implicit val sys = ActorSystem("Demo")
  implicit val mat = ActorMaterializer()
  implicit val timeout = Timeout(3 seconds)
  implicit val dispatcher = sys.dispatcher

  Source(List(1,2,3)).runForeach(println) //runs eagerly
  Source(List(1,2,3)).to(Sink.foreach(println)).run() //won't run unless you specify `.run`

  val numbers = Source(List(1,2,3))
  val strings = Source(List("a","b","c"))

  //fan in shape - more inputs and 1(less?) output

//  val composite = Source.combine(numbers,strings)
  


}
