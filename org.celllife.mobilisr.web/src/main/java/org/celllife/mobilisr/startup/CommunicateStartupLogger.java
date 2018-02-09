package org.celllife.mobilisr.startup;

import org.celllife.mobilisr.servlet.ServletContextProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommunicateStartupLogger {
	private static final Logger log = LoggerFactory
			.getLogger(CommunicateStartupLogger.class);
	private final BuildUtilsInfo buildUtilsInfo;

	public CommunicateStartupLogger() {
		this.buildUtilsInfo = new BuildUtilsInfoImpl();
	}

	public void printStartupMessage() {
		FormattedLogMsg logMsg = new FormattedLogMsg(log);
		logMsg.add("Communicate starting...");
		logMsg.printMessage(Level.INFO);
		try {
			CommunicateSystemInfo info = new CommunicateSystemInfo(logMsg);
			info.obtainBasicInfo(ServletContextProvider.getServletContext());
			info.obtainSystemProperties();
			info.obtainEnvironmentVariables();
		} catch (RuntimeException rte) {
			log.error("Cannnot obtain basic Communicate information", rte);
		} catch (Error e) {
			log.error("Cannot obtain basic Communicate information", e);
		}

		logMsg.printMessage(Level.INFO, false);
		
		StringBuffer sb = new StringBuffer().append("Communicate ")
				.append(this.buildUtilsInfo.getVersion()).append(" started.");
		logMsg.add(sb);

		logMsg.printMessage(Level.INFO);
	}
}