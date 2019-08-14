package ba.sake.stone

import scala.language.experimental.macros

import scala.annotation.{StaticAnnotation, compileTimeOnly}
import scala.reflect.macros.whitebox.Context

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

          val newWitherMethods = allParams.map { p =>
            val fn = p.name.decoded
            val withDefIdent = TermName("with" + fn(0).toUpper + fn.tail)
            q"def $withDefIdent($p) = new $tpname(..$namedArgs)"
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
