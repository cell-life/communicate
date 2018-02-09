package org.celllife.mobilisr.startup;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.celllife.mobilisr.util.CommunicateHome;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ContextStartupListener implements ServletContextListener {
	private static final Logger log = LoggerFactory
			.getLogger(ContextStartupListener.class);

	public void contextInitialized(ServletContextEvent sce) {
		log.debug("Launching Communicate");

		try {
			CommunicateStartupLogger logger = new CommunicateStartupLogger();
			logger.printStartupMessage();
			CommunicateHome.onStartup();
		} catch (Exception e) {
			log.error("Error initialising", e);
		}
	}

	public void contextDestroyed(ServletContextEvent sce) {
	}
}