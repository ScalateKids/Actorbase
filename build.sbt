name := "Actorbase-Server"
version := "1.0"
scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.4.6",
  "com.typesafe.akka" %% "akka-testkit" % "2.4.6",
  "com.typesafe.akka" %% "akka-cluster-sharding" % "2.4.6",
  "com.typesafe.akka" %% "akka-cluster-tools" % "2.4.6",
  "com.typesafe.akka" % "akka-cluster-metrics_2.11" % "2.4.6",
  "com.typesafe.akka" %% "akka-slf4j" % "2.4.6",
  "com.typesafe" % "config" % "1.2.1",
  "org.scalatest" % "scalatest_2.11" % "2.2.6" % "test",
  "io.spray" %% "spray-can" % "1.3.3",
  "io.spray" %% "spray-routing" % "1.3.3",
  "io.spray" %% "spray-json" % "1.3.2",
  "org.mindrot" % "jbcrypt" % "0.3m",
  "com.github.t3hnar" % "scala-bcrypt_2.10" % "2.6",
  "com.github.romix.akka" %% "akka-kryo-serialization" % "0.4.1",
  "org.apache.maven.plugins" % "maven-shade-plugin" % "2.4.3",
  "ch.qos.logback" % "logback-classic" % "1.1.3",
  "io.spray" % "spray-caching_2.11" % "1.3.3"
)

javaOptions ++= Seq("-Xmx2048m")

// addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.3.5")

// addSbtPlugin("org.scoverage" % "sbt-coveralls" % "1.1.0")

assemblyJarName in assembly := "Actorbase-Server.jar"
mainClass in assembly := Some("com.actorbase.actorsystem.actors.httpserver.HTTPServer")
test in assembly := {}
assemblyMergeStrategy in assembly := {
    case x if Assembly.isConfigFile(x) =>
      MergeStrategy.concat
    case PathList(ps @ _*) if Assembly.isReadme(ps.last) || Assembly.isLicenseFile(ps.last) =>
      MergeStrategy.rename
    case PathList("META-INF", xs @ _*) =>
      (xs map {_.toLowerCase}) match {
        case ("manifest.mf" :: Nil) | ("index.list" :: Nil) | ("dependencies" :: Nil) =>
          MergeStrategy.discard
        case ps @ (x :: xs) if ps.last.endsWith(".sf") || ps.last.endsWith(".dsa") =>
          MergeStrategy.discard
        case "plexus" :: xs =>
          MergeStrategy.discard
        case "services" :: xs =>
          MergeStrategy.filterDistinctLines
        case ("spring.schemas" :: Nil) | ("spring.handlers" :: Nil) =>
          MergeStrategy.filterDistinctLines
        case _ => MergeStrategy.first
      }
    case _ => MergeStrategy.first
}
