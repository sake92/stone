package ba.sake.stone

object Stuff {
  trait Inner
}

@Wither
class Stuff(
    val inner: Stuff.Inner
)


class CompanionTest extends StoneTest {
  val obj     = new Stuff(new Stuff.Inner {})
  val newData = new Stuff.Inner {}

  "Wither for companion `object` with types" should "copy simple property" in {
    val obj2 = obj.withInner(newData)
    obj2.inner shouldBe newData
  }
}
