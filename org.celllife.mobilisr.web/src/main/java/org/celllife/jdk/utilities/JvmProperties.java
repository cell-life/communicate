package org.celllife.jdk.utilities;

import org.celllife.jdk.utilities.exception.InvalidVersionException;

public class JvmProperties {
	public static float getJvmVersion() throws InvalidVersionException {
		String property = System.getProperty("java.specification.version");
		try {
			return Float.valueOf(property).floatValue();
		} catch (Exception e) {
			throw new InvalidVersionException("Invalid JVM version: '"
					+ property + "'. " + e.getMessage());
		}
	}

	public static boolean isJvmVersion(float versionNumber)
			throws InvalidVersionException {
		return getJvmVersion() >= versionNumber;
	}
}