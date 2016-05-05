import akka.actor.{Actor, ActorSystem, Props}
import akka.actor.Actor.Receive

/**
  * Created by Hierro on 5/4/16.
  */
class ActorWithImmutableList extends Actor {

  def update(list: List[Int]): Receive = {
    case "inc" => context.become(update(list ++ List(0)))
    case "get" => println(list.mkString(" "))
  }

  override def receive: Receive = update(List())
}

object Main extends App {
  implicit val system = ActorSystem("immutableList")

  val a = system.actorOf(Props[ActorWithImmutableList])

  a ! "inc"
  a ! "get"
  a ! "inc"
  a ! "get"
  a ! "inc"
  a ! "get"
  a ! "inc"
  a ! "get"
  a ! "inc"
  a ! "get"
  a ! "inc"
  a ! "get"
}