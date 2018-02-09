package org.celllife.mobilisr.dao.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import junit.framework.Assert;

import org.celllife.mobilisr.constants.CampaignStatus;
import org.celllife.mobilisr.constants.CampaignType;
import org.celllife.mobilisr.dao.api.CampaignDAO;
import org.celllife.mobilisr.dao.api.UserDAO;
import org.celllife.mobilisr.domain.Campaign;
import org.celllife.mobilisr.domain.CampaignContact;
import org.celllife.mobilisr.domain.CampaignMessage;
import org.celllife.mobilisr.domain.Contact;
import org.celllife.mobilisr.domain.User;
import org.celllife.mobilisr.test.AbstractDBTest;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.trg.search.Search;

public class CampaignDAOGenericCampaignTests extends AbstractDBTest{

	private Campaign campaign;
	private CampaignMessage campaignMessage;
	private User user;
	
	@Autowired
	private CampaignDAO campaignDAO;
	
	@Autowired
	private UserDAO userDAO;
	
	@Before
	public void before(){
		
		user = getUser();
		
		campaign = new Campaign("Demo Camp", null, CampaignType.FLEXI, CampaignStatus.INACTIVE, 10, 0, user.getOrganization());
		campaignMessage = new CampaignMessage("Hello, welcome to this campaign!", new Date(), new Date(), campaign );
		campaignDAO.save(campaign);
		getGeneralDao().save(campaignMessage);
		
	}
	
	@Test
	public void testGetContactsToProcessForGenericCampaign_progressLtCampaignDuration() {

		int campaignDuration = campaign.getDuration();


		int progress = campaignDuration - 2;
		Calendar joinDateCal = Calendar.getInstance();
		joinDateCal.add(Calendar.DAY_OF_MONTH, -1);
		List<CampaignContact> campaignContacts = generateContacts(5, campaign, progress, joinDateCal.getTime(), "078512010");

		List<CampaignContact> contactsFromDAO = campaignDAO.getContactsToProcessForFlexiCampaign(campaign, progress, new Date());
		Assert.assertEquals(campaignContacts.size(), contactsFromDAO.size());
		
		assertContactListsEqual(campaignContacts, contactsFromDAO);
	}

	@Test
    public void testGetContactsToProcessForGenericCampaign_joinedDateInFuture() {

        int campaignDuration = campaign.getDuration();
        int progress = campaignDuration - 2;
        Calendar joinDateCal = Calendar.getInstance();
        joinDateCal.add(Calendar.DAY_OF_MONTH, +3);
        List<CampaignContact> campaignContacts = generateContacts(3, campaign, progress, joinDateCal.getTime(), "078512010");

        Calendar joinDateCal2 = Calendar.getInstance();
        joinDateCal2.add(Calendar.DAY_OF_MONTH, -1);
        List<CampaignContact> campaignContacts2 = generateContacts(2, campaign, progress, joinDateCal2.getTime(), "078512011");

        List<CampaignContact> contactsFromDAO = campaignDAO.getContactsToProcessForFlexiCampaign(campaign, progress, new Date());
        Assert.assertEquals(campaignContacts2.size(), contactsFromDAO.size());

        assertContactListsEqual(campaignContacts2, contactsFromDAO);

    }

	@Test
	public void testGetContactsToProcessForGenericCampaign_progressEqCampaignDuration() {
		int campaignDuration = campaign.getDuration();
		
		int randomProgress = campaignDuration;
		Calendar joinDateCal = Calendar.getInstance();
		joinDateCal.add(Calendar.DAY_OF_MONTH, -1);
		List<CampaignContact> campaignContacts = generateContacts(5, campaign, randomProgress, joinDateCal.getTime(), "078512010");
		
		List<CampaignContact> contactsFromDAO = campaignDAO.getContactsToProcessForFlexiCampaign(campaign, randomProgress, new Date());
		Assert.assertEquals(campaignContacts.size(), contactsFromDAO.size());
		
		assertContactListsEqual(campaignContacts, contactsFromDAO);
	}
	
