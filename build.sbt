import Dependencies._

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "com.gjsduarte",
      scalaVersion := "2.11.11",
      version      := "0.1.0-SNAPSHOT"
    )),
    name := "logback-datadog-appender",
    libraryDependencies ++= Seq(
      logback,
      mockito % Test,
      scalaTest % Test
    )
  )