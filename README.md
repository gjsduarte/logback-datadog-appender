# Datadog logback appender

[![Codacy Badge](https://api.codacy.com/project/badge/Grade/aeaa427beba846c79c582c2a51765f15)](https://app.codacy.com/app/gjsduarte/logback-datadog-appender?utm_source=github.com&utm_medium=referral&utm_content=gjsduarte/logback-datadog-appender&utm_campaign=Badge_Grade_Dashboard)

This appender sends logs to your [Datadog](https://www.datadoghq.com/) account, in bulks using non-blocking threading. Please note that this appender requires logback version 1.2.3 and up, and java 8 and up.

### Technical Information
This appender uses the [SocketAppender](https://github.com/qos-ch/logback/blob/master/logback-classic/src/main/java/ch/qos/logback/classic/net/SocketAppender.java) implementation. All logs are placed in a in-memory buffer before being sent. Once you send a log, it will be enqueued in the buffer and 100% non-blocking. There is a background task that will handle the log shipment for you.

### Logback Example Configuration
```xml
<!-- Use debug=true here if you want to see output from the appender itself -->
<!-- Use line=true here if you want to see the line of code that generated this log -->
<configuration>
    <!-- Use shutdownHook so that we can close gracefully and finish the log drain -->
    <shutdownHook class="ch.qos.logback.core.hook.DelayingShutdownHook"/>
    <appender name="Datadog" class="com.gjsduarte.DatadogAppender">
        <apiKey>yourdatadogapitoken</apiKey>
        <service>my awesome application</service>
    </appender>
</configuration>
```

### Parameters
| Parameter          | Default                              | Explained                                                                                                                       |
| ------------------ | ------------------------------------ | ------------------------------------------------------------------------------------------------------------------------------- |
| **apiKey**         | *None*                               | Your Datadog api key, which can be found under ["integrations"](https://app.datadoghq.com/account/settings#api) in your account |
| **aws**            | *false*                              |                                                                                                                                 |
| **block**          | *5000*                               |                                                                                                                                 |
| **port**           | *10514*                              |                                                                                                                                 |
| **remoteHost**     | *intake.logs.datadoghq.com*          |                                                                                                                                 |
| **service**        | *None*                               |                                                                                                                                 |
| **source**         | *logback*                            |                                                                                                                                 |
| **sourceCategory** | *sourcecode*                         |                                                                                                                                 |

### Contribution
 - Fork
 - Code
 - ```sbt compile```
 - Issue a PR :)