	@Test
	public void testGetContactsToProcessForGenericCampaign_progressGtCampaignDuration() {

        int campaignDuration = campaign.getDuration();
		int progress = campaignDuration+1;
		Calendar joinDateCal = Calendar.getInstance();
		joinDateCal.set(Calendar.DAY_OF_MONTH, -1);
		generateContacts(5, campaign, progress, joinDateCal.getTime(), "078512010");
		
		List<CampaignContact> contactsFromDAO = campaignDAO.getContactsToProcessForFlexiCampaign(campaign, progress, new Date());
		Assert.assertEquals(0, contactsFromDAO.size());
	}
	
	@Test
	public void testGetContactsToProcessForGenericCampaign_joinDateLtCampaignEnd() {

		campaign.setEndDate(new Date());
		campaignDAO.saveOrUpdate(campaign);
		
		int progress = 0;
		Calendar joinDateCal = Calendar.getInstance();
		joinDateCal.add(Calendar.DAY_OF_MONTH, -1);
		List<CampaignContact> campaignContacts = generateContacts(5, campaign, 0, joinDateCal.getTime(), "078512010");
		
		List<CampaignContact> contactsFromDAO = campaignDAO.getContactsToProcessForFlexiCampaign(campaign, progress, new Date());
		Assert.assertEquals(5, contactsFromDAO.size());
		
		assertContactListsEqual(campaignContacts, contactsFromDAO);
	}
	
	@Test
	public void testGetContactsToProcessForGenericCampaign_joinDateGtCampaignEnd() {

		campaign.setEndDate(new Date());
		campaignDAO.saveOrUpdate(campaign);
		
		int progress = 0;
		Calendar joinDateCal = Calendar.getInstance();
		joinDateCal.add(Calendar.DAY_OF_MONTH, 1);
		generateContacts(5, campaign, 0, joinDateCal.getTime(), "078512010");
		
		List<CampaignContact> contactsFromDAO = campaignDAO.getContactsToProcessForFlexiCampaign(campaign, progress, new Date());
		Assert.assertEquals(0, contactsFromDAO.size());
	}
	
	@Test
	public void testDAOGetCampaignMessagesFromCampaign() {
		List<CampaignMessage> allCampMessages = campaignDAO.getAllCampMessages(campaign);
		Assert.assertEquals(1, allCampMessages.size());
	}
	
	private void assertContactListsEqual(
			List<CampaignContact> campaignContacts,
			List<CampaignContact> contactsFromDAO) {
		Collections.sort(contactsFromDAO, new Comparator<CampaignContact>() {
			@Override
			public int compare(CampaignContact o1, CampaignContact o2) {
				return o1.getMsisdn().compareTo(o2.getMsisdn());
			}
		});
		for (int i = 0; i < contactsFromDAO.size(); i++) {
			CampaignContact contactFromDAO = contactsFromDAO.get(i);
			CampaignContact campaignContact = campaignContacts.get(i);

			Assert.assertEquals(contactFromDAO.getMsisdn(), campaignContact.getMsisdn());
		}
	}
	
	private User getUser() {
		Search search = new Search(User.class);
		search.addFilterEqual(User.PROP_FIRST_NAME, "firstname 8");
		User user = userDAO.searchUnique(search);
		return user;
	}
	
	private List<CampaignContact> generateContacts(int numOfContacts, Campaign campaign, int randomProgress, Date joiningDate, String baseNumber)  {
		
		List<CampaignContact> campaignContacts = new ArrayList<CampaignContact>();
		
		for(int i = 0 ; i < numOfContacts ; i++ ){
			Contact contact = new Contact(baseNumber + i, user.getOrganization());
			CampaignContact campaignContact = new CampaignContact(campaign, contact,null, randomProgress, joiningDate);
			getGeneralDao().save(contact);
			getGeneralDao().save(campaignContact);
			
			campaignContacts.add(campaignContact);
		}
		
		return campaignContacts;
	}
}
