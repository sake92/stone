package ba.sake.stone

import org.scalatest.{FlatSpec, Matchers}
import fixtures._
import fixtures.Stuff.Inner

class CompanionTest extends FlatSpec with Matchers {
  val obj     = new Stuff(new Inner {})
  val newData = new Inner {}

  "Wither for companion `object` with types" should "copy simple property" in {
    val obj2 = obj.withInner(newData)
    obj2.inner shouldBe newData
  }
}
