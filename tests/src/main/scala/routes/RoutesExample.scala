package routes

import ba.sake.stone.Route

// /users/:id/:name ? minAge=:minAge & qs=:qs...
@Route
class UsersRoute(
    p1: "users",
    val id: Long,
    val name: String
)(
    val minAge: Int,
    val opt: Option[Int],
    val qs: Set[String]
)

object RoutesExample extends App {

  val route = UsersRoute(1, "Sake")(123, None, Set("q1"))
  println(s"Constructed: ${route.urlData}")
  println(s"Constructed: ${route.urlData.url}")

  "users/1/Sake?minAge=123&qs=q1&qs=q2&opt=456" match {
    case UsersRoute(id, name, minAge, qs, opt) =>
      println(s"Deconstructed: $id, $name, $minAge, $qs, $opt")
    case _ => println("404 Not Found")
  }
}

object Bla extends App {

  @Route class BlaRoute(p1: "users", val bla: "*")()

  "users/abc/def?aaaaa=b" match {
    case BlaRoute(bla) =>
      println(bla)
    case _ => println("404 Not Found")
  }

}
