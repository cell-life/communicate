package org.celllife.mobilisr.service.qrtz.beans;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import junit.framework.Assert;

import org.celllife.mobilisr.constants.CampaignStatus;
import org.celllife.mobilisr.constants.CampaignType;
import org.celllife.mobilisr.dao.api.CampaignDAO;
import org.celllife.mobilisr.dao.api.OrganizationDAO;
import org.celllife.mobilisr.dao.api.SmsLogDAO;
import org.celllife.mobilisr.dao.api.UserDAO;
import org.celllife.mobilisr.domain.Campaign;
import org.celllife.mobilisr.domain.CampaignContact;
import org.celllife.mobilisr.domain.CampaignMessage;
import org.celllife.mobilisr.domain.Contact;
import org.celllife.mobilisr.domain.Organization;
import org.celllife.mobilisr.domain.SmsLog;
import org.celllife.mobilisr.domain.User;
import org.celllife.mobilisr.service.constants.SettingsEnum;
import org.celllife.mobilisr.test.AbstractServiceTest;
import org.celllife.pconfig.model.IntegerParameter;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.trg.search.Search;

public class FixedCampaignJobIntegrationTest extends AbstractServiceTest {

	@Autowired
	private CampaignDAO campaignDAO;
	
	@Autowired
	private SmsLogDAO smsLogDAO;
	
	@Autowired
	private UserDAO userDAO;

	@Autowired
	private OrganizationDAO organizationDAO;
	
	@Autowired
	private FixedCampaignJob runner;

	private User user;

	private int startingBalance;

	@Before
	public void init(){
		user = getUser();
		startingBalance = user.getOrganization().getBalance();
	}
	
	@Test(timeout=30000)
	public void testFixedCampaignJob() throws Exception{
		Campaign campaign = createCampaign();
		List<CampaignContact> contacts = createContactsForCampaign(20, campaign);
		
		runner.sendMessagesForCampaign(campaign.getId(), user.getId(), null);
		
		waitForCampaignCompletion(campaign);
		
		assertSmsLogs(contacts.size(), campaign.getIdentifierString());
		assertOrgBalance(contacts.size());
	}
	
	@Test(timeout=30000)
	public void testFixedCampaignJob_contactsRemoved() throws Exception{
		Campaign campaign = createCampaign();
		List<CampaignContact> contacts = createContactsForCampaign(20, campaign);
		for (int i = 0; i < 5; i++) {
			contacts.get(i).setEndDate(new Date());
			getGeneralDao().save(contacts.get(i));
		}
		
		runner.sendMessagesForCampaign(campaign.getId(), user.getId(), null);
		
		waitForCampaignCompletion(campaign);
		
		assertSmsLogs(contacts.size()-5, campaign.getIdentifierString());
		assertOrgBalance(contacts.size()-5);
	}
	
	@Test(timeout=30000)
	public void testFixedCampaignJob_multiBatch() throws Exception{
		Campaign campaign = createCampaign();
		List<CampaignContact> contacts = createContactsForCampaign(20, campaign);

		((IntegerParameter)SettingsEnum.MESSAGE_BATCH_SIZE.getConfig()).setDefaultValue(5);
		
		runner.sendMessagesForCampaign(campaign.getId(), user.getId(), null);
		
		waitForCampaignCompletion(campaign);
		
		assertSmsLogs(contacts.size(), campaign.getIdentifierString());
		assertOrgBalance(contacts.size());
	}
	
	private User getUser() {
		Search search = new Search(User.class);
		search.addFilterEqual(User.PROP_FIRST_NAME, "firstname 8");
		User user = userDAO.searchUnique(search);
		return user;
	}
	
	private Campaign createCampaign(){
		Campaign campaign = new Campaign();
		campaign.setName("Test campaign");
		campaign.setStatus(CampaignStatus.INACTIVE);
		campaign.setOrganization(user.getOrganization());
		campaign.setType(CampaignType.FIXED);

		getGeneralDao().save(campaign);
		Assert.assertNotNull(campaign.getId());
		
		CampaignMessage campaignMessage = new CampaignMessage("Test message",
				new Date(), new Date(), 0, campaign);
		getGeneralDao().save(campaignMessage);
		return campaign;
	}
	
	private List<Contact> getContacts(int numOfContacts){
		List<Contact> contactList = new ArrayList<Contact>();
		
		for(int i = 0 ; i < numOfContacts ; i++){
			Contact contact = new Contact("2778999888" + i, user.getOrganization());
			getGeneralDao().save(contact);
			
			contactList.add(contact);
		}
		
		return contactList;
	}
	
	private List<CampaignContact> createContactsForCampaign(int numOfContacts, Campaign campaign){
		List<Contact> contacts = getContacts(numOfContacts);
		List<CampaignContact> contactList = new ArrayList<CampaignContact>();
		for (Contact contact : contacts) {
			CampaignContact campaignContact = new CampaignContact(campaign, contact);
			getGeneralDao().save(campaignContact);
			contactList.add(campaignContact);
		}				
		return contactList;
	}
	
	private void assertSmsLogs(int numExpected, String createdFor){
		List<SmsLog> smsWaspSendList = smsLogDAO.searchByPropertyEqual(SmsLog.PROP_CREATEDFOR, createdFor);
		Assert.assertEquals("Different number of smslogs than expected.",numExpected,smsWaspSendList.size());
	}

	private void assertOrgBalance(int totalContacts)throws InterruptedException {
		double expectedBalance = startingBalance - totalContacts;
		
		Organization org = user.getOrganization();
		Search search = new Search();
		search.addFilterEqual(Organization.PROP_ID, org.getId());
		org = organizationDAO.searchUnique(search);
		double orgBalance = org.getBalance();
		Assert.assertEquals("Org balance different than expected",expectedBalance,orgBalance);
	}
	
	private void waitForCampaignCompletion(Campaign campaign) throws InterruptedException {
		Long id = campaign.getId();
		do{
			Search search2 = new Search();
			search2.addFilterEqual(Campaign.PROP_ID, id);
			campaign = campaignDAO.searchUnique(search2);
			if (!campaign.getStatus().equals(CampaignStatus.FINISHED))
				Thread.sleep(1000);
		}while(!campaign.getStatus().equals(CampaignStatus.FINISHED));
	}
}
