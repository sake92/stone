package ba.sake.stone

import org.scalatest.{FlatSpec, Matchers}

case class Data(value: Int) // simple data has implicits conversions lurking

trait TestData {
  def simple: Data
  def variable: Data
  def opt: Option[Data]
}

@Wither
case class Test1(
    simple: Data,
    var variable: Data,
    opt: Option[Data],
    list: List[Data],
) extends TestData

class WitherTest extends FlatSpec with Matchers {
  val newData  = Data(42)
  val newData2 = Data(99)

  val TestData = List(
    Test1(Data(1), Data(2), None, Nil)
  )

  "Wither" should "copy simple property" in {
    TestData foreach { obj =>
      val obj2 = obj.withSimple(newData)
      obj2.simple shouldBe newData
    }
  }
  it should "copy var property" in {
    TestData foreach { obj =>
      val obj2 = obj.withVariable(newData)
      obj2.variable shouldBe newData
    }
  }
  it should "copy Option[_] property" in {
    TestData foreach { obj =>
      val obj2 = obj.withOpt(Option(newData))
      obj2.opt shouldBe Some(newData)
      val obj3 = obj.withOpt(newData2) // handy helper :)
      obj3.opt shouldBe Some(newData2)
    }
  }
  it should "copy List[_] property" in {
    TestData foreach { obj =>
      val obj2 = obj.withList(List(newData))
      obj2.list shouldBe List(newData)
      val obj3 = obj.withList(newData2) // handy helper :)
      obj3.list shouldBe List(newData2)
    }
  }
}
