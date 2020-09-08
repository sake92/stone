# Stone [![Maven Central](https://img.shields.io/maven-central/v/ba.sake/stone-macros_2.13.svg?style=flat-square&label=Scala+2.13)](https://mvnrepository.com/artifact/ba.sake/stone-macros) [![Build Status](https://img.shields.io/travis/sake92/stone/master.svg?logo=travis&style=flat-square)](https://travis-ci.com/sake92/stone) 

Scala **2.13 only**!  
ScalaJS 1 is supported.

## @Route
Generates apply/unapply methods for extracting/constructing URLs. Here's why:  
- type safe URLs/routes
- unlike [Play SIRD](https://www.playframework.com/documentation/2.8.x/ScalaSirdRouter) and others, it can also **construct a URL**

In Play, Angular and other frameworks you'd write something like this:  
`/users/:id/:name ? minAge=:minAge & qs=:qs...`

With `@Route` macro you write this:
```scala
@Route
class UsersRoute(
    p1: "users", // fixed path
    val id: Long, // path variable
    val name: String
)(
    val minAge: Int,      // query param, mandatory
    val opt: Option[Int], // query param, optional
    val qs: Set[String]   // query param, multi
)

// construct a URL, type-safely
val route = UsersRoute(1, "Sake")(123, Set("q1"), Some(18))
val redirectUrl = route.urlData.url // /users/1/Sake?a=123&qs=q1&minAge=18

// deconstruct a string URL to type-safe data
"users/1/Sake?minAge=123&qs=q1&qs=q2&opt=456" match {
  case UsersRoute(id, name, minAge, qs, opt) =>
    println(s"$id, $name, $minAge, $qs, $opt") // 1, Sake, 123, Set(q1, q2), Some(456)
  case _ => println("404 Not Found")
}
```

---

## @Wither

Generates `with*` methods. Here's why:  
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

data.withSimple(2)            // MyClass(2, Some(10), List(100))

data.withOpt(Some(11))        // MyClass(2, Some(11), List(100))
data.withOpt(12)              // MyClass(2, Some(12), List(100))

data.withList(List(101, 102)) // MyClass(7, None, List(101,102))
data.withList(103, 104)       // MyClass(7, None, List(103,104))

data.withSimple(2).withOpt(12).withList(103, 104) // MyClass(2, Some(12), List(103,104))
```
