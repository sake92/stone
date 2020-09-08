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

class urlDataTest extends FlatSpec with Matchers {

  "Route" should "generate `urlData` property" in {
    val r       = new MyRoute("users", 5, "Sake", 7.123)(7, Set("aBc", "deF"))
    val urlData = r.urlData
    urlData.path shouldBe "/users/5/Sake/7.123"
    urlData.pathParts shouldBe Seq("users", "5", "Sake", "7.123")
    urlData.query shouldBe "a=7&qs=aBc&qs=deF"
    urlData.queryParams shouldBe Map("a" -> Set("7"), "qs" -> Set("aBc", "deF"))
  }

  it should "generate apply method" in {
    val r = MyRoute(5, "Sake", 7.123)(1, Set("q1", "q2"))
    r.urlData.path shouldBe "/users/5/Sake/7.123"
    r.urlData.path shouldBe "/users/5/Sake/7.123"
    r.urlData.path shouldBe "/users/5/Sake/7.123"
    r.urlData.path shouldBe "/users/5/Sake/7.123"
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
