// -----------------------------------------------------------------------------
// configurations
// -----------------------------------------------------------------------------
val akkaVersion = "2.3.4"

val commonSettings = Seq(
  organization := "fr.inria.spirals.actress",
  version := "1.0.0-SNAPSHOT", 
  scalaVersion := "2.11.1",
  resolvers += Resolver.typesafeRepo("releases"),
  scalacOptions ++= Seq("-Xlint", "-feature", "-deprecation", "-unchecked"),
  libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.1" % "test"
)

// -----------------------------------------------------------------------------
// ACTRESS Projects
// -----------------------------------------------------------------------------

lazy val `actress-common` = project
  .settings(commonSettings: _*)

lazy val `actress-metamodel` = project
  .dependsOn(`actress-common`)
  .settings(commonSettings: _*)

lazy val `actress-runtime` = project
  .dependsOn(`actress-common`)
  .settings(commonSettings: _*)
  .settings(
    libraryDependencies += "com.typesafe.akka" %% "akka-actor" % akkaVersion,
    libraryDependencies += "com.typesafe.akka" %% "akka-testkit" % akkaVersion
  )

// -----------------------------------------------------------------------------
// MRT Projects
// -----------------------------------------------------------------------------

lazy val `mrt-sys` = project
  .dependsOn(`actress-common`)
  .dependsOn(`actress-metamodel`)
  .dependsOn(`actress-runtime`)
  .settings(commonSettings: _*)
  .settings(
    unmanagedSourceDirectories in Compile += baseDirectory.value / "src-gen/main/scala"
  )