package org.celllife.communicate.util;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.celllife.mobilisr.domain.MobilisrPermission;
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
public class CreatePerformanceTestData {

	@Autowired
	private DBUnitUtil util;

	@Autowired
	private GeneralDAO dao;
	
	public CreatePerformanceTestData() {
		// Make sure this property is set so that we don't clean out the wrong database
		if (System.getProperty("propertiesOverride") == null){
			throw new RuntimeException("System property propertiesOverride is not set");
		}
	}

	protected void createDefaultData() throws DataSetException, SQLException, DatabaseUnitException {
		util.setTableToIgnore(new String[]{"qrtz", "liquibase"});
		util.emptyDatabase();
		
		createAdminOrgAndUser();
	}
	
	protected void createAdminOrgAndUser() {
		Organization organization = new Organization();
		organization.setBalance(1000000);
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
		
		Transaction transaction = new Transaction(1000000, 0, "test setup", "test setup", "initial balance", user, user.getOrganization());
		dao.save(transaction);
	}

	@Test
	public void extract() throws Exception {
		createDefaultData();
		
		util.setDataSetName("src/test/resources/dbunit-dataset.xml");
		util.extract();
	}
}
