ThisBuild / scalafmtOnCompile := true
ThisBuild / organization := "com.rezonation"
ThisBuild / version      := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "3.3.6"

lazy val httpServer = (project in file("http-server"))
  .settings(
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio" % "2.0.15",
      "dev.zio" %% "zio-http" % "3.0.0-RC1"
    )
  )

lazy val kafkaConsumer = (project in file("kafka-consumer"))
  .settings(
    libraryDependencies ++= Seq()
  )
