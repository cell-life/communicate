package org.celllife.mobilisr.service.impl;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.celllife.mobilisr.constants.CampaignType;
import org.celllife.mobilisr.domain.Campaign;
import org.celllife.mobilisr.domain.CampaignContact;
import org.celllife.mobilisr.domain.CampaignMessage;
import org.celllife.mobilisr.domain.ContactMsgTime;
import org.celllife.mobilisr.domain.mock.DomainMockFactory;
import org.celllife.mobilisr.service.CampaignService;
import org.celllife.mobilisr.service.ContactsService;
import org.celllife.mobilisr.service.exception.DataexportException;
import org.celllife.mobilisr.util.CommunicateHome;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import com.extjs.gxt.ui.client.data.BasePagingLoadResult;
import com.extjs.gxt.ui.client.data.PagingLoadConfig;

@RunWith(MockitoJUnitRunner.class)
public class ExportServiceImplTest {
	
	@Mock
	private CampaignService campaignService;

	private ExportServiceImpl service;
	
	@Mock
	private ContactsService contactService;
	
	@Before
	public void setup(){
		service = new ExportServiceImpl();
		service.setCampaignService(campaignService);
		service.setContactService(contactService);
	}
	
	@Test
	public void testExportCampaignMessages() throws IOException, DataexportException{
		// setup mocking
		List<CampaignMessage> messages = DomainMockFactory._().on(CampaignMessage.class).create(10);
		Long campaignId = 3L;
		Campaign campaign = new Campaign();
		campaign.setName("test name for campaig'n");
		when(campaignService.getCampaign(eq(campaignId))).thenReturn(campaign);
		when(campaignService.findCampMessageByCampaign(eq(campaign))).thenReturn(messages);
		
		// call test method
		String filename = service.exportCampaignMessages(3L);
		
		// verify results
		Assert.assertTrue(filename.contains("test_name_for_campaig_n"));
		
		File folder = CommunicateHome.getDownloadsFolder();
		String path = folder.getAbsolutePath() + File.separator + filename;
		FileInputStream input = new FileInputStream(path);
		
		String csv = IOUtils.toString(input);
		for (CampaignMessage message : messages) {
			Assert.assertTrue(csv.contains(message.getMessage()));
		}
	}
	
	@Test
	public void testExportCampaignContacts() throws IOException, DataexportException{
		// setup mocking
		
//		List<CampaignMessage> messages = DomainMockFactory._().on(CampaignMessage.class).create(10);
		final List<CampaignContact> campContacts = DomainMockFactory._().on(CampaignContact.class).create(10);

		Long campaignId = 3L;
		
		CampaignType[] types = CampaignType.values();
		for (int j = 0; j< types.length; j++){
			Campaign campaign = DomainMockFactory._().on(Campaign.class).create();
			campaign.setType(types[j]);
			campaign.setName("test name for campaig'n");
		
			
			for (CampaignContact campaignContact : campContacts) {
				campaignContact.setCampaign(campaign);
				campaignContact.setJoiningDate(new Date());
				campaignContact.setProgress(2);
				
				List<ContactMsgTime> times = new ArrayList<ContactMsgTime>();
				
				for (int i = 0; i< 4; i++){
					ContactMsgTime c = new ContactMsgTime();
					c.setMsgTime(new Date());
					times.add(c);
				}
				campaignContact.setContactMsgTimes(times);
			}
			
			when(campaignService.getCampaign(eq(campaignId))).thenReturn(campaign);
			doAnswer(new Answer<BasePagingLoadResult<CampaignContact>>() {
				@Override
				public BasePagingLoadResult<CampaignContact> answer(InvocationOnMock invocation)
						throws Throwable {
					PagingLoadConfig config = (PagingLoadConfig) invocation.getArguments()[1];
					int offset = config.getOffset();
					if (offset > 0){
						return new BasePagingLoadResult<CampaignContact>(new ArrayList<CampaignContact>());
					}
					return new BasePagingLoadResult<CampaignContact>(campContacts);
				}
			}).when(contactService).listAllCampaignContactsForCampaign(eq(campaign), any(PagingLoadConfig.class), eq(true), eq(true));

			// call test method
			String filename = service.exportCampaignContacts(3L);
			
			// verify results
			Assert.assertTrue(filename.contains("test_name_for_campaig_n"));

			File folder = CommunicateHome.getDownloadsFolder();
			String path = folder.getAbsolutePath() + File.separator + filename;
			FileInputStream input = new FileInputStream(path);
			
			String csv = IOUtils.toString(input);
			for (CampaignContact contact : campContacts) {
				Assert.assertTrue(csv.contains(contact.getMsisdn()));
			}
			
		}
		
	}
	
}
