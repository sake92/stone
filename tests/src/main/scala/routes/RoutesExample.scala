package routes

import ba.sake.stone.Route

@Route
class UsersRoute(p1: "users", val id: Long, val name: String)(
    val a: Int,
    val qs: Set[String]
)

object RoutesExample extends App {

  // construct
  val route = UsersRoute(1, "Sake")(123, Set("q1", "q2"))
  println(s"Constructed: ${route.urlData}")

  // deconstruct
  route.urlData.url match {
    case UsersRoute(id, name, a, qs) =>
      println(s"Deconstructed: $id, $name, $a, $qs")
  }
}
