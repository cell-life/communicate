package org.celllife.mobilisr.liquibase;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import liquibase.FileOpener;
import liquibase.change.custom.CustomSqlChange;
import liquibase.change.custom.CustomSqlRollback;
import liquibase.database.Database;
import liquibase.database.sql.DeleteStatement;
import liquibase.database.sql.InsertStatement;
import liquibase.database.sql.RawSqlStatement;
import liquibase.database.sql.SqlStatement;
import liquibase.exception.CustomChangeException;
import liquibase.exception.InvalidChangeDefinitionException;
import liquibase.exception.RollbackImpossibleException;
import liquibase.exception.SetupException;
import liquibase.exception.UnsupportedChangeException;

import org.celllife.mobilisr.util.CommunicateHome;

public class MoveChannelSettingsToDatabase implements CustomSqlChange, CustomSqlRollback {

	/*
	 * Integrat HTTP settings
	 */
	public static final String INTEGRAT_HTTP_USERNAME = "integrat.mt.username";
	public static final String INTEGRAT_HTTP_PASSWORD = "integrat.mt.password";
	public static final String INTEGRAT_HTTP_URL = "integrat.mt.posturl";
	public static final String INTEGRAT_HTTP_TAG = "integrat.mt.tag";
	public static final String INTEGRAT_HTTP_SERVICECODE = "integrat.mt.servicecode";

	/*
	 * Tellfree HTTP settings
	 */
	public static final String TELFREE_HTTP_URL = "tellfree.mt.posturl";
	public static final String TELFREE_HTTP_USERNAME = "tellfree.mt.username";
	public static final String TELFREE_HTTP_PASSWORD = "tellfree.mt.password";

	/*
	 * Tellfree SMPP settings
	 */
	public static final String TELFREE_SMPP_HOST = "tellfree.smpp.host";
	public static final String TELFREE_SMPP_PORT = "tellfree.smpp.port";
	public static final String TELFREE_SMPP_USERNAME = "tellfree.smpp.username";
	public static final String TELFREE_SMPP_PASSWORD = "tellfree.smpp.password";
	public static final String TELFREE_SMPP_SERVICE_TYPE = "tellfree.smpp.service_type";
	public static final String TELFREE_SMPP_SYSTEM_TYPE = "tellfree.smpp.system_type";
	public static final String TELFREE_SMPP_SOURCE_ADDRESS = "tellfree.smpp.source_address";

	private Properties properties;

	@Override
	public String getConfirmationMessage() {
		return "Channel settings successfully moved to database";
	}

	@Override
	public void setFileOpener(FileOpener fileOpener) {
	}

	@Override
	public void setUp() throws SetupException {
		properties = CommunicateHome.getProperties();
	}

	@Override
	public void validate(Database arg0) throws InvalidChangeDefinitionException {
		// do nothing
	}

	@Override
	public SqlStatement[] generateStatements(Database arg0)
			throws UnsupportedChangeException, CustomChangeException {

		List<SqlStatement> statements = new ArrayList<SqlStatement>();
		statements.add(getIntegratHttpStatementInsert());
		statements.add(getTelfreeHttpStatementInsert());
		statements.add(getTelfreeSmppStatementInsert());
		statements.add(new RawSqlStatement("update channel set config_id = " +
				"(select id from channelconfig where channel.handler = channelconfig.handler)"));
		statements.add(new RawSqlStatement("update channel set handler = 'in-http'" +
				" where handler is null"));

		return statements.toArray(new SqlStatement[] {});
	}

	private SqlStatement getTelfreeSmppStatementInsert() {
		String config = "- !string {name: host, value: '%s'}\n"
			+ "- !integer {name: port, value: %s}\n"
			+ "- !string {name: username, value: '%s'}\n"
			+ "- !string {name: password, value: '%s'}\n"
			+ "- !string {name: source_address, value: '%s'}\n"
			+ "- !string {name: system_type, value: '%s'}\n"
			+ "- !string {name: service_type, value: '%s'}";
		
		String format = String.format(config, properties.getProperty(TELFREE_SMPP_HOST),
				properties.getProperty(TELFREE_SMPP_PORT),
				properties.getProperty(TELFREE_SMPP_USERNAME),
				properties.getProperty(TELFREE_SMPP_PASSWORD),
				properties.getProperty(TELFREE_SMPP_SOURCE_ADDRESS),
				properties.getProperty(TELFREE_SMPP_SYSTEM_TYPE),
				properties.getProperty(TELFREE_SMPP_SERVICE_TYPE));

		return getInsertStatement("Telfree SMPP", "telfreeSmpp", format);
	}

	private SqlStatement getIntegratHttpStatementInsert() {
		String config = "- !string {name: url, value: '%s'}\n"
				+ "- !string {name: password, value: '%s'}\n"
				+ "- !string {name: username, value: '%s'}\n"
				+ "- !string {name: serviceCode, value: '%s'}\n"
				+ "- !string {name: tag, value: '%s'}\n";

		String format = String.format(config, properties.getProperty(INTEGRAT_HTTP_URL),
				properties.getProperty(INTEGRAT_HTTP_PASSWORD),
				properties.getProperty(INTEGRAT_HTTP_USERNAME),
				properties.getProperty(INTEGRAT_HTTP_SERVICECODE),
				properties.getProperty(INTEGRAT_HTTP_TAG));
		
		return getInsertStatement("Integrat HTTP", "integratOutChannel", format);
	}

	private SqlStatement getTelfreeHttpStatementInsert() {
		String config = "- !string {name: url, value: '%s'}\n"
			+ "- !string {name: password, value: '%s'}\n"
			+ "- !string {name: username, value: '%s'}\n";
		
		String format = String.format(config, properties.getProperty(TELFREE_HTTP_URL),
				properties.getProperty(TELFREE_HTTP_PASSWORD),
				properties.getProperty(TELFREE_HTTP_USERNAME));
		
		return getInsertStatement("Telfree HTTP", "telfreeOutChannel", format);
	}
	
	private InsertStatement getInsertStatement(String name, String handler, String properties) {
		InsertStatement statement = new InsertStatement(null, "channelconfig")
				.addColumnValue("handler", handler)
				.addColumnValue("name", name)
				.addColumnValue("properties", properties)
				.addColumnValue("voided", false);
		return statement;
	}

	@Override
	public SqlStatement[] generateRollbackStatements(Database arg0)
			throws CustomChangeException, UnsupportedChangeException,
			RollbackImpossibleException {
		return new SqlStatement[] {
				new DeleteStatement(null, "channelconfig")
		};
	}

}
