package akkainaction.chapter4

import akka.actor.{ActorSystem, Props}
import akka.testkit.TestKit
import org.scalatest.{MustMatchers, WordSpecLike}

import scala.akkainaction.chapter3.StopSystemAfterAll

class LifeCycleHooksActorTest extends TestKit(ActorSystem("testsystem"))
  with WordSpecLike
  with MustMatchers
  with StopSystemAfterAll {

  "LifeCycleHooks Actor" must {
    "Print out all hooks" in {
      val testActorRef = system.actorOf(Props[LifeCycleHooksActor], "LifeCycleHooks")
      testActorRef ! "restart"
      testActorRef.tell("msg", testActor)
      expectMsg("msg")
      system.stop(testActorRef)
      Thread.sleep(3000)
    }
  }

}
