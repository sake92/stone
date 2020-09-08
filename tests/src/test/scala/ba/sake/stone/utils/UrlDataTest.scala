package ba.sake.stone.utils

import org.scalatest.{FlatSpec, Matchers}

class UrlDataTest extends FlatSpec with Matchers {
  "UrlData" should "create new UrlData if URL is valid" in {
    val urlData = UrlData.fromString("/users/5?a=7&qs=aBc&qs=deF")
    urlData.path shouldBe "/users/5"
    urlData.pathParts shouldBe Seq("users", "5")
    urlData.query shouldBe "a=7&qs=aBc&qs=deF"
    urlData.queryParams shouldBe Map("a" -> Set("7"), "qs" -> Set("aBc", "deF"))
  }
  it should "throw if URL is invalid" in {
    assertThrows[IllegalArgumentException] {
      UrlData.fromString("::/users/5?a=7&qs=aBc&qs=deF")
    }
  }
}
