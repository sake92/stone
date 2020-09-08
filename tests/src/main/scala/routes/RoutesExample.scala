package routes

import ba.sake.stone.Route

// /users/:id/:name ? a=:a & qs=:qs...
@Route
class UsersRoute(
    p1: "users",
    val id: Long,
    val name: String
)(
    val a: Int,
    val qs: Set[String],
    val opt: Option[Int]
)

object RoutesExample extends App {

  val route = UsersRoute(1, "Sake")(123, Set("q1"), None)
  println(s"Constructed: ${route.urlData}")

  "users/1/Sake?a=123&qs=q1&qs=q2&opt=456" match {
    case UsersRoute(id, name, a, qs, opt) =>
      println(s"Deconstructed: $id, $name, $a, $qs, $opt")
    case _ => println("404 Not Found")
  }
}
