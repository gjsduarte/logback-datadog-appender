import sbt._

object Dependencies {
  lazy val logback = "ch.qos.logback" % "logback-classic" % "1.2.3"
  lazy val mockito = "org.mockito" % "mockito-core" % "2.22.0"
  lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.0.5"
}
