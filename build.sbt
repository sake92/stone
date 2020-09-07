
lazy val supportedScalaVersions = List("2.12.12", "2.13.3")

// TODO drop 2.12 jer ne podržava LITERAL TYPES
inThisBuild(
  List(
    organization := "ba.sake",
    scalaVersion := "2.13.3",
    resolvers += Resolver.sonatypeRepo("releases"),
    licenses += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0.html")),
    developers += Developer("sake92", "Sakib Hadžiavdić", "sakib@sake.ba", url("http://sake.ba")),
    scmInfo := Some(
      ScmInfo(url("https://github.com/sake92/stone"), "scm:git:git@github.com:sake92/stone.git")
    ),
    homepage := Some(url("https://github.com/sake92/stone"))
  )
)

lazy val root = (project in file("."))
  .aggregate(stoneMacros.jvm, stoneMacros.js, stoneMacrosTests)
  .settings(
    crossScalaVersions := Nil,
    publish / skip := true
  )

lazy val stoneMacros = crossProject(JVMPlatform, JSPlatform)
  .crossType(CrossType.Pure)
  .in(file("macros"))
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
    publish / skip := true,
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
  .dependsOn(stoneMacros.jvm)
