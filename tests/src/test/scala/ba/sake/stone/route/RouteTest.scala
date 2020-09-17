package ba.sake.stone.route

import ba.sake.stone.Route
import ba.sake.stone.StoneTest

@Route
class MyRoute(
    path1: "users",
    val id: Int,
    val name: String,
    val rate: Double
)(
    val a: Int,
    val qOpt: Option[String],
    val qSet: Set[String],
    val qSeq: Seq[String],
    val qList: List[String]
)

class RouteTest extends StoneTest {

  "Route" should "generate `urlData` property" in {
    val r =
      new MyRoute("users", 5, "Sake", 7.123)(7, None, Set("aBc", "deF"), Seq.empty, List.empty)
    val urlData = r.urlData
    urlData.path shouldBe "/users/5/Sake/7.123"
    urlData.pathParts shouldBe Seq("users", "5", "Sake", "7.123")
    urlData.query shouldBe "a=7&qSet=aBc&qSet=deF"
    urlData.queryParams should contain allElementsOf Set(
      "a"    -> Set("7"),
      "qSet" -> Set("aBc", "deF")
    )
  }

  it should "generate apply method" in {
    val r = MyRoute(5, "Sake", 7.123)(7, None, Set("aBc", "deF"), Seq.empty, List.empty)
    r.urlData.path shouldBe "/users/5/Sake/7.123"
    r.urlData.pathParts shouldBe Seq("users", "5", "Sake", "7.123")
    r.urlData.query shouldBe "a=7&qSet=aBc&qSet=deF"
    r.urlData.queryParams should contain allElementsOf Set(
      "a"    -> Set("7"),
      "qSet" -> Set("aBc", "deF")
    )
  }

  it should "generate unapply method (extractor)" in {

    locally {
      val MyRoute(id, name, rate, a, qOpt, qSet, qSeq, qList) =
        "/users/5/Sake/7.123?a=444"
      id shouldBe 5
      name shouldBe "Sake"
      rate shouldBe 7.123
      a shouldBe 444
      qOpt shouldBe None
      qSet shouldBe Set.empty
      qSeq shouldBe Seq.empty
      qList shouldBe List.empty
      info("containers can be empty")
    }
    locally {
      val MyRoute(id, name, rate, a, qOpt, qSet, qSeq, qList) =
        "/users/5/Sake/7.123?a=444&opt=op1&qOpt=123&qSet=q1&qSet=q2&qSeq=seq1&qList=list1"
      id shouldBe 5
      name shouldBe "Sake"
      rate shouldBe 7.123
      a shouldBe 444
      qOpt shouldBe Some("123")
      qSet shouldBe Set("q1", "q2")
      qSeq shouldBe Seq("seq1")
      qList shouldBe List("list1")
      info("containers can be non empty")
    }
    locally {
      @Route class MyRoute2(path1: "users", path2: "bla")()

      "/users/bla" match {
        case MyRoute2() => succeed
        case _          => fail("no match..")
      }
      info("generate unapply: Boolean when no variables (only literals) ")
    }
  }

  it should "handle regex pattern" in {
    @Route class RegexRoute(p1: "users", val name: "<[a-z]+>", val id: "<\\d+>")()

    "users/sake/123" match {
      case RegexRoute(name, id) => succeed
      case _                    => fail
    }
    "users/sAke/123" match {
      case RegexRoute(name, id) => fail
      case _                    => succeed
    }
    "users/sake/123a" match {
      case RegexRoute(name, id) => fail
      case _                    => succeed
    }
  }

  it should "handle star multisegment pattern" in {
    @Route class StarRoute(p1: "users", val path: "*")()

    "users/abc/def?aaaaa=b" match {
      case StarRoute(path) => path shouldBe "abc/def"
      case _              => fail
    }
  }
}
