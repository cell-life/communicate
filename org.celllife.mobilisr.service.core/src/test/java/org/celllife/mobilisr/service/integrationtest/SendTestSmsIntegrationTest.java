package org.celllife.mobilisr.service.integrationtest;

import java.util.List;

import org.celllife.mobilisr.dao.api.MobilisrGeneralDAO;
import org.celllife.mobilisr.dao.api.SmsLogDAO;
import org.celllife.mobilisr.domain.Campaign;
import org.celllife.mobilisr.domain.SmsLog;
import org.celllife.mobilisr.domain.Transaction;
import org.celllife.mobilisr.domain.User;
import org.celllife.mobilisr.domain.mock.DomainMockFactory;
import org.celllife.mobilisr.exception.InsufficientBalanceException;
import org.celllife.mobilisr.exception.MsisdnFormatException;
import org.celllife.mobilisr.service.CampaignScheduleService;
import org.celllife.mobilisr.service.UserBalanceService;
import org.celllife.mobilisr.service.qrtz.BackgroundServices;
import org.celllife.mobilisr.test.AbstractServiceTest;
import org.celllife.mobilisr.utilbean.TransactionSummary;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.trg.search.Search;

public class SendTestSmsIntegrationTest extends AbstractServiceTest  {

	@Autowired
	private CampaignScheduleService campaignService;
	
	@Autowired
	private MobilisrGeneralDAO generalDao;

	@Autowired
	private SmsLogDAO smsLogDAO;
	
	@Autowired
	BackgroundServices backgroundServices;
	
	@Autowired
	UserBalanceService balanceService;
	
	private User user;

	@Before
	public void before() {
		user = getUser();
	}
	@Test
	public void testSendTestSms() throws InsufficientBalanceException, MsisdnFormatException{
		Campaign campaign = getCampaign();
		String smsMsg = "short message";
		
		campaignService.sendTestSMS(campaign, user, "27725467895", smsMsg);
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
		}
		
		checkTransactions(1, campaign, user.getIdentifierString(), false);
		
		TransactionSummary summary = balanceService.getAccountSummary(campaign);
		Assert.assertEquals(0, summary.getReserved().intValue());
		Assert.assertEquals(-1, summary.getCost().intValue());
		
		checkSmsLogs(campaign, 1);
	}
	
	private void checkSmsLogs(Campaign campaign, int totalContacts) {
		List<SmsLog> logs = smsLogDAO.searchByPropertyEqual(SmsLog.PROP_CREATEDFOR, campaign.getIdentifierString());
		Assert.assertEquals(totalContacts, logs.size());
	}
	
	private void checkTransactions(int expectedAmount, Campaign campaign, String createdBy, boolean checkCostNotReserved) {
		Search search = new Search(Transaction.class);
		search.addFilterEqual(Transaction.PROP_CREATED_FOR, campaign.getIdentifierString());
		search.addFilterEqual(Transaction.PROP_CREATED_BY, createdBy);
		@SuppressWarnings("unchecked")
		List<Transaction> transactions = getGeneralDao().search(search);
		Assert.assertEquals(1, transactions.size());
		if (checkCostNotReserved){
			Assert.assertEquals(expectedAmount, transactions.get(0).getCost());
		} else {
			Assert.assertEquals(expectedAmount, transactions.get(0).getReserved());
		}
	}
	
	private User getUser() {
		Search search = new Search(User.class);
		search.addFilterEqual(User.PROP_FIRST_NAME, "firstname 8");
		User user = (User) generalDao.searchUnique(search);
		return user;
	}
	
	private Campaign getCampaign(){
		Campaign campaign = DomainMockFactory._().on(Campaign.class).create();
		generalDao.save(campaign.getOrganization());
		generalDao.save(campaign);
		return campaign;
	}
}
