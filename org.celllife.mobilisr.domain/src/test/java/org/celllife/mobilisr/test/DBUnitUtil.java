package org.celllife.mobilisr.test;

import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import org.dbunit.DatabaseUnitException;
import org.dbunit.database.AmbiguousTableNameException;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.DatabaseSequenceFilter;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.database.QueryDataSet;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.FilteredDataSet;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.filter.AbstractTableFilter;
import org.dbunit.dataset.filter.ITableFilter;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.operation.DatabaseOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Extracts a DBUnit flat XML dataset from a database.
 * 
 * @author Bill Siggelkow
 */
@Component("DBUnitDataExtractor")
public class DBUnitUtil {

	private final class AbstractTableFilterExtension extends
			AbstractTableFilter {
		@Override
		public boolean isValidName(String name) throws DataSetException {
			boolean valid = true;
			if (tablesToIgnore!=null && tablesToIgnore.length > 0){
				String lowerCase = name.toLowerCase();
				for (String ignore : tablesToIgnore) {
					if (lowerCase.startsWith(ignore)){
						return false;
					}
				}
			}
			return valid;
		}
	}

	@Autowired
	private DataSource dataSource;
	private String dataSetName = "dbunit-dataset.xml";
	private List<String> queryList;
	private List<String> tableList;
	private Map<String, Object> dbUnitProperties;
	private Map<String, String> dbUnitFeatures;
	private String schema;

	/**
	 * A regular expression that is used to get the table name from a SQL
	 * 'select' statement. This pattern matches a string that starts with any
	 * characters, followed by the case-insensitive word 'from', followed by a
	 * table name of the form 'foo' or 'schema.foo', followed by any number of
	 * remaining characters.
	 */
	private static final Pattern TABLE_MATCH_PATTERN = Pattern.compile(
			".*\\s+from\\s+(\\w+(\\.\\w+)?).*", Pattern.CASE_INSENSITIVE);
	private static final Logger log = LoggerFactory.getLogger(DBUnitUtil.class);
	private String[] tablesToIgnore;

	/**
	 * The data source of the database from which the data will be extracted.
	 * This property is required.
	 * 
	 * @param ds
	 */
	public void setDataSource(DataSource ds) {
		dataSource = ds;
	}

	/**
	 * Set the schema.
	 * 
	 * @param schema
	 */
	public void setSchema(String schema) {
		this.schema = schema;
	}

	/**
	 * Name of the XML file that will be created. Defaults to
	 * <code>dbunit-dataset.xml</code>.
	 * 
	 * @param name
	 *            file name.
	 */
	public void setDataSetName(String name) {
		dataSetName = name;
	}

	/**
	 * Performs the extraction. If no tables or queries are specified, data from
	 * entire database will be extracted. Otherwise, a partial extraction will
	 * be performed.
	 * 
	 * @throws Exception
	 */
	public void extract() throws Exception {
		IDatabaseConnection connection = null;
		try {
			connection = getConnection();
			log.info("Beginning extraction from '" + connection.toString() + "'.");
			configConnection((DatabaseConnection) connection);
			if (tableList != null || queryList != null) {
				// partial database export
				QueryDataSet partialDataSet = new QueryDataSet(connection);
				addTables(partialDataSet);
				addQueries(partialDataSet);
				FlatXmlDataSet.write(partialDataSet, new FileOutputStream(dataSetName));
			} else {
				// full database export
				ITableFilter filter = new DatabaseSequenceFilter(connection);
				IDataSet fullDataSet = connection.createDataSet();
				IDataSet dataset = new FilteredDataSet(filter, fullDataSet);
				AbstractTableFilter excludeTableFilter = new AbstractTableFilterExtension();
				dataset = new FilteredDataSet(excludeTableFilter, dataset);
				FlatXmlDataSet.write(dataset, new FileOutputStream(dataSetName));
			}
		} finally {
			if (connection != null)
				connection.close();
		}
		log.info("Completed extraction to '" + dataSetName + "'.");
	}

	public void emptyDatabase() throws DataSetException, SQLException, DatabaseUnitException {
		IDatabaseConnection connection = null;
		try {
			connection = getConnection();
			configConnection((DatabaseConnection) connection);
			if (tableList != null || queryList != null) {
				QueryDataSet partialDataSet = new QueryDataSet(connection);
				addTables(partialDataSet);
				addQueries(partialDataSet);
				DatabaseOperation.DELETE_ALL.execute(connection, partialDataSet);
			} else {
				ITableFilter filter = new DatabaseSequenceFilter(connection);
				IDataSet fullDataSet = connection.createDataSet();
				IDataSet dataset = new FilteredDataSet(filter, fullDataSet);
				AbstractTableFilter excludeTableFilter = new AbstractTableFilterExtension();
				dataset = new FilteredDataSet(excludeTableFilter, dataset);
				DatabaseOperation.DELETE_ALL.execute(connection, dataset);
			}
		} finally {
			if (connection != null)
				connection.close();
		}
	}

