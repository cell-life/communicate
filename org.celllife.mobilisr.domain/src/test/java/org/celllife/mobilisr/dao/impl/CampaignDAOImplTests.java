package org.celllife.mobilisr.dao.impl;

import com.extjs.gxt.ui.client.data.BasePagingLoadConfig;
import com.trg.search.Search;
import org.celllife.mobilisr.constants.CampaignStatus;
import org.celllife.mobilisr.constants.CampaignType;
import org.celllife.mobilisr.dao.api.CampaignDAO;
import org.celllife.mobilisr.dao.api.ContactGroupDAO;
import org.celllife.mobilisr.domain.*;
import org.celllife.mobilisr.domain.mock.DomainMockFactory;
import org.celllife.mobilisr.exception.MsisdnFormatException;
import org.celllife.mobilisr.test.AbstractDBTest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class CampaignDAOImplTests extends AbstractDBTest {

	@Autowired
	private CampaignDAO campaignDAO;
	
	@Autowired
	private ContactGroupDAO contactGroupDAO;
	
	private User user;
	
	@Before
	public void before(){
		user = getUser();
	}
	
	@Test
	public void testSaveSameCampaignTwice(){
		Campaign campaign = new Campaign();
		campaign.setName("DevTest 103");
		campaign.setType(CampaignType.FIXED);
		campaign.setStartDate(new Date());
		campaign.setTimesPerDay(1);
		campaign.setOrganization(user.getOrganization());
		campaign.setStatus(CampaignStatus.INACTIVE);
		
		campaignDAO.saveOrUpdate(campaign);
		
		Assert.assertNotNull(campaign.getId());
		
		campaign.setName("DevTest 104");
		campaign.setType(CampaignType.FLEXI);
		campaign.setStartDate(new Date());
		campaign.setTimesPerDay(1);
		campaign.setOrganization(user.getOrganization());
		campaign.setStatus(CampaignStatus.INACTIVE);
		
		campaignDAO.saveOrUpdate(campaign);
		
		Assert.assertNotNull(campaign.getId());
		Assert.assertEquals(CampaignType.FLEXI, campaign.getType());
	}
	
	@Test
	public void testDAORemoveMessagesFromCampaign(){
		Campaign campaign = new Campaign(CampaignType.FIXED, CampaignStatus.INACTIVE,  new Date(), "Amelia's Campaign", "blah", user.getOrganization());
		
		campaignDAO.saveOrUpdate(campaign);
		List<CampaignMessage> cms = new ArrayList<CampaignMessage>();
		for (int i = 0; i< 5; i++){
			CampaignMessage e = new CampaignMessage("Hello " + i, new Date(), new Date(), campaign);
			cms.add(e);
			getGeneralDao().saveOrUpdate(e);
		}

		campaign.setCampaignMessages(cms);

		List<CampaignMessage> deletedMessages = new ArrayList<CampaignMessage>();
		deletedMessages.add(cms.get(0));
		deletedMessages.add(cms.get(1));
		
		Assert.assertEquals(5, campaignDAO.getAllCampMessages(campaign).size());
		
		campaignDAO.removeMessagesFromCampaign(campaign, deletedMessages);
		
		Assert.assertEquals(3,campaignDAO.getAllCampMessages(campaign).size());

		campaignDAO.removeMessagesFromCampaign(campaign, null);
		
		Assert.assertEquals(0, campaignDAO.getAllCampMessages(campaign).size());
		
	}
	
	@Test
	public void testDAOAddDuplicateContactsToCampaign(){
		Campaign campaign = new Campaign(CampaignType.FIXED, CampaignStatus.INACTIVE,  new Date(), "Amelia's Campaign", "blah", user.getOrganization());
		campaignDAO.saveOrUpdate(campaign);
		Contact c1 = new Contact("21111111111111111", user.getOrganization());
		Contact c2 = new Contact("21111111111111112", user.getOrganization());
		Contact c3 = new Contact("21111111111111113", user.getOrganization());
		List<Contact> newContacts = new ArrayList<Contact>();
		newContacts.add(c1);
		newContacts.add(c2);
		newContacts.add(c3);
		getGeneralDao().save(c1);
		getGeneralDao().save(c2);
		getGeneralDao().save(c3);

		
		campaignDAO.addContactsToCampaign(campaign, user.getOrganization(), newContacts, false);
		//campaignDAO.removeAllContactsFromCampaign(campaign);
		Contact c4 = new Contact("9999242422", user.getOrganization());
		getGeneralDao().save(c4);
		newContacts.add(c4);
		campaignDAO.addContactsToCampaign(campaign, user.getOrganization(), newContacts, false);
		
		Search search = new Search(CampaignContact.class);
		search.addFilterEqual(CampaignContact.PROP_CAMPAIGN, campaign);
		@SuppressWarnings("unchecked")
		List<CampaignContact> listOfContacts = getGeneralDao().search(search);
		Assert.assertEquals(4, listOfContacts.size());
		
	}
	
	@Test
	public void testDAOAddContactsToCampaign(){
		Campaign campaign = new Campaign(CampaignType.FIXED, CampaignStatus.INACTIVE,  new Date(), "Amelia's Campaign", "blah", user.getOrganization());
		campaignDAO.saveOrUpdate(campaign);
		Contact c1 = new Contact("21111111111111111", user.getOrganization());
		Contact c2 = new Contact("21111111111111112", user.getOrganization());
		Contact c3 = new Contact("21111111111111113", user.getOrganization());
		List<Contact> newContacts = new ArrayList<Contact>();
		newContacts.add(c1);
		newContacts.add(c2);
		newContacts.add(c3);
		getGeneralDao().save(c1);
		getGeneralDao().save(c2);
		getGeneralDao().save(c3);
		
		campaignDAO.addContactsToCampaign(campaign, user.getOrganization(), newContacts, false);
		
		Search search = new Search(CampaignContact.class);
		search.addFilterEqual(CampaignContact.PROP_CAMPAIGN, campaign);
		@SuppressWarnings("unchecked")
		List<CampaignContact> listOfCampaignContacts = getGeneralDao().search(search);
		Assert.assertEquals(3, listOfCampaignContacts.size());
		
		for (CampaignContact contact : listOfCampaignContacts) {
			Assert.assertNotNull(contact.getJoiningDate());
		}
	}
	
	/**
	 * Test adding all contacts to a campaign when some already belong to the campaign
	 */
	@Test
	public void testDAOAddAllContactsToCampaignExcludingList_contactsExist() {
		Campaign campaign = new Campaign(CampaignType.FIXED, CampaignStatus.INACTIVE,  new Date(), "Amelia's Campaign", "blah", user.getOrganization());
		campaignDAO.saveOrUpdate(campaign);
		
		Contact contact = DomainMockFactory._().on(Contact.class).create();
		contact.setOrganization(user.getOrganization());
		getGeneralDao().save(contact);
		
		CampaignContact campaignContact = new CampaignContact(campaign, contact);
		getGeneralDao().save(campaignContact);

		campaignDAO.addAllContactsToCampaign(campaign, false);
		
		Search search = new Search(CampaignContact.class);
		search.addFilterEqual(CampaignContact.PROP_CAMPAIGN, campaign);
		@SuppressWarnings("unchecked")
		List<CampaignContact> listOfCampaignContacts = getGeneralDao().search(search);
		
		Search search2 = new Search(Contact.class);
		search2.addFilterEqual(Contact.PROP_ORGANIZATION, user.getOrganization());
		@SuppressWarnings("unchecked")
		List<Contact> listOfContacts = getGeneralDao().search(search2);
		
		Assert.assertEquals(listOfContacts.size(), listOfCampaignContacts.size());
	}
	
	@Test
	public void testDAOAddAllContactsToCampaignExcludingList_setJoindate() {
		Campaign campaign = new Campaign(CampaignType.FIXED, CampaignStatus.INACTIVE,  new Date(), "Amelia's Campaign", "blah", user.getOrganization());
		campaignDAO.saveOrUpdate(campaign);
		campaignDAO.addAllContactsToCampaign(campaign, false);
		
		Search search = new Search(CampaignContact.class);
		search.addFilterEqual(CampaignContact.PROP_CAMPAIGN, campaign);
		@SuppressWarnings("unchecked")
		List<CampaignContact> listOfCampaignContacts = getGeneralDao().search(search);
		
		Search search2 = new Search(Contact.class);
		search2.addFilterEqual(Contact.PROP_ORGANIZATION, user.getOrganization());
		@SuppressWarnings("unchecked")
		List<Contact> listOfContacts = getGeneralDao().search(search2);
		
		Assert.assertEquals(listOfContacts.size(), listOfCampaignContacts.size());
		for (CampaignContact contact : listOfCampaignContacts) {
			Assert.assertNotNull(contact.getJoiningDate());
		}
	}
	
	
	private User getUser() {
		Search search = new Search(User.class);
		search.addFilterEqual(User.PROP_FIRST_NAME, "firstname 8");
		User user = (User) getGeneralDao().searchUnique(search);
		return user;
	}
	
	@Test
	public void testDAOFindDefaultTimeForRelativeCamp(){
		
		Campaign campaign = new Campaign("RelCamp101", null, CampaignType.DAILY, CampaignStatus.INACTIVE, 10, 4, user.getOrganization());
		campaignDAO.saveOrUpdate(campaign);
		
		for( int i = 1 ; i <= 4 ; i++){
			Calendar calendar = Calendar.getInstance();
			calendar.add(Calendar.HOUR, i);
			CampaignMessage campMsg = new CampaignMessage("Hello", new Date(), calendar.getTime(), i, campaign);
			getGeneralDao().saveOrUpdate(campMsg);
		}
		
		List<CampaignMessage> campaignMessages = campaignDAO.findDefaultTimesForRelativeCampaign(campaign);
		Assert.assertEquals(4, campaignMessages.size());
	}
	
	@Test
	public void testUpdateContactMsisdn() throws MsisdnFormatException{
		// setup
		Campaign campaign = DomainMockFactory._().on(Campaign.class).create();
		campaign.setOrganization(user.getOrganization());
		campaignDAO.save(campaign);
		
		Contact contact = DomainMockFactory._().on(Contact.class).create();
		contact.setOrganization(user.getOrganization());
		getGeneralDao().save(contact);
		
		CampaignContact campaignContact = new CampaignContact(campaign, contact);
		getGeneralDao().save(campaignContact);
		
		String msisdn = "27724567895";
		
		// call test method
		int num = campaignDAO.updateCampaignContactMsisdn(contact.getId(), msisdn);
		
		// verify
		Assert.assertEquals(1, num);
		CampaignContact find = getGeneralDao().find(CampaignContact.class, campaignContact.getId());
		Assert.assertEquals(msisdn, find.getMsisdn());
	}
	
	@Test
	public void testUpdateContactMsisdn_multiple() throws MsisdnFormatException{
		// setup
		Contact contact = DomainMockFactory._().on(Contact.class).create();
		contact.setOrganization(user.getOrganization());
		getGeneralDao().save(contact);
		
		// -- campaign 1
		Campaign campaign1 = DomainMockFactory._().on(Campaign.class).create();
		campaign1.setOrganization(user.getOrganization());
		campaignDAO.save(campaign1);

		CampaignContact campaignContact1 = new CampaignContact(campaign1, contact);
		getGeneralDao().save(campaignContact1);
		
		// -- campaign 2
		Campaign campaign2 = DomainMockFactory._().on(Campaign.class).create();
		campaign2.setOrganization(user.getOrganization());
		campaignDAO.save(campaign2);
		
		CampaignContact campaignContact2 = new CampaignContact(campaign2, contact);
		getGeneralDao().save(campaignContact2);
		
		String msisdn = "27724567895";
		
		// call test method
		int num = campaignDAO.updateCampaignContactMsisdn(contact.getId(), msisdn);
		
		// verify
		Assert.assertEquals(2, num);
		CampaignContact find1 = getGeneralDao().find(CampaignContact.class, campaignContact1.getId());
		Assert.assertEquals(msisdn, find1.getMsisdn());
		CampaignContact find2 = getGeneralDao().find(CampaignContact.class, campaignContact2.getId());
		Assert.assertEquals(msisdn, find2.getMsisdn());
	}
	
	@Test
	public void testIsAllContactsProcessedForCampaign_no_contacts(){
		// setup
		int campDuration = 30;
		Campaign c = new Campaign("test campaign 73", null, CampaignType.DAILY, 
				CampaignStatus.ACTIVE, 
				campDuration, 2, user.getOrganization());
		campaignDAO.save(c);
		
		// call test method
		boolean result = campaignDAO.isAllContactsProcessedForCampaign(c);
		
		// verify
		Assert.assertTrue(result);
	}
	
	public void testIsAllContactsProcessedForCampaign_no_end_date_all_processed() {
		// setup
		int campDuration = 30;
		Campaign c = new Campaign("test campaign 73", null, CampaignType.DAILY, 
				CampaignStatus.ACTIVE, 
				campDuration, 2, user.getOrganization());
		campaignDAO.save(c);
		
		Date joinedDate = new Date();
		Contact contact = new Contact("078512000", user.getOrganization());
		int progress = campDuration+1;
		CampaignContact campaignContact = new CampaignContact(c, contact, null, progress, joinedDate);
		getGeneralDao().save(contact);
		getGeneralDao().save(campaignContact);
		
		// call test method
		boolean result = campaignDAO.isAllContactsProcessedForCampaign(c);
		
		// verify
		Assert.assertTrue(result);
	}
	
	public void testIsAllContactsProcessedForCampaign_no_end_date_not_all_processed() {
		// setup
		int campDuration = 30;
		Campaign c = new Campaign("test campaign 73", null, CampaignType.DAILY, 
				CampaignStatus.ACTIVE, 
				campDuration, 2, user.getOrganization());
		campaignDAO.save(c);
		
		Date joinedDate = new Date();
		Contact contact = new Contact("078512000", user.getOrganization());
		int progress = 0;
		CampaignContact campaignContact = new CampaignContact(c, contact, null, progress, joinedDate);
		getGeneralDao().save(contact);
		getGeneralDao().save(campaignContact);
		
		// call test method
		boolean result = campaignDAO.isAllContactsProcessedForCampaign(c);
		
		// verify
		Assert.assertTrue(result);
	}
	
	@Test
	public void testIsAllContactsProcessedForCampaign_joindate_lt_enddate(){
		// setup
		Campaign c = createCampaignTestContacts(-3, -1);
		
		// call test method
		boolean result = campaignDAO.isAllContactsProcessedForCampaign(c);
		
		// verify
		Assert.assertEquals(false, result);
	}
	
	@Test
	public void testIsAllContactsProcessedForCampaign_joindate_gt_enddate(){
		// setup
		Campaign c = createCampaignTestContacts(-3, 1);
		
		// call test method
		boolean result = campaignDAO.isAllContactsProcessedForCampaign(c);
		
		// verify
		Assert.assertEquals(true, result);
	}
	
	@Test
	public void testIsAllContactsProcessedForCampaign_joindate_eq_enddate(){
		// setup
		Campaign c = createCampaignTestContacts(-3, 0);
		
		// call test method
		boolean result = campaignDAO.isAllContactsProcessedForCampaign(c);
		
		// verify
		Assert.assertEquals(false, result);
	}
	
	@Test
	public void testIsAllContactsProcessedForCampaign_currDay_lt_duration(){
		// setup
		Campaign c = createCampaignTestContacts(-3, -3);
		
		// call test method
		boolean result = campaignDAO.isAllContactsProcessedForCampaign(c);
		
		// verify
		Assert.assertEquals(false, result);
	}
	
	@Test
	public void testIsAllContactsProcessedForCampaign_currDay_gt_duration(){
		// setup
		Campaign c = createCampaignTestContacts(1, -3);
		
		// call test method
		boolean result = campaignDAO.isAllContactsProcessedForCampaign(c);
		
		// verify
		Assert.assertEquals(true, result);
	}
	
	@Test
	public void testIsAllContactsProcessedForCampaign_currDay_eq_duration(){
		// setup
		Campaign c = createCampaignTestContacts(0, -3);
		
		// call test method
		boolean result = campaignDAO.isAllContactsProcessedForCampaign(c);
		
		// verify
		Assert.assertEquals(false, result);
	}
	
	@Test
	public void testUpdateContactsProgress_currDay_lt_campDur(){
		int progressDiff = -1;
		int joinDateDiff = -3;
		Campaign c = createCampaignTestContacts(progressDiff, joinDateDiff);
		
		campaignDAO.updateCampaignContactsProgress(c);
		
		Search search = new Search(CampaignContact.class);
		search.addFilterEqual(CampaignContact.PROP_CAMPAIGN, c);
		CampaignContact contact = (CampaignContact) getGeneralDao().searchUnique(search);
		Assert.assertEquals(c.getDuration()+progressDiff+1, contact.getProgress());
	}
	
	@Test
	public void testUpdateContactsProgress_currDay_eq_campDur(){
		int progressDiff = 0;
		int joinDateDiff = -3;
		Campaign c = createCampaignTestContacts(progressDiff, joinDateDiff);
		
		campaignDAO.updateCampaignContactsProgress(c);
		
		Search search = new Search(CampaignContact.class);
		search.addFilterEqual(CampaignContact.PROP_CAMPAIGN, c);
		CampaignContact contact = (CampaignContact) getGeneralDao().searchUnique(search);
		Assert.assertEquals(c.getDuration()+progressDiff+1, contact.getProgress());
	}
	
	@Test
	public void testUpdateContactsProgress_currDay_gt_campDur(){
		int progressDiff = 1;
		int joinDateDiff = -3;
		Campaign c = createCampaignTestContacts(progressDiff, joinDateDiff);
		
		campaignDAO.updateCampaignContactsProgress(c);
		
		Search search = new Search(CampaignContact.class);
		search.addFilterEqual(CampaignContact.PROP_CAMPAIGN, c);
		CampaignContact contact = (CampaignContact) getGeneralDao().searchUnique(search);
		Assert.assertEquals(c.getDuration()+progressDiff, contact.getProgress());
	}
	

	@Test
	public void testUpdateContactsProgress_joinDate_lt_endDate(){
		int progressDiff = -3;
		int joinDateDiff = -3;
		Campaign c = createCampaignTestContacts(progressDiff, joinDateDiff);
		
		campaignDAO.updateCampaignContactsProgress(c);
		
		Search search = new Search(CampaignContact.class);
		search.addFilterEqual(CampaignContact.PROP_CAMPAIGN, c);
		CampaignContact contact = (CampaignContact) getGeneralDao().searchUnique(search);
		Assert.assertEquals(c.getDuration()+progressDiff+1, contact.getProgress());
	}
	
	@Test
	public void testUpdateContactsProgress_joinDate_eq_endDate(){
		int progressDiff = -3;
		int joinDateDiff = 0;
		Campaign c = createCampaignTestContacts(progressDiff, joinDateDiff);
		
		campaignDAO.updateCampaignContactsProgress(c);
		
		Search search = new Search(CampaignContact.class);
		search.addFilterEqual(CampaignContact.PROP_CAMPAIGN, c);
		CampaignContact contact = (CampaignContact) getGeneralDao().searchUnique(search);
		Assert.assertEquals(c.getDuration()+progressDiff+1, contact.getProgress());
	}
	
	@Test
	public void testUpdateContactsProgress_joinDate_gt_endDate(){
		int progressDiff = -3;
		int joinDateDiff = 1;
		Campaign c = createCampaignTestContacts(progressDiff, joinDateDiff);
		
		campaignDAO.updateCampaignContactsProgress(c);
		
		Search search = new Search(CampaignContact.class);
		search.addFilterEqual(CampaignContact.PROP_CAMPAIGN, c);
		CampaignContact contact = (CampaignContact) getGeneralDao().searchUnique(search);
		Assert.assertEquals(c.getDuration()+progressDiff, contact.getProgress());
	}
	
	/**
	 * @param progressDiff added to campaign duration to get contact current day
	 * @param joinDateDiff added to campaign end date to get contact join date
	 * @return
	 */
	private Campaign createCampaignTestContacts(int progressDiff,
			int joinDateDiff) {
		int campDuration = 30;
		Campaign c = new Campaign("test campaign 73", null, CampaignType.DAILY, 
				CampaignStatus.ACTIVE, 
				campDuration, 2, user.getOrganization());
		c.setEndDate(new Date());
		campaignDAO.save(c);
		
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(c.getEndDate());
		calendar.add(Calendar.DATE, joinDateDiff);
		Date joinedDate = calendar.getTime();

		Contact contact = new Contact("078512000", user.getOrganization());
		
		int progress = campDuration+progressDiff;
		
		CampaignContact campaignContact = new CampaignContact(c, contact, null, progress, joinedDate);
		getGeneralDao().save(contact);
		getGeneralDao().save(campaignContact);
		return c;
	}

	@Test
	public void testGetCampaignContactNeedingWelcomeMessage(){
		Campaign campaign = campaignDAO.searchByPropertyEqual(Campaign.PROP_NAME, "Program 0").get(0);
		Search search = new Search(CampaignContact.class);
		search.addFilterEqual(CampaignContact.PROP_CAMPAIGN, campaign);
		@SuppressWarnings("unchecked")
		List<CampaignContact> contacts = getGeneralDao().search(search);
		int totalContacts = contacts.size();
		contacts.subList(0, totalContacts/2).clear();
		int numReceivedWelcome = contacts.size();
		
		for (CampaignContact contact : contacts) {
			contact.setReceivedWelcome(true);
			getGeneralDao().saveOrUpdate(contact);
		}
		
		List<CampaignContact> contactsNeedingWelcomeMessage = campaignDAO.getCampaignContactsNeedingWelcomeMessage(campaign);
		Assert.assertEquals(totalContacts-numReceivedWelcome, contactsNeedingWelcomeMessage.size());
	}
	
	@Test
	public void testMark(){
		Campaign campaign = campaignDAO.searchByPropertyEqual(Campaign.PROP_NAME, "Program 0").get(0);
		Search search = new Search(CampaignContact.class);
		search.addFilterEqual(CampaignContact.PROP_CAMPAIGN, campaign);
		@SuppressWarnings("unchecked")
		List<CampaignContact> contacts = getGeneralDao().search(search);
		int totalContacts = contacts.size();
		contacts.subList(0, totalContacts/2).clear();
		
		campaignDAO.markCampaignContactsAsReceivedWelcomeMessage(contacts);
		
		@SuppressWarnings("unchecked")
		List<CampaignContact> allContacts = getGeneralDao().search(search);
		for (CampaignContact contact : allContacts) {
			for (CampaignContact oldContact : contacts) {
				if (oldContact.getId().equals(contact.getId())){
					Assert.assertTrue(contact.getReceivedWelcome());
				}
			}
		}
	}
	
	@Test
	public void testGetCampaignStatus(){
		Campaign campaign = campaignDAO.findAll().get(0);
		
		CampaignStatus status = campaignDAO.getCampaignStatus(campaign.getId());
		Assert.assertNotNull(status);
		Assert.assertEquals(campaign.getStatus(), status);
	}
	
	@Test
	public void testGetCampaignStatus_nonExistant(){
		CampaignStatus status = campaignDAO.getCampaignStatus(-1L);
		Assert.assertNull(status);
	}
	
	@Test
	public void testContactMsgTimeOrdering(){
		Campaign campaign = new Campaign(CampaignType.DAILY, CampaignStatus.INACTIVE,  new Date(), "Test ContactMsgTime Campaign", "", user.getOrganization());
		campaignDAO.saveOrUpdate(campaign);
		Contact c1 = new Contact("21111111111111111", user.getOrganization());
		getGeneralDao().save(c1);

		CampaignContact campaignContact = new CampaignContact(campaign, c1);
		getGeneralDao().save(campaignContact);
		getGeneralDao().save(new ContactMsgTime(new Date(), 3, campaignContact, campaign));
		getGeneralDao().save(new ContactMsgTime(new Date(), 1, campaignContact, campaign));
		getGeneralDao().save(new ContactMsgTime(new Date(), 2, campaignContact, campaign));
		
		Search search = new Search(CampaignContact.class);
		search.addFilterEqual(CampaignContact.PROP_CAMPAIGN, campaign);
		@SuppressWarnings("unchecked")
		List<CampaignContact> listOfCampaignContacts = getGeneralDao().search(search);
		Assert.assertEquals(1, listOfCampaignContacts.size());
		
		List<ContactMsgTime> contactMsgTimes = listOfCampaignContacts.get(0).getContactMsgTimes();
		Assert.assertEquals(3, contactMsgTimes.size());
		for (int i = 0; i < contactMsgTimes.size(); i++) {
			ContactMsgTime msgTime = contactMsgTimes.get(i);
			Assert.assertEquals(i+1, msgTime.getMsgSlot());
		}
	}
	
	@Test
	public void testGetCampaignMessageLengthsAndDay(){
		Campaign campaign = new Campaign(CampaignType.DAILY, CampaignStatus.INACTIVE,  new Date(), "Test ContactMsgTime Campaign", "", user.getOrganization());
		campaignDAO.saveOrUpdate(campaign);
		
		String text = "Hello";
		for( int i = 1 ; i <= 4 ; i++){
			Calendar calendar = Calendar.getInstance();
			calendar.add(Calendar.HOUR, i);
			CampaignMessage campMsg = new CampaignMessage(text.substring(0, i), new Date(), calendar.getTime(), i, campaign);
			campMsg.setMsgDay(i);
			getGeneralDao().saveOrUpdate(campMsg);
		}
		
		List<CampaignMessage> lengths = campaignDAO.getCampaignMessageLengthsAndDay(campaign.getId());
		for (int i = 0; i < lengths.size(); i++) {
			CampaignMessage msg = lengths.get(i);
			Assert.assertEquals(i+1, msg.getMsgLength().intValue());
			Assert.assertEquals(i+1, msg.getMsgDay());
		}
	}
	
	@Test
	public void testCreateDefaultMessageTimes(){
		Campaign campaign = new Campaign(CampaignType.DAILY, CampaignStatus.INACTIVE,  new Date(), "Test ContactMsgTime Campaign", "", user.getOrganization());
		campaignDAO.saveOrUpdate(campaign);
		
		Contact c1 = new Contact("21111111111111111", user.getOrganization());
		getGeneralDao().save(c1);
		CampaignContact cc1 = new CampaignContact(campaign, c1);
		getGeneralDao().save(cc1);
		
		Contact c2 = new Contact("21111111111111112", user.getOrganization());
		getGeneralDao().save(c2);
		CampaignContact cc2 = new CampaignContact(campaign, c2);
		getGeneralDao().save(cc2);
		
		List<ContactMsgTime> defaultTimes = new ArrayList<ContactMsgTime>();
		defaultTimes.add(new ContactMsgTime(new Date(), 1, null, null));
		defaultTimes.add(new ContactMsgTime(new Date(), 2, null, null));
		campaignDAO.createDefaultMessageTimesForContacts(campaign, defaultTimes);
		
		Search search = new Search(ContactMsgTime.class);
		search.addFilterEqual(ContactMsgTime.PROP_CAMPAIGN, campaign);
		@SuppressWarnings("unchecked")
		List<ContactMsgTime> list = getGeneralDao().search(search);
		Assert.assertEquals(4, list.size());
	}
	
	@Test
	public void testSetAllCampaignContactsEndDates() {
		Campaign campaign = new Campaign(CampaignType.FLEXI, CampaignStatus.INACTIVE,  new Date(), "Test Campaign", "Testing Set End Dates", user.getOrganization());
		campaignDAO.saveOrUpdate(campaign);
		
		Contact c1 = new Contact("27111111111111111", user.getOrganization());
		Contact c2 = new Contact("27111111111111112", user.getOrganization());
		Contact c3 = new Contact("27111111111111113", user.getOrganization());
		Contact c4 = new Contact("27111111111111114", user.getOrganization());
		Contact c5 = new Contact("27111111111111115", user.getOrganization());
		
		List<Contact> newContacts = new ArrayList<Contact>();
		newContacts.add(c1);
		newContacts.add(c2);
		newContacts.add(c3);
		newContacts.add(c4);
		newContacts.add(c5);
		
		getGeneralDao().save(c1);
		getGeneralDao().save(c2);
		getGeneralDao().save(c3);
		getGeneralDao().save(c4);
		getGeneralDao().save(c5);
		
		campaignDAO.addContactsToCampaign(campaign, user.getOrganization(), newContacts, true);		
		
		List<CampaignContact> cc = campaign.getCampaignContacts();
		
		for (int i = 0; i < cc.size(); i++) {
			Assert.assertNull(cc.get(i).getEndDate());
		}
		
		campaignDAO.setAllCampaignContactsEndDates(campaign);
		
		cc = campaign.getCampaignContacts();
		
		for (int i = 0; i < cc.size(); i++) {
			Assert.assertNotNull(cc.get(i).getEndDate());
		}		
	}
	
	@SuppressWarnings("static-access")
	@Test
	public void testSetEndDateForCampaignContactsByGroup() {
		
		Contact c1 = new Contact("27111111111111111", user.getOrganization());
		Contact c2 = new Contact("27111111111111112", user.getOrganization());
		Contact c3 = new Contact("27111111111111113", user.getOrganization());
		
		List<Contact> newContacts = new ArrayList<Contact>();
		newContacts.add(c1);
		newContacts.add(c2);
		newContacts.add(c3);
		
		getGeneralDao().save(c1);
		getGeneralDao().save(c2);
		getGeneralDao().save(c3);
		
		ContactGroup myGroup = new ContactGroup("TestGroup", "this is a test group");
		myGroup.setOrganization(user.getOrganization());
		myGroup.setContacts(newContacts);
		contactGroupDAO.save(myGroup);		
		myGroup = contactGroupDAO.searchByPropertyEqual(ContactGroup.PROP_GROUP_NAME, "TestGroup").get(0);
		
		Campaign campaign = new Campaign(CampaignType.FLEXI, CampaignStatus.INACTIVE,  new Date(), "Test Campaign Add Group", "Testing add group to Campaign", user.getOrganization());
		campaignDAO.saveOrUpdate(campaign);

		List<Long> groupList = new ArrayList<Long>();
		groupList.add(myGroup.getId());		
		campaignDAO.addContactGroupsToCampaignById(campaign, groupList, true);				
		campaignDAO.setEndDateForCampaignContactsByGroup(campaign, groupList);
		
		campaign = campaignDAO.searchByPropertyEqual(campaign.PROP_NAME, "Test Campaign Add Group").get(0);			
		List<CampaignContact> cc = campaign.getCampaignContacts();
		
		for (int i = 0; i < cc.size(); i++) {
			Assert.assertNotNull(cc.get(i).getEndDate());
		}	
		
	}	
	
	@Test
	public void testSetCampaignContactsEndDateByMsisdn() {
		
		Campaign campaign = new Campaign(CampaignType.FLEXI, CampaignStatus.INACTIVE,  new Date(), "Test Campaign", "Testing Set End Dates", user.getOrganization());
		campaignDAO.saveOrUpdate(campaign);
		
		Contact c1 = new Contact("27111111111111111", user.getOrganization());
		Contact c2 = new Contact("27111111111111112", user.getOrganization());
		Contact c3 = new Contact("27111111111111113", user.getOrganization());
		
		List<Contact> newContacts = new ArrayList<Contact>();
		newContacts.add(c1);
		newContacts.add(c2);
		newContacts.add(c3);
		
		getGeneralDao().save(c1);
		getGeneralDao().save(c2);
		getGeneralDao().save(c3);
		
		campaignDAO.addContactsToCampaign(campaign, user.getOrganization(), newContacts, true);		

		List<String> msisdnList = new ArrayList<String>();
		msisdnList.add("27111111111111111");
		msisdnList.add("27111111111111112");
		msisdnList.add("27111111111111113");
		
		campaignDAO.setCampaignContactsEndDateByMsisdn(campaign, msisdnList);
		
		List<CampaignContact> cc = campaign.getCampaignContacts();
		
		for (int i = 0; i < cc.size(); i++) {
			Assert.assertNull(cc.get(i).getEndDate());
		}
		
	}
	
	@SuppressWarnings("static-access")
	@Test
	public void testAddAllContactsToCampaign() {
		
		Campaign campaign = new Campaign(CampaignType.FLEXI, CampaignStatus.INACTIVE,  new Date(), "Test Add All Campaign", "Testing Set End Dates", user.getOrganization());
		campaignDAO.saveOrUpdate(campaign);		
		
		Contact c1 = new Contact("27111111111111111", user.getOrganization());
		List<Contact> newContacts = new ArrayList<Contact>();
		newContacts.add(c1);
		getGeneralDao().save(c1);

		campaignDAO.addAllContactsToCampaign(campaign, true);		
		campaign = campaignDAO.searchByPropertyEqual(campaign.PROP_NAME, "Test Add All Campaign").get(0);
		
		List<Contact> clist = getGeneralDao().findAll(Contact.class);
		int count = 0;		
		for (int i = 0; i < clist.size(); i++) {
			if (clist.get(i).getOrganization().getId().equals(user.getOrganization().getId()))
			count++;	
		}
		
		Assert.assertEquals(count,(int)campaign.getCampaignContacts().size());		
	}
	
	@SuppressWarnings("static-access")
	@Test
	public void testAddContactGroupsToCampaignById() {
		
		Contact c1 = new Contact("27111111111111111", user.getOrganization());
		Contact c2 = new Contact("27111111111111112", user.getOrganization());
		Contact c3 = new Contact("27111111111111113", user.getOrganization());
		Contact c4 = new Contact("27111111111111114", user.getOrganization());
		Contact c5 = new Contact("27111111111111115", user.getOrganization());
		
		List<Contact> newContacts = new ArrayList<Contact>();
		newContacts.add(c1);
		newContacts.add(c2);
		newContacts.add(c3);
		newContacts.add(c4);
		newContacts.add(c5);
		
		getGeneralDao().save(c1);
		getGeneralDao().save(c2);
		getGeneralDao().save(c3);
		getGeneralDao().save(c4);
		getGeneralDao().save(c5);
		
		ContactGroup myGroup = new ContactGroup("TestGroup", "this is a test group");
		myGroup.setOrganization(user.getOrganization());
		myGroup.setContacts(newContacts);
		contactGroupDAO.save(myGroup);
		
		myGroup = contactGroupDAO.searchByPropertyEqual(ContactGroup.PROP_GROUP_NAME, "TestGroup").get(0);
		Campaign campaign = new Campaign(CampaignType.FLEXI, CampaignStatus.INACTIVE,  new Date(), "Test Campaign Add Group", "Testing add group to Campaign", user.getOrganization());
		campaignDAO.saveOrUpdate(campaign);

		List<Long> addGroupList = new ArrayList<Long>();
		addGroupList.add(myGroup.getId());		
		campaignDAO.addContactGroupsToCampaignById(campaign, addGroupList, true);
		
		campaign = campaignDAO.searchByPropertyEqual(campaign.PROP_NAME, "Test Campaign Add Group").get(0);		
		
		Assert.assertEquals(myGroup.getContacts().size(), campaign.getCampaignContacts().size());		
	}

    @Test
    public void testGetContactsInPaginationForCampaign() {

        Campaign campaign = campaignDAO.getCampaign("Program 0");

        List<CampaignContact> campaignContactList = campaignDAO.getContactsInPaginationForCampaign(campaign, new BasePagingLoadConfig(0,100));

        Assert.assertEquals(19,campaignContactList.size());

    }

}
