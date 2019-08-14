
inThisBuild(
  List(
    organization := "ba.sake",
    scalaVersion := "2.12.8",
    resolvers += Resolver.sonatypeRepo("releases"),
    addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full) // needed in all projects
  )
)

lazy val stoneMacros = (project in file("macros"))
  .settings(
    name := "stone-macros",
    libraryDependencies ++= Seq(
      "org.scala-lang" % "scala-reflect" % scalaVersion.value
    )
  )


lazy val stoneMacrosExample = (project in file("example"))
  .settings(
    name := "stone-example"
  )
  .dependsOn(stoneMacros)



