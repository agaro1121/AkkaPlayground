class A {
  def foo() = "A"
}

trait B extends A {
  override def foo() = "B"
}

trait C extends B {
  override def foo() = "C"
}

class D extends A {
  override def foo() = "D"
}

val z = new D with C {}

val s = z.foo()

trait SimulationEntity {
  def handleMessage(): String = " SE "
}

trait MixableParent extends SimulationEntity {
  override def handleMessage(): String = " MP "
}

trait NetworkEntity extends MixableParent {
  override def handleMessage() = " NE "
}

trait Router extends SimulationEntity {
  override def handleMessage(): String = " R "
}

val a = new Router with NetworkEntity {}

val t = a.handleMessage()

val b = new NetworkEntity with MixableParent with Router {}

b.handleMessage()

val test = new MixableParent with Router with NetworkEntity {}
val tt = test.handleMessage()