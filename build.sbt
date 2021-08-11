name := "trg-assessment"

version := "0.1"

ThisBuild / scalaVersion := "2.11.8"


lazy val sparkVersion = "2.3.0"
lazy val akkaHttpVersion = "10.1.10"
lazy val akkaVersion = "2.5.25"

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % sparkVersion,
  "org.apache.spark" %% "spark-sql" % sparkVersion,
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-xml" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.typesafe" % "config" % "1.3.1",
  "org.apache.logging.log4j" % "log4j-core" % "2.11.1",
  "com.github.nscala-time" %% "nscala-time" % "2.22.0",
//  "com.github.pureconfig" %% "pureconfig" % "0.10.2",
  "io.circe" %% "circe-core" % "0.11.1",
  "io.circe" %% "circe-generic" % "0.11.1",
  "io.circe" %% "circe-parser" % "0.11.1",
  "io.circe" %% "circe-generic-extras" % "0.11.1",
  "com.propensive" %% "rapture-io" % "2.0.0-M9",
  "com.propensive" %% "rapture-json" % "2.0.0-M9",
)

assemblyMergeStrategy in assembly := {
  case PathList("javax", "servlet", xs@_*) => MergeStrategy.last
  case PathList("org", "apache", "commons", "beanutils", xs@_*) => MergeStrategy.last
  case PathList("javax", "activation", xs@_*) => MergeStrategy.last
  case PathList("javax", "el", xs@_*) => MergeStrategy.last
  case PathList("javax", "xml", xs@_*) => MergeStrategy.last
  case PathList("org", "apache", xs@_*) => MergeStrategy.first
  case PathList("org", "slf4j", xs@_*) => MergeStrategy.last
  case PathList("com", "google", xs@_*) => MergeStrategy.last
  case PathList("play", "core", "server", "ServerWithStop.class") => MergeStrategy.first
  case PathList("com", "esotericsoftware", xs@_*) => MergeStrategy.last
  case PathList("com", "codahale", xs@_*) => MergeStrategy.last
  case PathList("com", "yammer", xs@_*) => MergeStrategy.last
  case PathList("javax", "transaction", xs@_*) => MergeStrategy.last
  case "about.html" => MergeStrategy.rename
  case "META-INF/ECLIPSEF.RSA" => MergeStrategy.last
  case "META-INF/mailcap" => MergeStrategy.last
  case "META-INF/mimetypes.default" => MergeStrategy.last
  case "plugin.properties" => MergeStrategy.last
  case "log4j.properties" => MergeStrategy.last
  case x if x contains "LocalizedFormats_fr" => MergeStrategy.last
  case x => val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}