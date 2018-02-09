package org.celllife.mobilisr;

import org.celllife.mobilisr.exception.MobilisrException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

/** 
 * This is a utility class for testing the logging configuration.
 * 
 * @author Simon Kelly
 */
public class LoggingDemo {
	
	public static void main(String[] args){
		testLogger();
		
		new Thread(new Runnable(){
			@Override
			public void run() {
				testLogger();
			}
			
		}, "qrtzScheduler_Worker-12").start();
	}

	/**
	 * 
	 */
	private static void testLogger() {
		Logger longLogger = LoggerFactory.getLogger("org.celllife.a.very.long.logger.name.indeed.that.is.for.sure");
		longLogger.trace("longlogger.trace [{}]", "traceinfo");
		longLogger.debug("longlogger.debug [{}]", "debuginfo");
		longLogger.info("longlogger.info [{}]", "infoinfo");
		longLogger.warn("longlogger.warn [{}]", "warninfo");
		MDC.put("user", "testuser");
		MDC.put("req.remoteHost", "172.16.23.200");
		longLogger.error("longlogger.error", new MobilisrException("This is a demo exception"));
	}

}
