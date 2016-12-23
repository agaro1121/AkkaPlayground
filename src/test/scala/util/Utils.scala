package util

import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory

object Utils {
  val testSystem: ActorSystem = ActorSystem("testsystem")

  val testSystemWithConfig = {
    //creates a system that attaches a test event listener
    val config = ConfigFactory.parseString(
      """
        |akka.loggers = [akka.testkit.TestEventListener]
      """.stripMargin
    )
    ActorSystem("testsystem", config)
  }

}
