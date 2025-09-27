ThisBuild / scalafmtOnCompile := true
ThisBuild / organization := "com.rezonation"
ThisBuild / version      := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "3.3.6"

lazy val httpServer = project
  .in(file("http-server"))
  .settings(
    name := "http-server",
    scalaVersion := "3.3.6",
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio" % "2.0.21",
      "dev.zio" %% "zio-http" % "3.5.1",
      "dev.zio" %% "zio-kafka" % "3.1.0",
      "dev.zio" %% "zio-json"    % "0.7.44",
      "dev.zio" %% "zio-config"          % "4.0.5",
      "dev.zio" %% "zio-config-magnolia" % "4.0.5",
      "dev.zio" %% "zio-config-typesafe" % "4.0.5",
      "dev.zio" %% "zio-config-refined"  % "4.0.5"
    ),
    Compile / run / mainClass := Some("com.rezonation.Main")
  )

lazy val kafkaConsumer = (project in file("kafka-consumer"))
  .settings(
    libraryDependencies ++= Seq()
  )
