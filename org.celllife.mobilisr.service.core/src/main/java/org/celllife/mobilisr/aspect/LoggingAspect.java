package org.celllife.mobilisr.aspect;

import org.apache.commons.lang.ArrayUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.celllife.mobilisr.annotation.LogLevel;
import org.celllife.mobilisr.annotation.Loggable;
import org.celllife.mobilisr.domain.MobilisrEntity;
import org.celllife.mobilisr.logger.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {

	private static String BEFORE_STRING = "[ entering < {0} > ]";

	private static String BEFORE_WITH_PARAMS_STRING = "[ entering < {0} > with params ( {1} ) ]";

//	private static String AFTER_THROWING = "[ exception thrown from < {0} > exception message \"{1}\" with params ( {2} ) ]";

	private static String AFTER_RETURNING = "[ leaving < {0} > returning ( {1} ) ]";

	private static String AFTER_RETURNING_VOID = "[ leaving < {0} > ]";

	@Autowired
	@Qualifier("aspectLogger")
	private Logger logger;

	@Before(value = "@annotation(trace)", argNames = "joinPoint, trace")
	public void before(JoinPoint joinPoint, Loggable loggable) {
		LogLevel logLevel = loggable.value();
		Class<? extends Object> clazz = joinPoint.getTarget().getClass();
		
		if (!logger.isLogLevel(logLevel, clazz)){
			return;
		}
		
		String name = joinPoint.getSignature().getName();

		if (ArrayUtils.isEmpty(joinPoint.getArgs())) {
			logger.log(logLevel, clazz, null, BEFORE_STRING, name, constructArgumentsString(clazz, joinPoint
					.getArgs()));
		} else {
			logger.log(logLevel, clazz, null, BEFORE_WITH_PARAMS_STRING, name, constructArgumentsString(clazz,
					joinPoint.getArgs()));
		}
	}

	/*@AfterThrowing(value = "@annotation(org.celllife.mobilisr.annotation.Loggable)", throwing = "throwable", argNames = "joinPoint, throwable")
	public void afterThrowing(JoinPoint joinPoint, Throwable throwable) {
		Class<? extends Object> clazz = joinPoint.getTarget().getClass();
		String name = joinPoint.getSignature().getName();
		logger.log(LogLevel.ERROR, clazz, throwable, AFTER_THROWING, name, throwable.getMessage(),
				constructArgumentsString(clazz, joinPoint.getArgs()));
	}*/

	@AfterReturning(value = "@annotation(trace)", returning = "returnValue", argNames = "joinPoint, trace, returnValue")
	public void afterReturning(JoinPoint joinPoint, Loggable loggable, Object returnValue) {
		LogLevel logLevel = loggable.value();
		Class<? extends Object> clazz = joinPoint.getTarget().getClass();
		
		if (!logger.isLogLevel(logLevel, clazz)){
			return;
		}
		
		String name = joinPoint.getSignature().getName();

		if (joinPoint.getSignature() instanceof MethodSignature) {
			MethodSignature signature = (MethodSignature) joinPoint.getSignature();
			Class<?> returnType = signature.getReturnType();
			if (returnType.getName().compareTo("void") == 0) {
				logger.log(logLevel, clazz, null, AFTER_RETURNING_VOID, name, constructArgumentsString(clazz,
						returnValue));
				return;
			}
		}

		logger.log(logLevel, clazz, null, AFTER_RETURNING, name, constructArgumentsString(clazz, returnValue));
	}

	private String constructArgumentsString(Class<?> clazz, Object... arguments) {
		if (arguments.length == 0) {
			return "";
		}

		StringBuffer buffer = new StringBuffer();
		for (Object object : arguments) {
			if (object instanceof MobilisrEntity) {
				MobilisrEntity entity = (MobilisrEntity) object;
				buffer.append(entity.getIdentifierString());
			} else {
				buffer.append(object);
			}
			buffer.append(",");
		}

		return buffer.substring(0, buffer.length() - 1);
	}
}
