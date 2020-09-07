package ba.sake.stone.route

import org.scalatest.{FlatSpec, Matchers}
import ba.sake.stone.Route

@Route
class MyRoute(
    path1: "users",
    val id: Int
)(val a: Int, val qs: Set[String])

class UriDataTest extends FlatSpec with Matchers {
  "Route" should "extract UriData properly" in {
    val r       = new MyRoute("users", 5)(7, Set("aBc", "deF"))
    val uriData = r.uriData
    uriData.path shouldBe "/users/5"
    uriData.pathParts shouldBe Seq("users", "5")
    uriData.query shouldBe "a=7&qs=aBc&qs=deF"
    uriData.queryParams shouldBe Map("a" -> Set("7"), "qs" -> Set("aBc", "deF"))
  }
}
