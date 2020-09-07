package ba.sake.stone

import scala.language.experimental.macros

import scala.annotation.{compileTimeOnly, StaticAnnotation}
import scala.reflect.macros.whitebox.Context

@compileTimeOnly("Please enable macro paradise to expand macro annotations")
class Route extends StaticAnnotation {
  def macroTransform(annottees: Any*): Any = macro RouteMacro.impl
}

private object RouteMacro {

  def impl(c: Context)(annottees: c.Expr[Any]*): c.Expr[Any] = {
    import c.universe._

    def getModifiedClass(clsTree: Tree) = clsTree match {
      case q"""$mods class $tpname[..$tparams] $ctorMods(...$paramss)
                       extends { ..$earlydefns } with ..$parents { $self =>
                ..$stats
              }""" =>
        if (paramss.size > 2)
          c.abort(c.enclosingPosition, "Can't handle more than 2 parameter groups")

        // path params
        val pathParams = if (paramss.size >= 1) paramss(0) else List.empty
        val pathPartsField = // TODO validirat samo Int, String i Seq
          q"private val pathParts: Seq[String] = ${pathParams.map(_.name)}.map(_.toString)"
        val pathField = q"""private val path: String = "/" + pathParts.mkString("/")"""

        // query params
        val queryParams = if (paramss.size == 2) paramss(1) else List.empty
        val queryParamTuples = queryParams.map {
          case param @ ValDef(mods, paramName, paramTpt, _) =>
            // TODO validirat samo Int, String, Set i Option
            val paramSetTuple = paramTpt match {
              case AppliedTypeTree(hkt, args) =>
                q"(${paramName.decodedName.toString()}, $paramName.map(_.toString).toSet)"
              case other => q"(${paramName.decodedName.toString}, Set($paramName.toString))"
            }
            paramSetTuple
        }
        val queryParamsField =
          q"private val queryParams: Map[String, Set[String]] = $queryParamTuples.toMap"
        val queryField =
          q"""private val query: String = 
                queryParams.flatMap { case (qName, qValues) =>
                  qValues.map(qValue => s"$$qName=$$qValue")
                }
                .mkString("&")
          """

        val uriDataField = q"""val uriData: ba.sake.stone.utils.UriData = 
            ba.sake.stone.utils.UriData(
              path, pathParts, query, queryParams
            )
          """

        q"""$mods class $tpname[..$tparams] $ctorMods(...$paramss)
                    extends { ..$earlydefns } with ..$parents { $self =>
              ..$stats

              $pathPartsField
              $pathField

              $queryParamsField
              $queryField

              $uriDataField
        }"""
    }

    val result = annottees.map(_.tree) match {
      case List(classDef, objectDef) =>
        q"""
          ${getModifiedClass(classDef)}
          $objectDef
        """
      case List(classDef) =>
        getModifiedClass(classDef)
      case notClass =>
        c.abort(c.enclosingPosition, "@Wither must annotate a class")
    }

    c.Expr[Any](result)
  }
  
}
