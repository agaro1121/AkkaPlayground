package scala.akkainaction.chapter3

import akka.actor.{ActorSystem, Props}
import akka.testkit.{CallingThreadDispatcher, EventFilter, TestKit}
import akkainaction.chapter3.SideEffectingActor
import akkainaction.chapter3.SideEffectingActor.Greeting
import com.typesafe.config.ConfigFactory
import org.scalatest.{MustMatchers, WordSpecLike}
import SideEffectingActorTest._

class SideEffectingActorTest extends TestKit(testSystem)
  with MustMatchers
  with WordSpecLike
  with StopSystemAfterAll {

  "The SideEffectingActor" must {

    """say Hello World! when a Greeting("World") is sent to it""" in {
      /*
      * The test is run in a single-threaded environment
      * because we want to check that the log event has
      * been recorded by the TestEventListener when the
      * SideEffectingActor is sent the “World” Greeting.
      * */
      val dispatcherId = CallingThreadDispatcher.Id
      val props = Props[SideEffectingActor].withDispatcher(dispatcherId) //creates single-threaded environment
      val greeter = system.actorOf(props)

      EventFilter.info(message = "Hello World!", occurrences = 1).intercept {
        //intercepts logged messages
        greeter ! Greeting("World")
      }
    }
  }

}

object SideEffectingActorTest {
  val testSystem = {
    //creates a system that attaches a test event listener
    val config = ConfigFactory.parseString(
      """
        |akka.loggers = [akka.testkit.TestEventListener]
      """.stripMargin
    )
    ActorSystem("testsystem", config)
  }
}
