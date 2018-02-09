package org.celllife.mobilisr.dao.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import junit.framework.Assert;

import org.celllife.mobilisr.constants.CampaignStatus;
import org.celllife.mobilisr.constants.CampaignType;
import org.celllife.mobilisr.dao.api.CampaignDAO;
import org.celllife.mobilisr.domain.Campaign;
import org.celllife.mobilisr.domain.CampaignContact;
import org.celllife.mobilisr.domain.CampaignMessage;
import org.celllife.mobilisr.domain.Contact;
import org.celllife.mobilisr.domain.ContactMsgTime;
import org.celllife.mobilisr.domain.User;
import org.celllife.mobilisr.test.AbstractDBTest;
import org.celllife.mobilisr.util.MobilisrDomainUtility;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.trg.search.Search;

public class CampaignDAORelativeCampaignTests extends AbstractDBTest {

	private Campaign campaign;
	private User user;
	int timesPerDay = 2;
	
	@Autowired
	private CampaignDAO campaignDAO;
	
	private int campaignDuration = 5;
	
	@Before
	public void before(){
		user = getUser();
		
		campaign = new Campaign("Demo Camp", null, CampaignType.DAILY, CampaignStatus.INACTIVE, campaignDuration, timesPerDay, user.getOrganization());
		campaignDAO.save(campaign);
		generateMsgsForNMsgPerDayCamp();
	}
	
	/**
	 * campaingStartDate = today
	 * currentDate = startDate
	 * contact1 join date = startDate-1, progress = 0
	 * contact2 join date = startDate, progress = 0
	 */
	@Test
	public void testGetContactsToProcessForRelativeCampaign_atCampaignStart(){
		Date currentDate = getStartDatePlusDays(0);
		testGetContactsToProcessForRelativeCampaign(0,currentDate);
	}
	
	/**
	 * campaingStartDate = today
	 * currentDate = startDate+1
	 * contact1 join date = startDate-1, progress = 1
	 * contact2 join date = startDate, progress = 1
	 * contact3 join date = startDate+1, progress = 0
	 */
	@Test
	public void testGetContactsToProcessForRelativeCampaign_1afterCampaignStart(){
		Date currentDate = getStartDatePlusDays(1);
		testGetContactsToProcessForRelativeCampaign(2,currentDate);
	}
	
	/**
	 * campaingStartDate = today
	 * currentDate = startDate+3
	 * contact1 join date = startDate-1, progress = 3
	 * contact2 join date = startDate, progress = 3
	 * contact3 join date = startDate+1, progress = 2
	 * contact4 join date = startDate+2, progress = 1
	 * contact5 join date = startDate+3, progress = 0
	 */
	@Test
	public void testGetContactsToProcessForRelativeCampaign_2afterCampaignStart(){
		Date currentDate = getStartDatePlusDays(3);
		testGetContactsToProcessForRelativeCampaign(4,currentDate);
	}
	
	/**
	 * campaingStartDate = today
	 * currentDate = startDate+5
	 * contact1 join date = startDate-1, progress = 5
	 * contact2 join date = startDate, progress = 5
	 * contact3 join date = startDate+1, progress = 4
	 * contact4 join date = startDate+2, progress = 3
	 * contact5 join date = startDate+3, progress = 2
	 * contact6 join date = startDate+4, progress = 1
	 * contact7 join date = startDate+5, progress = 0
	 */
	@Test
	public void testGetContactsToProcessForRelativeCampaign_atCampaignDuration(){
		Date currentDate = getStartDatePlusDays(campaignDuration);
		testGetContactsToProcessForRelativeCampaign(campaignDuration+1,currentDate);
	}
	
	/**
	 * campaingStartDate = today
	 * currentDate = startDate+6
	 * contact1 join date = startDate-1, progress = 6
	 * contact2 join date = startDate, progress = 6
	 * contact3 join date = startDate+1, progress = 5
	 * contact4 join date = startDate+2, progress = 4
	 * contact5 join date = startDate+3, progress = 3
	 * contact6 join date = startDate+4, progress = 2
	 * contact7 join date = startDate+5, progress = 1
	 * contact8 join date = startDate+6, progress = 0
	 */
	@Test
	public void testGetContactsToProcessForRelativeCampaign_1afterCampaignDuration(){
		Date currentDate = getStartDatePlusDays(campaignDuration+1);
		testGetContactsToProcessForRelativeCampaign(campaignDuration,currentDate);
	}
	
