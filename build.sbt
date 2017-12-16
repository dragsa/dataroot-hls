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
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.7.2",
  "com.typesafe.slick" %% "slick-hikaricp" % "3.2.1",
  "org.postgresql" % "postgresql" % "42.1.4",
  "com.typesafe.akka" %% "akka-http" % "10.0.11",
  "de.heikoseeberger" %% "akka-http-circe" % "1.18.0"
)

// no need for this, using plain assembly

//enablePlugins(DockerPlugin)
//
//dockerfile in docker := {
//  // The assembly task generates a fat JAR file
//  val artifact: File = assembly.value
//  val artifactTargetPath = s"/app/${artifact.name}"
//
//  new Dockerfile {
//    from("dragsasgard/postgres_96")
//    add(artifact, artifactTargetPath)
//  }
//}
//
//imageNames in docker := Seq(
//  // Sets the latest tag
//  ImageName(s"dragsasgard/dataroot_hls:latest")
//)

// just run "sbt assembly" to get fat jar in docker subfolder

assemblyOutputPath in assembly := file(Option("docker/").getOrElse("") + "dataroot-hls-assembly-" + version.value + ".jar")

