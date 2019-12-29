package ba.sake.stone

@Wither
class ExampleData(val x: Int, private var y: Int, val opt: Option[String]) {
  override def toString(): String = s"ExampleData($x, $y, $opt)"
}

@Wither
class ExampleGen[T](val x: Int, val y: T) {
  override def toString(): String = s"ExampleGen($x, $y)"
}

object ExampleApp extends App {
  val data  = new ExampleData(5, 6, None)
  val data2 = data.withX(7).withY(9).withOpt("abc")
  println(data2)

  val dataGen  = new ExampleGen[String](5, "abc")
  val dataGen2 = dataGen.withX(7).withY("def").withX(7)
  println(dataGen2)
}
