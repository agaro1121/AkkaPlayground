package scala.akkainaction.chapter3

import akka.actor.ActorSystem
import akka.testkit.{TestActorRef, TestKit}
import org.scalatest.{MustMatchers, WordSpecLike}
import akkainaction.chapter3.SilentActor

class SilentActor01Test extends TestKit(ActorSystem("testsystem"))
  with WordSpecLike
  with MustMatchers
  with StopSystemAfterAll {

  "A Silent Actor" must {
    import SilentActor._

    "change state when it receives a message, single threaded" in {
      val silentActor = TestActorRef[SilentActor]
      silentActor ! SilentMessage("whisper")
      //checks internal state of actor
      silentActor.underlyingActor.state must contain("whisper")
    }

    "change state when it receives a message, multi threaded" in {
      fail("not implemented yet")
    }

  }
}
