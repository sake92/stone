package ba.sake.stone

import scala.language.experimental.macros

import scala.annotation.{compileTimeOnly, StaticAnnotation}
import scala.reflect.macros.whitebox.Context

/**
  * Only handles these types in path:
  * - String, Int, Long, Double
  *
  * Only handles these types in query:
  * - String, Int, Long, Double
  * - Option, Seq, List, Vector, Array, Buffer of aboves
  */
@compileTimeOnly("Please enable macro paradise to expand macro annotations")
class Route extends StaticAnnotation {
  def macroTransform(annottees: Any*): Any = macro RouteMacro.impl
}

private object RouteMacro {

  def impl(c: Context)(annottees: c.Expr[Any]*): c.Expr[Any] = {
    import c.universe._

    val result = annottees.map(_.tree) match {
      case classTree :: others =>
        val helper = new Helper[c.type](c)(classTree, others.headOption)
        q"""
          ${helper.modifiedClass}
          ${helper.modifiedObject}
        """
      case notClass =>
        c.abort(c.enclosingPosition, "@Route must annotate a class")
    }

    c.Expr[Any](result)
  }

  private class Helper[C <: Context](val c: C)(clsTree: C#Tree, maybeObjTree: Option[C#Tree]) {
    import c.universe._

    val (modifiedClass, pps, qps) = clsTree match {
      case q"""$mods class $tpname[..$tparams] $ctorMods(...$paramss)
                       extends { ..$earlydefns } with ..$parents { $self =>
                ..$stats
              }""" =>
        if (paramss.size != 2)
          c.abort(c.enclosingPosition, "Route must have exactly 2 parameter groups!")

        // path params
        val pathParams = if (paramss.size >= 1) paramss(0) else List.empty
        val pathPartsField =
          q"private val pathParts: Seq[String] = ${pathParams.map(_.name)}.map(_.toString)"
        val pathField = q"""private val path: String = "/" + pathParts.mkString("/")"""

        val pps: List[(ValDef, Tree, Option[Constant])] = pathParams.map {
          case param @ ValDef(mods, paramName, paramTpt, _) =>
            //println( showRaw(paramTpt) ) // ooooooopaaaaaaaa
            paramTpt match {
              case SingletonTypeTree(Literal(lit @ Constant(litValue))) =>
                (param, paramTpt, Some(lit))
              case _ =>
                (param, paramTpt, None)
            }
        }

        // query params
        val queryParams = if (paramss.size == 2) paramss(1) else List.empty
        val queryParamTuples = queryParams.map {
          case param @ ValDef(mods, paramName, paramTpt, _) =>
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

        val urlDataField = q"""val urlData: ba.sake.stone.utils.UrlData = 
            ba.sake.stone.utils.UrlData(
              path, pathParts, query, queryParams
            )
          """

        (q"""$mods class $tpname[..$tparams] $ctorMods(...$paramss)
                    extends { ..$earlydefns } with ..$parents { $self =>
              ..$stats

              $pathPartsField
              $pathField

              $queryParamsField
              $queryField

              $urlDataField
        }""", pps, queryParams)
    }

    /* apply && unapply */
    val modifiedObject = {

      /* path */
      val pathFields = pps.filter(!_._3.isDefined)
      val pathTpes   = pathFields.map(_._2)
      val pathExtractors = pps.zipWithIndex.filter(!_._1._3.isDefined).map {
        case ((_, tpt, _), idx) =>
          val tptString = tpt.toString
          if (tptString == "String") q"urlData.pathParts($idx)"
          else if (tptString == "Int") q"urlData.pathParts($idx).toInt"
          else if (tptString == "Long") q"urlData.pathParts($idx).toLong"
          else if (tptString == "Double") q"urlData.pathParts($idx).toDouble"
          else c.abort(tpt.pos, s"Can't handle type '$tptString' in path")
      }
      val literalValidators = pps.zipWithIndex.filter(_._1._3.isDefined).map {
        case ((_, tpt, Some(lit)), idx) =>
          val expected = q"urlData.pathParts($idx)"
          q"""if ($lit != $expected)
                throw new IllegalArgumentException("Path literal mismatch")"""
      }

      val pathParamPairs = pathFields.map(_._1).map {
        case ValDef(mods, paramName, paramTpt, _) =>
          q"$paramName: $paramTpt"
      }
      val pathParams = pps.map {
        case (valDef, _, maybeLiteral) =>
          maybeLiteral match {
            case Some(lit) => q"$lit"
            case None      => q"${valDef.name}"
          }
      }

      /* query */
      val queryFields = qps
      val queryTpes   = queryFields.map(_.tpt)
      val queryExtractors = qps.map {
        case ValDef(mods, paramName, paramTpt, _) =>
          val qpName    = paramName.toString
          val tptString = paramTpt.toString
          val SeqLikeRegex  = "(Seq|List|Vector|Array|Buffer)\\[(String|Int|Long|Double)\\]".r
          if (tptString == "String") q"urlData.getFirstQP($qpName)"
          else if (tptString == "Int") q"urlData.getFirstQP($qpName).toInt"
          else if (tptString == "Long") q"urlData.getFirstQP($qpName).toLong"
          else if (tptString == "Double") q"urlData.getFirstQP($qpName).toDouble"
          else if (tptString == "Option[String]") q"urlData.firstQP($qpName)"
          else if (tptString == "Option[Int]") q"urlData.firstQP($qpName).map(_.toInt)"
          else if (tptString == "Option[Long]") q"urlData.firstQP($qpName).map(_.toLong)"
          else if (tptString == "Option[Double]") q"urlData.firstQP($qpName).map(_.toDouble)"
          else if (tptString == "Set[String]") q"urlData.getQP($qpName)"
          else if (tptString == "Set[Int]") q"urlData.getQP($qpName).map(_.toInt)"
          else if (tptString == "Set[Long]") q"urlData.getQP($qpName).map(_.toLong)"
          else if (tptString == "Set[Double]") q"urlData.getQP($qpName).map(_.toDouble)"
          else if (SeqLikeRegex.matches(tptString)) {
            c.warning(
              paramTpt.pos,
              "Please use `Set` collection! Using an ordered collection can give you unexpected results!"
            )
            val SeqLikeRegex(seqTpe, innerTpe) = tptString
            val toSeqLike = TermName(s"to$seqTpe")
            if (innerTpe == "String") q"urlData.getQP($qpName).$toSeqLike"
            else if (innerTpe == "Int") q"urlData.getQP($qpName).$toSeqLike.map(_.toInt)"
            else if (innerTpe == "Long") q"urlData.getQP($qpName).$toSeqLike.map(_.toLong)"
            else if (innerTpe == "Double") q"urlData.getQP($qpName).$toSeqLike.map(_.toDouble)"
            else c.abort(paramTpt.pos, s"Can't handle type '$tptString' in query")
          } else c.abort(paramTpt.pos, s"Can't handle type '$tptString' in query")
      }

      val queryParamPairs = queryFields.map {
        case ValDef(mods, paramName, paramTpt, _) =>
          q"$paramName: $paramTpt"
      }
      val queryParams = queryFields.map(_.name)

      /* the main stuff */
      val applyDef = q"""
        def apply(..$pathParamPairs)(..$queryParamPairs): ${modifiedClass.name} = { 
          new ${modifiedClass.name}(..$pathParams)(..$queryParams)
        }
      """

      val unapplyDef = q"""
        def unapply(str: String): Option[(..$pathTpes, ..$queryTpes)] = {
          val urlData = ba.sake.stone.utils.UrlData.fromString(str)
          if (urlData.pathParts.size != ${pps.size}) None
          else {
            scala.util.Try{
              $literalValidators
              ( ..$pathExtractors, ..$queryExtractors )
            }.toOption
          }
        }
      """

      val objTree = maybeObjTree.getOrElse { // create companion if doesnt exist
        q"""object ${modifiedClass.name.toTermName}"""
      }
      objTree match {
        case q"""$mods object $tpname extends { ..$earlydefns } with ..$parents { $self =>
                ..$stats
              }""" =>
          q"""$mods object $tpname extends { ..$earlydefns } with ..$parents { $self =>
                ..$stats
                $applyDef
                $unapplyDef
              }"""
      }
    }

  }

}
