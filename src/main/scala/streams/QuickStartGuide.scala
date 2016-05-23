package streams

import java.nio.file.Paths

import akka.{Done, NotUsed}
import akka.actor.ActorSystem
import akka.stream._
import akka.stream.scaladsl.Tcp.{IncomingConnection, ServerBinding}
import akka.stream.scaladsl._
import akka.stream.scaladsl.Framing
import akka.util.ByteString

import scala.concurrent.Future
import scala.util.{Failure, Success}
import scala.concurrent.duration._


/**
  * Created by Hierro on 5/21/16.
  */
object CommonReqs {
  implicit val actorSystem = ActorSystem("QuickStartGuide")
  implicit val actorMaterializer = ActorMaterializer()
  implicit val executionContext = actorSystem.dispatcher

}



object QuickStartGuideWithSource extends App {
  import CommonReqs._

  val source: Source[Int, NotUsed] = Source(1 to 100) //emits numbers 1-100
  source.runForeach(i => println(i))
}


object QuickStartGuideWithSink extends App {
  import CommonReqs._

  val source: Source[Int, NotUsed] = Source(1 to 100)
  val factorials = source.scan(BigInt(1))((acc, next) => acc * next)

  val result: Future[IOResult] =
    factorials
      .map(num => ByteString(s"$num\n"))
      .runWith(FileIO.toPath(Paths.get("factorials.txt")))

  result.onComplete{
    case Success(x) => println(x)
    case Failure(x) => println(x)
  }

}

object QuickStartGuideWithFlow extends App {
  import CommonReqs._

  val source: Source[Int, NotUsed] = Source(1 to 100)
  val factorials = source.scan(BigInt(1))((acc, next) => acc * next)

  def lineSink(filename: String): Sink[String, Future[IOResult]] =
    Flow[String]
      .map(s => ByteString(s + "\n"))
  /**
    * when chaining operations on a Source or Flow the type of the auxiliary
    * information, materialized value, is given by the leftmost
    * starting point; since we want to retain what the FileIO.toFile sink
    * has to offer, we need to say Keep.right).
    * */

      /*
        * Regular Comment
        * */
      .toMat(FileIO.toPath(Paths.get(filename)))(Keep.right)


  factorials.map(_.toString).runWith(lineSink("factorial2.txt"))
}

object QuickStartGuideWithThrottle extends App {
  import CommonReqs._

  val source: Source[Int, NotUsed] = Source(1 to 100)
  val factorials = source.scan(BigInt(1))((acc, next) => acc * next)

  val done: Future[Done] =
    factorials
      .zipWith[Int,String](Source(0 to 10))((num, idx) => s"$idx! = $num")
      .throttle(1, 1 second, 9, ThrottleMode.Shaping) //1 element per second with burst = 9 (spits out 0-8 in the first shot)
      .runForeach(println)

  done.onComplete{ d => println(d) ; actorSystem.terminate() }
}

object CoreConceptsWithRunnableGraph extends App {
  import CommonReqs._
  val source: Source[Int,NotUsed] = Source(0 to 10)
  val sink: Sink[Int,Future[Int]] = Sink.fold[Int,Int](0)(_+_)

  val runnableGraph: RunnableGraph[Future[Int]] = source.toMat(sink)(Keep.right)

  val sum = runnableGraph.run().onComplete{ d => println(d) ; actorSystem.terminate()}
}

object CoreConceptsRunWith extends App {
  import CommonReqs._

  val source = Source(0 to 10)
  val sink = Sink.fold[Int,Int](0)(_+_)

  val sum: Future[Int] = source.runWith(sink)
    sum.onComplete{x => println(x) ; actorSystem.terminate() }
}

object DifferentWaysToWire extends App {
  import CommonReqs._

//  Starting from a Source
  val source = Source(1 to 6).map(_ * 2)
  source.to(Sink.foreach(println(_)))
//    .run()


//  Starting from a Sink
  val sink: Sink[Int, NotUsed] = Flow[Int].map(_ * 2).to(Sink.foreach(println(_)))
  Source(1 to 6).to(sink)
//    .run()

//  Broadcast to a sink inline
  val otherSink: Sink[Int, NotUsed] =
    Flow[Int].alsoTo(Sink.foreach(println(_))).to(Sink.ignore)
  Source(1 to 6).to(otherSink)
//    .run()

//  Broadcast to a sink inline
  val otherSink2: Sink[Int, NotUsed] =
    Flow[Int]
      .alsoTo(Sink.foreach(s => println(s"first  sink: $s")))
      .to(Sink.foreach(s => println(s"second sink: $s")))
  Source(1 to 6).to(otherSink2)
    .run()

  actorSystem.terminate()
}

object FusingOperators extends App {
  import akka.stream.Fusing
  import CommonReqs._

  val flow = Flow[Int].map(_ * 2).filter(_ > 500) //this can be done asynchronously across different actors
  val fused = Fusing.aggressive(flow) //this will be executed in the same actor

  Source.fromIterator { () => Iterator from 0 }
    .via(fused)
    .take(1000) //take first 1000 elements that fit criteria. Stops at 2500
      .runForeach(println)

}

object AsyncBoundaries extends App {
  import CommonReqs._

  Source(List(1, 2, 3))
    .map(_ + 1).async //sets async boundary => Source(List(1, 2, 3)).map(_ + 1) will be done in one actor
    .map(_ * 2)
    .to(Sink.foreach(println)).run()
}

object StreamIOServer extends App {
  import CommonReqs._

  val binding: Future[ServerBinding] =
    Tcp().bind("127.0.0.1", 8888).to(Sink.ignore).run()

  binding.map { b =>
    println(b) //prints server binding



    b.unbind() onComplete {
      case x => println(s"COMPLETED: $x")
        actorSystem.terminate()
    }
  }
}

object StreamIOServerWithIncomingConnections extends App {
  import CommonReqs._

  val connections: Source[IncomingConnection,Future[ServerBinding]] = Tcp().bind("127.0.0.1",8888)

  connections runForeach { connection =>
    println(s"New connection from: ${connection.remoteAddress}")

    val echo = Flow[ByteString]
      .via(Framing.delimiter(
        ByteString("\n"),
        maximumFrameLength = 256,
        allowTruncation = true))
      .map(_.utf8String)
      .map(_ + "!!!\n")
      .map(ByteString(_))

    connection.handleWith(echo)
  }
}



