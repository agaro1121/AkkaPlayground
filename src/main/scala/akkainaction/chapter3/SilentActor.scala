package akkainaction.chapter3

import akka.actor.{Actor, ActorRef}

class SilentActor extends Actor {
  import SilentActor._

  var internalState = Seq[String]()
  def state = internalState

  override def receive: Receive = {
    case SilentMessage(msg) ⇒ internalState = internalState :+ msg
    case GetState(actorRef) ⇒ actorRef ! state
  }
}

object SilentActor {
  case class SilentMessage(msg: String)
  case class GetState(receiver: ActorRef)
}