package org.celllife.mobilisr.service.action;

import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.chain.Context;
import org.apache.commons.chain.impl.ContextBase;
import org.celllife.mobilisr.api.messaging.SmsMt;
import org.celllife.mobilisr.constants.CampaignStatus;
import org.celllife.mobilisr.dao.api.MobilisrGeneralDAO;
import org.celllife.mobilisr.domain.Campaign;
import org.celllife.mobilisr.domain.CampaignContact;
import org.celllife.mobilisr.domain.Contact;
import org.celllife.mobilisr.domain.MessageFilter;
import org.celllife.mobilisr.domain.Organization;
import org.celllife.mobilisr.domain.SmsLog;
import org.celllife.mobilisr.domain.User;
import org.celllife.mobilisr.service.CampaignService;
import org.celllife.mobilisr.service.filter.Action;
import org.celllife.mobilisr.service.message.route.MessageService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CampaignManagementCommandTest {

	@Mock
	private CampaignService campaignService;
	
	@Mock
	private MobilisrGeneralDAO generalDao;
	
	@Mock
	private MessageService messageService;
	
	private AddToCampaignAction addAction = new AddToCampaignAction();
	private RemoveFromCampaignAction removeAction = new RemoveFromCampaignAction();
	private AddRemoveFromCampaignAction addRemoveAction = new AddRemoveFromCampaignAction();
	
	@Before
	public void setup() {
		addAction.setDao(generalDao);
		addAction.setCampaignService(campaignService);
		removeAction.setDao(generalDao);
		removeAction.setCampaignService(campaignService);
		addRemoveAction.setDao(generalDao);
		addRemoveAction.setCampaignService(campaignService);
		addRemoveAction.setMessageService(messageService);
	}
	
	@Test
	public void addToCampaignTest() throws Exception {	
		Context context = createContext("0768198075", "Cell-Life", 1L);
		Contact contact = (Contact)context.get(GetContactCommand.CONTACT);
		
		// setup mock
		Campaign campaign = new Campaign();
		campaign.setStatus(CampaignStatus.ACTIVE);
        campaign.setId(1L);
		Long campaignId = (Long)context.get(CampaignManagementCommand.CAMPAIGN_ID);
		when(generalDao.find(Campaign.class, campaignId)).thenReturn(campaign);
		
		CampaignContact campaignContact = new CampaignContact(campaign, contact);
		when(campaignService.convertContactToCampaignContact(
						eq(Arrays.asList(contact)), eq(campaign))).thenReturn(
				Arrays.asList(campaignContact));
		
		// run test
		addAction.execute(context);
		
		// verify
		verify(campaignService).saveOrUpdateCampaignContact(argThat(new ArgumentMatcher<CampaignContact>() {
			@Override
			public boolean matches(Object argument) {
				CampaignContact cc = (CampaignContact)argument;
				return cc.getEndDate() == null && cc.getProgress() == 0;
			}
		}));
		
		verify(campaignService).rescheduleRelativeCampaign(eq(campaign), (User) isNull());
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void addToCampaignTest_restartExisting() throws Exception {	
		Context context = createContext("0768198075", "Cell-Life", 1L);
		context.put(AddToCampaignAction.RESTART_EXISTING, true);
		Contact contact = (Contact)context.get(GetContactCommand.CONTACT);
		
		// setup mock
		Campaign campaign = new Campaign();
		campaign.setStatus(CampaignStatus.ACTIVE);
		Long campaignId = (Long)context.get(CampaignManagementCommand.CAMPAIGN_ID);
		when(generalDao.find(Campaign.class, campaignId)).thenReturn(campaign);
		
		CampaignContact campaignContact = new CampaignContact(campaign, contact);
		campaignContact.setEndDate(new Date());
		campaignContact.setProgress(10);
		when(campaignService.convertContactToCampaignContact(
						eq(Arrays.asList(contact)), eq(campaign))).thenReturn(
				Arrays.asList(campaignContact));
		
		// run test
		addAction.execute(context);
		
		// verify
		verify(campaignService).saveOrUpdateCampaignContact(argThat(new ArgumentMatcher<CampaignContact>() {
			@Override
			public boolean matches(Object argument) {
				CampaignContact cc = (CampaignContact)argument;
				return cc.getEndDate() == null && cc.getProgress() == 0;
			}
		}));
		verify(campaignService).rescheduleRelativeCampaign(eq(campaign), (User) isNull());
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void removeFromCampaignTest() throws Exception {	
		Context context = createContext("0768198075", "Cell-Life", 1L);
		context.put(AddToCampaignAction.RESTART_EXISTING, true);
		Contact contact = (Contact)context.get(GetContactCommand.CONTACT);
		
		// setup mock
		Campaign campaign = new Campaign();
		Long campaignId = (Long)context.get(CampaignManagementCommand.CAMPAIGN_ID);
		when(generalDao.find(Campaign.class, campaignId)).thenReturn(campaign);
		
		CampaignContact campaignContact = new CampaignContact(campaign, contact);
		List<CampaignContact> conatctList = Arrays.asList(campaignContact);
		when(campaignService.convertContactToCampaignContact(
						eq(Arrays.asList(contact)), eq(campaign))).thenReturn(
				conatctList);
		
		// run test
		removeAction.execute(context);
		
		// verify
		verify(campaignService).removeContactsFromCampaign(eq(campaign), eq(conatctList));
		verify(campaignService).rescheduleRelativeCampaign(eq(campaign), (User) isNull());
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void addRemoveFromCampaignTest_add() throws Exception {	
		Context context = createContext("0768198075", "Cell-Life", 1L);
		final Contact contact = (Contact)context.get(GetContactCommand.CONTACT);
		final String message = "response message";
		context.put(AddRemoveFromCampaignAction.SUBSCRIBE, message);
		
		// setup mock
		Campaign campaign = new Campaign();
        campaign.setStatus(CampaignStatus.ACTIVE);
		Long campaignId = (Long)context.get(CampaignManagementCommand.CAMPAIGN_ID);
		when(generalDao.find(Campaign.class, campaignId)).thenReturn(campaign);
		
		CampaignContact campaignContact = new CampaignContact(campaign, contact);
		when(campaignService.convertContactToCampaignContact(
						eq(Arrays.asList(contact)), eq(campaign))).thenReturn(
				Arrays.asList(campaignContact));
		
		// run test
		addRemoveAction.execute(context);
		
		// verify
		verify(campaignService).saveOrUpdateCampaignContact(argThat(new ArgumentMatcher<CampaignContact>() {
			@Override
			public boolean matches(Object argument) {
				CampaignContact cc = (CampaignContact)argument;
				return cc.getEndDate() == null && cc.getProgress() == 0;
			}
		}));
		verify(campaignService).rescheduleRelativeCampaign(eq(campaign), (User) isNull());
		verify(messageService).sendMessage(argThat(new ArgumentMatcher<SmsMt>() {
			@Override
			public boolean matches(Object argument) {
				SmsMt mt = (SmsMt)argument;
				return mt.getMsisdn().equals(contact.getMsisdn())
					&& mt.getMessage().equals(message);
			}
		}));
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void addRemoveFromCampaignTest_remove() throws Exception {	
		Context context = createContext("0768198075", "Cell-Life", 1L);
		final Contact contact = (Contact)context.get(GetContactCommand.CONTACT);
		final String message = "response message";
		context.put(AddRemoveFromCampaignAction.UNSUBSCRIBE, message);
		
		// setup mock
		Campaign campaign = new Campaign();
		Long campaignId = (Long)context.get(CampaignManagementCommand.CAMPAIGN_ID);
		when(generalDao.find(Campaign.class, campaignId)).thenReturn(campaign);
		
		CampaignContact campaignContact = new CampaignContact(campaign, contact);
		campaignContact.setId(3L);
		when(campaignService.convertContactToCampaignContact(
						eq(Arrays.asList(contact)), eq(campaign))).thenReturn(
				Arrays.asList(campaignContact));
		
		// run test
		addRemoveAction.execute(context);
		
		// verify
		verify(campaignService).saveOrUpdateCampaignContact(argThat(new ArgumentMatcher<CampaignContact>() {
			@Override
			public boolean matches(Object argument) {
				CampaignContact cc = (CampaignContact)argument;
				return cc.getEndDate() != null;
			}
		}));
		verify(campaignService).rescheduleRelativeCampaign(eq(campaign), (User) isNull());
		verify(messageService).sendMessage(argThat(new ArgumentMatcher<SmsMt>() {
			@Override
			public boolean matches(Object argument) {
				SmsMt mt = (SmsMt)argument;
				return mt.getMsisdn().equals(contact.getMsisdn())
					&& mt.getMessage().equals(message);
			}
		}));
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void addRemoveFromCampaignTest_addAndRestart() throws Exception {	
		Context context = createContext("0768198075", "Cell-Life", 1L);
		context.put(AddRemoveFromCampaignAction.RESTART_EXISTING, true);
		final Contact contact = (Contact)context.get(GetContactCommand.CONTACT);
		final String message = "response message";
		context.put(AddRemoveFromCampaignAction.SUBSCRIBE, message);
		
		// setup mock
		Campaign campaign = new Campaign();
        campaign.setStatus(CampaignStatus.ACTIVE);
		Long campaignId = (Long)context.get(CampaignManagementCommand.CAMPAIGN_ID);
		when(generalDao.find(Campaign.class, campaignId)).thenReturn(campaign);
		
		CampaignContact campaignContact = new CampaignContact(campaign, contact);
		campaignContact.setEndDate(new Date());
		campaignContact.setProgress(10);
		when(campaignService.convertContactToCampaignContact(
						eq(Arrays.asList(contact)), eq(campaign))).thenReturn(
				Arrays.asList(campaignContact));
		
		// run test
		addRemoveAction.execute(context);
		
		// verify
		verify(campaignService).saveOrUpdateCampaignContact(argThat(new ArgumentMatcher<CampaignContact>() {
			@Override
			public boolean matches(Object argument) {
				CampaignContact cc = (CampaignContact)argument;
				return cc.getEndDate() == null && cc.getProgress() == 0;
			}
		}));
		verify(campaignService).rescheduleRelativeCampaign(eq(campaign), (User) isNull());
		verify(messageService).sendMessage(argThat(new ArgumentMatcher<SmsMt>() {
			@Override
			public boolean matches(Object argument) {
				SmsMt mt = (SmsMt)argument;
				return mt.getMsisdn().equals(contact.getMsisdn())
					&& mt.getMessage().equals(message);
			}
		}));
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void addRemoveFromCampaignTest_executeNoMessage() throws Exception {	
		Context context = createContext("0768198075", "Cell-Life", 1L);
		context.put(AddRemoveFromCampaignAction.RESTART_EXISTING, true);
		final Contact contact = (Contact)context.get(GetContactCommand.CONTACT);
		
		// setup mock
		Campaign campaign = new Campaign();
		Long campaignId = (Long)context.get(CampaignManagementCommand.CAMPAIGN_ID);
		when(generalDao.find(Campaign.class, campaignId)).thenReturn(campaign);
		
		CampaignContact campaignContact = new CampaignContact(campaign, contact);
		when(campaignService.convertContactToCampaignContact(
						eq(Arrays.asList(contact)), eq(campaign))).thenReturn(
				Arrays.asList(campaignContact));
		
		// run test
		addRemoveAction.execute(context);
		
		// verify
		verify(messageService, never()).sendMessage(any(SmsMt.class));
	}
	
	@SuppressWarnings("unchecked")
	protected Context createContext(String msisdn, String orgName, Long campaignId) {
		Context context = new ContextBase();
		MessageFilter filter = new MessageFilter();
		context.put(Action.FILTER, filter);
		Organization org = new Organization();
		org.setName(orgName);
		SmsLog smsLog = new SmsLog();
		smsLog.setMsisdn(msisdn);
		smsLog.setOrganization(org);
		context.put(Action.SMS_LOG, smsLog);
		Contact contact = new Contact(msisdn, org);
		context.put(GetContactCommand.CONTACT, contact);
		context.put(CampaignManagementCommand.CAMPAIGN_ID, campaignId);
		return context;
	}
}
