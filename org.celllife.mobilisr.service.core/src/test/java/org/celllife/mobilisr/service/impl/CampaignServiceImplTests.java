package org.celllife.mobilisr.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.celllife.mobilisr.constants.CampaignStatus;
import org.celllife.mobilisr.constants.CampaignType;
import org.celllife.mobilisr.dao.api.CampaignDAO;
import org.celllife.mobilisr.domain.Campaign;
import org.celllife.mobilisr.domain.CampaignContact;
import org.celllife.mobilisr.domain.CampaignMessage;
import org.celllife.mobilisr.domain.Contact;
import org.celllife.mobilisr.domain.ContactMsgTime;
import org.celllife.mobilisr.domain.User;
import org.celllife.mobilisr.domain.mock.DomainMockFactory;
import org.celllife.mobilisr.service.CampaignService;
import org.celllife.mobilisr.service.ContactsService;
import org.celllife.mobilisr.service.exception.UniquePropertyException;
import org.celllife.mobilisr.test.AbstractServiceTest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;

import com.extjs.gxt.ui.client.data.BasePagingLoadConfig;
import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.trg.search.Filter;
import com.trg.search.Search;

public class CampaignServiceImplTests extends AbstractServiceTest {
		
	@Autowired
	private CampaignDAO campaignDao;
	
	@Autowired
	private CampaignService crudCampaignService;
	
	@Autowired
	private ContactsService contactService;
	
	private User user;
	
	@Before
	public void before(){
		user = getUser();
	}
	
	@Test
	public void testListAllCampaigns_allTypes_allStatus(){
		createTestCampaigns();
		
		PagingLoadConfig loadConfig = new BasePagingLoadConfig(0, 10);
		PagingLoadResult<Campaign> campaignList = crudCampaignService.listAllCampaigns(user.getOrganization(), null, loadConfig,null);
		
		List<Campaign> listOfCamps = campaignList.getData();
		Assert.assertEquals(6, campaignList.getTotalLength());
		Assert.assertEquals(6, listOfCamps.size());
	}

	@Test
	public void testListAllCampaigns_fixedType_allStatus(){
		createTestCampaigns();
		
		PagingLoadConfig loadConfig = new BasePagingLoadConfig(0, 10);
		PagingLoadResult<Campaign> campaignList = crudCampaignService.listAllCampaigns(user.getOrganization(), new CampaignType[]{CampaignType.FIXED}, loadConfig, null);
		
		List<Campaign> listOfCamps = campaignList.getData();
		Assert.assertEquals(2, campaignList.getTotalLength());
		Assert.assertEquals(2, listOfCamps.size());
	}
	
	@Test
	public void testGetPersistedContactsForCampaign(){
		Campaign campaign = new Campaign();
		campaign.setName("DevTest 103");
		campaign.setType(CampaignType.FIXED);
		campaign.setStartDate(new Date());
		campaign.setTimesPerDay(1);
		campaign.setOrganization(user.getOrganization());
		campaign.setStatus(CampaignStatus.INACTIVE);
		
		getGeneralDao().saveOrUpdate(campaign);
		
		for(int i = 0 ; i <= 3 ; i++){
			Contact c = new Contact("0345353" + i, user.getOrganization());
			getGeneralDao().save(c);	
			CampaignContact cc = new CampaignContact(campaign, c);
			getGeneralDao().save(cc);			
		}
		
		int totalContacts = campaignDao.countNumberOfContactsForCampaign(campaign, false);
		
		Assert.assertEquals(4, totalContacts);
	}
	
