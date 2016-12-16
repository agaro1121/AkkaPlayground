package http

import akka.http.scaladsl.model.{StatusCodes, Uri}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route

object Playground extends App {
  /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
  * Routes:
  *
  * 1. /api/product/{some-segment}
  * 2. /api/product/{some-segment}/{quantity-value}
  * 3. /api/product${query-parameters}
  * 4. /api/products -> redirect ->  /api/product?url=redirectUrl
  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

  val route1: Route =
    pathPrefix("api") {
      pathPrefix("product") {
        path(Segment) { segment ⇒ // http://localhost:8080/api/product/red
          complete(s"segment=$segment")
        } ~
        parameters('url.as[String]) { url ⇒ // http://localhost:8080/api/product?url=Some
          get { complete(s"url=$url") }
        } ~
        path(Segment / IntNumber.?) { (seg, num) ⇒ // http://localhost:8080/api/product/red/ OR
          complete(s"seg=$seg :: num=$num") // http://localhost:8080/api/product/red/5
        }
      }
    }

  val route2: Route =
    pathPrefix("api") {
      pathPrefix("product") {
        pathPrefix(Segment) { segment ⇒ // http://localhost:8080/api/product/red
          path(IntNumber) { num ⇒
            complete(s"segment=$segment :: num=$num") //http://localhost:8080/api/product/red/5
          } ~
          pathEndOrSingleSlash {
            complete(s"segment=$segment") // http://localhost:8080/api/product/red/ OR http://localhost:8080/api/product/red
          }
        } ~
          parameters('url.as[String]) { url ⇒ // http://localhost:8080/api/product?url=Some
            get { complete(s"url=$url") }
          }
      } ~ path("redirect"){
          redirect(Uri("/api/product?url=Some"), StatusCodes.PermanentRedirect)
      } ~
        complete("Went straight to product huh")
    }


  runSingleRequestServer(route2)
}
