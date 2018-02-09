package org.celllife.mobilisr.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.Vector;

import javax.sql.DataSource;

import liquibase.FileOpener;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.exception.LiquibaseException;

import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;


public class SpringLiquibaseUpdater implements ResourceLoaderAware {

	public void init() throws Exception {
		
		Liquibase liquibase = new Liquibase(changeLog, new SpringFileOpener(
				changeLog, getResourceLoader()), getDatabase());
		try {
			liquibase.update(contexts);
		} catch (LiquibaseException e) {
			e.printStackTrace();
			throw new RuntimeException(
					"Could not update database through Liquibase", e);
		}
	}

	private DataSource dataSource;

	private String changeLog;

	private String contexts;

	private ResourceLoader resourceLoader;

	private Database getDatabase() {
		try {
			Database databaseImplementation = DatabaseFactory.getInstance()
					.findCorrectDatabaseImplementation(
							dataSource.getConnection());
			databaseImplementation
					.setDatabaseChangeLogTableName("liquibase_changelog");
			databaseImplementation
					.setDatabaseChangeLogLockTableName("liquibase_lock");
			return databaseImplementation;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Error getting database", e);
		}
	}

	public DataSource getDataSource() {
		return dataSource;
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public String getChangeLog() {
		return changeLog;
	}

	public void setChangeLog(String changeLog) {
		this.changeLog = changeLog;
	}

	public String getContexts() {
		return contexts;
	}

	public void setContexts(String contexts) {
		this.contexts = contexts;
	}

	public ResourceLoader getResourceLoader() {
		return resourceLoader;
	}

	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}

	static class SpringFileOpener implements FileOpener {
		private String parentFile;

		private ResourceLoader resourceLoader;

		public SpringFileOpener(String parentFile, ResourceLoader resourceLoader) {
			this.parentFile = parentFile;
			this.resourceLoader = resourceLoader;
		}

		public InputStream getResourceAsStream(String file) throws IOException {
			Resource resource = getResource(file);

			return resource.getInputStream();
		}

		public Enumeration<URL> getResources(String packageName)
				throws IOException {
			Vector<URL> tmp = new Vector<URL>();
			tmp.add(getResource(packageName).getURL());
			return tmp.elements();
		}

		public Resource getResource(String file) {
			return getResourceLoader().getResource(adjustClasspath(file));
		}

		private String adjustClasspath(String file) {
			return isClasspathPrefixPresent(parentFile)
					&& !isClasspathPrefixPresent(file) ? ResourceLoader.CLASSPATH_URL_PREFIX
					+ file
					: file;
		}

		public boolean isClasspathPrefixPresent(String file) {
			return file.startsWith(ResourceLoader.CLASSPATH_URL_PREFIX);
		}

		public ClassLoader toClassLoader() {
			return getResourceLoader().getClassLoader();
		}

		public ResourceLoader getResourceLoader() {
			return resourceLoader;
		}

		public void setResourceLoader(ResourceLoader resourceLoader) {
			this.resourceLoader = resourceLoader;
		}
	}
}
