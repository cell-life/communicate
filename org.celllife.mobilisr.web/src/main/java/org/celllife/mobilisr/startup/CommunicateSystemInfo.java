package org.celllife.mobilisr.startup;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;

import javax.servlet.ServletContext;

import org.celllife.jdk.utilities.runtimeinformation.MemoryInformation;

public class CommunicateSystemInfo {

	private static final String STRANGELY_UNKNOWN = "unknown??";
	private static final long MEGABYTE = 1048576L;
	private static final List<String> PATH_RELATED_KEYS;
	private static final Set<String> IGNORE_THESE_KEYS;
	private final FormattedLogMsg logMsg;

	public CommunicateSystemInfo(FormattedLogMsg logMsg) {
		this.logMsg = logMsg;
	}

	public void obtainBasicInfo(ServletContext context) {
		SystemInfoUtils systemInfoUtils = new SystemInfoUtilsImpl();

		this.logMsg.outputHeader("Environment");

		if (context != null) {
			this.logMsg.outputProperty(
					"Application Server",
					context.getServerInfo() + " - Servlet API "
							+ context.getMajorVersion() + "."
							+ context.getMinorVersion());
		}

		this.logMsg.outputProperty("Java Version",
				System.getProperty("java.version", STRANGELY_UNKNOWN) + " - "
						+ System.getProperty("java.vendor", STRANGELY_UNKNOWN));

		this.logMsg.outputProperty("Current Working Directory",
				System.getProperty("user.dir", STRANGELY_UNKNOWN));

		Runtime rt = Runtime.getRuntime();
		long maxMemory = rt.maxMemory() / MEGABYTE;
		long totalMemory = rt.totalMemory() / MEGABYTE;
		long freeMemory = rt.freeMemory() / MEGABYTE;
		long usedMemory = totalMemory - freeMemory;

		this.logMsg
				.outputProperty("Maximum Allowable Memory", maxMemory + "MB");
		this.logMsg.outputProperty("Total Memory", totalMemory + "MB");
		this.logMsg.outputProperty("Free Memory", freeMemory + "MB");
		this.logMsg.outputProperty("Used Memory", usedMemory + "MB");

		for (MemoryInformation memory : systemInfoUtils
				.getMemoryPoolInformation()) {
			this.logMsg.outputProperty("Memory Pool: " + memory.getName(),
					memory.toString());
		}
		this.logMsg.outputProperty("JVM Input Arguments",
				systemInfoUtils.getJvmInputArguments());
	}

	public void obtainSystemProperties() {
		Properties sysProps = System.getProperties();

		Map<Object, Object> properties = new TreeMap<Object, Object>(sysProps);
		properties.keySet().removeAll(IGNORE_THESE_KEYS);
		this.logMsg.outputHeader("Java System Properties");
		for (Map.Entry<Object, Object> entry : properties.entrySet()) {
			this.logMsg.outputProperty((String) entry.getKey(),
					(String) entry.getValue(), ",");
		}
	}
	
	public void obtainEnvironmentVariables() {
		Map<String, String> sysEnv = System.getenv();

		this.logMsg.outputHeader("System Evnironment Variables");
		for (Entry<String, String> entry : sysEnv.entrySet()) {
			this.logMsg.outputProperty(entry.getKey(),
					entry.getValue(), ",");
		}
	}

	public void obtainSystemPathProperties() {
		Properties sysProps = System.getProperties();
		this.logMsg.outputHeader("Java Class Paths");
		for (String key : PATH_RELATED_KEYS) {
			String value = sysProps.getProperty(key, null);
			if (value != null) {
				this.logMsg.outputProperty(key, value, File.pathSeparator);
				this.logMsg.add("");
			}
		}
	}

	static {
		CollectionBuilder<String> pathRelatedKeys = CollectionBuilder
				.newBuilder();
		pathRelatedKeys.add("sun.boot.class.path");
		pathRelatedKeys.add("com.ibm.oti.vm.bootstrap.library.path");
		pathRelatedKeys.add("java.library.path");
		pathRelatedKeys.add("java.endorsed.dirs");
		pathRelatedKeys.add("java.ext.dirs");
		pathRelatedKeys.add("java.class.path");
		PATH_RELATED_KEYS = pathRelatedKeys.asList();

		CollectionBuilder<String> ignoreTheseKeys = CollectionBuilder
				.newBuilder();
		ignoreTheseKeys.addAll(PATH_RELATED_KEYS);
		ignoreTheseKeys.add("line.separator");
		ignoreTheseKeys.add("path.separator");
		ignoreTheseKeys.add("file.separator");
		IGNORE_THESE_KEYS = ignoreTheseKeys.asSet();
	}
}