	@Test
	public void testGetRelativeCampaignMessages(){
		Campaign campaign = new Campaign();
		campaign.setDuration(4);
		campaign.setTimesPerDay(3);
		DomainMockFactory._().on(CampaignMessage.class).reset();
		List<CampaignMessage> existingMessages = DomainMockFactory._().on(CampaignMessage.class).create(12);
		campaign.setCampaignMessages(existingMessages);
		
		CampaignServiceImpl serviceImpl = new CampaignServiceImpl();
		CampaignDAO dao = Mockito.mock(CampaignDAO.class);
		CampaignMessage[] msgTimes = new CampaignMessage[campaign.getTimesPerDay()];
		for (int i = 0; i < campaign.getTimesPerDay(); i++){
			msgTimes[i] = new CampaignMessage();
			msgTimes[i].setMsgTime(existingMessages.get(i).getMsgTime());
		}
		Mockito.when(dao.findDefaultTimesForRelativeCampaign(campaign)).thenReturn(Arrays.asList(msgTimes));
		serviceImpl.setCampaignDao(dao);
		
		List<List<String>> data = new ArrayList<List<String>>();
		for (int i = 0; i < 15; i++) {
			ArrayList<String> list = new ArrayList<String>();
			list.add("message text");
			data.add(list);
		}
		
		List<CampaignMessage> messages = serviceImpl.getRelativeCampaignMessages(campaign, data);
		
		Assert.assertEquals(12, messages.size());
		for (int i = 0; i < messages.size(); i++) {
			CampaignMessage msg = messages.get(i);
			CampaignMessage existingMsg = existingMessages.get(i);
			if (existingMsg.getMsgDay()!= msg.getMsgDay()){
				System.out.println("err");
			}
			Assert.assertEquals(existingMsg.getMsgDay(), msg.getMsgDay());
			Assert.assertEquals(existingMsg.getMsgSlot(), msg.getMsgSlot());
			Assert.assertEquals(existingMsg.getMsgTime(), msg.getMsgTime());
		}
	}
	
	@Test
	public void testSaveOrUpdateCampaign_regenerateMsgs() throws UniquePropertyException{
		int duration = 10;
		int timesPerDay = 2;
		Campaign campaign = new Campaign("Demo Camp", null, CampaignType.DAILY, CampaignStatus.INACTIVE, duration, timesPerDay, user.getOrganization());
		campaign.setWelcomeMsg("Welcome message");
		campaign.setRebuildMessages(true);
		campaign.setMessageTimes(Arrays.asList(new Date[]{new Date(),new Date()}));
		
		campaign = crudCampaignService.saveOrUpdateCampaign(campaign, null);
		List<CampaignMessage> msgs = crudCampaignService.findCampMessageByCampaign(campaign);
		Assert.assertEquals(duration*timesPerDay, msgs.size());
		Assert.assertEquals(duration*timesPerDay+1, campaign.getCost());
	}
	
	@Test
	public void testSaveOrUpdateCampaign_notRegenerateMsgs() throws UniquePropertyException{
		int duration = 10;
		int timesPerDay = 2;
		Campaign campaign = new Campaign("Demo Camp", null, CampaignType.DAILY, CampaignStatus.INACTIVE, duration, timesPerDay, user.getOrganization());
		getGeneralDao().save(campaign);
		
		CampaignMessage campaignMessage = new CampaignMessage("Hello, welcome to this campaign!", new Date(), new Date(), campaign );
		getGeneralDao().save(campaignMessage);
		CampaignMessage campaignMessage2 = new CampaignMessage("Hello, welcome to this campaign!", new Date(), new Date(), campaign );
		getGeneralDao().save(campaignMessage2);
		
		crudCampaignService.saveOrUpdateCampaign(campaign,
				Arrays.asList(campaignMessage, campaignMessage2));
		List<CampaignMessage> msgs = crudCampaignService.findCampMessageByCampaign(campaign);
		Assert.assertEquals(2, msgs.size());
		Assert.assertEquals(2, campaign.getCost());
	}
	
