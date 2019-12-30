# stone

Handy little macros.

## @Wither 

Generates with* methods. Why? Bit more readable than named args, autocomplete is nicer, formatting also.

```scala
@Wither
class ExampleData(val x: Int, val y: Int)
```
generates
```scala
class ExampleData(val x: Int, val y: Int) {
  def withX(x: Int) = new ExampleData(x = x, y = y)
  def withY(y: Int) = new ExampleData(x = x, y = y)
}
```
so you can do
```scala
val data = new ExampleData(5, 6)
val data2 = data.withX(7).withY(9)

println(data2) // ExampleData(7, 9)
```
