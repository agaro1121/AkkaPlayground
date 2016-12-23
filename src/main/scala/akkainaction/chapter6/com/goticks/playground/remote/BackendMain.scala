package akkainaction.chapter6.com.goticks.playground.remote

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.event.LoggingReceive
import com.typesafe.config.ConfigFactory

class Simple extends Actor with ActorLogging {

  override def receive: Receive = LoggingReceive {
    case msg ⇒
      println(s"received message=$msg!")
      log.info("received message={}", msg)
  }


}

object BackendMain extends App {

  val conf =
    """
       akka {
         actor {
           provider = "akka.remote.RemoteActorRefProvider"
         }
         remote {
           enabled-transports = ["akka.remote.netty.tcp"]
           netty.tcp {
             hostname = "0.0.0.0"
             port = 2553
           }
         }
       }
    """

  val config = ConfigFactory.parseString(conf)

  val system = ActorSystem("backend", config)

  system.actorOf(Props[Simple], "simple")
}
