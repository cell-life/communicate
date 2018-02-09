import ch.qos.logback.classic.encoder.PatternLayoutEncoder 
import ch.qos.logback.core.ConsoleAppender
import ch.qos.logback.classic.net.SMTPAppender
import ch.qos.logback.core.rolling.RollingFileAppender
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy
import ch.qos.logback.core.status.OnConsoleStatusListener
import ch.qos.logback.classic.PatternLayout
import ch.qos.logback.classic.html.HTMLLayout
import ch.qos.logback.classic.filter.ThresholdFilter
import ch.qos.logback.core.FileAppender
import ch.qos.logback.classic.boolex.OnMarkerEvaluator
import ch.qos.logback.classic.turbo.DuplicateMessageFilter

import static ch.qos.logback.classic.Level.TRACE
import static ch.qos.logback.classic.Level.DEBUG
import static ch.qos.logback.classic.Level.INFO
import static ch.qos.logback.classic.Level.WARN
import static ch.qos.logback.classic.Level.ERROR
import java.util.Properties

statusListener(OnConsoleStatusListener)

context.name = "Communicate"
addInfo("loading context " + context.name)

def env = System.getenv()

def home = env['COMMUNICATE_HOME'] 
home = home == null ? System.getProperty("COMMUNICATE_HOME") : home
if (home == null || !new File(home).exists()){
	addError("COMMUNICATE_HOME folder does not exist: " + home);
	home = "COMMUNICATE_HOME"
}

def logNamePrefix = System.getProperty("logNamePrefix")
def props = new Properties()

try {
	new File(home + "/communicate.properties").withInputStream { 
	  stream -> props.load(stream) 
	}
} catch (e){
	addError("Error loading system properties")
}

def HOSTNAME=hostname
def appenderList = ["Console", "File"]
def enable_email = props["log.error.email_enable"]
enable_email = enable_email == null ? false : enable_email.toBoolean()

if(enable_email) {
	addInfo("Email logging enabled for level ERROR or greater")
	appender("EMAIL", SMTPAppender) {
	  turboFilter(DuplicateMessageFilter)
	  evaluator(OnMarkerEvaluator) {
        marker = "NOTIFY_ADMIN"
      }
	  SMTPHost = props["mailSender.host"]
	  SMTPPort = props["mailSender.port"] as int
	  STARTTLS = props["mail.smtp.starttls.enable"]
	  username = props["mailSender.username"]
	  password = props["mailSender.password"]
	  to = props["log.error.email_to"]
	  subject = "Mobilisr ERROR from ${HOSTNAME}: %logger{20} - %m"
	  layout(HTMLLayout) 
	}
	appenderList.add("EMAIL")
}

appender("File", RollingFileAppender) {
  if (logNamePrefix == null)
    logNamePrefix = "";
  file = home + "/logs/" + logNamePrefix + "communicate.log"
  rollingPolicy(TimeBasedRollingPolicy) {
    fileNamePattern = home + "/logs/" + logNamePrefix + "communicate.%d.log"
    maxHistory = 7
  }
  append = true
  encoder(PatternLayoutEncoder) {
    pattern = "%-42(%d{yyyy-MM-dd HH:mm:ss} [%.20thread]) %.-1level [U:%X{user},H:%X{req.remoteHost}] - %-30.30logger{36} - %msg%n"
  }
}

appender("Console", ConsoleAppender) {
  filter(ThresholdFilter) {
    level = INFO
  }
  encoder(PatternLayoutEncoder) {
    pattern = "%-42(%d{yyyy-MM-dd HH:mm:ss} [%.20thread]) %.-1level [U:%X{user},H:%X{req.remoteHost}] - %-30.30logger{36} - %msg%n"
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
logger("org.celllife.mobilisr.service", INFO)
logger("org.celllife.mobilisr.service.channel", DEBUG)
logger("org.celllife.mobilisr.service.message", INFO)
logger("org.celllife.mobilisr.service.qrtz", INFO)
logger("org.celllife.mobilisr.service.security", INFO)
logger("org.celllife.mobilisr.service.trigger", INFO)
logger("org.celllife.mobilisr.service.utility", INFO)
logger("org.celllife.mobilisr.service.wasp", INFO)
logger("org.celllife.mobilisr.service.writer", INFO)
logger("org.celllife.mobilisr.client", INFO)
logger("org.celllife.mobilisr.controller", INFO)
logger("org.celllife.mobilisr.servlet", INFO)
logger("org.celllife.mobilisr.rest", INFO)

// Other loggers
logger("org.apache.activemq", ERROR);
logger("org.springframework", WARN)
logger("org.springframework.security", WARN)
logger("org.springframework.batch", WARN)
logger("org.springframework.ui", ERROR);
logger("org.springframework.integration.handler", ERROR);
logger("org.hibernate", ERROR)
logger("net.sf.gilead", ERROR)
logger("net.sf.ehcache", ERROR)
logger("org.gwtwidgets.server.mapping", INFO)
logger("org.apache.velocity", ERROR);
logger("org.apache.http.impl", ERROR);

scan("10 seconds")

root(ERROR, appenderList)