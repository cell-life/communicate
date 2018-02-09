package org.celllife.mobilisr.service.integrationtest;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

import junit.framework.Assert;

import org.celllife.mobilisr.constants.CampaignStatus;
import org.celllife.mobilisr.constants.CampaignType;
import org.celllife.mobilisr.dao.api.SmsLogDAO;
import org.celllife.mobilisr.dao.api.UserDAO;
import org.celllife.mobilisr.domain.Campaign;
import org.celllife.mobilisr.domain.CampaignContact;
import org.celllife.mobilisr.domain.CampaignMessage;
import org.celllife.mobilisr.domain.Contact;
import org.celllife.mobilisr.domain.ContactMsgTime;
import org.celllife.mobilisr.domain.SmsLog;
import org.celllife.mobilisr.domain.User;
import org.celllife.mobilisr.exception.InsufficientBalanceException;
import org.celllife.mobilisr.service.CampaignScheduleService;
import org.celllife.mobilisr.service.UserBalanceService;
import org.celllife.mobilisr.service.exception.CampaignStateException;
import org.celllife.mobilisr.test.AbstractServiceTest;
import org.celllife.mobilisr.utilbean.TransactionSummary;
import org.junit.Before;
import org.junit.Test;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

import com.trg.search.Search;

public class RelativeCampaignIntegrationTests extends AbstractServiceTest{

	@Autowired
	private CampaignScheduleService campaignService;
	
	@Autowired
	private UserDAO userDAO;

	@Autowired
	private SmsLogDAO smsLogDAO;
	
	@Autowired
	private UserBalanceService userBalanceService;
	
	private User user;
	
	@Before
	public void before(){
		
		user = getUser();
	}
	
	@Test(timeout=60000)
	public void testContactsWithRandomProgress() throws JobExecutionException, InsufficientBalanceException, InterruptedException, CampaignStateException{
		
		int timesPerDay = 2;
		int numOfDays = 5;
		int numOfContacts = 10;
		
		Campaign campaign = new Campaign("Demo Camp", null, CampaignType.DAILY, CampaignStatus.INACTIVE, numOfDays, timesPerDay, user.getOrganization());
		getGeneralDao().save(campaign);
		
		//Generate msgs
		List<CampaignMessage> campaignMessages = generateMsgsForNMsgPerDayCamp(timesPerDay, numOfDays, campaign);
		
		//Generate contacts
		List<CampaignContact> campaignContacts = generateContactsWithRandomDays(numOfDays, timesPerDay, numOfContacts, campaign, campaignMessages.size());
		
		//Schedule campaign
		campaignService.scheduleCampaign(campaign, user);
		
		int numOfRecords = timesPerDay*campaignContacts.size();
		checkAllRecordsProcessed(numOfRecords);
		
		for (CampaignContact campaignContact : campaignContacts) {
			
			int progress = campaignContact.getProgress();
			
			Search search = new Search();
			search.addFilterEqual(SmsLog.PROP_MSISDN, campaignContact.getMsisdn());
			List<SmsLog> results = smsLogDAO.search(search);
			Assert.assertEquals(timesPerDay, results.size());

			for(int i = 1 ; i <= timesPerDay ; i++){
				Assert.assertEquals("Hello " + i + " for Day " + progress, results.get(i-1).getMessage());
			}
		}
		
		TransactionSummary summary = userBalanceService.getAccountSummary(campaign);
		Assert.assertEquals(-numOfRecords, summary.getCost().intValue());
		Assert.assertEquals(0,summary.getReserved().intValue());
		// one transaction for reserve, one for debit per contact
		Assert.assertEquals(numOfRecords*2,summary.getTransactionCount().intValue());
	}
	
	private User getUser() {
		Search search = new Search(User.class);
		search.addFilterEqual(User.PROP_FIRST_NAME, "firstname 8");
		User user = userDAO.searchUnique(search);
		return user;
	}
	
	private List<CampaignContact> generateContactsWithRandomDays(int numOfDays, int timesPerDay, int numOfContacts, Campaign campaign, int totalMsgs) throws InsufficientBalanceException {
		
		List<CampaignContact> campaignContacts = new ArrayList<CampaignContact>();
		
		for(int i = 1 ; i <= numOfContacts ; i++ ){
			
			Calendar contactJD = Calendar.getInstance();
			contactJD.set(Calendar.DAY_OF_MONTH, Calendar.DAY_OF_MONTH-2);

			Random random = new Random();
			int randomProgress = random.nextInt(numOfDays)+1;
			
			Contact contact = new Contact("2778512010" + i,user.getOrganization());
			getGeneralDao().save(contact);
			CampaignContact campaignContact = new CampaignContact(campaign, contact, null, randomProgress, contactJD.getTime());
			getGeneralDao().save(campaignContact);
			
			Calendar cMTCal = Calendar.getInstance();
			for (int msgSlot = 1; msgSlot <= timesPerDay; msgSlot++) {
				
				cMTCal.add(Calendar.SECOND, 1*msgSlot);
				ContactMsgTime contactMsgTime = new ContactMsgTime(cMTCal.getTime(), msgSlot, campaignContact, campaign);
				getGeneralDao().save(contactMsgTime);
			}
			
			campaignContacts.add(campaignContact);
		}
		
		return campaignContacts;
	}
	
	private List<CampaignMessage> generateMsgsForNMsgPerDayCamp(int timesPerDay, int numOfDays, Campaign campaign) {
		Calendar msgCalendar = Calendar.getInstance();
		
		List<CampaignMessage> campaignMessages = new ArrayList<CampaignMessage>();
		
		for(int days = 1; days <= numOfDays ; days++){
			
			for (int msgSlot = 1; msgSlot <= timesPerDay ; msgSlot++) {
				
				msgCalendar.add(Calendar.SECOND, 2);
				CampaignMessage campMsg = new CampaignMessage("Hello " + msgSlot + " for Day " + days, days, msgCalendar.getTime(), msgSlot, campaign);
				getGeneralDao().save(campMsg);	
				campaignMessages.add(campMsg);
			}
		}
		
		return campaignMessages;
	}
	
	private void checkAllRecordsProcessed(int numOfRecords) throws InterruptedException {
		int actualRecords = 0;
		System.out.print("Waiting for messages.");
		do{
			
			List<SmsLog> smsLogs = smsLogDAO.findAll();
			actualRecords = smsLogs.size();
			System.out.print(".");
			Thread.sleep(2000);
		}while(actualRecords != numOfRecords );
	}
}
