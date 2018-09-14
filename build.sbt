import Dependencies._

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "com.example",
      scalaVersion := "2.12.6",
      version      := "0.1.0-SNAPSHOT",
      scalacOptions ++= Seq(
          "-language:_",
          "-Ypartial-unification",
          "-Xfatal-warnings"
          )
      )),
    name := "Hello",
    libraryDependencies ++= Seq(
      "com.github.mpilquist" %% "simulacrum"  % "0.13.0",
      "org.scalaz"           %% "scalaz-core" % "7.2.26"
    )
  )
addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.7")
addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full)