package org.celllife.mobilisr.service.message.route;

import java.util.Date;
import java.util.List;

import junit.framework.Assert;

import org.celllife.mobilisr.api.messaging.SmsMo;
import org.celllife.mobilisr.api.messaging.SmsMt;
import org.celllife.mobilisr.constants.CampaignStatus;
import org.celllife.mobilisr.constants.CampaignType;
import org.celllife.mobilisr.constants.ChannelType;
import org.celllife.mobilisr.constants.SmsStatus;
import org.celllife.mobilisr.domain.Campaign;
import org.celllife.mobilisr.domain.CampaignContact;
import org.celllife.mobilisr.domain.CampaignMessage;
import org.celllife.mobilisr.domain.Channel;
import org.celllife.mobilisr.domain.Contact;
import org.celllife.mobilisr.domain.Messagable;
import org.celllife.mobilisr.domain.SmsLog;
import org.celllife.mobilisr.domain.Transaction;
import org.celllife.mobilisr.domain.User;
import org.celllife.mobilisr.domain.mock.DomainMockFactory;
import org.celllife.mobilisr.service.UserBalanceService;
import org.celllife.mobilisr.service.message.processors.ChannelSelector;
import org.celllife.mobilisr.service.qrtz.beans.SmsBatchConfig;
import org.celllife.mobilisr.test.AbstractServiceTest;
import org.celllife.mobilisr.utilbean.TransactionSummary;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.trg.search.Search;

public class MessageServiceTests extends AbstractServiceTest {

	@Autowired
	MessageService service;

	@Autowired
	UserBalanceService balanceService;
	
	@Autowired
	private ChannelSelector channelSelector;

	private User user;

	@Before
	public void setup(){
		user = getUser();
	}

	@Test(timeout=50000)
	public void testMessageService_sendSingle_prereserve() throws InterruptedException{

		Campaign campaign = getCampaign(CampaignType.FIXED);
		Transaction transaction = createTransaction(0, 1, campaign.getIdentifierString(), user.getIdentifierString());

		String message = "smsMsg";
		SmsMt smsMT = new SmsMt("27854564565", message, campaign.getIdentifierString());
		smsMT.setOrganizationId(campaign.getOrganization().getId());
		smsMT.setTransactionRef(transaction.getId());
		smsMT.setUserId(user.getId());
		smsMT.setProcessCampaignCompletion(true);

		service.sendMessage(smsMT);
		boolean notDone = false;
		do{
			Thread.sleep(500);
			getGeneralDao().refresh(campaign);
			notDone = campaign.getStatus() != CampaignStatus.FINISHED;
		}while (notDone);

		Search s = new Search(SmsLog.class);
		s.addFilterEqual(SmsLog.PROP_MSISDN, "27854564565");
		List<?> logs = getGeneralDao().search(s);
		Assert.assertEquals(1, logs.size());

		getGeneralDao().refresh(campaign);
		Assert.assertEquals(CampaignStatus.FINISHED, campaign.getStatus());

		TransactionSummary summary = balanceService.getAccountSummary(campaign);
		Assert.assertEquals(-1,summary.getCost().intValue());
		Assert.assertEquals(0,summary.getReserved().intValue());
	}

	@Test(timeout=50000)
	public void testMessageService_sendSingle() throws InterruptedException{

		String message = "smsMsg";
		SmsMt smsMT = new SmsMt("27722310095", message, "createdFor");
		smsMT.setOrganizationId(user.getOrganization().getId());
		smsMT.setUserId(user.getId());
		Long conatctId = getGeneralDao().findAll(Contact.class).get(0).getId();
		smsMT.setContactId(conatctId);
		smsMT.setProcessCampaignCompletion(false);

		service.sendMessage(smsMT);
		boolean notDone = false;
		do{
			Thread.sleep(500);
			Search s = new Search(SmsLog.class);
			s.addFilterEqual(SmsLog.PROP_MSISDN, smsMT.getMsisdn());
			s.addFilterNotEmpty(SmsLog.PROP_TRACKING_NUM);
			List<?> logs = getGeneralDao().search(s);
			notDone = logs.isEmpty();
		}while (notDone);

		Search s = new Search(SmsLog.class);
		s.addFilterEqual(SmsLog.PROP_MSISDN, "27722310095");
		@SuppressWarnings("unchecked")
		List<SmsLog> logs = getGeneralDao().search(s);
		Assert.assertEquals(1, logs.size());

		TransactionSummary summary = balanceService.getAccountSummary(smsMT.getCreatedFor());
		Assert.assertEquals(-1,summary.getCost().intValue());
		Assert.assertEquals(0,summary.getReserved().intValue());
	}
	
