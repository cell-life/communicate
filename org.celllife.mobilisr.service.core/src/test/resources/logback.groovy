import ch.qos.logback.classic.encoder.PatternLayoutEncoder 
import ch.qos.logback.core.ConsoleAppender
import ch.qos.logback.core.FileAppender
import ch.qos.logback.core.status.OnConsoleStatusListener
import ch.qos.logback.classic.PatternLayout

import static ch.qos.logback.classic.Level.TRACE
import static ch.qos.logback.classic.Level.DEBUG
import static ch.qos.logback.classic.Level.INFO
import static ch.qos.logback.classic.Level.WARN
import static ch.qos.logback.classic.Level.ERROR

statusListener(OnConsoleStatusListener)

context.name = "service.core"
addInfo("loading context " + context.name)

appender("Console", ConsoleAppender) {
  encoder(PatternLayoutEncoder) {
    pattern = "%-30(%d{yyyy-MM-dd HH:mm:ss} [%thread]) %.-1level - %-40.40logger{36} - %msg%n"
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
logger("org.celllife.simulator", WARN)
logger("org.celllife.mobilisr.test", INFO)
logger("org.celllife.mobilisr.dao", INFO)
logger("org.celllife.mobilisr.domain", INFO)
logger("org.celllife.mobilisr.util", INFO)
logger("org.celllife.mobilisr.utilbean", INFO)
logger("org.celllife.mobilisr.service.campaign", INFO)
logger("org.celllife.mobilisr.service.channel", DEBUG)
logger("org.celllife.mobilisr.service.contacts", INFO)
logger("org.celllife.mobilisr.service.core", DEBUG)
logger("org.celllife.mobilisr.service.crud", INFO)
logger("org.celllife.mobilisr.service.message", INFO)
logger("org.celllife.mobilisr.service.organization", INFO)
logger("org.celllife.mobilisr.service.qrtz", DEBUG)
logger("org.celllife.mobilisr.service.rest", DEBUG)
logger("org.celllife.mobilisr.service.role", INFO)
logger("org.celllife.mobilisr.service.security", INFO)
logger("org.celllife.mobilisr.service.trigger", INFO)
logger("org.celllife.mobilisr.service.triggerhandler", INFO)
logger("org.celllife.mobilisr.service.user", DEBUG)
logger("org.celllife.mobilisr.service.utility", INFO)
logger("org.celllife.mobilisr.service.wasp", INFO)
logger("org.celllife.mobilisr.service.writer", INFO)
logger("org.celllife.mobilisr.client", INFO)
logger("org.celllife.mobilisr.controller", INFO)
logger("org.celllife.mobilisr.csvservlet", INFO)
logger("org.celllife.mobilisr.test", INFO)
logger("org.apache.activemq", ERROR);
logger("org.springframework.integration.handler", ERROR);

// Other loggers
logger("org.springframework", WARN)
logger("org.springframework.security", WARN)
logger("org.springframework.batch", WARN)
logger("org.hibernate", ERROR)
logger("net.sf.gilead", ERROR)
logger("net.sf.ehcache", ERROR)
logger("org.gwtwidgets.server.mapping", INFO)
logger("org.springframework.ui", ERROR);
logger("org.apache.velocity", ERROR);
logger("org.apache.http.impl", ERROR);
logger("org.apache.commons.digester", ERROR)

root(ERROR, ["File"])