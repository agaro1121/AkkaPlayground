package akkainaction.chapter3

import akka.actor.{Actor, ActorLogging}

class SideEffectingActor extends Actor with ActorLogging {
  import SideEffectingActor._

  override def receive: Receive = {
    case Greeting(msg) ⇒ log.info("Hello {}!", msg)
  }
}

object SideEffectingActor {
  case class Greeting(msg: String)
}