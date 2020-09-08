package ba.sake.stone

import scala.language.experimental.macros

import scala.annotation.{compileTimeOnly, StaticAnnotation}
import scala.reflect.macros.whitebox.Context

// TODO
// - multiple params lists
// - hide private members ?

@compileTimeOnly("enable macro paradise to expand macro annotations")
class Wither extends StaticAnnotation {
  def macroTransform(annottees: Any*): Any = macro witherMacro.impl
}

private object witherMacro {

  def impl(c: Context)(annottees: c.Expr[Any]*): c.Expr[Any] = {
    import c.universe._

    def getModifiedClass(clsTree: Tree) =
      clsTree match {
        case q"""$mods  class $tpname[..$tparams] $ctorMods(...$paramss) 
                        extends { ..$earlydefns } with ..$parents { $self =>
                ..$stats
              }""" =>
          if (paramss.size > 1)
            c.abort(c.enclosingPosition, "Can't handle multiple param groups")

          val allParams = paramss.flatten
          val namedArgs = allParams.map { param =>
            q"${param.name}=${param.name}" // p will be taken from wither method param :)
          }

          val methodDefs = stats.flatMap {
            case q"$mods def $tname[..$tparams](...$paramss): $tpt = $expr" =>
              Some(tname)
            case _ => None
          }

          val newWitherMethods = allParams.flatMap { // param =>
            case param @ ValDef(mods, paramName, paramTpt, _) =>
              val withDefIdent = TermName("with" + paramName.decoded.capitalize)

              val isUserDeclared = methodDefs.contains(withDefIdent)
              if (isUserDeclared) None
              else {
                val maybeHKT = paramTpt match {
                  case AppliedTypeTree(hkt, args) => Some(hkt -> args)
                  case other                      => None
                }

                val maybeHktWith: Option[DefDef] = maybeHKT match {
                  case None => None
                  case Some((hkt, args)) =>
                    val paramNameIdent = q"$paramName"
                    val wrappedTpe     = args.head
                    if (hkt.toString == "Option") { // withX(x) instead of withX(Some(x))

                      val namedArg = q"$paramName = Some($paramNameIdent)"
                      val newArgs = namedArgs.filterNot(
                        _.asInstanceOf[Assign].lhs.asInstanceOf[Ident].name == paramName
                      ) :+ namedArg
                      Some(q"def $withDefIdent($paramName: $wrappedTpe) = new $tpname(..$newArgs)")
                    } else if (hkt.toString == "List") { // withX(x1,x2) instead of withX(List(x1,x2))
                      val namedArg = q"$paramName = $paramNameIdent.toList"
                      val newArgs = namedArgs.filterNot(
                        _.asInstanceOf[Assign].lhs.asInstanceOf[Ident].name == paramName
                      ) :+ namedArg
                      Some(q"def $withDefIdent($paramName: $wrappedTpe*) = new $tpname(..$newArgs)")
                    } else None
                }
                List(
                  q"def $withDefIdent($param) = new $tpname(..$namedArgs)"
                ) ++ maybeHktWith
              }
          }

          // just add newWitherMethods
          q"""$mods class $tpname[..$tparams] $ctorMods(...$paramss) 
                    extends { ..$earlydefns } with ..$parents { $self =>
              ..$stats
              ..$newWitherMethods
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
