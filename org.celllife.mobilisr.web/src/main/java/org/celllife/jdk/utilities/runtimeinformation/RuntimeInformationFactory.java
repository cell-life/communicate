package org.celllife.jdk.utilities.runtimeinformation;

import org.celllife.jdk.utilities.JvmProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RuntimeInformationFactory {
	private static final RuntimeInformation runtimeInformationBean = canGenerateRuntimeInformationBean() ? new RuntimeInformationBean()
			: new RuntimeInformationStub();

	public static RuntimeInformation getRuntimeInformation() {
		return runtimeInformationBean;
	}

	static boolean canGenerateRuntimeInformationBean() {
		try {
			return JvmProperties.isJvmVersion(1.5F);
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(RuntimeInformationFactory.class);
			log.warn("Cannot determine JVM version: " + e.getMessage());
		}
		return false;
	}
}