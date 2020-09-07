package ba.sake.stone.utils

import java.net.URI
import scala.collection.mutable

case class UriData(
    path: String,
    pathParts: Seq[String],
    query: String,
    queryParams: Map[String, Set[String]]
) {
  def firstQP(name: String): Option[String] = None // TODO
  def getFirstQP(name: String): String      = firstQP(name).get
}

object UriData {

  def fromString(str: String): UriData = {
    val uri = URI.create(str)

    val path      = uri.getPath
    val pathParts = path.dropWhile(_ == '/').split("/")

    val query = uri.getQuery
    val queryParams = query.split("&").toSeq.map { param =>
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
