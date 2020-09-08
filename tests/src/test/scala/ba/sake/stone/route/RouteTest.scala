package ba.sake.stone.route

import org.scalatest.{FlatSpec, Matchers}
import ba.sake.stone.Route

@Route
class MyRoute(
    path1: "users",
    val id: Int,
    val name: String,
    val rate: Double
)(
    val a: Int,
    val qs: Set[String]
)

class UriDataTest extends FlatSpec with Matchers {

  "Route" should "generate `uriData` property" in {
    val r       = new MyRoute("users", 5, "Sake", 7.123)(7, Set("aBc", "deF"))
    val uriData = r.uriData
    uriData.path shouldBe "/users/5/Sake/7.123"
    uriData.pathParts shouldBe Seq("users", "5", "Sake", "7.123")
    uriData.query shouldBe "a=7&qs=aBc&qs=deF"
    uriData.queryParams shouldBe Map("a" -> Set("7"), "qs" -> Set("aBc", "deF"))
  }

  it should "generate apply method" in {
    val r = MyRoute(5, "Sake", 7.123)(1, Set("q1", "q2"))
    r.uriData.path shouldBe "/users/5/Sake/7.123"
    r.uriData.path shouldBe "/users/5/Sake/7.123"
    r.uriData.path shouldBe "/users/5/Sake/7.123"
    r.uriData.path shouldBe "/users/5/Sake/7.123"
  }

  it should "generate unapply method (extractor)" in {
    val MyRoute(id, name, rate, a, qs) = "/users/5/Sake/7.123?a=444&qs=q1&qs=q2"
    id shouldBe 5
    name shouldBe "Sake"
    rate shouldBe 7.123
    a shouldBe 444
    qs shouldBe Set("q1", "q2")
  }

}
