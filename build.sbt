lazy val supportedScalaVersions = List("2.12.8", "2.13.1")

inThisBuild(
  List(
    organization := "ba.sake",
    scalaVersion := "2.13.1",
    resolvers += Resolver.sonatypeRepo("releases")
  )
)

lazy val root = (project in file("."))
  .aggregate(stoneMacros, stoneMacrosTests)
  .settings(
    crossScalaVersions := Nil,
    publish / skip := true
  )

lazy val stoneMacros = (project in file("macros"))
  .settings(
    name := "stone-macros",
    crossScalaVersions := supportedScalaVersions,
    libraryDependencies ++= Seq(
      "org.scala-lang" % "scala-reflect" % scalaVersion.value
    ),
    libraryDependencies ++= {
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, n)) if n >= 13 => Nil
        case _ =>
          List(compilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full))
      }
    },
    Compile / scalacOptions ++= {
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, n)) if n >= 13 => List("-Ymacro-annotations")
        case _                       => Nil
      }
    }
  )

lazy val stoneMacrosTests = (project in file("tests"))
  .settings(
    name := "stone-tests",
    crossScalaVersions := supportedScalaVersions,
    libraryDependencies ++= Seq(
      "org.scalatest" %% "scalatest" % "3.0.8" % "test"
    ),
    libraryDependencies ++= {
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, n)) if n >= 13 => Nil
        case _ =>
          List(compilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full))
      }
    },
    Compile / scalacOptions ++= {
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, n)) if n >= 13 => List("-Ymacro-annotations")
        case _                       => Nil
      }
    }
  )
  .dependsOn(stoneMacros)
