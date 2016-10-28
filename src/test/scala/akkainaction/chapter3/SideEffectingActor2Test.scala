package scala.akkainaction.chapter3

import akka.actor.{ActorSystem, UnhandledMessage}
import akka.testkit.TestKit
import akkainaction.chapter3.SideEffectingActor2
import akkainaction.chapter3.SideEffectingActor2.Greeting
import org.scalatest.{MustMatchers, WordSpecLike}

class SideEffectingActor2Test extends TestKit(ActorSystem("testsystem"))
  with WordSpecLike
  with MustMatchers
  with StopSystemAfterAll {

  "The SideEffectingActor2" must {
    """say Hello World! when a Greeting("World") is sent to it""" in {

      val props = SideEffectingActor2.props(Some(testActor))
      val greeter = system.actorOf(props, "greeter02-1")
      greeter ! Greeting("World")

      expectMsg("Hello World!")
    }

    "say something else and see what happens" in {
      val props = SideEffectingActor2.props(Some(testActor))
      val greeter = system.actorOf(props, "greeter02-2")
      greeter ! Greeting("Anthony, his royal highness")

      expectMsg("Hello Anthony, his royal highness!")
    }

    "send an unhandled message type see what happens" in {
      val props = SideEffectingActor2.props(Some(testActor))
      val greeter = system.actorOf(props, "greeter02-3")
      greeter ! "Anthony, his royal highness"
      system.eventStream.subscribe(testActor, classOf[UnhandledMessage])

      /*UnhandledMessage(message: Any, sender: ActorRef, recipient: ActorRef)
       * sender = who sent the message
       * recipient = actor who was supposed to receive it
       */
      expectMsg(UnhandledMessage("Anthony, his royal highness", system.deadLetters, greeter))
    }
  }

}
