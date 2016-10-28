package akkainaction.chapter3

import akka.actor.{Actor, ActorRef, Props}

class FilteringActor(nextActor: ActorRef, bufferSize: Int) extends Actor {

  import FilteringActor._

  var lastMessages = Vector[Event]() //LRU cache

  override def receive: Receive = {
    case msg: Event ⇒
      if (!lastMessages.contains(msg)) {
        lastMessages = lastMessages :+ msg
        nextActor ! msg
        if (lastMessages.size > bufferSize) {
          // discard the oldest
          lastMessages = lastMessages.tail
        }
      }
  }
}

object FilteringActor {

  case class Event(id: Long)

  def props(nextActor: ActorRef, bufferSize: Int) =
    Props(new FilteringActor(nextActor, bufferSize))
}

