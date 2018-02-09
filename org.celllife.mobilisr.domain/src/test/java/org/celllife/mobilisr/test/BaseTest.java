package org.celllife.mobilisr.test;

import org.junit.Rule;
import org.junit.rules.MethodRule;
import org.junit.rules.TestWatchman;
import org.junit.runners.model.FrameworkMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseTest {

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	@Rule
	public MethodRule watchman = new TestWatchman() {
		public void starting(FrameworkMethod method) {
			log.info("---------------- " + method.getName() + " being run ...");
		}
	};

}
