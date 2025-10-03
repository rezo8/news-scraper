ThisBuild / scalafmtOnCompile := true
ThisBuild / organization := "com.rezonation"
ThisBuild / version      := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "3.3.6"

val commonDeps = Seq(
      "dev.zio" %% "zio" % "2.0.21",
      "dev.zio" %% "zio-json" % "0.7.44",
      "dev.zio" %% "zio-kafka" % "3.1.0" 
    )

// Common Kafka Messages Project
lazy val kafkaCommon = (project in file("kafka-common"))
  .settings(
    name := "kafka-common",
    scalaVersion := "3.3.6",
    libraryDependencies ++= commonDeps
  )


lazy val httpServer = project
  .dependsOn(kafkaCommon)
  .in(file("http-server"))
  .settings(
    name := "http-server",
    scalaVersion := "3.3.6",
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio-http" % "3.5.1",
      "dev.zio" %% "zio-config"          % "4.0.5",
      "dev.zio" %% "zio-config-magnolia" % "4.0.5",
      "dev.zio" %% "zio-config-typesafe" % "4.0.5",
      "dev.zio" %% "zio-config-refined"  % "4.0.5"
    ) ++ commonDeps
  )

lazy val kafkaConsumer = project
  .dependsOn(kafkaCommon)
  .in(file("kafka-consumer"))
  .settings(
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio-http" % "3.5.1",
      "dev.zio" %% "zio-config"          % "4.0.5",
      "dev.zio" %% "zio-config-magnolia" % "4.0.5",
      "dev.zio" %% "zio-config-typesafe" % "4.0.5",
      "dev.zio" %% "zio-config-refined"  % "4.0.5",
      "net.ruippeixotog" %% "scala-scraper" % "3.2.0",
      "net.dankito.readability4j" % "readability4j" % "1.0.8"
    ) ++ commonDeps,
  )
