import Dependencies._

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "com.doogie.fpmortals",
      scalaVersion := "2.12.7",
      version      := "0.1.0-SNAPSHOT",
      scalacOptions ++= Seq(
          "-language:_",
          "-Ypartial-unification",
          "-Xfatal-warnings"
          )
      )),
    name := "fpmortals",
    libraryDependencies ++= Seq(
      "com.github.mpilquist" %% "simulacrum"  % "0.13.0",
      "org.scalaz" %% "scalaz-core" % "7.2.26",
      "org.scala-lang" % "scala-reflect" % scalaVersion.value
      /* "com.lihaoyi" % "ammonite" % "1.2.1" % "test" cross CrossVersion.full */
    )
    /* sourceGenerators in Test += Def.task { */
    /*   val file = (sourceManaged in Test).value / "amm.scala" */
    /*   IO.write(file, """object amm extends App { ammonite.Main.main(args) }""") */
    /*   Seq(file) */
    /* }.taskValue */
    // FIXME: How can I use ammonite in main?
    /* sourceGenerators in Compile += Def.task { */
    /*   val file = (sourceManaged in Compile).value / "amm.scala" */
    /*   IO.write(file, """object amm extends App { ammonite.Main.main(args) }""") */
    /*   Seq(file) */
    /* }.taskValue */
  )

addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.7")
addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full)
addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.2.4")

