package akkainaction.chapter13

import akka.NotUsed
import akka.http.scaladsl.marshalling.{Marshaller, ToEntityMarshaller}
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.stream.scaladsl.JsonFraming
import akka.stream.scaladsl.{Flow, Framing, Source}
import akka.util.ByteString
import spray.json._

object EventMarshallar {

  type LEM = ToEntityMarshaller[Source[ByteString, _]]

  def create(): LEM = {
    def toText(src: Source[ByteString, _]) = {

      def toLogLine(event: Event): ByteString = {
        ByteString(s"${event.host} | ${event.service} | ${event.state} | ${event.time} | ${event.description}||\n")
      }

      val maxLine = 100000
      /* Frames the log line into a specific shape by supplied delimiter */
      val frame: Flow[ByteString, Event, NotUsed] =
        JsonFraming.objectScanner(maxLine)
          .map(_.utf8String.parseJson.convertTo[Event])

      val toLogLineFlow: Flow[Event, ByteString, NotUsed] =
        Flow[Event]
          .map(event ⇒ toLogLine(event))

      src via frame via toLogLineFlow
    }


    val js = ContentTypes.`application/json`
    val txt = ContentTypes.`text/plain(UTF-8)`

    val jsMarshaller = Marshaller.withFixedContentType(js) {
      src: Source[ByteString, _] ⇒ HttpEntity(js, src)
    }

    val txtMarshaller = Marshaller.withFixedContentType(txt) {
      src: Source[ByteString, _] ⇒ HttpEntity(txt, toText(src))
    }

    Marshaller.oneOf(jsMarshaller, txtMarshaller)
  }

}
