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

context.name = "domain"
addInfo("loading context " + context.name)

def appenderList = ["Console"]

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

// Mobilisr loggers
logger("org.celllife", INFO)
logger("org.celllife.mobilisr.test", INFO)
logger("org.celllife.mobilisr.dao", INFO)
logger("org.celllife.mobilisr.domain", INFO)
logger("org.celllife.mobilisr.util", INFO)
logger("org.celllife.mobilisr.utilbean", INFO)
logger("org.celllife.mobilisr.test", INFO)

// Other loggers
logger("org.springframework", WARN)
logger("org.springframework.security", WARN)
logger("org.springframework.batch", WARN)
logger("org.hibernate", WARN)
logger("org.hibernate.transaction", WARN)
logger("org.hibernate.jdbc", WARN)
logger("net.sf.gilead", ERROR)
logger("net.sf.ehcache", ERROR)
logger("org.gwtwidgets.server.mapping", INFO)
logger("com.mchange", ERROR)

scan("30 seconds")

root(ERROR, ["File"])