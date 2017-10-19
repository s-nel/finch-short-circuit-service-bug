name := "FinchClientServerError"

version := "0.1"

scalaVersion := "2.12.2"

libraryDependencies += "com.twitter" %% "twitter-server" % "1.27.0"
libraryDependencies += "com.github.finagle" %% "finch-core" % "0.16.0-RC1"
libraryDependencies += "com.github.finagle" %% "finch-circe" % "0.16.0-RC1"
libraryDependencies += "io.circe" %% "circe-core" % "0.8.0"
libraryDependencies += "io.circe" %% "circe-generic" % "0.8.0"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.4"
