# Stone [![Maven Central](https://img.shields.io/maven-central/v/ba.sake/stone-macros_2.13.svg?style=flat-square&label=Scala+2.13)](https://mvnrepository.com/artifact/ba.sake/stone-macros) [![Build Status](https://img.shields.io/travis/sake92/stone/master.svg?logo=travis&style=flat-square)](https://travis-ci.com/sake92/stone) 

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
