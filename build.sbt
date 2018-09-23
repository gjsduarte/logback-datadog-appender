import Dependencies._

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "com.github.gjsduarte",
      scalaVersion := "2.11.11"
    )),
    name := "logback-datadog-appender",
    libraryDependencies ++= Seq(
      logback,
      mockito % Test,
      scalaTest % Test
    )
  )

publishTo := sonatypePublishTo.value