	private void createTestCampaigns() {
		Campaign campaign = new Campaign();
		campaign.setName("Testing 123");
		campaign.setType(CampaignType.FLEXI);
		campaign.setStartDate(new Date());
		campaign.setTimesPerDay(1);
		campaign.setOrganization(user.getOrganization());
		campaign.setStatus(CampaignStatus.ACTIVE);
		getGeneralDao().save(campaign);
		
		campaign = new Campaign();
		campaign.setName("Testing 123abc");
		campaign.setType(CampaignType.FIXED);
		campaign.setStartDate(new Date());
		campaign.setTimesPerDay(1);
		campaign.setOrganization(user.getOrganization());
		campaign.setStatus(CampaignStatus.INACTIVE);
		getGeneralDao().save(campaign);
	}
	
	private User getUser() {
		Search search = new Search(User.class);
		search.addFilterEqual(User.PROP_FIRST_NAME, "firstname 8");
		User user = (User) getGeneralDao().searchUnique(search);
		return user;
	}
	
	@Test
	public void testAddCsvFileToCampaign() throws UniquePropertyException {		
		List<String> fieldOrder = new ArrayList<String>();
		fieldOrder.add(Contact.PROP_FIRST_NAME);
		fieldOrder.add(Contact.PROP_MSISDN);
			
		Campaign campaign = new Campaign("Demo Camp", null, CampaignType.FLEXI, CampaignStatus.INACTIVE, 10, 0, user.getOrganization());
		campaign.setWelcomeMsg("Welcome");
		getGeneralDao().save(campaign);
		
		Search search = new Search(CampaignContact.class);
		search.addFilterEqual(CampaignContact.PROP_CAMPAIGN, campaign);
		search.addFilterOr(Filter.isNull(CampaignContact.PROP_END_DATE), Filter.greaterOrEqual(CampaignContact.PROP_END_DATE, new Date()));
		int countBefore = getGeneralDao().count(search);
		
		List<ContactMsgTime> messageTimes = new ArrayList<ContactMsgTime>();
		
		Long jobid = crudCampaignService.addCsvFileToCampaign(fieldOrder,this.getClass().getClassLoader().getResource("csv-test.csv").getFile(), campaign, messageTimes, true);
		int timeout = 0;
		do {
			try { Thread.sleep(500); } catch (InterruptedException e) {}
		} while (timeout++ < 10 && !contactService.isJobComplete(jobid));
		// wait one second after job is complete for contacts to be added to the campaign
		try { Thread.sleep(1000); } catch (InterruptedException e) {}
		
		
		int countAfter = getGeneralDao().count(search);		
		Assert.assertEquals(countBefore+10, countAfter);
		
		crudCampaignService.removeCsvFileFromCampaign(fieldOrder, this.getClass().getClassLoader().getResource("csv-test.csv").getFile(), campaign);
		countAfter = getGeneralDao().count(search);
		Assert.assertEquals(countBefore, countAfter);
		
	}
	
	@Test
	public void testConvertContactToCampaignContact(){
		
		Campaign campaign = new Campaign(CampaignType.FIXED,CampaignStatus.INACTIVE, new Date(),"DevTest 103","",user.getOrganization());
		campaign.setTimesPerDay(1);
		getGeneralDao().saveOrUpdate(campaign);
		
		List<Contact> contactList = new ArrayList<Contact>();
		
		Contact c1 = new Contact("27724567346", user.getOrganization());
		getGeneralDao().save(c1);	
		contactList.add(c1);
		Contact c2 = new Contact("27724567378", user.getOrganization());
		getGeneralDao().save(c2);	
		contactList.add(c2);
		Contact c3 = new Contact("27724567350", user.getOrganization());
		getGeneralDao().save(c3);	
		contactList.add(c3);
		
		List<CampaignContact> cc = crudCampaignService.convertContactToCampaignContact(contactList, campaign);	
		
		Assert.assertNotNull(cc.get(0));
		Assert.assertEquals(3, cc.size());
		Assert.assertEquals(c1.getMsisdn(), cc.get(0).getMsisdn());
		Assert.assertEquals(c2.getMsisdn(), cc.get(1).getMsisdn());
		Assert.assertEquals(c3.getMsisdn(), cc.get(2).getMsisdn());	
	}
	
}

