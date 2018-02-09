package org.celllife.mobilisr.service.message.processors;

import java.util.Date;

import junit.framework.Assert;

import org.celllife.mobilisr.constants.CampaignStatus;
import org.celllife.mobilisr.constants.CampaignType;
import org.celllife.mobilisr.dao.api.TransactionDAO;
import org.celllife.mobilisr.domain.Campaign;
import org.celllife.mobilisr.domain.CampaignMessage;
import org.celllife.mobilisr.domain.User;
import org.celllife.mobilisr.exception.TransactionNotFoundException;
import org.celllife.mobilisr.service.message.route.BatchStats;
import org.celllife.mobilisr.test.AbstractServiceTest;
import org.celllife.mobilisr.utilbean.TransactionSummary;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.trg.search.Search;

public class BatchStatsProcessorTest extends AbstractServiceTest {

	@Autowired
	private BatchStatsProcessor processor;

	private User user;

	@Autowired
	private TransactionDAO transactionDao;

	@Before
	public void init() {
		user = getUser();
	}

	@Test
	public void testBatchStatsProcessor_fixedCampaign() throws TransactionNotFoundException{
		BatchStats stats = new BatchStats();
		Campaign campaign = getCampaign(CampaignType.FIXED);
		int totalCount = 26;
		int totalSuccess = 23;
		int totalFail = totalCount-totalSuccess;

		Long ref = transactionDao.reserveAmount(totalCount, campaign.getOrganization(), campaign.getIdentifierString(), user.getIdentifierString(), "message", user);
		
		stats.setCreatedFor(campaign.getIdentifierString());
		stats.setMessageLength(100);
		stats.setProcessCampaignCompletion(true);
		stats.setTotalCount(totalCount);
		
		stats.setTotalFail(totalFail);
		stats.setTotalSuccess(totalSuccess);
		stats.setTransactionRef(ref);
		stats.setUserId(user.getId());
		
		processor.processStats(stats);
		
		getGeneralDao().refresh(campaign);
		Assert.assertEquals(CampaignStatus.FINISHED, campaign.getStatus());
		
		TransactionSummary summary = transactionDao.getSummaryTransaction(ref);
		Assert.assertEquals(0,summary.getReserved().intValue());
		Assert.assertEquals(-totalSuccess, summary.getCost().intValue());
	}
	
	@Test
	public void testBatchStatsProcessor_relativeCampaign() throws TransactionNotFoundException{
		BatchStats stats = new BatchStats();
		Campaign campaign = getCampaign(CampaignType.DAILY);
		CampaignStatus initialStatus = campaign.getStatus();
		int totalCount = 26;
		int totalSuccess = 23;
		int totalFail = totalCount-totalSuccess;

		Long ref = transactionDao.reserveAmount(totalCount, campaign.getOrganization(), campaign.getIdentifierString(), user.getIdentifierString(), "message", user);
		
		stats.setCreatedFor(campaign.getIdentifierString());
		stats.setMessageLength(100);
		stats.setProcessCampaignCompletion(false);
		stats.setTotalCount(totalCount);
		stats.setTotalFail(totalFail);
		stats.setTotalSuccess(totalSuccess);
		stats.setTransactionRef(ref);
		stats.setUserId(user.getId());
		
		processor.processStats(stats);
		
		getGeneralDao().refresh(campaign);
		Assert.assertEquals(initialStatus, campaign.getStatus());
		
		TransactionSummary summary = transactionDao.getSummaryTransaction(ref);
		Assert.assertEquals(0,summary.getReserved().intValue());
		Assert.assertEquals(-totalSuccess, summary.getCost().intValue());
	}

	private Campaign getCampaign(CampaignType type) {

			Campaign campaign = new Campaign();
			campaign.setName("Sched Program ");
			campaign.setStatus(CampaignStatus.INACTIVE);
			campaign.setOrganization(user.getOrganization());
			campaign.setType(type);
			getGeneralDao().save(campaign);
			Assert.assertNotNull(campaign.getId());
			String msg = "Sms for Program ";
			CampaignMessage campaignMessage = new CampaignMessage(
					msg, new Date(), new Date(),
					msg.length(), campaign);
			getGeneralDao().save(campaignMessage);

		return campaign;
	}

	private User getUser() {
		Search search = new Search(User.class);
		search.addFilterEqual(User.PROP_FIRST_NAME, "firstname 8");
		User user = (User) getGeneralDao().searchUnique(search);
		return user;
	}

}
