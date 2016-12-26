package akkainaction.chapter13

import akka.NotUsed
import akka.http.scaladsl.model.{ContentTypeRange, ContentTypes, HttpEntity}
import akka.http.scaladsl.unmarshalling.{FromEntityUnmarshaller, Unmarshaller}
import akka.http.scaladsl.unmarshalling.Unmarshaller.UnsupportedContentTypeException
import akka.stream.Materializer
import akka.stream.scaladsl.{Flow, Framing, JsonFraming, Source}
import akka.util.ByteString
import spray.json.pimpString

import scala.concurrent.{ExecutionContext, Future}

object EventUnmarshaller {

  val supported: Set[ContentTypeRange] = Set[ContentTypeRange](
    ContentTypes.`text/plain(UTF-8)`,
    ContentTypes.`application/json`
  )

  def create(): FromEntityUnmarshaller[Source[Event, _]] = {
    val unmarshaller: Unmarshaller[HttpEntity, Source[Event, _]] =
      new Unmarshaller[HttpEntity, Source[Event, _]] {

        override def apply(entity: HttpEntity)(implicit
          ec: ExecutionContext,
          materializer: Materializer): Future[Source[Event, _]] = {
          entity.dataBytes.runForeach(bs ⇒ println(bs.utf8String))
          val future = entity.contentType match {
            case ContentTypes.`text/plain(UTF-8)` ⇒
              val maxLine = 10000
              /* Frames the log line into a specific shape by supplied delimiter */
              val frame: Flow[ByteString, String, NotUsed] =
                Framing.delimiter(ByteString("\n"), maxLine)
                  .map(_.utf8String)

              /* Parsing */
              val parse: Flow[String, Event, NotUsed] =
                Flow[String]
                  .map(Event(_))
                  .collect { case Some(event) => event } //discards blank lines

              /* Filtering */
              val filter: Flow[Event, Event, NotUsed] =
                Flow[Event]
                  .filter(_.state == ERROR)

              val inFlow: Flow[ByteString, Event, NotUsed] = frame via parse

              Future.successful(inFlow)

            case ContentTypes.`application/json` ⇒
              val inFlow: Flow[ByteString, Event, NotUsed] =
                JsonFraming.objectScanner(128)
                  .map(_.utf8String.parseJson.convertTo[Event])
              Future.successful(inFlow)
            case _ ⇒
              Future.failed(new UnsupportedContentTypeException(supported))
          }
          future.map(flow ⇒ entity.dataBytes.via(flow))
        }

      }
    unmarshaller.forContentTypes(supported.toList: _*)
  }

}
