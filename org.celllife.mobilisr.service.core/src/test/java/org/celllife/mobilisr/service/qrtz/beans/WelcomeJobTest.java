package org.celllife.mobilisr.service.qrtz.beans;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.celllife.mobilisr.api.messaging.SmsMt;
import org.celllife.mobilisr.dao.api.CampaignDAO;
import org.celllife.mobilisr.dao.api.UserDAO;
import org.celllife.mobilisr.domain.Campaign;
import org.celllife.mobilisr.domain.CampaignContact;
import org.celllife.mobilisr.domain.Channel;
import org.celllife.mobilisr.domain.User;
import org.celllife.mobilisr.domain.mock.DomainMockFactory;
import org.celllife.mobilisr.exception.InsufficientBalanceException;
import org.celllife.mobilisr.service.MailService;
import org.celllife.mobilisr.service.UserBalanceService;
import org.celllife.mobilisr.service.message.route.MessageService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.ApplicationContext;

@RunWith(MockitoJUnitRunner.class)
public class WelcomeJobTest {
	
	@Mock
	private UserDAO userDAO;
	
	@Mock
	private CampaignDAO campaignDao;
	
	@Mock
	ApplicationContext applicationContext;
	
	@Mock
	MessageService messageService;

	private WelcomeJob job;

	@Mock
	private MailService mailService;

	@Mock
	private UserBalanceService userBalanceService;
	
	@Before
	public void setup(){
		job = new WelcomeJob();
		job.setApplicationContext(applicationContext);
		job.setCampaignDao(campaignDao);
		job.setMessageService(messageService);
		job.setUserDAO(userDAO);
		job.setClientAlertService(mailService);
		job.setUserBalanceService(userBalanceService);
	}
	
	@Test
	public void testJob() throws InsufficientBalanceException{
		// setup
		long userId = 11L;
		long campaignId = 17L;
		
		Campaign campaign = DomainMockFactory._().on(Campaign.class).create();
		when(campaignDao.find(eq(campaignId))).thenReturn(campaign);
		User user = DomainMockFactory._().on(User.class).create();
		when(userDAO.find(eq(userId))).thenReturn(user);
		
		List<CampaignContact> contacts = DomainMockFactory._().on(CampaignContact.class).create(5);
		when(campaignDao.getCampaignContactsNeedingWelcomeMessage(eq(campaign))).thenReturn(contacts);
		
		Channel channel = new Channel();
		channel.setName("channel");
		
		when(userBalanceService.reserveAmount(
						eq(campaign.getOrganization()), anyInt(),
						eq(campaign.getIdentifierString()), eq(user),
						any(String.class))).thenReturn(
				123L);
		// call test method
		job.sendWelcomeMessages(campaignId, userId);
		
		// verify
		verify(messageService, times(contacts.size())).sendMessage(any(SmsMt.class));
		verify(campaignDao, times(1)).markCampaignContactsAsReceivedWelcomeMessage(eq(contacts));
	}

}
