package fixtures

// simple data has implicits conversions lurking
// if we used String, there is conversion to Seq etc..
case class Data(value: Int)
