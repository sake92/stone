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
    val qs: Set[String]
)

object RoutesExample extends App {

  val route = UsersRoute(1, "Sake")(123, Set("q1", "q2"))
  println(s"Constructed: ${route.urlData}")

  "users/1/Sake?a=123&qs=q1&qs=q2" match {
    case UsersRoute(id, name, a, qs) =>
      println(s"Deconstructed: $id, $name, $a, $qs")
  }
}
