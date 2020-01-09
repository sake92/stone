package fixtures

import ba.sake.stone.Wither


// @Wither kinda ok compile warning..
abstract class Test3(
    val simple: Data,
    var variable: Data,
    val opt: Option[Data],
    val list: List[Data]
)
