package akkainaction.chapter13

import java.nio.file.{FileSystems, Path}

import akka.NotUsed
import akka.stream.scaladsl.{Broadcast, FileIO, Flow, GraphDSL, Sink, Source}
import akka.stream._
import akka.util.ByteString
import spray.json._

import scala.concurrent.Future
import java.nio.file.StandardOpenOption.{APPEND, CREATE, WRITE}

class FanOut {

  type FlowLike = Graph[FlowShape[Event, ByteString], NotUsed]

  def processStates(logId: String): FlowLike = {
    val jsFlow: Flow[Event, ByteString, NotUsed] = {
      val jsonOutFlow = Flow[Event].map { event =>
        ByteString(event.toJson.compactPrint)
      }
      jsonOutFlow
    }


    Flow.fromGraph(
      GraphDSL.create() {
        implicit builder ⇒
          import GraphDSL.Implicits._
          //5 outputs
          val bcast: UniformFanOutShape[Event, Event] = builder.add(Broadcast[Event](5))
          val js: FlowShape[Event, ByteString] = builder.add(jsFlow)

          val ok = Flow[Event].filter(_.state == OK)
          val warning = Flow[Event].filter(_.state == WARNING)
          val error = Flow[Event].filter(_.state == ERROR)
          val critical = Flow[Event].filter(_.state == CRITICAL)

          bcast ~> js.in
          bcast ~> ok       ~> jsFlow ~> logFileSink(logId, OK)
          bcast ~> warning  ~> jsFlow ~> logFileSink(logId, WARNING)
          bcast ~> error    ~> jsFlow ~> logFileSink(logId, ERROR)
          bcast ~> critical ~> jsFlow ~> logFileSink(logId, CRITICAL)

          FlowShape(bcast.in, js.out)
      })
  }

  private def logsDir: Path = FileSystems.getDefault.getPath("src/main/scala/akkainaction/chapter13/SampleOutput")
  private def logFile(fileName: String): Path = logsDir.resolve(fileName)

  private def logFileSource(logId: String, state: State): Source[ByteString, Future[IOResult]] =
    FileIO.fromPath(logStateFile(logId, state))
  private def logFileSink(logId: String, state: State): Sink[ByteString, Future[IOResult]] =
    FileIO.toPath(logStateFile(logId, state), Set(CREATE, WRITE, APPEND))
  private def logStateFile(logId: String, state: State): Path =
    logFile(s"$logId-${state.toString.toLowerCase}")

}
