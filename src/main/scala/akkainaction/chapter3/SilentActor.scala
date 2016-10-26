package akkainaction.chapter3

import akka.actor.Actor

class SilentActor extends Actor {
  import SilentActor._

  var state = Seq[String]()

  override def receive: Receive = {
    case SilentMessage(msg) ⇒ state = state :+ msg
  }
}

object SilentActor {
  case class SilentMessage(msg: String)
}