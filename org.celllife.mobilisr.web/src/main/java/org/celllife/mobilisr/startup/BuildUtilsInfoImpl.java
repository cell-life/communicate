package org.celllife.mobilisr.startup;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.celllife.mobilisr.servlet.ServletContextProvider;

public class BuildUtilsInfoImpl implements BuildUtilsInfo {

	private static String version;

	@Override
	public String getVersion() {
		if (version == null) {
			readVersion();
		}
		return version;
	}

	private void readVersion() {
		BuildUtilsInfoImpl.version = "Unknown version";
		try {
			Properties prop = new Properties();
			InputStream stream = ServletContextProvider.getServletContext()
					.getResourceAsStream("/META-INF/MANIFEST.MF");
			if (stream != null) {
				prop.load(stream);
				String version = prop.getProperty("Implementation-Build");
				if (version != null) {
					BuildUtilsInfoImpl.version = version;
				}
			}
		} catch (IOException e) {
		}
	}
}
