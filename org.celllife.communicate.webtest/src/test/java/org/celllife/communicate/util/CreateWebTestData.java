package org.celllife.communicate.util;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.celllife.mobilisr.constants.ChannelType;
import org.celllife.mobilisr.domain.Channel;
import org.celllife.mobilisr.domain.MobilisrPermission;
import org.celllife.mobilisr.domain.NumberInfo;
import org.celllife.mobilisr.domain.Organization;
import org.celllife.mobilisr.domain.Role;
import org.celllife.mobilisr.domain.Transaction;
import org.celllife.mobilisr.domain.User;
import org.celllife.mobilisr.test.DBUnitUtil;
import org.dbunit.DatabaseUnitException;
import org.dbunit.dataset.DataSetException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.trg.dao.hibernate.GeneralDAO;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:mobilisr-applicationContext.xml" })
public class CreateWebTestData {

	@Autowired
	private DBUnitUtil util;

	@Autowired
	private GeneralDAO dao;

	public CreateWebTestData() {
		// Make sure this property is set so that we don't clean out the wrong database
		if (System.getProperty("propertiesOverride") == null){
			throw new RuntimeException("System property propertiesOverride is not set");
		}
	}

	protected void createDefaultData() throws DataSetException, SQLException, DatabaseUnitException {
		util.setTableToIgnore(new String[]{"qrtz", "liquibase"});
		util.emptyDatabase();

		createOutgoingChannel();
		createIncomingChannel();
		createAdminOrgAndUser();
	}

	protected void createOutgoingChannel() {
		Channel chOut = new Channel("Simulation outgoing", ChannelType.OUT, "simOutChannel",
				 "0");
		dao.save(chOut);
	}
	
	protected void createIncomingChannel() {
		Channel chIn = new Channel("Simulation incoming", ChannelType.IN, "simInChannel",
				"12345");
		dao.save(chIn);
	}

	protected void createAdminOrgAndUser() {
		Organization organization = new Organization();
		organization.setBalance(10000);
		organization.setName("Admin organisation");
		dao.save(organization);

		Role role = new Role();
		role.setName("ADMINISTRATOR");
		role.setPermissions(MobilisrPermission.ROLE_ADMIN.toString());
		dao.save(role);

		User user = new User();
		user.setFirstName("admin");
		user.setLastName("admin");
		user.setUserName("admin");
		// admin
		user.setPassword("5074d3501fe15f27170ee15e13b7c864130adc9c");
		// admin
		user.setSalt("170b8223e596c12d314f9fa80cc54f1673791484");

		user.setEmailAddress("admin@mobilisr.org");
		user.setMsisdn("27722545698");
		user.setOrganization(organization);

		List<Role> roleList = new ArrayList<Role>();
		roleList.add(role);
		user.setRoles(roleList);
		dao.save(user);

		Transaction transaction = new Transaction(10000, 0, "test setup", "test setup", "initial balance", user, user.getOrganization());
		dao.save(transaction);

		dao.save(new NumberInfo("SA", "27", "^27[1-9][0-9]{8}$"));
	}

	@Test
	public void extract() throws Exception {
		createDefaultData();

		util.setDataSetName("src/test/resources/dbunit-dataset.xml");
		util.extract();
	}
}