	@Test(timeout=50000)
	public void testMessageService_sendSingle_no_user() throws InterruptedException{

		String message = "smsMsg";
		SmsMt smsMT = new SmsMt("27722310095", message, "createdFor");
		smsMT.setOrganizationId(user.getOrganization().getId());
		smsMT.setContactId(7L);
		smsMT.setProcessCampaignCompletion(false);

		service.sendMessage(smsMT);
		boolean notDone = false;
		do{
			Thread.sleep(500);
			Search s = new Search(SmsLog.class);
			s.addFilterEqual(SmsLog.PROP_MSISDN, smsMT.getMsisdn());
			s.addFilterNotEmpty(SmsLog.PROP_TRACKING_NUM);
			List<?> logs = getGeneralDao().search(s);
			notDone = logs.isEmpty();
		}while (notDone);

		Search s = new Search(SmsLog.class);
		s.addFilterEqual(SmsLog.PROP_MSISDN, "27722310095");
		List<?> logs = getGeneralDao().search(s);
		Assert.assertEquals(1, logs.size());

		TransactionSummary summary = balanceService.getAccountSummary(smsMT.getCreatedFor());
		Assert.assertEquals(-1,summary.getCost().intValue());
		Assert.assertEquals(0,summary.getReserved().intValue());
	}

	@Test(timeout=50000)
	public void testMessageService_sendSingleNoCredit() throws InterruptedException{

		Campaign campaign = getCampaign(CampaignType.FIXED);

		createTransaction(-user.getOrganization().getAvailableBalance(), 0, "createdFor", "createdBy");
		balanceService.updateOrgBalances();

		String message = "smsMsg";
		SmsMt smsMT = new SmsMt("27722310097", message, campaign.getIdentifierString());
		smsMT.setOrganizationId(user.getOrganization().getId());
		smsMT.setUserId(user.getId());
		smsMT.setProcessCampaignCompletion(true);

		service.sendMessage(smsMT);
		boolean notDone = false;
		do{
			Thread.sleep(500);
			getGeneralDao().refresh(campaign);
			notDone = campaign.getStatus() != CampaignStatus.FINISHED;
		}while (notDone);

		Search s = new Search(SmsLog.class);
		s.addFilterEqual(SmsLog.PROP_MSISDN, "27722310097");
		@SuppressWarnings("unchecked")
		List<SmsLog> logs = getGeneralDao().search(s);
		Assert.assertEquals(1, logs.size());
		Assert.assertEquals(SmsStatus.QUEUE_FAIL, logs.get(0).getStatus());

		TransactionSummary summary = balanceService.getAccountSummary(smsMT.getCreatedFor());
		Assert.assertEquals(0,summary.getCost().intValue());
		Assert.assertEquals(0,summary.getReserved().intValue());
	}

	@Test(timeout=50000)
	public void testMessageService_sendBatch_prereserve() throws InterruptedException{

		Campaign campaign = getCampaign(CampaignType.FIXED);
		int numMsgs = 3;
		Transaction transaction = createTransaction(0, numMsgs, campaign.getIdentifierString(), user.getIdentifierString());

		List<? extends Messagable> campaignContacts = getRecipients(campaign, numMsgs);
		SmsBatchConfig batch = new SmsBatchConfig("correlation_id", campaign.getIdentifierString(), "message", campaignContacts,
				3, transaction.getId(), user.getId(),
				campaign.getOrganization().getId(), true);

		service.sendMessage(batch);

		boolean notDone = false;
		do{
			Thread.sleep(500);
			getGeneralDao().refresh(campaign);
			notDone = campaign.getStatus() != CampaignStatus.FINISHED;
		}while (notDone);

		Search s = new Search(SmsLog.class);
		s.addFilterEqual(SmsLog.PROP_CREATEDFOR, campaign.getIdentifierString());
		List<?> logs = getGeneralDao().search(s);
		Assert.assertEquals(numMsgs, logs.size());

		TransactionSummary summary = balanceService.getAccountSummary(campaign);
		Assert.assertEquals(-numMsgs,summary.getCost().intValue());
		Assert.assertEquals(0,summary.getReserved().intValue());
	}