	/**
	 * campaingStartDate = today
	 * currentDate = startDate+7
	 * contact1 join date = startDate-1, progress = 6
	 * contact2 join date = startDate, progress = 6
	 * contact3 join date = startDate+1, progress = 6
	 * contact4 join date = startDate+2, progress = 5
	 * contact5 join date = startDate+3, progress = 4
	 * contact6 join date = startDate+4, progress = 3
	 * contact7 join date = startDate+5, progress = 2
	 * contact8 join date = startDate+6, progress = 1
	 */
	@Test
	public void testGetContactsToProcessForRelativeCampaign_2afterCampaignDuration(){
		Date currentDate = getStartDatePlusDays(campaignDuration+2);
		testGetContactsToProcessForRelativeCampaign(campaignDuration,currentDate);
	}
	
	/**
	 * campaingStartDate = today
	 * currentDate = startDate+15
	 * contact1 join date = startDate-1, progress = 6
	 * contact2 join date = startDate, progress = 6
	 * contact3 join date = startDate+1, progress = 6
	 * contact4 join date = startDate+2, progress = 6
	 * contact5 join date = startDate+3, progress = 6
	 * contact6 join date = startDate+4, progress = 6
	 * contact7 join date = startDate+5, progress = 6
	 * contact8 join date = startDate+6, progress = 6
	 */
	@Test
	public void testGetContactsToProcessForRelativeCampaign_10afterCampaignDuration(){
		Date currentDate = getStartDatePlusDays(campaignDuration+10);
		testGetContactsToProcessForRelativeCampaign(0,currentDate);
	}

    @Test
    public void testGetContactsToProcessForDailyCampaign(){

        Calendar currentDate = Calendar.getInstance();
        currentDate.add(Calendar.DATE, 2);

        List<CampaignContact> campaignContacts = new ArrayList<CampaignContact>();

        Calendar joinDate = Calendar.getInstance();
        joinDate.add(Calendar.DATE, 4);

        int progress = 1;
        Contact contact = new Contact("078512010" + 1, user.getOrganization());
        CampaignContact campaignContact = new CampaignContact(campaign, contact, null, progress, joinDate.getTime());
        getGeneralDao().save(contact);
        getGeneralDao().save(campaignContact);

        Calendar cMTCal = Calendar.getInstance();
        cMTCal.setTime(campaign.getStartDate());
        ContactMsgTime contactMsgTime = new ContactMsgTime();
        for (int msgSlot = 1; msgSlot <= timesPerDay; msgSlot++) {
         contactMsgTime = new ContactMsgTime(cMTCal.getTime(), msgSlot, campaignContact, campaign);
                getGeneralDao().save(contactMsgTime);
                cMTCal.add(Calendar.MINUTE, 10);
        }

        campaignContacts.add(campaignContact);
        campaign.setCampaignContacts(campaignContacts);

        // campaign.startDate=x, currentDate=x+2, contact.joinedDate=x+4
        List<CampaignContact> contactsFromDAO = campaignDAO.getContactsToProcessForDailyCampaign(campaign, contactMsgTime.getMsgTime(), timesPerDay, currentDate.getTime());
        Assert.assertEquals(0, contactsFromDAO.size());

        // campaign.startDate=x, currentDate=x+5, contact.joinedDate=x+4
        currentDate.add(Calendar.DATE, 3);
        contactsFromDAO = campaignDAO.getContactsToProcessForDailyCampaign(campaign, contactMsgTime.getMsgTime(), timesPerDay, currentDate.getTime());
        Assert.assertEquals(1, contactsFromDAO.size());
    }

	@SuppressWarnings("unchecked")
	public void testGetContactsToProcessForRelativeCampaign(int expectedNumContacts, Date currentDate) {
		// setup
		campaign.setCampaignContacts(generateContactsForRelativeCampaign(currentDate));
		
		int msgSlot = 1;
		Search search = new Search(ContactMsgTime.class);
		search.addFilterEqual(ContactMsgTime.PROP_MSG_SLOT, msgSlot);
		search.addFilterEqual(ContactMsgTime.PROP_CAMPAIGN, campaign);
		List<ContactMsgTime> contactMsgTimes = getGeneralDao().search(search);
		ContactMsgTime contactMsgTime = contactMsgTimes.get(0);
	
		// call test method
		List<CampaignContact> contactsFromDAO = campaignDAO.getContactsToProcessForDailyCampaign(campaign, contactMsgTime.getMsgTime(), msgSlot, currentDate);
	
		// verify
		Assert.assertEquals(expectedNumContacts, contactsFromDAO.size());
	}
	
