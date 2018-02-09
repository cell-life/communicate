package org.celllife.mobilisr.util;

import java.io.IOException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.core.io.FileSystemResource;

/**
 * Extension of PropertyPlaceholderConfigurer to remove prefix from properties.
 * This allows multiple properties to exist in the same file with different
 * prefixes. The properties that are used can then be configured in the Spring
 * context.
 *
 * @author simon@cell-life.org
 */
public class MobilisrPropertyPlaceholderConfigurer extends PropertyPlaceholderConfigurer  {

	private static final Logger log = LoggerFactory.getLogger(MobilisrPropertyPlaceholderConfigurer.class);

	private String prefix;

	private Properties properties;

	public void setPrefix(String prefix){
		this.prefix = prefix;
	}

	@Override
	protected void loadProperties(Properties props) throws IOException {
		setIgnoreResourceNotFound(true);
		super.loadProperties(props);

		loadFromSystemProperty(props);
	}

	private void loadFromSystemProperty(Properties props) throws IOException {
		String propOverride = System.getProperty("propertiesOverride");
		if (propOverride != null) {
			FileSystemResource location = new FileSystemResource(propOverride);
			log.info("Attempting to override properties from: " + location.getPath() );
			setLocation(location);
			super.loadProperties(props);
			String liveDbUrl = props.getProperty("live.jdbc.url");
			log.info("Property 'live.jdbc.url': " + liveDbUrl);
		}
		else
			log.info("No properties override found.");
	}

	/**
	 * If the property starts with the prefix, remove the prefix and any leading
	 * '.' and re-insert the property into the property list.
	 */
	@Override
	protected void convertProperties(Properties props) {
		if (prefix == null || prefix.isEmpty()){
			return;
		}

		Set<Object> keySet = new HashSet<Object>();
		keySet.addAll(props.keySet());
		for (Object propO : keySet) {
			String prop = (String) propO;
			if (prop.startsWith(prefix)){
				String newProp = prop.replaceFirst(prefix, "");
				if (newProp.startsWith(".")){
					newProp = newProp.substring(1);
				}
				log.debug("Replacing property '" + prop + "' with '" + newProp + "'");
				props.put(newProp, props.get(prop));
				props.remove(prop);
			}
		}
		properties = new Properties();
		properties.putAll(props);
		
		if (log.isTraceEnabled()){
			for (Object key : properties.keySet()) {
				log.trace("Property: {} - {}", key, properties.get(key));
			}
		}
	}

	public Properties getProperties() {
		return properties;
	}
}