	@Test(timeout=50000)
	public void testMessageService_sendBatch_notEnoughCredit() throws InterruptedException{
		Campaign campaign = getCampaign(CampaignType.FIXED);
		int numMsgs = 10;
		int currentBalance = user.getOrganization().getBalance();
		int newBalance = numMsgs / 2;
		int debitAmount = currentBalance - newBalance;

		createTransaction(-debitAmount, 0, "createdFor", "createdBy");
		balanceService.updateOrgBalances();

		List<? extends Messagable> campaignContacts = getRecipients(campaign, numMsgs);
		SmsBatchConfig batch = new SmsBatchConfig("correlation_id", campaign.getIdentifierString(), "message", campaignContacts,
				numMsgs, null, user.getId(),
				campaign.getOrganization().getId(), true);

		service.sendMessage(batch);

		boolean notDone = false;
		do{
			Thread.sleep(500);
			getGeneralDao().refresh(campaign);
			notDone = campaign.getStatus() != CampaignStatus.FINISHED;
		}while (notDone);

		Search s = new Search(SmsLog.class);
		s.addFilterEqual(SmsLog.PROP_CREATEDFOR, campaign.getIdentifierString());
		@SuppressWarnings("unchecked")
		List<SmsLog> logs = getGeneralDao().search(s);
		Assert.assertEquals(numMsgs, logs.size());

		s = new Search(SmsLog.class);
		s.addFilterEqual(SmsLog.PROP_CREATEDFOR, campaign.getIdentifierString());
		s.addFilterEqual(SmsLog.PROP_STATUS, SmsStatus.WASP_SUCCESS);
		@SuppressWarnings("unchecked")
		List<SmsLog> logsSuccess = getGeneralDao().search(s);
		Assert.assertEquals(newBalance, logsSuccess.size());

		s = new Search(SmsLog.class);
		s.addFilterEqual(SmsLog.PROP_CREATEDFOR, campaign.getIdentifierString());
		s.addFilterEqual(SmsLog.PROP_STATUS, SmsStatus.QUEUE_FAIL);
		@SuppressWarnings("unchecked")
		List<SmsLog> logsFail = getGeneralDao().search(s);
		Assert.assertEquals(numMsgs-newBalance, logsFail.size());

		TransactionSummary summary = balanceService.getAccountSummary(campaign);
		Assert.assertEquals(-newBalance,summary.getCost().intValue());
		Assert.assertEquals(0,summary.getReserved().intValue());
	}

	@Test(timeout=50000)
	public void testMessageService_sendBatch() throws InterruptedException{

		int numMsgs = 3;

		List<? extends Messagable> recipients = getRecipients(null, numMsgs);
		SmsBatchConfig batch = new SmsBatchConfig("correlation_id","createdFor", "message", recipients,
				3, null, user.getId(),
				user.getOrganization().getId(), false);

		service.sendMessage(batch);

		boolean notDone = false;
		do{
			Thread.sleep(500);
			Search s = new Search(SmsLog.class);
			s.addFilterEqual(SmsLog.PROP_CREATEDFOR, "createdFor");
			s.addFilterNotEmpty(SmsLog.PROP_TRACKING_NUM);
			List<?> logs = getGeneralDao().search(s);
			notDone = logs.size() < recipients.size();
		}while (notDone);

		Search s = new Search(SmsLog.class);
		s.addFilterEqual(SmsLog.PROP_CREATEDFOR, "createdFor");
		List<?> logs = getGeneralDao().search(s);
		Assert.assertEquals(numMsgs, logs.size());

		TransactionSummary summary = balanceService.getAccountSummary("createdFor");
		Assert.assertEquals(-numMsgs,summary.getCost().intValue());
		Assert.assertEquals(0,summary.getReserved().intValue());
	}
	
	@Test(timeout=50000)
	public void testMessageService_sendBatch_no_user() throws InterruptedException{

		int numMsgs = 3;

		List<? extends Messagable> recipients = getRecipients(null, numMsgs);
		SmsBatchConfig batch = new SmsBatchConfig("correlation_id","createdFor", "message", recipients,
				3, null, null,
				user.getOrganization().getId(), false);

		service.sendMessage(batch);

		boolean notDone = false;
		do{
			Thread.sleep(500);
			Search s = new Search(SmsLog.class);
			s.addFilterEqual(SmsLog.PROP_CREATEDFOR, "createdFor");
			s.addFilterNotEmpty(SmsLog.PROP_TRACKING_NUM);
			List<?> logs = getGeneralDao().search(s);
			notDone = logs.size() < recipients.size();
		}while (notDone);

		Search s = new Search(SmsLog.class);
		s.addFilterEqual(SmsLog.PROP_CREATEDFOR, "createdFor");
		List<?> logs = getGeneralDao().search(s);
		Assert.assertEquals(numMsgs, logs.size());

		TransactionSummary summary = balanceService.getAccountSummary("createdFor");
		Assert.assertEquals(-numMsgs,summary.getCost().intValue());
		Assert.assertEquals(0,summary.getReserved().intValue());
	}

