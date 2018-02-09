package org.celllife.mobilisr.service.integrationtest;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import junit.framework.Assert;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.celllife.mobilisr.constants.CampaignStatus;
import org.celllife.mobilisr.constants.CampaignType;
import org.celllife.mobilisr.dao.api.SmsLogDAO;
import org.celllife.mobilisr.dao.api.UserDAO;
import org.celllife.mobilisr.domain.Campaign;
import org.celllife.mobilisr.domain.CampaignContact;
import org.celllife.mobilisr.domain.CampaignMessage;
import org.celllife.mobilisr.domain.Contact;
import org.celllife.mobilisr.domain.SmsLog;
import org.celllife.mobilisr.domain.User;
import org.celllife.mobilisr.exception.InsufficientBalanceException;
import org.celllife.mobilisr.service.CampaignScheduleService;
import org.celllife.mobilisr.service.UserBalanceService;
import org.celllife.mobilisr.service.exception.CampaignStateException;
import org.celllife.mobilisr.test.AbstractServiceTest;
import org.celllife.mobilisr.utilbean.TransactionSummary;
import org.gwttime.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.trg.search.Search;

public class GenericCampaignIntegrationTests extends AbstractServiceTest{

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
	
	@Test//(timeout=60000)
	public void testRandomMsgOnRandomDaysWithRandomProgresssForContacts() throws InsufficientBalanceException, InterruptedException, CampaignStateException{
		
		int numOfDays = 3;		
		int maxNumOfMsgsInDay = 4;
		int numOfContacts = 5;
		
		Campaign campaign = new Campaign("Demo Camp", null, CampaignType.FLEXI, CampaignStatus.INACTIVE, numOfDays, 0, user.getOrganization());
		getGeneralDao().save(campaign);		
		
		Assert.assertNotNull(campaign.getId());
		
		Map<Integer, Integer> msgDayMap = new HashMap<Integer, Integer>();
		
		List<CampaignMessage> campaignMessages = generateMsgsForGenericCamp( numOfDays, maxNumOfMsgsInDay, campaign, msgDayMap);

		List<CampaignContact> campaignContacts = generateContactsForGenericCampaign(numOfContacts, campaign, campaignMessages.size(), numOfDays);
		
		int totalMsgs = 0;
		//Calculate totalNumOfMsgs to be send for whole campaign
		for (CampaignContact campaignContact : campaignContacts) {
			
			int progress = campaignContact.getProgress();
			Integer numOfMsgs = msgDayMap.get(progress);
			totalMsgs += numOfMsgs;
		}
		
		campaignService.scheduleCampaign(campaign, user);

		checkAllRecordsProcessed(totalMsgs);
		
		//Checking: Each contact got correct message
		checkEachContactGotCorrectMsg(msgDayMap, campaignContacts);
		
		TransactionSummary summary = userBalanceService.getAccountSummary(campaign);
		Assert.assertEquals(-totalMsgs, summary.getCost().intValue());
		Assert.assertEquals(0,summary.getReserved().intValue());
	}

	private List<CampaignMessage> generateMsgsForGenericCamp(int numOfDays,
			int maxNumOfMsgsInDay, Campaign campaign,
			Map<Integer, Integer> msgDayMap) {
		List<CampaignMessage> campaignMessages = new ArrayList<CampaignMessage>();
		for( int day = 0; day < numOfDays ; day++){
			
			Random random = new Random();
			int numOfMsgsInDay = random.nextInt(maxNumOfMsgsInDay) + 1;
			msgDayMap.put(day, numOfMsgsInDay);
			
			for( int msgNum = 1 ; msgNum <= numOfMsgsInDay ; msgNum++ ){
				
				CampaignMessage campaignMessage = new CampaignMessage("Msg "
						+ msgNum + " on day " + day, day, new DateTime()
						.plusSeconds(2).toDate(), campaign);
				getGeneralDao().save(campaignMessage);
				campaignMessages.add(campaignMessage);
			}
		}
		return campaignMessages;
	}

	/**
	 * @param msgDayMap
	 * @param campaignContacts
	 */
	private void checkEachContactGotCorrectMsg(Map<Integer, Integer> msgDayMap,
			List<CampaignContact> campaignContacts) {
		for (CampaignContact campaignContact : campaignContacts) {
			
			Search search = new Search();
			search.addFilterEqual(SmsLog.PROP_MSISDN, campaignContact.getMsisdn());
			List<SmsLog> smsLogs = smsLogDAO.search(search);
			
			int progress = campaignContact.getProgress();
			Integer numOfMsgsInDay = msgDayMap.get(progress);
			
			Assert.assertEquals(numOfMsgsInDay.intValue(), smsLogs.size());
			for (int i = 0; i < numOfMsgsInDay; i++) {
				final String expected = "Msg " + (i+1) + " on day " + progress;
				
				int countMatches = CollectionUtils.countMatches(smsLogs, new Predicate(){
					@Override
					public boolean evaluate(Object object) {
						return ((SmsLog)object).getMessage().equals(expected);
					}
				});

				Assert.assertEquals("Expected an smslog with message: "
						+ expected, 1, countMatches);
			}
		}
	}

	/**
	 * @param numOfRecords
	 * @throws InterruptedException
	 */
	private void checkAllRecordsProcessed(int numOfRecords)
			throws InterruptedException {
		int actualRecords = 0;
		do{
			List<SmsLog> smsLogs = smsLogDAO.findAll();
			actualRecords = smsLogs.size();
			Thread.sleep(2000);
		}while(actualRecords != numOfRecords );
	}
	
	private List<CampaignContact> generateContactsForGenericCampaign(int numOfContacts, Campaign campaign, int totalMsgs, int numOfDays) {
		
		List<CampaignContact> campaignContacts = new ArrayList<CampaignContact>();
		
		for(int i = 0 ; i < numOfContacts ; i++ ){

			Random random = new Random();
			int randomProgress = random.nextInt(numOfDays);

			Calendar contactJD = Calendar.getInstance();
			contactJD.set(Calendar.DAY_OF_MONTH, -randomProgress);

			Contact contact = new Contact("2778512010" + i, user.getOrganization());
			getGeneralDao().save(contact);

			CampaignContact campaignContact = new CampaignContact(campaign, contact, null, randomProgress, contactJD.getTime());
			getGeneralDao().save(campaignContact);
			
			campaignContacts.add(campaignContact);
		}
		
		return campaignContacts;
	}
	
	private User getUser() {
		Search search = new Search(User.class);
		search.addFilterEqual(User.PROP_FIRST_NAME, "firstname 8");
		User user = userDAO.searchUnique(search);
		return user;
	}
}
