package scala.akkainaction.chapter3

import akka.testkit.TestKit
import org.scalatest.{BeforeAndAfterAll, Suite}

trait StopSystemAfterAll extends BeforeAndAfterAll {
  //This trait can only be used if it’s mixed in with a test that uses the TestKit.
  this: TestKit with Suite ⇒

  override protected def afterAll(): Unit = {
    super.afterAll()
    system.terminate()
  }

}
