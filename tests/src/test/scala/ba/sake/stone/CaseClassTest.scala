package ba.sake.stone

import org.scalatest.{FlatSpec, Matchers}
import fixtures._

@Wither
case class Test1(
    simple: Data,
    var variable: Data,
    opt: Option[Data],
    list: List[Data]
)

class CaseClassTest extends FlatSpec with Matchers {
  val obj = Test1(Data(1), Data(2), None, Nil)

  val newData  = Data(42)
  val newData2 = Data(99)

  "Wither for `case class`" should "copy simple property" in {
    val obj2 = obj.withSimple(newData)
    obj2.simple shouldBe newData
  }
  it should "copy var property" in {
    val obj2 = obj.withVariable(newData)
    obj2.variable shouldBe newData
  }
  it should "copy Option[_] property" in {
    val obj2 = obj.withOpt(Option(newData))
    obj2.opt shouldBe Some(newData)
    val obj3 = obj.withOpt(newData2) // handy helper :)
    obj3.opt shouldBe Some(newData2)
  }
  it should "copy List[_] property" in {
    val obj2 = obj.withList(List(newData))
    obj2.list shouldBe List(newData)
    val obj3 = obj.withList(newData2) // handy helper :)
    obj3.list shouldBe List(newData2)
  }
}
