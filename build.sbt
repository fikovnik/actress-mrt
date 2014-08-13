name := "ACTRESS"
 
version := "1.0"
 
scalaVersion := "2.11.1"
 
resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"
 
libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.3.4"

libraryDependencies += "com.typesafe.akka" %% "akka-testkit" % "2.3.4"

libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.1" % "test"