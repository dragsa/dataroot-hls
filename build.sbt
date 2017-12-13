name := "dataroot-hls"

version := "0.1"

scalaVersion := "2.12.4"

resolvers += Resolver.bintrayRepo("hseeberger", "maven")

val circeVersion = "0.8.0"

libraryDependencies ++= Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser",
  "io.circe" %% "circe-streaming"
).map(_ % circeVersion) ++ Seq(
  "io.iteratee" %% "iteratee-core" % "0.11.0",
  "io.iteratee" %% "iteratee-scalaz" % "0.11.0",
  "com.typesafe.slick" %% "slick" % "3.2.1",
  "org.slf4j" % "slf4j-nop" % "1.6.4",
  "com.typesafe.slick" %% "slick-hikaricp" % "3.2.1",
  "org.postgresql" % "postgresql" % "42.1.4",
  "com.typesafe.akka" %% "akka-http" % "10.0.11",
  "com.typesafe.akka" %% "akka-http-testkit" % "10.0.11" % Test,
  "de.heikoseeberger" %% "akka-http-circe" % "1.18.0",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.7.2",
)
