package fixtures

import ba.sake.stone.Wither

@Wither
case class Test1(
    simple: Data,
    var variable: Data,
    opt: Option[Data],
    list: List[Data]
)

@Wither
class Test2(
    val simple: Data,
    var variable: Data,
    val opt: Option[Data],
    val list: List[Data]
)

// @Wither kinda ok compile warning..
abstract class Test3(
    val simple: Data,
    var variable: Data,
    val opt: Option[Data],
    val list: List[Data]
)
