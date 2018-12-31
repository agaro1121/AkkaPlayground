package akkainaction.chapter13

import java.nio.file.StandardOpenOption.{APPEND, CREATE, WRITE}
import java.nio.file.{FileSystems, Files, Path}

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.unmarshalling.FromEntityUnmarshaller
import akka.stream.scaladsl.{BidiFlow, FileIO, Flow, JsonFraming, Keep, Sink, Source}
import akka.stream.{ActorMaterializer, IOResult}
import akka.util.ByteString
import akka.{Done, NotUsed}
import akkainaction.chapter13.EventMarshallar.LEM
import spray.json._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class LogsApiWithFanOut(implicit val mat: ActorMaterializer, ec: ExecutionContext) extends SprayJsonSupport with DefaultJsonProtocol {

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

  implicit val unmarshaller: FromEntityUnmarshaller[Source[Event, _]] = EventUnmarshaller.create()
  implicit val marshaller: LEM = EventMarshallar.create()
  val fanOut = new FanOut()

  def route: Route =
    pathPrefix("logs" / Segment) { logId ⇒
      pathEndOrSingleSlash {
        post {
          entity(as[Source[Event, _]]) { src ⇒
            onComplete(
              src
                .via(fanOut.processStates(logId))
                .toMat(logFileSink(logId))(Keep.right) //writes JSON to file
                .run()
            ){
              case Success(IOResult(count, Success(Done))) ⇒
                /*
                  can also be:
                   StatusCodes.OK -> LogReceipt(logId, count)
                   both are tuples
                 */
                complete((StatusCodes.OK, LogReceipt(logId, count)))

              case Success(IOResult(_, Failure(e))) ⇒
                complete(StatusCodes.BadRequest → ParseError(logId, e.getMessage))

              case Failure(e) ⇒
                complete(StatusCodes.BadRequest → ParseError(logId, e.getMessage))
            }
          }
        } ~
          get {
            extractRequest { req ⇒
              if (Files.exists(logFile(logId))) {
                val src: Source[ByteString, Future[IOResult]] = logFileSource(logId)
                src.runForeach(bs ⇒ println("***********"+bs.utf8String))
                complete(Marshal(src).toResponseFor(req))
              } else {
                complete(StatusCodes.NotFound)
              }
            }
          }
      }
    }

}
