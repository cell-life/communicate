package org.celllife.mobilisr.test;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.celllife.mobilisr.constants.CampaignStatus;
import org.celllife.mobilisr.constants.CampaignType;
import org.celllife.mobilisr.constants.ChannelType;
import org.celllife.mobilisr.dao.api.MobilisrGeneralDAO;
import org.celllife.mobilisr.domain.Campaign;
import org.celllife.mobilisr.domain.CampaignContact;
import org.celllife.mobilisr.domain.Channel;
import org.celllife.mobilisr.domain.Contact;
import org.celllife.mobilisr.domain.ContactGroup;
import org.celllife.mobilisr.domain.ContactMsgTime;
import org.celllife.mobilisr.domain.Contact_ContactGroup;
import org.celllife.mobilisr.domain.MobilisrPermission;
import org.celllife.mobilisr.domain.Organization;
import org.celllife.mobilisr.domain.Role;
import org.celllife.mobilisr.domain.Transaction;
import org.celllife.mobilisr.domain.User;
import org.celllife.mobilisr.util.MobilisrSecurityUtility;
import org.dbunit.DatabaseUnitException;
import org.dbunit.dataset.DataSetException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:applicationContext.xml" })
public class CreateDomainTestData {

	@Autowired
	private DBUnitUtil util;

	@Autowired
	private MobilisrGeneralDAO dao;

	protected void createDefaultData() throws DataSetException, SQLException, DatabaseUnitException {
		util.setTableToIgnore(new String[]{"qrtz", "liquibase"});
		util.emptyDatabase();
		
		createAdminOrgAndUser();
		
		Organization organization = null;
		
		Channel channel = new Channel("Test Channel", ChannelType.OUT, "Integrat", "0");
		channel.setVoided(true);
		dao.save(channel);
		channel = new Channel("Mock channel",  ChannelType.OUT, "mockOutChannel", "1");
		dao.save(channel);
		
		for (int i = 0; i < 10; i++) {
			
			Role role = new Role();
			role.setName("role" + i);
			role.setPermissions(MobilisrPermission.ORGANISATIONS_CREDIT_BALANCE.toString());
			dao.save(role);
			
			if (0 == i || 5 == i) {
				
				organization = new Organization();
				organization.setBalance(10000);
				organization.setName("test org " + i);
				dao.save(organization);
				
				User user = new User();
				user.setFirstName("firstname " + i);
				user.setLastName("lastname " + (9-i));
				user.setUserName("temp_username " + i);
				user.setPassword("testpassword");
				user.setEmailAddress("t"+i+"@t.com");
				user.setMsisdn("3242422" + i);
				user.setSalt(MobilisrSecurityUtility.getRandomToken());
				user.setOrganization(organization);
				List<Role> roleList = new ArrayList<Role>();
				roleList.add(role);
				user.setRoles(roleList);
				dao.save(user);
				
				Transaction transaction = new Transaction(10000, 0, "test setup", "test setup", "initial balance", user, user.getOrganization());
				dao.save(transaction);
				
				Campaign campaign = new Campaign();
				campaign.setName("Program " + i);
				campaign.setStatus(CampaignStatus.INACTIVE);
				campaign.setType(CampaignType.FIXED);
				campaign.setOrganization(user.getOrganization());
				dao.save(campaign);
				
				for( int a = 0 ; a < 20 ; a++ ){
					Contact contact = new Contact("MSISDN " + ((i*20)+a), "MTN", organization.getName() + a, "contact lName" + (i+a));
					contact.setOrganization(organization);
					dao.save(contact);
				
					CampaignContact campaignContact = new CampaignContact(campaign, contact);
					campaignContact.setContact(contact);
					dao.save(campaignContact);
					
					ContactMsgTime contactMsgTime = new ContactMsgTime(new Date(), 1, campaignContact, campaign);
					dao.save(contactMsgTime);
				}
				
				for( int a = 0 ; a < 10 ; a++){
					ContactGroup contactGroup = new ContactGroup("Test Group " + i + "" + a , null);
					contactGroup.setOrganization(organization);
					dao.save(contactGroup);
				}
				
			}

			User user = new User();
			user.setFirstName("firstname " + i);
			user.setLastName("lastname " + (9-i));
			user.setUserName("username " + i);
			user.setPassword("testpassword");
			user.setEmailAddress("fsfs"+i+"@dfsfs.com");
			user.setMsisdn("422224242" + i);
			user.setSalt(MobilisrSecurityUtility.getRandomToken());
			user.setOrganization(organization);
			List<Role> roleList = new ArrayList<Role>();
			roleList.add(role);
			user.setRoles(roleList);
			dao.save(user);
			
		}
		
		Contact c1 = dao.findAll(Contact.class).get(0);
		ContactGroup cg1 = dao.findAll(ContactGroup.class).get(0);
		dao.save(new Contact_ContactGroup(c1, cg1));
		
		c1 = dao.findAll(Contact.class).get(0);
		cg1 = dao.findAll(ContactGroup.class).get(1);
		dao.save(new Contact_ContactGroup(c1, cg1));
		
		c1 = dao.findAll(Contact.class).get(1);
		cg1 = dao.findAll(ContactGroup.class).get(1);
		dao.save(new Contact_ContactGroup(c1, cg1));
		
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
	}

	@Test
	public void extract() throws Exception {
		createDefaultData();
		
		util.setDataSetName("src/test/resources/dbunit-dataset.xml");
		util.extract();
	}
}
