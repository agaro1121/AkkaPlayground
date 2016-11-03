package akkainaction.chapter4

import akka.actor.{Actor, ActorLogging}

class LifeCycleHooksActor extends Actor with ActorLogging {

  println("Constructor")

  @scala.throws[Exception](classOf[Exception])
  override def preStart(): Unit = {
    println("preStart")
    super.preStart()
  }

  @scala.throws[Exception](classOf[Exception])
  override def postStop(): Unit = {
    println("postStop")
    super.postStop()
  }

  @scala.throws[Exception](classOf[Exception])
  override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
    println("preRestart")
    super.preRestart(reason, message)
  }


  @scala.throws[Exception](classOf[Exception])
  override def postRestart(reason: Throwable): Unit = {
    println("postRestart")
    super.postRestart(reason)
  }

  override def receive: Receive = {
    case "restart" ⇒ 
      throw new IllegalStateException("force restart")
    case msg: AnyRef ⇒
      println("Receive")
      sender() ! msg
  }

}
