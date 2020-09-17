package routes

import ba.sake.stone.Route

object RoutesExample extends App {

  @Route
  class UsersRoute(p1: "users", val id: Long, val name: String)(
      val minAge: Int,
      val opt: Option[Int],
      val qs: Set[String]
  )

  val route = UsersRoute(1, "Sake")(123, None, Set("q1"))
  println(s"Constructed: ${route.urlData}")
  println(s"Constructed: ${route.urlData.url}")

  "users/1/Sake?minAge=123&qs=q1&qs=q2&opt=456" match {
    case UsersRoute(id, name, minAge, qs, opt) =>
      println(s"Deconstructed: $id, $name, $minAge, $qs, $opt")
    case _ => println("404 Not Found")
  }
}

object RegexExample extends App {

  @Route class RegexRoute(p1: "users", val name: "<[a-z]+>", val id: "<\\d+>")()

  "users/sake/123" match {
    case RegexRoute(name, id) =>
      println(s"name=$name, id=$id")
    case _ => println("404 Not Found")
  }
}

object StarExample extends App {

  @Route class StarRoute(p1: "users", val path: "*")()

  "users/abc/def?aaaaa=b" match {
    case StarRoute(path) =>
      println(path)
    case _ => println("404 Not Found")
  }
}
