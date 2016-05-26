package http

import akka.http.scaladsl.marshalling.ToEntityMarshaller
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.directives.{ContentTypeResolver, DirectoryListing}
import akka.http.scaladsl.server.directives.FileAndResourceDirectives.DirectoryRenderer

/**
  * Created by Hierro on 5/25/16.
  */
object FileAndResourceDerivatives1 extends App{

  val route =
    path("tmp") {
      getFromBrowseableDirectories("/Users/Hierro/dev/forked", "/Users/Hierro/dev/apps")
    }

  runSingleRequestServer(route)
}

object FileAndResourceDerictives2 extends App {
  val route =
    path("tmp") {
      getFromBrowseableDirectory("/tmp")
    }

  runSingleRequestServer(route)

}

object FileAndResourceDerictives3 extends App {
  val route =
    pathPrefix("tmp") {
      getFromDirectory("/Users/Hierro/test") //http://localhost:8080/tmp/file.txt - >/Users/Hierro/test/file.txt
    }
  runSingleRequestServer(route)
}

object FileAndResourceDirectives4 extends App {
  val route =
    path("tmp") {
      getFromFile("/Users/Hierro/test/file.txt")
    }
  runSingleRequestServer(route)
}

object FileAndResourceDirectives5 extends App {
  import  ContentTypeResolver.Default
  val route =
    path("tmp") {
      getFromResource("factorials.txt") //pulls from resource folder
    }
  runSingleRequestServer(route)
}

object FileAndResourceDirectives6 extends App {
  val route =
    pathPrefix("tmp") {
        getFromResourceDirectory("test") //Need to pass filename as part of URL -> http://localhost:8080/tmp/test.txt -> http://localhost:8080/tmp/test2/index.html
    }
  runSingleRequestServer(route)
}

object FileAndResourceDirectives7 extends App {
  val route =
    path("tmp") {
      listDirectoryContents("/tmp")
    } ~
      path("custom") {
        val renderer = new DirectoryRenderer {
          override def marshaller(renderVanityFooter: Boolean): ToEntityMarshaller[DirectoryListing] = ???
        }
        listDirectoryContents("/tmp")(renderer)
      }
  runSingleRequestServer(route)
}

