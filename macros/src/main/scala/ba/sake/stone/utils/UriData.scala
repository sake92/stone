package ba.sake.stone.utils

import java.net.URI
import scala.collection.mutable

case class UriData(
    path: String,
    pathParts: Seq[String],
    query: String,
    queryParams: Map[String, Set[String]]
) {
  private val qps = queryParams.withDefaultValue(Set.empty)

  val url: String = path + Option.when(query.nonEmpty)(s"?$query").getOrElse("")

  def getQP(name: String): Set[String]      = qps(name)
  def firstQP(name: String): Option[String] = queryParams.get(name).flatMap(_.headOption)
  def getFirstQP(name: String): String      = firstQP(name).get
}

object UriData {

  def fromString(str: String): UriData = {
    val uri = URI.create(str)

    val path      = uri.getPath
    val pathParts = path.dropWhile(_ == '/').split("/")

    val query = Option(uri.getQuery).getOrElse("")
    val queryParams = query.split("&").toSeq.filter(_.trim.nonEmpty).map { param =>
      val Array(name, value) = param.split("=")
      (name, value)
    }
    val queryParamsMap = new mutable.HashMap[String, mutable.Set[String]]
      with mutable.MultiMap[String, String]
    queryParams.foreach {
      case (name, value) => queryParamsMap.addBinding(name, value)
    }
    val queryParamsMapImmutable = queryParamsMap.mapValues(_.toSet).toMap
    UriData(path, pathParts.toSeq, query, queryParamsMapImmutable)
  }

}
