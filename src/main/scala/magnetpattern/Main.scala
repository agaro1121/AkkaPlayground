package magnetpattern

import scala.language.implicitConversions

sealed trait Magnet {
  type Result

  def apply(): Result
}

object Magnet {
  implicit def fromSomething(something: Something): Magnet =
    new Magnet {
      override type Result = String
      override def apply(): Result = something.s
    }

  implicit def fromSomethingElse(somethingElse: SomethingElse): Magnet =
    new Magnet {
      override type Result = Int
      override def apply(): Result = somethingElse.i
    }
}

case class Something(s: String, i: Int)
case class SomethingElse(i: Int)

object Main extends App {

  /**
   * Could also be(for clarity):
   * {{{
   *   def doSomethingWithMagnet(magnet: Magnet): magnet.Result = magnet.apply()
   * }}}
   */
  def doSomethingWithMagnet(magnet: Magnet): magnet.Result = magnet()

  val s = Something("johnny", 5)
  val se = SomethingElse(6)

  println(doSomethingWithMagnet(s))
  println(doSomethingWithMagnet(se))

}
