package akkainaction.chapter3

import akka.actor.{Actor, ActorLogging, ActorRef, Props}

class SideEffectingActor2(listener: Option[ActorRef]) extends Actor with ActorLogging {

  import akkainaction.chapter3.SideEffectingActor2._

  override def receive: Receive = {
    case Greeting(who) ⇒
      val message = s"Hello $who!"
      log.info(message)
      listener.foreach(_ ! message)
  }

}

object SideEffectingActor2 {
  def props(listener: Option[ActorRef] = None) =
    Props(new SideEffectingActor2(listener))

  case class Greeting(msg: String)

}