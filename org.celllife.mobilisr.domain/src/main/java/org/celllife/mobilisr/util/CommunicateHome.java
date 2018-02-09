package org.celllife.mobilisr.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.celllife.mobilisr.exception.MobilisrRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommunicateHome {
	
	public enum Folders {
		forDownload,
		attachments;
	}
	
	private static final Logger log = LoggerFactory.getLogger(CommunicateHome.class);

	private static final String COMMUNICATE_HOME_ENV = "COMMUNICATE_HOME";
	private static final String COMMUNICATE_PROPERTIES = "communicate.properties";

	private static String home;

	private static Properties properties;
	
	public static void onStartup(){
		home = System.getenv(COMMUNICATE_HOME_ENV);
		if (home == null) {
			home = System.getProperty(COMMUNICATE_HOME_ENV);
		}
		
		if (home == null){
			home = "unknown";
			throw new MobilisrRuntimeException("Unable to find COMMUNICATE_HOME property");
		}
		
		File homeFolder = getHomeFolder();
		log.info("COMMUNICATE_HOME set to: {}", homeFolder.getAbsolutePath());
		if (!homeFolder.exists()){
			throw new MobilisrRuntimeException("COMMUNICATE_HOME folder does not exist:" 
					+ homeFolder.getAbsolutePath());
		}
		
		if (!homeFolder.canRead()){
			throw new MobilisrRuntimeException("COMMUNICATE_HOME folder is not readable:" 
					+ homeFolder.getAbsolutePath());
		}
		
		if (!homeFolder.canWrite()){
			throw new MobilisrRuntimeException("COMMUNICATE_HOME folder is not writable:" 
					+ homeFolder.getAbsolutePath());
		}
		
		try {
			for (Folders folder : Folders.values()) {
				File subFolder = getSubFolder(folder);
				FileUtils.forceMkdir(subFolder);
			}
		} catch (IOException e) {
			throw new MobilisrRuntimeException("Error initialising COMMUNICATE_HOME", e);
		}
	}

	public static String getHomeProperty() {
		if (home == null){
			onStartup();
		}
		return home;
	}
	
	public static File getHomeFolder() {
		File home = new File(getHomeProperty());
		return home;
	}
	
	public static File getAttachmentsFolder(){
		return getSubFolder(Folders.attachments);
	}
	
	public static File getDownloadsFolder(){
		return getSubFolder(Folders.forDownload);
	}
	
	static File getSubFolder(Folders subFolder) {
		File home = getHomeFolder();
		File sub = new File(home.getAbsolutePath() + File.separator + subFolder.name());
		return sub;
	}

	public static File getPropertiesFile() {
		File propsFile = new File(getHomeProperty() + File.separator
				+ COMMUNICATE_PROPERTIES);
		return propsFile;
	}

	public static Properties getProperties() {
		if (properties == null) {
			properties = new Properties();
			try {
				properties.load(new FileInputStream(getPropertiesFile()));
			} catch (FileNotFoundException e) {
				log.warn("Unable to find properties file", e);
			} catch (IOException e) {
				log.warn("Error reading properties file", e);
			}
		}
		return properties;
	}
	
}
