package fixtures

import ba.sake.stone.Wither

object Stuff {
  trait Inner
}

@Wither
class Stuff(
    val inner: Stuff.Inner
)