	/**
	 * Inserts an instance of IDataSet into the database using the CLEAN_INSERT
	 * method.
	 * 
	 * @param dataSet
	 * @return true if insert successful
	 * @throws DatabaseUnitException
	 * @throws SQLException
	 */
	public boolean insertDataSet(IDataSet dataSet) throws DatabaseUnitException, SQLException {
		IDatabaseConnection connection = getConnection();
		try {
			if (dataSet != null) {
				log.debug("Inserting data.");
				DatabaseOperation.CLEAN_INSERT.execute(connection, dataSet);
				return true;
			} else {
				log.info("No data inserted.");
				return false;
			}
		} finally {
			if (connection != null)
				connection.close();
		}
	}

	/**
	 * List of table names to extract data from.
	 * 
	 * @param list
	 *            of table names.
	 */
	public void setTableList(List<String> list) {
		tableList = list;
	}

	/**
	 * List of SQL queries (i.e. 'select' statements) that will be used executed
	 * to retrieve the data to be extracted. If the table being queried is also
	 * specified in the <code>tableList</code> property, the query will be
	 * ignored and all rows will be extracted from that table.
	 * 
	 * @param list
	 *            of SQL queries.
	 */
	public void setQueryList(List<String> list) {
		queryList = list;
	}

	public void setDbUnitFeatures(Map<String, String> dbUnitFeatures) {
		this.dbUnitFeatures = dbUnitFeatures;
	}

	public void setDbUnitProperties(Map<String, Object> dbUnitProperties) {
		this.dbUnitProperties = dbUnitProperties;
	}

	private void configConnection(IDatabaseConnection conn) {
		DatabaseConfig config = conn.getConfig();

		String productName;
		try {
			productName = conn.getConnection().getMetaData().getDatabaseProductName();
			if (productName.equals("H2")) {
				config.setProperty("http://www.dbunit.org/properties/datatypeFactory",
						new org.dbunit.ext.h2.H2DataTypeFactory());
			} else if (productName.equals("MySQL")) {
				config.setProperty("http://www.dbunit.org/properties/datatypeFactory",
						new org.dbunit.ext.mysql.MySqlDataTypeFactory());
			} else if (productName.equals("PostgreSQL")) {
				config.setProperty("http://www.dbunit.org/properties/datatypeFactory",
						new org.dbunit.ext.postgresql.PostgresqlDataTypeFactory());
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		if (dbUnitProperties != null) {
			for (Iterator<Map.Entry<String, Object>> k = dbUnitProperties.entrySet().iterator(); k.hasNext();) {
				Map.Entry<String, Object> entry = k.next();
				String name = entry.getKey();
				if (name.startsWith("http://www.dbunit.org/properties")){
					Object value = entry.getValue();
					config.setProperty(name, value);
				}
			}
		}
		if (dbUnitFeatures != null) {
			for (Iterator<Map.Entry<String, String>> k = dbUnitFeatures.entrySet().iterator(); k.hasNext();) {
				Map.Entry<String, String> entry = k.next();
				String name =  entry.getKey();
				if (name.startsWith("http://www.dbunit.org/features")){
					boolean value = Boolean.valueOf(entry.getValue()).booleanValue();
					config.setProperty(name, value);
				}
			}
		}
		
		// Added by Simon Kelly to ensure Quartz table case is preserved
		config.setProperty("http://www.dbunit.org/features/caseSensitiveTableNames", false);
	}

	private void addTables(QueryDataSet dataSet) throws AmbiguousTableNameException {
		if (tableList == null)
			return;
		for (Iterator<String> k = tableList.iterator(); k.hasNext();) {
			String table = (String) k.next();
			dataSet.addTable(table);
		}
	}

	private void addQueries(QueryDataSet dataSet) throws AmbiguousTableNameException {
		if (queryList == null)
			return;
		for (Iterator<String> k = queryList.iterator(); k.hasNext();) {
			String query = k.next();
			Matcher m = TABLE_MATCH_PATTERN.matcher(query);
			if (!m.matches()) {
				log.warn("Unable to parse query. Ignoring '" + query + "'.");
			} else {
				String table = m.group(1);
				// only add if the table has not been added
				if (tableList != null && tableList.contains(table)) {
					log.warn("Table '" + table + "' already added. Ignoring '" + query + "'.");
				} else {
					dataSet.addTable(table, query);
				}
			}
		}
	}

	private IDatabaseConnection getConnection() throws SQLException, DatabaseUnitException {
		Connection connection = dataSource.getConnection();
		IDatabaseConnection dbUnitConn = new DatabaseConnection(connection, schema);
		configConnection(dbUnitConn);
		return dbUnitConn;
	}
	
	public void setTableToIgnore(String[] tablesToIgnore){
		this.tablesToIgnore = tablesToIgnore;
	}
}
