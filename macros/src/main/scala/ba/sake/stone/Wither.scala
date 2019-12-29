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

    val result = {
      annottees.map(_.tree).head match {
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

          val newWitherMethods = allParams.flatMap { param =>
            val fn           = param.name.decoded
            val withDefIdent = TermName("with" + fn.capitalize)

            val paramTpe =
              c.typecheck(q"${param.tpt}", c.TYPEmode, silent = true)
            List(
              q"def $withDefIdent($param) = new $tpname(..$namedArgs)"
            ) ++ Option.when(paramTpe.tpe <:< typeOf[Option[_]]) { // with(x:T) instead of with(Some(x))

              val paramName   = param.name.decoded
              val paramOptTpe = paramTpe.tpe.typeArgs.head

              val namedArg = q"${param.name} = Some($paramName)"
              val newArgs = namedArgs.filterNot(
                _.asInstanceOf[Assign].lhs.asInstanceOf[Ident].name == param.name
              ) :+ namedArg
              q"def $withDefIdent(${param.name}: $paramOptTpe) = new $tpname(..$newArgs)"
            }
          }

          // just add newWitherMethods
          q"""$mods class $tpname[..$tparams] $ctorMods(...$paramss) 
                    extends { ..$earlydefns } with ..$parents { $self =>
              ..$stats
              ..$newWitherMethods
          }"""
        case _ =>
          c.abort(c.enclosingPosition, "@Wither must annotate a class")
      }
    }
    c.Expr[Any](result)
  }
}
