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

// TODO handle default values of params
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

        val pps: List[(ValDef, Tree, Option[Constant], Boolean)] = pathParams.map {
          case param @ q"$mods val $paramName: $paramTpt = $expr" =>
            //println( showRaw(paramTpt) ) // ooooooopaaaaaaaa
            val isField = !param.mods.hasFlag(Flag.LOCAL)
            paramTpt match { // TODO limit to String literals only !!!
              case SingletonTypeTree(Literal(lit @ Constant(litValue))) =>
                (param, paramTpt, Some(lit), isField)
              case _ =>
                (param, paramTpt, None, isField)
            }
        }

        // query params
        val queryParams = if (paramss.size == 2) paramss(1) else List.empty
        val queryParamTuples = queryParams.map {
          case param @ q"$mods val $paramName: $paramTpt = $expr" =>
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
      val (pathFields, pathParams) = pps.partition(_._4)
      val pathTpes = pathFields.map {
        case (_, paramTpt, maybeLiteral, _) =>
          maybeLiteral match {
            case Some(value) => tq"String"
            case None        => paramTpt
          }
      }
      // TODO 
      // regex <[a-z]> specijalno izvadit String koji matcha 1 path tim regexom

      val pathExtractorsAndValidators = pps.zipWithIndex.map {
        case ((_, tpt, None, isField), idx) =>
          val tptString = tpt.toString
          val res = tptString match {
            case "String" => q"urlData.pathParts($idx)"
            case "Int"    => q"urlData.pathParts($idx).toInt"
            case "Long"   => q"urlData.pathParts($idx).toLong"
            case "Double" => q"urlData.pathParts($idx).toDouble"
            case _        => c.abort(tpt.pos, s"Can't handle type '$tptString' in path")
          }
          res -> isField
        case ((_, tpt, Some(lit), isField), idx) =>
          val tptString = tpt.toString

          val res =
            if (lit.value == "*") {
              q"""urlData.pathParts.drop($idx).mkString("/")"""
            } else {
              val expected = q"urlData.pathParts($idx)"
              q"""if ($lit != $expected)
                throw new IllegalArgumentException("Path literal mismatch")"""
            }
          // else c.abort(tpt.pos, s"Can't handle type '$tptString' in path")
          res -> isField
      }
      val pathExtractors = pathExtractorsAndValidators.filter(_._2).map(_._1)
      val pathValidators = pathExtractorsAndValidators.filter(!_._2).map(_._1)

      val pathParamPairs = pathFields.map(_._1).map {
        case param @ q"$mods val $paramName: $paramTpt = $expr" =>
          q"$paramName: $paramTpt"
      }
      val pathParamValues = pps.map {
        case (valDef, _, maybeLiteral, _) =>
          maybeLiteral match {
            case Some(lit) => q"$lit"
            case None      => q"${valDef.name}"
          }
      }

      /* query */
      val queryFields = qps
      val queryTpes   = queryFields.map(_.tpt)
      val queryExtractors = qps.map {
        case param @ q"$mods val $paramName: $paramTpt = $expr" =>
          val qpName       = paramName.toString
          val tptString    = paramTpt.toString
          val SeqLikeRegex = "(Seq|List|Vector|Array|Buffer)\\[(String|Int|Long|Double)\\]".r
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
            val toSeqLike                      = TermName(s"to$seqTpe")
            if (innerTpe == "String") q"urlData.getQP($qpName).$toSeqLike"
            else if (innerTpe == "Int") q"urlData.getQP($qpName).$toSeqLike.map(_.toInt)"
            else if (innerTpe == "Long") q"urlData.getQP($qpName).$toSeqLike.map(_.toLong)"
            else if (innerTpe == "Double") q"urlData.getQP($qpName).$toSeqLike.map(_.toDouble)"
            else c.abort(paramTpt.pos, s"Can't handle type '$tptString' in query")
          } else c.abort(paramTpt.pos, s"Can't handle type '$tptString' in query")
      }

      val queryParamPairs = queryFields.map {
        case param @ q"$mods val $paramName: $paramTpt = $expr" =>
          q"$paramName: $paramTpt"
      }
      val queryParamValues = queryFields.map(_.name)

      /* the main stuff */
      val applyDef = q"""
        def apply(..$pathParamPairs)(..$queryParamPairs): ${modifiedClass.name} = { 
          new ${modifiedClass.name}(..$pathParamValues)(..$queryParamValues)
        }
      """

      val unapplyDef = if (pathTpes.isEmpty && queryTpes.isEmpty) {
        q"""
          def unapply(str: String): Boolean = {
            val urlData = ba.sake.stone.utils.UrlData.fromString(str)
            scala.util.Try { $pathValidators }.isSuccess
          }
        """
      } else {
        q"""
          def unapply(str: String): Option[(..$pathTpes, ..$queryTpes)] = {
            val urlData = ba.sake.stone.utils.UrlData.fromString(str)
            scala.util.Try {
              $pathValidators
              ( ..$pathExtractors, ..$queryExtractors )
            }.toOption
          }
        """
      }

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
