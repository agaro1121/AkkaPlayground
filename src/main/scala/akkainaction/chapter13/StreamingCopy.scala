package akkainaction.chapter13

import java.nio.file.FileSystems
import java.nio.file.StandardOpenOption.{APPEND, CREATE, WRITE}
import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, IOResult}
import akka.stream.scaladsl.{FileIO, RunnableGraph, Sink, Source}
import akka.util.ByteString
import scala.concurrent.Future


object StreamingCopy extends App {
  val inputFile = FileSystems.getDefault.getPath("SampleInput", "ProdLogs.log")
  val outputFile = FileSystems.getDefault.getPath("SampleOutput", "ProdLogsCopy.log")

  val source: Source[ByteString, Future[IOResult]] =
    FileIO.fromPath(inputFile)

  val sink: Sink[ByteString, Future[IOResult]] =
    FileIO.toPath(outputFile, Set(CREATE, WRITE, APPEND))

  val runnableGraph: RunnableGraph[Future[IOResult]] = source to sink

  implicit val system = ActorSystem()
  implicit val ec = system.dispatcher
  implicit val mat = ActorMaterializer()

  runnableGraph.run().foreach { ioResult =>
      println(s"${ioResult.status}, ${ioResult.count} bytes read.")
      system.terminate()
  }




}
