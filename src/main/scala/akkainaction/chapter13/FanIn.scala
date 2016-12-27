package akkainaction.chapter13

import java.nio.file.{FileSystems, Path}
import java.nio.file.StandardOpenOption.{APPEND, CREATE, WRITE}

import akka.NotUsed
import akka.stream.{IOResult, SourceShape}
import akka.stream.scaladsl.{FileIO, GraphDSL, JsonFraming, Merge, Sink, Source}
import akka.util.ByteString

import scala.concurrent.Future

class FanIn {

  private def logsDir: Path = FileSystems.getDefault.getPath("src/main/scala/akkainaction/chapter13/SampleOutput")
  private def logFile(fileName: String): Path = logsDir.resolve(fileName)

  private def logFileSource(logId: String, state: State): Source[ByteString, Future[IOResult]] =
    FileIO.fromPath(logStateFile(logId, state))

  private def logFileSink(logId: String, state: State): Sink[ByteString, Future[IOResult]] =
    FileIO.toPath(logStateFile(logId, state), Set(CREATE, WRITE, APPEND))

  private def logStateFile(logId: String, state: State): Path =
    logFile(s"$logId-${state.toString.toLowerCase}")

  def mergeNotOk(logId: String): Source[ByteString, NotUsed] = {

    val jsonFramed = JsonFraming.objectScanner(128)

    val warning = logFileSource(logId, WARNING).via(jsonFramed)
    val error = logFileSource(logId, ERROR).via(jsonFramed)
    val critical = logFileSource(logId, CRITICAL).via(jsonFramed)

    Source.fromGraph(
      GraphDSL.create() {
        implicit builder ⇒
          import GraphDSL.Implicits._

          val warningShape = builder.add(warning)
          val errorShape = builder.add(error)
          val criticalShape = builder.add(critical)
          val merge = builder.add(Merge[ByteString](3))

          warningShape ~> merge
          errorShape ~> merge
          criticalShape ~> merge
          SourceShape(merge.out)
      }
    )

  }

}
