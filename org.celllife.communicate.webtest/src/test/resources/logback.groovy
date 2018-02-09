import ch.qos.logback.classic.encoder.PatternLayoutEncoder 
import ch.qos.logback.core.ConsoleAppender
import ch.qos.logback.core.FileAppender
import ch.qos.logback.classic.net.SMTPAppender
import ch.qos.logback.core.status.OnConsoleStatusListener
import ch.qos.logback.classic.PatternLayout
import ch.qos.logback.classic.html.HTMLLayout

import static ch.qos.logback.classic.Level.TRACE
import static ch.qos.logback.classic.Level.DEBUG
import static ch.qos.logback.classic.Level.INFO
import static ch.qos.logback.classic.Level.WARN
import static ch.qos.logback.classic.Level.ERROR

statusListener(OnConsoleStatusListener)

appender("Console", ConsoleAppender) {
  encoder(PatternLayoutEncoder) {
    pattern = "%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"
  }
}

appender("File", FileAppender) {
  file = "target/testOutput.log"
  append = true
  encoder(PatternLayoutEncoder) {
    pattern = "%-30(%d{yyyy-MM-dd HH:mm:ss} [%thread]) %.-1level - %-40.40logger{36} - %msg%n"
  }
}

logger("org.celllife", INFO)

root(ERROR, ["File"])