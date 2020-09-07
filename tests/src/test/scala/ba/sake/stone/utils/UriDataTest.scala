package ba.sake.stone.utils

import org.scalatest.{FlatSpec, Matchers}

class UriDataTest extends FlatSpec with Matchers {
  "UriData" should "create new UriData if URL is valid" in {
    val uriData = UriData.fromString("/users/5?a=7&qs=aBc&qs=deF")
    uriData.path shouldBe "/users/5"
    uriData.pathParts shouldBe Seq("users", "5")
    uriData.query shouldBe "a=7&qs=aBc&qs=deF"
    uriData.queryParams shouldBe Map("a" -> Set("7"), "qs" -> Set("aBc", "deF"))
  }
  it should "throw if URL is invalid" in {
    assertThrows[IllegalArgumentException] {
      UriData.fromString("::/users/5?a=7&qs=aBc&qs=deF")
    }
  }
}
