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
// Projects
// -----------------------------------------------------------------------------

lazy val `actress-runtime` = project
  .settings(commonSettings: _*)
  .settings(
    unmanagedSourceDirectories in Compile += baseDirectory.value / "src-gen/main/scala",
    libraryDependencies += "com.typesafe.akka" %% "akka-actor" % akkaVersion,
    libraryDependencies += "com.typesafe.akka" %% "akka-agent" % akkaVersion,
    libraryDependencies += "com.typesafe.akka" %% "akka-testkit" % akkaVersion
  )

lazy val `mrt-sys` = project
  .dependsOn(`actress-runtime`)
  .settings(commonSettings: _*)
