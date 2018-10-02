package com.gjsduarte

import java.io.Serializable
import java.net.InetAddress
import java.util.concurrent.BlockingDeque

import ch.qos.logback.classic.net.SocketAppender
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.spi.PreSerializationTransformer

import scala.beans.BeanProperty
import scala.concurrent.duration._
import scala.io.Source

class DatadogAppender extends SocketAppender with PreSerializationTransformer[ILoggingEvent] {

  // Configuration properties
  // TODO: Load configuration value from environment variables
  @BeanProperty var apiKey: String = _
  @BeanProperty var aws = false
  @BeanProperty var block = 5
  @BeanProperty var service: String = _
  @BeanProperty var source = "logback"
  @BeanProperty var sourceCategory = "sourcecode"

  // Set defaults
  setRemoteHost("intake.logs.datadoghq.com")
  setPort(10514)

  private lazy val host =
    if (aws) {
      // TODO: Add timeout
      Source.fromURL("http://169.254.169.254/latest/meta-data/instance-id").mkString
    }
    else InetAddress.getLocalHost.getHostName

  private lazy val queue = {
    // TODO: Find a way to avoid reflection to measure the queue size
    val field = getClass.getSuperclass.getSuperclass.getDeclaredField("deque")
    field.setAccessible(true)
    field.get(this).asInstanceOf[BlockingDeque[ILoggingEvent]]
  }

  override def getPST: PreSerializationTransformer[ILoggingEvent] = this

  override def start() {
    if (apiKey == null) {
      addError("An API Key must be provided")
    } else {
      super.start()
    }
  }

  override def stop() {
    // Block waiting for events to be sent
    val timeout = block.seconds.fromNow

    while (!queue.isEmpty && timeout.hasTimeLeft) {
      addInfo(s"Waiting ${timeout.timeLeft} for ${queue.size} events in the queue to be sent...")
      Thread.sleep(1000)
    }
    super.stop()
  }

  override def transform(event: ILoggingEvent): Serializable = {
    s"""
       |$apiKey ${serialize(event).replace("\n", "")}
       |${resolveValue(apiKey)} ${serialize(event).replace("\n", "")}
       |""".stripMargin
  }

  private def error(event: ILoggingEvent) = Option(event.getThrowableProxy) match {
    case Some(proxy) =>
      s""",
         |  "error.stack": "${escape(proxy.getStackTraceElementProxyArray.map(x => x.getSTEAsString).mkString("\n"))}",
         |  "error.message": "${escape(proxy.getMessage)}",
         |  "error.kind": "${proxy.getClassName}" """.stripMargin
    case _ => ""
  }

  private def escape(string: String) = string.flatMap(escapedChar)

  private def escapedChar(char: Char) = char match {
    case '\b' => "\\b"
    case '\t' => "\\t"
    case '\n' => "\\n"
    case '\f' => "\\f"
    case '\r' => "\\r"
    case '"'  => "\\\""
    case '\\' => "\\\\"
    case _ if char.isControl =>
      "\\0" + Integer.toOctalString(char.toInt)
    case _ => String.valueOf(char)
  }

  private def message(event: ILoggingEvent) = {
    Option(event.getFormattedMessage) match {
      case Some(message) if message.nonEmpty => message
      case _ => Option(event.getThrowableProxy).map(_.getMessage).getOrElse("")
    }
  }

  private def resolveValue(value: String): String = {
    if (value != null && value.startsWith("$")) {
      val name = value.replace("$", "")
      val variable = System.getenv(name)
      if (variable == null || variable.isEmpty) {
        System.getProperty(name)
      } else {
        variable
      }
    } else {
      value
    }
  }

  private def serialize(event: ILoggingEvent) = {
    s"""{
       |  "host": "${escape(resolveValue(host))}",
       |  "service": "${escape(resolveValue(service))}",
       |  "source": "${escape(resolveValue(source))}",
       |  "sourcecategory": "${escape(resolveValue(sourceCategory))}",
       |  "logger.name": "${escape(event.getLoggerName)}",
       |  "logger.thread_name": "${escape(event.getThreadName)}",
       |  "level": "${event.getLevel.levelStr}",
       |  "timestamp": ${event.getTimeStamp},
       |  "message": "${escape(message(event))}"
       |  ${error(event)}
       }""".stripMargin
  }
}
