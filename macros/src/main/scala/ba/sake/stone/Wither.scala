package ba.sake.stone

import scala.language.experimental.macros

import scala.annotation.{compileTimeOnly, StaticAnnotation}
import scala.reflect.macros.whitebox.Context

// TODO
// - multiple params lists
// - overrideable withX
// - hide private members ?

@compileTimeOnly("enable macro paradise to expand macro annotations")
class Wither extends StaticAnnotation {
  def macroTransform(annottees: Any*): Any = macro witherMacro.impl
}

// class MyClass(val abc: Int)
// gets a method withAbc(abc: Int): MyClass
private object witherMacro {

  def impl(c: Context)(annottees: c.Expr[Any]*): c.Expr[Any] = {
    import c.universe._

    def getModifiedClass(clsTree: Tree) = clsTree match {
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

        val newWitherMethods = allParams.flatMap { param =>
          val fn           = param.name.decoded
          val withDefIdent = TermName("with" + fn.capitalize)

          val isUserDeclared = methodDefs.contains(withDefIdent)
          if (isUserDeclared) None
          else {
            val paramTpe = c.typecheck(q"${param.tpt}", c.TYPEmode, silent = true)
            val maybeOptionWith =
              if (paramTpe.tpe <:< typeOf[Option[_]]) { // withX(x:T) instead of withX(Some(x))
                val paramName        = param.name
                val paramNameDecoded = q"$paramName"
                val paramOptTpe      = paramTpe.tpe.typeArgs.head
                val namedArg         = q"$paramName = Some($paramNameDecoded)"
                val newArgs = namedArgs.filterNot(
                  _.asInstanceOf[Assign].lhs.asInstanceOf[Ident].name == paramName
                ) :+ namedArg
                Some(q"def $withDefIdent($paramName: $paramOptTpe) = new $tpname(..$newArgs)")
              } else None
            val maybeListWith =
              if (paramTpe.tpe <:< typeOf[scala.collection.immutable.List[_]]) { // withXs(xs1,xs2) instead of withXs(Seq(xs1,xs2))
                val paramName        = param.name
                val paramNameDecoded = q"$paramName"
                val paramListTpe     = paramTpe.tpe.typeArgs.head
                val namedArg         = q"$paramName = $paramNameDecoded.toList"
                val newArgs = namedArgs.filterNot(
                  _.asInstanceOf[Assign].lhs.asInstanceOf[Ident].name == paramName
                ) :+ namedArg
                Some(q"def $withDefIdent($paramName: $paramListTpe*) = new $tpname(..$newArgs)")
              } else None
            List(
              q"def $withDefIdent($param) = new $tpname(..$namedArgs)"
            ) ++ maybeOptionWith ++ maybeListWith
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
      case List(classDef, objectDef) => // if class with companion
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
