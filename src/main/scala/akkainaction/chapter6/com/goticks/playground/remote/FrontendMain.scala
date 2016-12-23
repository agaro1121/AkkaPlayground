package akkainaction.chapter6.com.goticks.playground.remote

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import com.typesafe.config.ConfigFactory

class SimpleFront extends Actor with ActorLogging {
  override def receive: Receive = {
    case msg ⇒
      println(s"received $msg")
      log.info("received {}", msg)
  }
}

object FrontendMain extends App {

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
             port = 2554
           }
         }
       }
    """

  val config = ConfigFactory.parseString(conf)

  val system = ActorSystem("frontend", config)

  system.actorOf(Props[SimpleFront])

  val simple = system.actorSelection("akka.tcp://backend@0.0.0.0:2553/user/simple")

  simple ! "wassup backend"
}
