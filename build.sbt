inThisBuild(
  List(
    organization := "ba.sake",
    scalaVersion := "2.13.1",
    resolvers += Resolver.sonatypeRepo("releases")
  )
)

lazy val stoneMacros = (project in file("macros"))
  .settings(
    name := "stone-macros",
    crossScalaVersions := Seq("2.12.8", "2.13.1"),
    libraryDependencies ++= Seq(
      "org.scala-lang" % "scala-reflect" % scalaVersion.value
    ),
    libraryDependencies ++= {
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, n)) if n >= 13 => Nil
        case _ =>
          List("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full)
      }
    },
    Compile / scalacOptions ++= {
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, n)) if n >= 13 => "-Ymacro-annotations" :: Nil
        case _                       => Nil
      }
    }
  )

lazy val stoneMacrosExample = (project in file("example"))
  .settings(
    name := "stone-example",
    scalacOptions += "-Ymacro-annotations"
  )
  .dependsOn(stoneMacros)