	@SuppressWarnings("unchecked")
	@Test(timeout=50000)
	public void testMessageService_noPrefixForMsisdn() throws InterruptedException{
		channelSelector.simulateMessageSending(false);
		
		String message = "smsMsg";
		SmsMt smsMT = new SmsMt("28722310095", message, "createdFor");
		smsMT.setOrganizationId(user.getOrganization().getId());
		smsMT.setUserId(user.getId());
		smsMT.setContactId(7L);
		smsMT.setProcessCampaignCompletion(false);

		service.sendMessage(smsMT);
		boolean notDone = false;
		List<SmsLog> logs = null;
		do{
			Thread.sleep(500);
			Search s = new Search(SmsLog.class);
			s.addFilterEqual(SmsLog.PROP_MSISDN, smsMT.getMsisdn());
			s.addFilterEqual(SmsLog.PROP_STATUS, SmsStatus.QUEUE_FAIL);
			logs = getGeneralDao().search(s);
			notDone = logs.isEmpty();
		}while (notDone);

		TransactionSummary summary = balanceService.getAccountSummary(smsMT.getCreatedFor());
		Assert.assertEquals(0,summary.getCost().intValue());
		Assert.assertEquals(0,summary.getReserved().intValue());
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testMessageService_receive() throws InterruptedException{

		String message = "incoming message";
		SmsMo mo = new SmsMo("277223456789", "31759", message, new Date(), null);
		
		service.messageReceived(mo);
		
		boolean notDone = false;
		List<SmsLog> logs = null;
		do{
			Thread.sleep(500);
			Search s = new Search(SmsLog.class);
			s.addFilterEqual(SmsLog.PROP_MSISDN, mo.getSourceAddr());
			s.addFilterNotEqual(SmsLog.PROP_STATUS, SmsStatus.QUEUED_PROCESSING);
			logs = getGeneralDao().search(s);
			notDone = logs.isEmpty();
		}while (notDone);
		
		Assert.assertEquals(SmsStatus.RX_CHANNEL_FAIL, logs.get(0).getStatus());
	}
	
	@SuppressWarnings("unchecked")
	@Test(timeout=20000)
	public void testMessageService_receiveAndProcess() throws InterruptedException{

		String desinationAddress = "31759";
		
		Channel channel = new Channel("test in", ChannelType.IN, "test channel"
				,desinationAddress );
		getGeneralDao().save(channel);
		
		String sourceAddr = "277223456789";
		for (int i = 0; i < 50; i++) {
			String message = "incoming message";
			SmsMo mo = new SmsMo(sourceAddr, desinationAddress, message,
					new Date(), null);
			service.messageReceived(mo);
		}
		
		boolean notDone = false;
		List<SmsLog> logs = null;
		do{
			Thread.sleep(500);
			Search s = new Search(SmsLog.class);
			s.addFilterEqual(SmsLog.PROP_STATUS, SmsStatus.RX_FILTER_FAIL);
			logs = getGeneralDao().search(s);
			notDone = logs.size() < 50;
		}while (notDone);
	}

	private List<? extends Messagable> getRecipients(Campaign campaign, int num) {
		if (campaign != null){
			List<CampaignContact> contacts = DomainMockFactory._().on(CampaignContact.class).create(num);
			for (CampaignContact c : contacts) {
				c.setCampaign(campaign);
				c.getContact().setOrganization(campaign.getOrganization());
				getGeneralDao().save(c.getContact());
				getGeneralDao().save(c);
			}
			return contacts;
		} else {
			List<Contact> contacts = DomainMockFactory._().on(Contact.class).create(num);
			for (Contact contact : contacts) {
				contact.setOrganization(user.getOrganization());
				getGeneralDao().save(contact);
			}
			return contacts;
		}
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

	private Transaction createTransaction(int amountToDebit, int amountToReserve, String createdFor, String createdBy) {
		Transaction transaction = new Transaction(amountToDebit, amountToReserve, createdFor, createdBy,
				"message", user, user.getOrganization());
		getGeneralDao().save(transaction);
		return transaction;
	}

	private User getUser() {
		Search search = new Search(User.class);
		search.addFilterEqual(User.PROP_FIRST_NAME, "firstname 8");
		User user = (User) getGeneralDao().searchUnique(search);
		return user;
	}

}
