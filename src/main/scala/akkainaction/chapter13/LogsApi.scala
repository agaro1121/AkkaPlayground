package akkainaction.chapter13

import java.nio.file.{FileSystems, Files, Path}
import java.nio.file.StandardOpenOption.{APPEND, CREATE, WRITE}

import akka.{Done, NotUsed}
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, StatusCodes}
import akka.stream.{ActorMaterializer, IOResult}
import akka.stream.scaladsl.{BidiFlow, FileIO, Flow, JsonFraming, Keep, Sink, Source}
import akka.util.ByteString

import scala.concurrent.Future
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import spray.json.DefaultJsonProtocol
import spray.json.{pimpAny, pimpString}

import scala.util.{Failure, Success}

class LogsApi(implicit val mat: ActorMaterializer) extends DefaultJsonProtocol {

  def logsDir: Path = FileSystems.getDefault.getPath("src/main/scala/akkainaction/chapter13/SampleOutput")

  def logFile(fileName: String): Path = logsDir.resolve(fileName)

  val maxLine = 1024
  val inFlow: Flow[ByteString, Event, NotUsed] =
    JsonFraming.objectScanner(maxLine)
      .map(_.utf8String.parseJson.convertTo[Event])

  val outFlow: Flow[Event, ByteString, NotUsed] = Flow[Event].map(event => ByteString(event.toJson.compactPrint))

  def bidiFlow: Flow[ByteString, ByteString, NotUsed] =
    BidiFlow.fromFlows(inFlow, outFlow).join(Flow[Event])

  def logFileSink(logId: String): Sink[ByteString, Future[IOResult]] =
    FileIO.toPath(logFile(logId), Set(CREATE, WRITE, APPEND))

  def logFileSource(logId: String): Source[ByteString, Future[IOResult]] =
    FileIO.fromPath(logFile(logId))

  def route: Route =
    pathPrefix("logs" / Segment) { logId ⇒
      pathEndOrSingleSlash {
        post {
          entity(as[HttpEntity]) { entity ⇒
            onComplete(
              entity.dataBytes
                .via(bidiFlow)
                .toMat(logFileSink(logId))(Keep.right) //writes JSON to file
                .run()
            ) {
              case Success(IOResult(count, Success(Done))) ⇒
                /*
                  can also be:
                   StatusCodes.OK -> LogReceipt(logId, count)
                   both are tuples
                 */
                complete((StatusCodes.OK, LogReceipt(logId, count)))

              case Success(IOResult(count, Failure(e))) ⇒
                complete(StatusCodes.BadRequest → ParseError(logId, e.getMessage))

              case Failure(e) ⇒
                complete(StatusCodes.BadRequest → ParseError(logId, e.getMessage))
            }
          }
        } ~
          get {
            if (Files.exists(logFile(logId))) {
              val src: Source[ByteString, Future[IOResult]] = logFileSource(logId)
              complete(HttpEntity(ContentTypes.`application/json`, src))
            } else {
              complete(StatusCodes.NotFound)
            }
          }

      }
    }

}
