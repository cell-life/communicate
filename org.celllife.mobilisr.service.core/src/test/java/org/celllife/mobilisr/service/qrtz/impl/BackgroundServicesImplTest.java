package org.celllife.mobilisr.service.qrtz.impl;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.celllife.mobilisr.dao.api.CampaignDAO;
import org.celllife.mobilisr.domain.Campaign;
import org.celllife.mobilisr.domain.mock.DomainMockFactory;
import org.celllife.mobilisr.exception.TransactionNotFoundException;
import org.celllife.mobilisr.service.CampaignScheduleService;
import org.celllife.mobilisr.service.UserBalanceService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.trg.search.Search;

@RunWith(MockitoJUnitRunner.class)
public class BackgroundServicesImplTest {

	@Mock
	private CampaignDAO campaignDao;
	
	@Mock
	private UserBalanceService balanceService;

	private BackgroundServicesImpl backgroundService;

	@Mock
	private CampaignScheduleService scheduleService;
	
	@Before
	public void before(){
		backgroundService = new BackgroundServicesImpl();
		backgroundService.setCampaignDao(campaignDao);
		backgroundService.setUserBalanceService(balanceService);
		backgroundService.setScheduleService(scheduleService);
	}
	
	@Test
	public void testProcessCampContactProgress_no_campaigns(){
		// setup mocking
		List<Object> returns = new ArrayList<Object>();
		when(campaignDao.search(any(Search.class))).thenReturn(returns);
		
		// call test method
		backgroundService.processCampContactProgress();
		
		// verify results
		verify(campaignDao, never()).updateCampaignContactsProgress(any(Campaign.class));
	}
	
	@Test
	public void testProcessCampContactProgress_no_finished_contacts() throws TransactionNotFoundException{
		// setup mocking
		Campaign campaign = DomainMockFactory._().on(Campaign.class).create();
		List<Object> returns = new ArrayList<Object>();
		returns.add(campaign);
		when(campaignDao.search(any(Search.class))).thenReturn(returns);
		
		// call test method
		backgroundService.processCampContactProgress();
		
		// verify results
		verify(campaignDao, times(1)).updateCampaignContactsProgress(eq(campaign));
	}
	
	@Test
	public void testProcessCampContactProgress_finished_contacts() throws TransactionNotFoundException{
		// setup mocking
		Campaign campaign = DomainMockFactory._().on(Campaign.class).create();
		List<Object> returns = new ArrayList<Object>();
		returns.add(campaign);
		when(campaignDao.search(any(Search.class))).thenReturn(returns);
		
		// call test method
		backgroundService.processCampContactProgress();
		
		// verify results
		verify(campaignDao, times(1)).updateCampaignContactsProgress(eq(campaign));
	}
}
