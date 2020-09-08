package ba.sake.stone.utils

import java.net.URI
import scala.collection.mutable

case class UrlData(
    path: String,
    pathParts: Seq[String],
    query: String,
    queryParams: Map[String, Set[String]]
) {
  private val qps = queryParams.withDefaultValue(Set.empty)

  val url: String = path + Option.when(query.nonEmpty)(s"?$query").getOrElse("")

  val asURI: URI = URI.create(url)

  def getQP(name: String): Set[String]      = qps(name)
  def firstQP(name: String): Option[String] = queryParams.get(name).flatMap(_.headOption)
  def getFirstQP(name: String): String      = firstQP(name).get
}

object UrlData {

  def fromString(str: String): UrlData = {
    val uri = URI.create(str)

    val path      = uri.getPath
    val pathParts = path.dropWhile(_ == '/').split("/")

    val query = Option(uri.getQuery).getOrElse("")
    val queryParams = query.split("&").toSeq.filter(_.trim.nonEmpty)
      .flatMap { param =>
        val nameValue = param.split("=")
        if (nameValue.size >= 2) {
          val Array(name, value, _*) = nameValue
          Option.when(name.trim.nonEmpty)((name, value))
        } else None
      }
    val queryParamsMap = new mutable.HashMap[String, mutable.Set[String]]
      with mutable.MultiMap[String, String]
    queryParams.foreach {
      case (name, value) => queryParamsMap.addBinding(name, value)
    }
    val queryParamsMapImmutable = queryParamsMap.mapValues(_.toSet).toMap
    UrlData(path, pathParts.toSeq, query, queryParamsMapImmutable)
  }

}
