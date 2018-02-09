package org.celllife.mobilisr.service.impl;

import org.celllife.mobilisr.api.messaging.SmsMt;
import org.celllife.mobilisr.api.validation.MsisdnRule;
import org.celllife.mobilisr.api.validation.MsisdnValidator;
import org.celllife.mobilisr.api.validation.ValidationError;
import org.celllife.mobilisr.api.validation.ValidatorFactory;
import org.celllife.mobilisr.constants.CampaignStatus;
import org.celllife.mobilisr.constants.ErrorCode;
import org.celllife.mobilisr.dao.api.CampaignDAO;
import org.celllife.mobilisr.domain.Campaign;
import org.celllife.mobilisr.domain.CampaignMessage;
import org.celllife.mobilisr.domain.Channel;
import org.celllife.mobilisr.domain.User;
import org.celllife.mobilisr.domain.mock.DomainMockFactory;
import org.celllife.mobilisr.exception.InsufficientBalanceException;
import org.celllife.mobilisr.exception.MsisdnFormatException;
import org.celllife.mobilisr.service.MailService;
import org.celllife.mobilisr.service.UserBalanceService;
import org.celllife.mobilisr.service.exception.CampaignStateException;
import org.celllife.mobilisr.service.message.route.MessageService;
import org.celllife.mobilisr.service.qrtz.QuartzService;
import org.celllife.mobilisr.service.utility.MobilisrUtility;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.quartz.SchedulerException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CampaignScheduleServiceTest {

	@Mock
	private QuartzService quartzService;
	
	@Mock
	private UserBalanceService userBalanceService;
	
	@Mock
	private MailService mailService;

	@Mock
	private MessageService messageService;
	
	@Mock
	private CampaignDAO campaignDAO;
	
	@Mock
	private ValidatorFactory validatorFactory;

	private CampaignScheduleServiceImpl service;
	
	@Before
	public void setup(){
		service = new CampaignScheduleServiceImpl();
		service.setCampaignDAO(campaignDAO);
		service.setMessageService(messageService);
		service.setClientAlertService(mailService);
		service.setQuartzService(quartzService);
		service.setUserBalanceService(userBalanceService);
		service.setValidatorFactory(validatorFactory);
		
		when(validatorFactory.getMsisdnValidator()).thenReturn(
				new MsisdnValidator(Arrays.asList(new MsisdnRule("SA", "27",
						"^27[1-9][0-9]{8}$"))));
	}
	
	@Test
	public void testSendTestSms() throws InsufficientBalanceException, MsisdnFormatException{
		Campaign campaign = DomainMockFactory._().on(Campaign.class).create();
		User user = DomainMockFactory._().on(User.class).create();
		Channel channel = new Channel();
		String handler = "testHandler";
		channel.setHandler(handler);
		String smsMsg = "short message";
		int amountToReserve = MobilisrUtility.calculateMessageCost(smsMsg, 1);
		
		service.sendTestSMS(campaign, user, "27725467895", smsMsg);
		
		verify(userBalanceService).reserveAmount(eq(campaign.getOrganization()), eq(amountToReserve), eq(campaign.getIdentifierString()), eq(user), anyString());
		verify(messageService).sendMessage(any(SmsMt.class));
	}
	
	@Test(expected=MsisdnFormatException.class)
	public void testSendTestSms_invlidMsisdn() throws InsufficientBalanceException, MsisdnFormatException{
		Campaign campaign = DomainMockFactory._().on(Campaign.class).create();
		User user = DomainMockFactory._().on(User.class).create();
		
		when(validatorFactory.validateMsisdn(any(String.class))).thenReturn(
				new ValidationError(ErrorCode.INVALID_MSISDN, ""));
		
		service.sendTestSMS(campaign, user, "2772546d7895", "short message");
	}
	
	@Test
	public void testScheduleFixedCampaign() throws InsufficientBalanceException, CampaignStateException{
		Campaign campaign = DomainMockFactory._().on(Campaign.class).create();
		List<CampaignMessage> messages = DomainMockFactory._().on(CampaignMessage.class).create(1);
		User user = DomainMockFactory._().on(User.class).create();
		int numcontacts = 12;
		when(campaignDAO.getAllCampMessages(eq(campaign))).thenReturn(messages);
		when(campaignDAO.countNumberOfContactsForCampaign(eq(campaign), eq(false))).thenReturn(numcontacts);
		when(campaignDAO.getCampaignStatus(eq(campaign.getId()))).thenReturn(campaign.getStatus());
		
		service.scheduleCampaign(campaign, user);
		
		// verify
		verify(userBalanceService).reserveAmount(eq(campaign.getOrganization()), eq(numcontacts), eq(campaign.getIdentifierString()), eq(user), anyString());
		verify(campaignDAO).saveOrUpdate(eq(campaign));
		verify(quartzService).scheduleFixedCampaignJob(eq(campaign), eq(user), any(Date.class), eq(0L));
		
	}
	
	@Test
	public void testScheduleFixedCampaign_futureDate() throws InsufficientBalanceException, CampaignStateException{
		Campaign campaign = DomainMockFactory._().on(Campaign.class).create();
		List<CampaignMessage> messages = DomainMockFactory._().on(CampaignMessage.class).create(1);
		User user = DomainMockFactory._().on(User.class).create();
		int numcontacts = 12;
		when(campaignDAO.getAllCampMessages(eq(campaign))).thenReturn(messages);
		when(campaignDAO.countNumberOfContactsForCampaign(eq(campaign), eq(false))).thenReturn(numcontacts);
		when(campaignDAO.getCampaignStatus(eq(campaign.getId()))).thenReturn(campaign.getStatus());
		
		service.scheduleCampaign(campaign, user);
		
		// verify
		CampaignMessage msg = messages.get(0);
		Date expectedDate = MobilisrUtility.combineDateAndTime(msg.getMsgDate(), msg.getMsgTime());
		verify(userBalanceService).reserveAmount(eq(campaign.getOrganization()), eq(numcontacts), eq(campaign.getIdentifierString()), eq(user), anyString());
		verify(campaignDAO).saveOrUpdate(eq(campaign));
		verify(quartzService).scheduleFixedCampaignJob(eq(campaign), eq(user), eq(expectedDate), eq(0L));
	}
	
	@Test
	public void testProcessCampaignFinish_no_end_dates() throws SchedulerException{
		// setup mocking
		List<Campaign> campaigns = DomainMockFactory._().on(Campaign.class).create(3);
		List<Campaign> returns = new ArrayList<Campaign>();
		returns.addAll(campaigns);
		when(campaignDAO.getRunningRelativeCampaignsWithEndDate()).thenReturn(returns);
		
		// call test method
		service.processCampaignFinish();
		
		// verify results
		verify(campaignDAO, never()).isAllContactsProcessedForCampaign(any(Campaign.class));
	}
	
	@Test
	public void testProcessCampaignFinish_not_all_contacts_processed() throws SchedulerException{
		// setup mocking
		Campaign campaign = DomainMockFactory._().on(Campaign.class).create();
		campaign.setEndDate(new Date());
		List<Campaign> returns = new ArrayList<Campaign>();
		returns.add(campaign);
		when(campaignDAO.getRunningRelativeCampaignsWithEndDate()).thenReturn(returns);
		when(campaignDAO.isAllContactsProcessedForCampaign(eq(campaign))).thenReturn(false);
		
		// call test method
		service.processCampaignFinish();
		
		// verify results
		verify(quartzService, never()).clearScheduleForCampaign(eq(campaign));
	}
	
	@Test
	public void testProcessCampaignFinish_all_contacts_processed() throws SchedulerException{
		// setup mocking
		Campaign campaign = DomainMockFactory._().on(Campaign.class).create();
		campaign.setEndDate(new Date());
		List<Campaign> returns = new ArrayList<Campaign>();
		returns.add(campaign);
		when(campaignDAO.getRunningRelativeCampaignsWithEndDate()).thenReturn(returns);
		when(campaignDAO.isAllContactsProcessedForCampaign(eq(campaign))).thenReturn(true);
		
		// call test method
		service.processCampaignFinish();
		
		// verify results
		verify(quartzService, times(1)).clearScheduleForCampaign(eq(campaign));
		verify(campaignDAO, times(1)).saveOrUpdate(eq(campaign));
		Assert.assertEquals(CampaignStatus.FINISHED, campaign.getStatus());
	}
}
