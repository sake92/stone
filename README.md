# Stone [![Maven Central](https://img.shields.io/maven-central/v/ba.sake/stone-macros_2.13.svg?style=flat-square&label=Scala+2.13)](https://mvnrepository.com/artifact/ba.sake/stone-macros) [![Build Status](https://img.shields.io/travis/sake92/stone/master.svg?logo=travis&style=flat-square)](https://travis-ci.com/sake92/stone) 

Handy little macros.

## @Wither 

Generates with* methods. Why? :  
- more readable than named args
- autocomplete is nicer
- additional `with`ers for `Option`, `List` etc

If you have this:
```scala
@Wither
class MyClass(
  simple: Int,
  opt: Option[Int],
  list: List[Int]
)
```
you get to write:
```scala
val data = new ExampleData(1, Some(10), List(100))

val data2 = data.withSimple(2)        // MyClass(2, Some(10), List(100))

val dataOpt1 = data.withOpt(Some(11)) // MyClass(2, Some(11), List(100))
val dataOpt2 = data.withOpt(12)       // MyClass(2, Some(12), List(100))

val dataList1 = data.withList(List(101, 102)) // MyClass(7, None, List(101,102))
val dataList2 = data.withList(103, 104)       // MyClass(7, None, List(103,104))
```
