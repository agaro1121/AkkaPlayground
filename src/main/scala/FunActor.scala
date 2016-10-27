import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Created by Hierro on 4/28/16.
  */
object FunActor extends App {

  Future(5)
    .recover { case e ⇒ 6 }
    .map(_ + 1)

  val two = Seq(Future(1), Future(2))

  val res: Future[Int] = Future.fold(two)(0) {
    (acc, elem) ⇒
      println("acc=" + acc + ", elem=" + elem)
      acc + elem
  }

  res.onComplete(println)

  readLine()

}
