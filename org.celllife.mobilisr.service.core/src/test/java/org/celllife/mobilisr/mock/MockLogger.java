package org.celllife.mobilisr.mock;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.celllife.mobilisr.annotation.LogLevel;
import org.celllife.mobilisr.logger.Slf4jLogger;

public class MockLogger extends Slf4jLogger {

	private Map<Class<?>, LogLevel> logLevelMap = new HashMap<Class<?>, LogLevel>();

	private Map<Class<?>, List<LogMessage>> messages = new HashMap<Class<?>, List<LogMessage>>();

	public boolean isLogLevel(LogLevel logLevel, Class<?> clazz) {
		boolean result = isLogLevelEnabled(clazz, logLevel);
		return result;
	}

	public void log(LogLevel logLevel, Class<?> clazz, Throwable throwable, String pattern, Object... arguments) {
		super.log(logLevel, clazz, throwable, pattern, arguments);

		switch (logLevel) {
		case DEBUG:
			debug(clazz, throwable, pattern, arguments);
			break;
		case ERROR:
			error(clazz, throwable, pattern, arguments);
			break;
		case INFO:
			info(clazz, throwable, pattern, arguments);
			break;
		case TRACE:
			trace(clazz, throwable, pattern, arguments);
			break;
		case WARN:
			warn(clazz, throwable, pattern, arguments);
			break;
		}
	}

	private void debug(Class<?> clazz, Throwable throwable, String pattern, Object... arguments) {
		if (throwable != null) {
			getMessages(clazz).add(new LogMessage(LogLevel.DEBUG, format(pattern, arguments), throwable));
		} else {
			getMessages(clazz).add(new LogMessage(LogLevel.DEBUG, format(pattern, arguments)));
		}
	}

	private void error(Class<?> clazz, Throwable throwable, String pattern, Object... arguments) {

		if (throwable != null) {
			getMessages(clazz).add(new LogMessage(LogLevel.ERROR, format(pattern, arguments), throwable));
		} else {
			getMessages(clazz).add(new LogMessage(LogLevel.ERROR, format(pattern, arguments)));
		}
	}

	private void info(Class<?> clazz, Throwable throwable, String pattern, Object... arguments) {

		if (throwable != null) {
			getMessages(clazz).add(new LogMessage(LogLevel.INFO, format(pattern, arguments), throwable));
		} else {
			getMessages(clazz).add(new LogMessage(LogLevel.INFO, format(pattern, arguments)));
		}
	}

	private void trace(Class<?> clazz, Throwable throwable, String pattern, Object... arguments) {

		if (throwable != null) {
			getMessages(clazz).add(new LogMessage(LogLevel.TRACE, format(pattern, arguments), throwable));
		} else {
			getMessages(clazz).add(new LogMessage(LogLevel.TRACE, format(pattern, arguments)));
		}
	}

	private void warn(Class<?> clazz, Throwable throwable, String pattern, Object... arguments) {

		if (throwable != null) {
			getMessages(clazz).add(new LogMessage(LogLevel.WARN, format(pattern, arguments), throwable));
		} else {
			getMessages(clazz).add(new LogMessage(LogLevel.WARN, format(pattern, arguments)));
		}
	}

	private String format(String pattern, Object... arguments) {

		return MessageFormat.format(pattern, arguments);
	}

	public void resetLoggers() {
		messages = new HashMap<Class<?>, List<LogMessage>>();
	}

	public List<LogMessage> getMessages(Class<?> clazz) {
		if (messages.get(clazz) == null) {
			messages.put(clazz, new ArrayList<LogMessage>());
		}

		return messages.get(clazz);
	}

	private boolean isLogLevelEnabled(Class<?> clazz, LogLevel logLevel) {
		boolean slfLogLevelEnabled = super.isLogLevel(logLevel, clazz);
		boolean mockLogLevelEnabled = logLevelMap.get(clazz) != null && logLevelMap.get(clazz).equals(logLevel);
		return mockLogLevelEnabled || slfLogLevelEnabled;
	}

	public void setLogLevel(Class<?> clazz, LogLevel logLevel) {
		logLevelMap.put(clazz, logLevel);
	}

	public class LogMessage {
		private LogLevel logLevel;

		private String message;

		private Throwable throwable;

		public LogMessage(LogLevel logLevel, String message, Throwable throwable) {
			this(logLevel, message);
			this.throwable = throwable;
		}

		public LogMessage(LogLevel logLevel, String message) {
			this.logLevel = logLevel;
			this.message = message;
		}

		public LogLevel getLogLevel() {
			return logLevel;
		}

		public String getMessage() {
			return message;
		}

		public Throwable getThrowable() {
			return throwable;
		}
	}
}
