package org.celllife.mobilisr.service.integrationtest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import junit.framework.Assert;

import org.celllife.mobilisr.constants.CampaignStatus;
import org.celllife.mobilisr.constants.CampaignType;
import org.celllife.mobilisr.dao.api.CampaignDAO;
import org.celllife.mobilisr.dao.api.SmsLogDAO;
import org.celllife.mobilisr.dao.api.UserDAO;
import org.celllife.mobilisr.domain.Campaign;
import org.celllife.mobilisr.domain.CampaignContact;
import org.celllife.mobilisr.domain.CampaignMessage;
import org.celllife.mobilisr.domain.Contact;
import org.celllife.mobilisr.domain.SmsLog;
import org.celllife.mobilisr.domain.Transaction;
import org.celllife.mobilisr.domain.User;
import org.celllife.mobilisr.exception.InsufficientBalanceException;
import org.celllife.mobilisr.service.CampaignScheduleService;
import org.celllife.mobilisr.service.UserBalanceService;
import org.celllife.mobilisr.service.exception.CampaignStateException;
import org.celllife.mobilisr.service.qrtz.BackgroundServices;
import org.celllife.mobilisr.test.AbstractServiceTest;
import org.celllife.mobilisr.utilbean.TransactionSummary;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.trg.search.Search;

public class FixedCampaignIntegrationTests extends AbstractServiceTest{

	@Autowired
	private CampaignDAO campaignDAO;
	
	@Autowired
	private CampaignScheduleService campaignService;
	
	@Autowired
	private UserDAO userDAO;

	@Autowired
	private SmsLogDAO smsLogDAO;
	
	@Autowired
	BackgroundServices backgroundServices;
	
	@Autowired
	UserBalanceService balanceService;
	
	private User user;
	
	@Before
	public void before(){
		
		user = getUser();
	}
	
	@Test(timeout=5000)
	public void testScheduleFixedCampaign() throws InsufficientBalanceException, InterruptedException, CampaignStateException{
		
		Campaign campaign = getCampaigns(true);
		int totalContacts = createContactsForProgram(campaign,5);
			
		campaignService.scheduleCampaign(campaign, user);
		
		// check for reserved transaction
		checkTransactions(totalContacts, campaign, user.getIdentifierString(), false);
		
		// wait for program to complete
		waitForCompletion(campaign);
		
		TransactionSummary summary = balanceService.getAccountSummary(campaign);
		Assert.assertEquals(0, summary.getReserved().intValue());
		Assert.assertEquals(-totalContacts, summary.getCost().intValue());
		
		checkSmsLogs(campaign, totalContacts);
	}

	@Test(timeout=10000)
	public void testScheduleProgramJobAtScheduledDate() throws InsufficientBalanceException, InterruptedException, CampaignStateException{
		
		Campaign campaign = getCampaigns(false);
		int totalContacts = createContactsForProgram(campaign,5);
			
		campaignService.scheduleCampaign(campaign, user);
		
		// check for reserved transaction
		checkTransactions(totalContacts, campaign, user.getIdentifierString(), false);
		
		// wait for program to complete
		waitForCompletion(campaign);	
		
		TransactionSummary summary = balanceService.getAccountSummary(campaign);
		Assert.assertEquals(0, summary.getReserved().intValue());
		Assert.assertEquals(-totalContacts, summary.getCost().intValue());
		
		checkSmsLogs(campaign, totalContacts);
	}
	
	private void checkSmsLogs(Campaign campaign, int totalContacts) {
		List<SmsLog> logs = smsLogDAO.searchByPropertyEqual(SmsLog.PROP_CREATEDFOR, campaign.getIdentifierString());
		Assert.assertEquals(totalContacts, logs.size());
	}
	
	private User getUser() {
		Search search = new Search(User.class);
		search.addFilterEqual(User.PROP_FIRST_NAME, "firstname 8");
		User user = userDAO.searchUnique(search);
		return user;
	}
	
	private Campaign getCampaigns(boolean sendNow){
		Campaign campaign = new Campaign();
		campaign.setName("Sched Program ");
		campaign.setStatus(CampaignStatus.INACTIVE);
		campaign.setOrganization(user.getOrganization());
		campaign.setSendNow(sendNow);
		campaign.setType(CampaignType.FIXED);
		campaign.setStartDate(new Date());
		campaignDAO.save(campaign);
		Assert.assertNotNull(campaign.getId());
		String msg = "Sms for Program " +  campaign.getId();
		Date msgDate = new Date();
		if (!sendNow){
			Calendar calendar = Calendar.getInstance();
			calendar.set(Calendar.SECOND, calendar.get(Calendar.SECOND)+2);
			msgDate = calendar.getTime();
		}
		CampaignMessage campaignMessage = new CampaignMessage(msg, msgDate, new Date(), msg.length(), campaign);
		getGeneralDao().save(campaignMessage);
		campaign.setCampaignMessages(Arrays.asList(new CampaignMessage[]{campaignMessage}));
		return campaign;
	}
	
	private int createContactsForProgram(Campaign campaign, int numOfContactsPerProg){
		
		List<Contact> contacts = getContacts(numOfContactsPerProg);
		int totalContacts = contacts.size();
		for (Contact contact : contacts) {
			CampaignContact campaignContact = new CampaignContact(campaign, contact);
			campaignContact.setContact(contact);
			getGeneralDao().save(campaignContact);
		}				
		return totalContacts;
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
	
	private void checkTransactions(int expectedAmount, Campaign campaign, String createdBy, boolean checkCostNotReserved) {
		Search search = new Search(Transaction.class);
		search.addFilterEqual(Transaction.PROP_CREATED_FOR, campaign.getIdentifierString());
		search.addFilterEqual(Transaction.PROP_CREATED_BY, createdBy);
		@SuppressWarnings("unchecked")
		List<Transaction> transactions = getGeneralDao().search(search);
		Assert.assertEquals(1, transactions.size());
		if (checkCostNotReserved){
			Assert.assertEquals(expectedAmount, transactions.get(0).getCost());
		} else {
			Assert.assertEquals(expectedAmount, transactions.get(0).getReserved());
		}
	}

	private void waitForCompletion(Campaign campaign) throws InterruptedException {
		do{
			Search search2 = new Search();
			search2.addFilterEqual(Campaign.PROP_STATUS, CampaignStatus.FINISHED);
			search2.addFilterEqual(Campaign.PROP_ID, campaign.getId());
			Campaign searchUnique = campaignDAO.searchUnique(search2);
			if(searchUnique != null){
				break;
			}
			Thread.sleep(500);
		}while( true );
	}
}
