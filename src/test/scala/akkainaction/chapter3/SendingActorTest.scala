package akkainaction.chapter3

import akka.actor.ActorSystem
import akka.testkit.TestKit
import org.scalatest.{MustMatchers, WordSpecLike}

import scala.akkainaction.chapter3.StopSystemAfterAll
import scala.util.Random

/*
* The MutatingCopyActor,
* ForwardingActor, and TransformingActor
* can all be tested in the same way.
* */

class SendingActorTest extends TestKit(ActorSystem("testsystem"))
  with WordSpecLike
  with MustMatchers
  with StopSystemAfterAll {

  "A Sending Actor" must {
    "send a message to another actor when it has finished processing" in {
      import SendingActor._
      val props = SendingActor.props(testActor)
      val sendingActor = system.actorOf(props, "sendingActor")
      val size = 1000
      val maxInclusive = 100000
      def randomEvents() = (0 until size).map { _ =>
        Event(Random.nextInt(maxInclusive))
      }.toVector
      val unsorted = randomEvents()
      val sortEvents = SortEvents(unsorted)
      sendingActor ! sortEvents

      expectMsgPF() { //PF = partial function
        case SortedEvents(events) =>
          events.size must be(size)
          unsorted.sortBy(_.id) must be(events)
      }
    }
  }

}
