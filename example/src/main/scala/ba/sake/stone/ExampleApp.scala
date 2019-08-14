package ba.sake.stone

@Wither
class ExampleData(val x: Int, var y: Int) {
  override def toString(): String = s"ExampleData($x, $y)"
}

object ExampleApp extends App {

  val data = new ExampleData(5, 6)
  val data2 = data.withX(7).withY(9)

  println(data2)

}
