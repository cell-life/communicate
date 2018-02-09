package org.celllife.mobilisr.service.integrationtest;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import junit.framework.Assert;

import org.celllife.mobilisr.api.mock.DtoMockFactory;
import org.celllife.mobilisr.api.rest.CampaignDto;
import org.celllife.mobilisr.api.rest.ContactDto;
import org.celllife.mobilisr.constants.ApiVersion;
import org.celllife.mobilisr.constants.CampaignStatus;
import org.celllife.mobilisr.constants.CampaignType;
import org.celllife.mobilisr.domain.Campaign;
import org.celllife.mobilisr.domain.CampaignContact;
import org.celllife.mobilisr.domain.CampaignMessage;
import org.celllife.mobilisr.domain.Contact;
import org.celllife.mobilisr.domain.User;
import org.celllife.mobilisr.exception.MobilisrException;
import org.celllife.mobilisr.service.CampaignRestService;
import org.celllife.mobilisr.test.AbstractServiceTest;
import org.junit.Before;
import org.junit.Test;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.trg.search.Search;

public class CampaignRestServiceImplIntegrationTest extends AbstractServiceTest{

	private static final Logger log = LoggerFactory.getLogger(CampaignRestServiceImplIntegrationTest.class);

	@Autowired
	private Scheduler quartzScheduler;

	@Autowired
	private CampaignRestService service;

	private User loggedInUser;
	private DateFormat timeFormat;

	@Before
	public void setup() {
		Search search = new Search(User.class);
		search.addFilterEqual(User.PROP_USERNAME, "username 0");
		loggedInUser = (User) getGeneralDao().searchUnique(search);
		timeFormat = new SimpleDateFormat("hh:mm");
	}

	@Test
	public void testCreateAndRunCampaign() throws MobilisrException, SchedulerException{
		// setup 
		CampaignDto campDto = DtoMockFactory._().on(CampaignDto.class).withMode(DtoMockFactory.MODE_POST).create();

		// call test method
		Campaign campaign = service.createAndRunCampaign(loggedInUser, campDto, ApiVersion.getLatest());
		
		// verify
		Assert.assertNotNull(campaign);
		String[] triggerNames = quartzScheduler.getTriggerNames(campaign.getIdentifierString());
		Assert.assertEquals(1, triggerNames.length);
		Assert.assertTrue(triggerNames[0].startsWith(campaign.getName()));
		
		// cleanup
		quartzScheduler.unscheduleJob(triggerNames[0], campaign.getIdentifierString());
	}
	
	@Test
	@Transactional
	public void testUpdateContactDetails() throws MobilisrException{
		// setup 
		ContactDto contactDto = DtoMockFactory._().on(ContactDto.class).create();
		String newMsisdn = contactDto.getMsisdn();
		Contact existingContact = getGeneralDao().findAll(Contact.class).get(0);
		log.debug("update contact [id={}] [oldMsisdn={}] [newMsisdn={}]", 
				new Object[] {existingContact.getId(), existingContact.getMsisdn(), newMsisdn});

		// call test method
		service.updateContactDetails(loggedInUser, existingContact.getMsisdn(), contactDto);
		
		// verify
		Contact saved = getGeneralDao().find(Contact.class, existingContact.getId());
		Assert.assertEquals(newMsisdn, saved.getMsisdn());
		
		Search search = new Search(CampaignContact.class);
		search.addFilterEqual(CampaignContact.PROP_CONTACT, existingContact);
		@SuppressWarnings("unchecked")
		List<CampaignContact> campContacts = getGeneralDao().search(search);
		for (CampaignContact cc : campContacts) {
			Assert.assertEquals(newMsisdn, cc.getMsisdn());
		}
	}
	
	@Test
	public void testAddContactToCampaign() throws ParseException, MobilisrException{
		
		List<ContactDto> contacts = DtoMockFactory._().on(ContactDto.class).create(2);
		Date time1 = timeFormat.parse("08:00");
		contacts.get(0).setContactMessageTimes(Arrays.asList(time1));

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND,0);
        calendar.add(Calendar.DATE, 5);
        Date joiningDate = calendar.getTime();
        String joiningDateString = new SimpleDateFormat("yyyy-MM-dd").format(joiningDate);

        for (ContactDto contactDto : contacts) {
            contactDto.setStartDate(joiningDateString);
        }
		
		Campaign campaign = createTestCampaign();
		Date time2 = timeFormat.parse("07:00");
		CampaignMessage campaignMessage = new CampaignMessage("test message", new Date(), time2, campaign);
		getGeneralDao().save(campaignMessage);
			
		service.addContactsToCampaign(loggedInUser, campaign.getId(), contacts, ApiVersion.getLatest());
		
		Search search = new Search(CampaignContact.class);
		search.addFilterEqual(CampaignContact.PROP_CAMPAIGN, campaign);
		@SuppressWarnings("unchecked")
		List<CampaignContact> campContacts = getGeneralDao().search(search);
		Assert.assertEquals(2, campContacts.size());
		for (CampaignContact cc : campContacts) {
			if (cc.getMsisdn().equals(contacts.get(0).getMsisdn())){
				Assert.assertEquals(1, cc.getContactMsgTimes().size());
				Assert.assertEquals(0, cc.getContactMsgTimes().get(0).getMsgSlot());
				Assert.assertEquals(time1, cc.getContactMsgTimes().get(0).getMsgTime());
                Assert.assertEquals(joiningDate, cc.getJoiningDate());
			} else {
				Assert.assertEquals(1, cc.getContactMsgTimes().size());
				Assert.assertEquals(0, cc.getContactMsgTimes().get(0).getMsgSlot());
				Assert.assertEquals(time2, cc.getContactMsgTimes().get(0).getMsgTime());
                Assert.assertEquals(joiningDate, cc.getJoiningDate());
			}
		}
	}
	
	private Campaign createTestCampaign() throws ParseException {
		Campaign campaign = new Campaign();
		campaign.setName("Testing 123");
		campaign.setType(CampaignType.DAILY);
		campaign.setStartDate(new Date());
		campaign.setTimesPerDay(1);
		campaign.setOrganization(loggedInUser.getOrganization());
		campaign.setStatus(CampaignStatus.ACTIVE);
		getGeneralDao().save(campaign);
		
		return campaign;
	}
	
}
