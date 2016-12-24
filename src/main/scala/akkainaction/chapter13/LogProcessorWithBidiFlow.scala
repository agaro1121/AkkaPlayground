package akkainaction.chapter13

import scala.concurrent.duration._
import scala.language.postfixOps
import java.nio.file.FileSystems
import java.nio.file.StandardOpenOption.{APPEND, CREATE, WRITE}
import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.Supervision.{Resume, Stop}
import akka.stream._
import akka.stream.scaladsl.{BidiFlow, FileIO, Flow, Framing, JsonFraming, Keep, RunnableGraph, Sink, Source}
import akka.util.ByteString
import spray.json.{pimpString, pimpAny}
import spray.json.DefaultJsonProtocol
import scala.concurrent.Future

object LogProcessorWithBidiFlow extends App with DefaultJsonProtocol {
  implicit val system = ActorSystem()
  implicit val ec = system.dispatcher
  implicit val mat = ActorMaterializer()

  val inputFile = FileSystems.getDefault.getPath("src/main/scala/akkainaction/chapter13/SampleInput", "jsonLogs.log")
  val outputFile = FileSystems.getDefault.getPath("src/main/scala/akkainaction/chapter13/SampleOutput", "jsonLogsBidi.log")

  val source: Source[ByteString, Future[IOResult]] =
    FileIO.fromPath(inputFile)

  val sink: Sink[ByteString, Future[IOResult]] =
    FileIO.toPath(outputFile, Set(CREATE, WRITE, APPEND))

  val maxLine = 128
  val inFlow: Flow[ByteString, Event, NotUsed] =
    JsonFraming.objectScanner(maxLine)
    .map(_.utf8String.parseJson.convertTo[Event])

  val outFlow: Flow[Event, ByteString, NotUsed] = Flow[Event].map{
    event =>
      ByteString(event.toJson.compactPrint)
  }

  /* Filtering */
  val filter: Flow[Event, Event, NotUsed] =
    Flow[Event]
      .filter(_.state == ERROR)

  val bidiFlow: BidiFlow[ByteString, Event, Event, ByteString, NotUsed] = BidiFlow.fromFlows(inFlow,outFlow)

  val flow: Flow[ByteString, ByteString, NotUsed] = bidiFlow.join(filter)

  val decider: Supervision.Decider = {
    case _: LogParseException ⇒ Resume
    case _ ⇒ Stop
  }

  val runnableGraph: RunnableGraph[Future[IOResult]] =
    source.via(flow).to(sink)
//    source.via(flow).to(Sink.foreach(bs ⇒ println(bs.utf8String)))
      .withAttributes(ActorAttributes.supervisionStrategy(decider))

  runnableGraph.run().foreach {
    result =>
      println(s"Wrote ${result.count} bytes to '$outputFile'.")
      system.scheduler.scheduleOnce(1 second)(system.terminate()) //system terminates too fast
  }


}
