package akkainaction.chapter13

import java.nio.charset.StandardCharsets
import java.nio.file.FileSystems
import java.nio.file.StandardOpenOption.{APPEND, CREATE, WRITE}

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.Supervision.{Resume, Stop}
import akka.stream.scaladsl.{FileIO, Flow, Framing, Keep, RunnableGraph, Sink, Source}
import akka.stream._
import akka.util.ByteString
import spray.json.pimpAny

import scala.concurrent.Future

object LogProcessorWithErrorHandling extends App {
  implicit val system = ActorSystem()
  implicit val ec = system.dispatcher
  implicit val mat = ActorMaterializer()
  /*implicit val mat = ActorMaterializer( //Supervisor Strategy can be put here too!!!
    ActorMaterializerSettings(system)
      .withSupervisionStrategy(decider)
  )*/


  val maxLine = 100
  /* Frames the log line into a specific shape by supplied delimiter */
  val frame: Flow[ByteString, String, NotUsed] =
    Framing.delimiter(ByteString("\n"), maxLine)
      .map(_.utf8String)

  /* Parsing */
  val parse: Flow[String, Event, NotUsed] =
    Flow[String]
      .map(Event(_))
      .collect { case Some(event) => event } //discards blank lines
      //.withAttributes(ActorAttributes.supervisionStrategy(decider)) // decider can be put here

  /* Filtering */
  val filter: Flow[Event, Event, NotUsed] =
    Flow[Event]
      .filter(_.state == ERROR)

  /* Serialize */
  val serialize: Flow[Event, ByteString, NotUsed] =
    Flow[Event]
      .map(event => ByteString(event.toJson.compactPrint))

  val composedFlow: Flow[ByteString, ByteString, NotUsed] =
    frame via parse via filter via serialize

  val inputFile = FileSystems.getDefault.getPath("src/main/scala/akkainaction/chapter13/SampleInput", "logs.log")
  val outputFile = FileSystems.getDefault.getPath("src/main/scala/akkainaction/chapter13/SampleOutput", "jsonLogs.log")

  val source: Source[ByteString, Future[IOResult]] =
    FileIO.fromPath(inputFile)

  val sink: Sink[ByteString, Future[IOResult]] =
    FileIO.toPath(outputFile, Set(CREATE, WRITE, APPEND))

  val decider: Supervision.Decider = {
    case _: LogParseException ⇒ Resume
    case _ ⇒ Stop
  }

  //composed
  val runnableGraph: RunnableGraph[Future[IOResult]] =
    source.via(composedFlow).toMat(sink)(Keep.right)
      .withAttributes(ActorAttributes.supervisionStrategy(decider)) //error handling can go here!!!



  //or one flow
  val flow : Flow[ByteString, ByteString, NotUsed] =
    Framing.delimiter(ByteString("\n"), maxLine)
      .map(_.utf8String)
      .map(Event(_))
      .collect { case Some(event) => event }
      .filter(_.state == ERROR)
      .map(event => ByteString(event.toJson.compactPrint))
      .withAttributes(ActorAttributes.supervisionStrategy(decider))



  runnableGraph.run().foreach {
    result =>
      println(s"Wrote ${result.count} bytes to '$outputFile'.")
      system.terminate()
  }
}
