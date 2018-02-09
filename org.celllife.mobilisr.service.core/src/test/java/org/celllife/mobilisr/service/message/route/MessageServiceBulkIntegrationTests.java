package org.celllife.mobilisr.service.message.route;

import java.util.List;
import java.util.UUID;

import junit.framework.Assert;

import org.celllife.mobilisr.api.messaging.SmsMt;
import org.celllife.mobilisr.api.mock.DtoMockFactory;
import org.celllife.mobilisr.domain.Campaign;
import org.celllife.mobilisr.domain.CampaignContact;
import org.celllife.mobilisr.domain.Contact;
import org.celllife.mobilisr.domain.Messagable;
import org.celllife.mobilisr.domain.SmsLog;
import org.celllife.mobilisr.domain.Transaction;
import org.celllife.mobilisr.domain.User;
import org.celllife.mobilisr.domain.mock.DomainMockFactory;
import org.celllife.mobilisr.service.UserBalanceService;
import org.celllife.mobilisr.service.qrtz.beans.SmsBatchConfig;
import org.celllife.mobilisr.test.AbstractServiceTest;
import org.celllife.mobilisr.utilbean.TransactionSummary;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.trg.search.Search;

public class MessageServiceBulkIntegrationTests extends AbstractServiceTest {
	
	@Autowired
	MessageService service;
	
	@Autowired
	UserBalanceService balanceService;
	
	private User user;
	
	@Before
	public void setup(){
		user = getUser();
	}
	
	@Test(timeout=60000)
	public void testMessageService_bulkBatch() throws InterruptedException{
		
		final int numSends = 10;
		final int batchesPerSend = 30;
		final int recipientsPerBatch = 10;
		final int expectedTotalMessages = numSends * batchesPerSend * recipientsPerBatch;
		
		final List<? extends Messagable> recipients = getRecipients(null, recipientsPerBatch);
		
		for (int j = 0; j < numSends; j++) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					String correlationId = UUID.randomUUID().toString();
					SmsBatchConfig batch;
					for (int i = 0; i < batchesPerSend; i++) {
						batch = new SmsBatchConfig(correlationId, "createdFor",
								"message" + i, recipients, batchesPerSend
										* recipientsPerBatch, null,
								user.getId(), user.getOrganization().getId(),
								false);
						service.sendMessage(batch);
					}
				}
			}).start();
		}
		
		boolean notDone = false;
		do{
			Thread.sleep(500);
			Search s = new Search(SmsLog.class);
			s.addFilterEqual(SmsLog.PROP_CREATEDFOR, "createdFor");
			s.addFilterNotEmpty(SmsLog.PROP_TRACKING_NUM);
			List<?> logs = getGeneralDao().search(s);
			notDone = logs.size() < expectedTotalMessages;
		}while (notDone);
		
		TransactionSummary summary = balanceService.getAccountSummary("createdFor");
		Assert.assertEquals(-expectedTotalMessages,summary.getCost().intValue());
		Assert.assertEquals(0,summary.getReserved().intValue());
	}
	
	@Test(timeout=50000)
	public void testMessageService_bulkSingle() throws InterruptedException{
		int initialBalance = user.getOrganization().getBalance();
		
		final int numBatches = 10;
		final int numPerBatch = 100;
		final int totalNumOfContacts = numBatches * numPerBatch;
		
		for(int i = 0; i < numBatches; i ++){
			new Thread(new Runnable(){
				@Override
				public void run() {
					List<SmsMt> mts = DtoMockFactory._().on(SmsMt.class).create(numPerBatch);
					for (SmsMt mt : mts) {
						mt.setCreatedFor("createdFor");
						mt.setOrganizationId(user.getOrganization().getId());
						mt.setUserId(user.getId());
						service.sendMessage(mt);
					}
				}
			}).start();
		}
		
		boolean notDone = false;
		do{
			Thread.sleep(500);
			Search s = new Search(Transaction.class);
			s.addFilterEqual(Transaction.PROP_ORGANIZATION, user.getOrganization());
			s.addFilterEqual(Transaction.PROP_USER, user);
			s.addFilterNotEqual(Transaction.PROP_COST, 0);
			List<?> logs = getGeneralDao().search(s);
			notDone = logs.size() < totalNumOfContacts;
		}while (notDone);
		
		TransactionSummary summary = balanceService.getAccountSummary(user.getOrganization());
		Assert.assertEquals(initialBalance-totalNumOfContacts,summary.getCost().intValue());
		Assert.assertEquals(0,summary.getReserved().intValue());
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

	private User getUser() {
		Search search = new Search(User.class);
		search.addFilterEqual(User.PROP_FIRST_NAME, "firstname 8");
		User user = (User) getGeneralDao().searchUnique(search);
		return user;
	}

}
