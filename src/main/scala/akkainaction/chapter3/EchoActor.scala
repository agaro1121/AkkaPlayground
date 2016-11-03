package akkainaction.chapter3

import akka.actor.Actor

class EchoActor extends Actor {

  override def receive: Receive = {
    case msg: String ⇒ sender() ! msg
  }
}