	private Date getStartDatePlusDays(int days) {
		Calendar joinDate = Calendar.getInstance();
		joinDate.setTime(campaign.getStartDate());
		joinDate.add(Calendar.DATE, days);
		return joinDate.getTime();
	}
	
	private User getUser() {
		Search search = new Search(User.class);
		search.addFilterEqual(User.PROP_FIRST_NAME, "firstname 8");
		User user = (User) getGeneralDao().searchUnique(search);
		return user;
	}
	
	/**
	 * Generate messages for campaign based on timesPerDay field and 
	 * campaignDuration field.
	 * 
	 * @return
	 */
	private List<CampaignMessage> generateMsgsForNMsgPerDayCamp() {
		
		List<CampaignMessage> campaignMessages = new ArrayList<CampaignMessage>();
		Calendar msgDate = Calendar.getInstance();
		msgDate.setTime(campaign.getStartDate());
		
		Calendar msgCalendar = Calendar.getInstance();
		msgCalendar.setTime(campaign.getStartDate());
		for(int days = 0; days < campaignDuration ; days++){
			for (int msgSlot = 1; msgSlot <= timesPerDay ; msgSlot++) {
				msgCalendar.add(Calendar.MINUTE, 10);
				CampaignMessage campMsg = new CampaignMessage("Hello " + msgSlot + " for Day " + (days+1), msgDate.getTime(), msgCalendar.getTime(), msgSlot, campaign);
				getGeneralDao().save(campMsg);	
				campaignMessages.add(campMsg);
			}
			msgCalendar.add(Calendar.MINUTE, -10*timesPerDay);
			msgDate.add(Calendar.DAY_OF_MONTH, 1);
		}
		
		return campaignMessages;
	}
	
	/**
	 * Generate contacts for campaign based on current date such that there
	 * will be one contact joining on every day of the campaign from:
	 * (campaignStartDate - 1) until (campaignStartDate + campaignDuration + 1)
	 * 
	 * Contact current day is calculated using the currentDate parameter
	 * 
	 * e.g.
	 * campaignDuration = 3
	 * campaingStartDate = 05/01/2010
	 * currentDate = 07/01/2010
	 * contact1 join date = 04/01/2010, progress = 2
	 * contact2 join date = 05/01/2010, progress = 2
	 * contact3 join date = 06/01/2010, progress = 1
	 * contact4 join date = 07/01/2010, progress = 0
	 * contact5 not created as join date is after current date
	 *
	 * @param currentDate
	 * @return
	 */
		
	private List<CampaignContact> generateContactsForRelativeCampaign(Date currentDate) {
		List<CampaignContact> campaignContacts = new ArrayList<CampaignContact>();
		Calendar joinDate = Calendar.getInstance();
		joinDate.setTime(campaign.getStartDate());
		joinDate.add(Calendar.DATE, -1);
		
		for(int i = 0 ; i <= campaignDuration+2 ; i++ ){
			if(joinDate.getTime().compareTo(currentDate) > 0){
				// don't create contacts with join dates in the future
				break;
            }
			
			int progress = 0;
			if (joinDate.getTime().compareTo(campaign.getStartDate())<=0){
				// if joinDate is before campaign start date then current day is relative to campaign start date
				progress = MobilisrDomainUtility.getDaysBetween(campaign.getStartDate(),currentDate);
			} else {
				progress = MobilisrDomainUtility.getDaysBetween(joinDate.getTime(),currentDate);
			}
			
			// current day can never go above campaignDuration + 1
			progress = progress > campaignDuration ? campaignDuration+1 : progress;
			
			// create contacts
			Contact contact = new Contact("078512010" + i, user.getOrganization());
			CampaignContact campaignContact = new CampaignContact(campaign, contact, null, progress, joinDate.getTime());
			getGeneralDao().save(contact);
			getGeneralDao().save(campaignContact);
			
			// create contact message times
			Calendar cMTCal = Calendar.getInstance();
			cMTCal.setTime(campaign.getStartDate());
			for (int msgSlot = 1; msgSlot <= timesPerDay; msgSlot++) {
				ContactMsgTime contactMsgTime = new ContactMsgTime(cMTCal.getTime(), msgSlot, campaignContact, campaign);
				getGeneralDao().save(contactMsgTime);
				cMTCal.add(Calendar.MINUTE, 10);
			}
			campaignContacts.add(campaignContact);
			
			// increment join date
			joinDate.add(Calendar.DATE, 1);
		}
		
		return campaignContacts;
	}
}