package org.celllife.mobilisr.service.qrtz.beans;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.celllife.mobilisr.constants.CampaignStatus;
import org.celllife.mobilisr.dao.api.CampaignDAO;
import org.celllife.mobilisr.domain.Campaign;
import org.celllife.mobilisr.domain.CampaignContact;
import org.celllife.mobilisr.domain.mock.DomainMockFactory;
import org.celllife.mobilisr.service.SettingService;
import org.celllife.mobilisr.service.message.route.MessageService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.quartz.SchedulerException;

import com.extjs.gxt.ui.client.data.PagingLoadConfig;

@RunWith(MockitoJUnitRunner.class)
public class FixedCampaignJobTest {

	private FixedCampaignJob runner;
	
	@Mock
	private CampaignDAO campaignDAO;
	
	@Mock
	private MessageService messageService;

	@Mock
	private SettingService settingService;

	private Campaign campaign;

	private long campaignId = 7L;

	private long userId = 3L;

	@Before
	public void setup() throws SchedulerException{
		runner = new FixedCampaignJob();
		runner.setCampaignDAO(campaignDAO);
		runner.setMessageService(messageService);
		runner.setSettingService(settingService);
		
		campaign = DomainMockFactory._().on(Campaign.class).create();
		when(campaignDAO.find(eq(campaignId))).thenReturn(campaign);
	}
	
	@Test
	@SuppressWarnings("unchecked")
	public void testManageSmsTaskExecution() throws Exception{
		when(campaignDAO.getCampMsgForCampaign(campaign)).thenReturn("message");
		List<CampaignContact> list1 = DomainMockFactory._().on(CampaignContact.class).create(4);
		List<CampaignContact> list2 = DomainMockFactory._().on(CampaignContact.class).create(4);
		List<CampaignContact> list3 = new ArrayList<CampaignContact>();
		when(campaignDAO.getContactsInPaginationForCampaign(eq(campaign), any(PagingLoadConfig.class)))
			.thenReturn(list1, list2, list3);
		
		Long transactionRef = 17L;
		runner.sendMessagesForCampaign(campaignId, userId, transactionRef);
		
		verify(messageService, times(2)).sendMessage(any(SmsBatchConfig.class));
		verify(campaignDAO).updateCampaignStatus(eq(campaign.getId()), eq(CampaignStatus.RUNNING));
	}
}
