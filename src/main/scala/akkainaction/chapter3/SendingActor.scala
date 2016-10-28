package akkainaction.chapter3

import akka.actor.{Actor, ActorRef, Props}


class SendingActor(receiver: ActorRef) extends Actor {
import SendingActor._

  override def receive: Receive = {
    case SortEvents(unsorted) ⇒ receiver ! SortedEvents(unsorted.sortBy(_.id))
  }
}

object SendingActor {
  case class Event(id: Long)
  case class SortEvents(unsorted: Vector[Event])
  case class SortedEvents(sorted: Vector[Event])

  def props(receiver: ActorRef) = Props(new SendingActor(receiver))
}