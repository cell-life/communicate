package org.celllife.mobilisr.test;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.celllife.mobilisr.dao.api.MobilisrGeneralDAO;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ReplacementDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:applicationContext.xml" })
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public abstract class AbstractDBTest extends BaseTest {

	private static final String DATASET_FILENAME = "dbunit-dataset.xml";

	private static Map<String, IDataSet> cachedDatasets = new HashMap<String, IDataSet>();
	
	@Autowired
	private DataSource dataSource;
	
	@Autowired
	private DBUnitUtil util;
	
	@Autowired
	private MobilisrGeneralDAO generalDao;
	
	private boolean insertData = true;
	private boolean autoLogin = true;

	@Before
	public void insertBefore() throws Exception {
		if (insertData) {
			cleanQuartz();
			executeDataSet(DATASET_FILENAME);
		}
	}
	
	private void cleanQuartz() throws SQLException {
		Connection connection = null;
		try {
			connection = dataSource.getConnection();
			Statement statement = connection.createStatement();
			statement.execute("delete from QRTZ_TRIGGERS where "
					+ "TRIGGER_GROUP not like 'background%' "
					+ "and TRIGGER_GROUP not like 'campaign%'");
			statement.execute("delete from QRTZ_CRON_TRIGGERS where "
					+ "TRIGGER_GROUP not like 'background%' "
					+ "and TRIGGER_GROUP not like 'campaign%'");
			statement.execute("delete from QRTZ_SIMPLE_TRIGGERS where "
					+ "TRIGGER_GROUP not like 'background%' "
					+ "and TRIGGER_GROUP not like 'campaign%'");
		} finally {
			if (connection != null) {
				connection.close();
			}
		}
	}

	/**
	 * Disables inserting the test data into the database
	 */
	public void disableInsertData() {
		this.insertData = false;
	}
	
	public MobilisrGeneralDAO getGeneralDao() {
		return generalDao;
	}
	
	@Before
	public void autoLogin(){
		if(autoLogin) {
			login("admin", "admin");
		}
	}

	/**
	 * Disables auto login of the admin user
	 */
	public void disableAutoLogin(){
		this.autoLogin = false;
	}

	public void executeDataSet(String datasetFilename) throws Exception {

		IDataSet xmlDataSetToRun = cachedDatasets.get(datasetFilename);

		if (xmlDataSetToRun == null) {
			InputStream stream = AbstractDBTest.class.getClassLoader()
					.getResourceAsStream(DATASET_FILENAME);
			ReplacementDataSet replacementDataSet = new ReplacementDataSet(
					new FlatXmlDataSetBuilder().build(stream));
			replacementDataSet.addReplacementObject("[NULL]", null);
			xmlDataSetToRun = replacementDataSet;
		}

		cachedDatasets.put(datasetFilename, xmlDataSetToRun);
		util.setTableToIgnore(new String[]{"qrtz", "liquibase_changelog"});
		util.insertDataSet(xmlDataSetToRun);
	}

	protected void login(String username, String password) {
		SecurityContextHolder.getContext().setAuthentication(
				new UsernamePasswordAuthenticationToken(username, password));
	}
}
