package org.celllife.mobilisr.logger;

import java.text.MessageFormat;

import org.celllife.mobilisr.annotation.LogLevel;
import org.slf4j.LoggerFactory;

public class Slf4jLogger implements Logger {

	public boolean isLogLevel(LogLevel logLevel, Class<?> clazz) {

		boolean result = false;

		switch (logLevel) {
		case DEBUG:
			result = getLogger(clazz).isDebugEnabled();
			break;
		case ERROR:
			result = getLogger(clazz).isErrorEnabled();
			break;
		case INFO:
			result = getLogger(clazz).isInfoEnabled();
			break;
		case TRACE:
			result = getLogger(clazz).isTraceEnabled();
			break;
		case WARN:
			result = getLogger(clazz).isWarnEnabled();
			break;
		default:
			result = false;
		}
		return result;
	}

	public void log(LogLevel logLevel, Class<?> clazz, Throwable throwable, String pattern, Object... arguments) {
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
			getLogger(clazz).debug(format(pattern, arguments), throwable);
		} else {
			getLogger(clazz).debug(format(pattern, arguments));
		}
	}

	private void error(Class<?> clazz, Throwable throwable, String pattern, Object... arguments) {
		if (throwable != null) {
			getLogger(clazz).error(format(pattern, arguments), throwable);
		} else {
			getLogger(clazz).error(format(pattern, arguments));
		}
	}

	private void info(Class<?> clazz, Throwable throwable, String pattern, Object... arguments) {
		if (throwable != null) {
			getLogger(clazz).info(format(pattern, arguments), throwable);
		} else {
			getLogger(clazz).info(format(pattern, arguments));
		}
	}

	private void trace(Class<?> clazz, Throwable throwable, String pattern, Object... arguments) {
		if (throwable != null) {
			getLogger(clazz).trace(format(pattern, arguments), throwable);
		} else {
			getLogger(clazz).trace(format(pattern, arguments));
		}
	}

	private void warn(Class<?> clazz, Throwable throwable, String pattern, Object... arguments) {
		if (throwable != null) {
			getLogger(clazz).warn(format(pattern, arguments), throwable);
		} else {
			getLogger(clazz).warn(format(pattern, arguments));
		}
	}

	private String format(String pattern, Object... arguments) {
		return MessageFormat.format(pattern, arguments);
	}

	private org.slf4j.Logger getLogger(Class<?> clazz) {
		return LoggerFactory.getLogger(clazz);
	}
}