package akkainaction.chapter3

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.WordSpecLike

import scala.akkainaction.chapter3.StopSystemAfterAll

class EchoActorTest extends TestKit(ActorSystem("testsystem"))
  with WordSpecLike
  with ImplicitSender //preferred way to test 2-way messages
  with StopSystemAfterAll {

  "Reply with the same message it receives without ask" in {
    val echo = system.actorOf(Props[EchoActor], "echo2")
    echo ! "some message"
    expectMsg("some message")
  }

}
