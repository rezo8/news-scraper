name := "news-scraper"

version := "0.1"

scalaVersion := "3.3.6"
organization := "com.rezonation"

libraryDependencies ++= Seq(
  "dev.zio" %% "zio" % "2.0.15"
)

ThisBuild / scalafmtOnCompile := true
