inThisBuild(
  List(
    organization := "ba.sake",
    scalaVersion := "2.13.3",
    publish / skip := true, // dont publish by default
    resolvers += Resolver.sonatypeRepo("releases"),
    licenses += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0.html")),
    developers += Developer("sake92", "Sakib Hadžiavdić", "sakib@sake.ba", url("https://sake.ba")),
    scmInfo := Some(
      ScmInfo(url("https://github.com/sake92/stone"), "scm:git:git@github.com:sake92/stone.git")
    ),
    homepage := Some(url("https://github.com/sake92/stone"))
  )
)

lazy val root = (project in file("."))
  .aggregate(stoneMacros.jvm, stoneMacros.js, stoneMacrosTests)

lazy val stoneMacros = crossProject(JVMPlatform, JSPlatform)
  .crossType(CrossType.Pure)
  .in(file("macros"))
  .settings(
    name := "stone-macros",
    publish / skip := false, // do publish
    libraryDependencies ++= Seq(
      "org.scala-lang" % "scala-reflect" % scalaVersion.value
    ),
    Compile / scalacOptions ++= List("-Ymacro-annotations")
  )

lazy val stoneMacrosTests = (project in file("tests"))
  .settings(
    name := "stone-tests",
    libraryDependencies ++= Seq(
      "org.scalatest" %% "scalatest" % "3.2.0" % "test"
    ),
    Compile / scalacOptions ++= List("-Ymacro-annotations")
  )
  .dependsOn(stoneMacros.jvm)
