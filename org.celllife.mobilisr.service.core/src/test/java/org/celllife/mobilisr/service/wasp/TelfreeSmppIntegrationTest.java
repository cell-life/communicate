package org.celllife.mobilisr.service.wasp;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import junit.framework.Assert;

import org.celllife.mobilisr.api.messaging.SmsMt;
import org.celllife.mobilisr.constants.ChannelType;
import org.celllife.mobilisr.constants.SmsStatus;
import org.celllife.mobilisr.domain.Campaign;
import org.celllife.mobilisr.domain.CampaignContact;
import org.celllife.mobilisr.domain.Channel;
import org.celllife.mobilisr.domain.ChannelConfig;
import org.celllife.mobilisr.domain.Contact;
import org.celllife.mobilisr.domain.Messagable;
import org.celllife.mobilisr.domain.NumberInfo;
import org.celllife.mobilisr.domain.SmsLog;
import org.celllife.mobilisr.domain.User;
import org.celllife.mobilisr.domain.mock.DomainMockFactory;
import org.celllife.mobilisr.service.exception.ChannelStateException;
import org.celllife.mobilisr.service.exception.UniquePropertyException;
import org.celllife.mobilisr.service.message.processors.ChannelSelector;
import org.celllife.mobilisr.service.message.route.MessageService;
import org.celllife.mobilisr.service.qrtz.beans.SmsBatchConfig;
import org.celllife.mobilisr.service.yaml.YamlUtils;
import org.celllife.mobilisr.test.AbstractServiceTest;
import org.celllife.mobilisr.test.TestUtils;
import org.celllife.pconfig.model.Parameter;
import org.celllife.pconfig.model.Pconfig;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.trg.search.Search;

/**
 * FIXME TelfreeSmppIntegrationTest
 * For some reason this test fails when executed with other tests e.g. by maven
 */
@Ignore
public class TelfreeSmppIntegrationTest extends AbstractServiceTest {

	private static SMPPServerSimulator smppServerSimulator;

	@Autowired
	private MessageService messageService;

	@Autowired
	private ChannelSelector channelSelector;
	
	@Autowired
	private ChannelManager channelManager;
	
	@BeforeClass
	public static void setupClass(){
		smppServerSimulator = new SMPPServerSimulator(8065, false);
	}
	
	@AfterClass
	public static void tearDownClass(){
		smppServerSimulator.shutdown();
	}
	
	private User user;

	private Channel channel;

	@SuppressWarnings("unchecked")
	@Before
	public void setup() throws ChannelStateException, UniquePropertyException{
		if (smppServerSimulator.isShutdown()){
			smppServerSimulator.run();
		}
		
		user = getUser();
		
		Pconfig pconfig = new TelfreeSmppHandler().getConfigDescriptor();
		((Parameter<String>)pconfig.getParameter(TelfreeSmppHandler.HOST)).setValue("localhost");
		((Parameter<String>)pconfig.getParameter(TelfreeSmppHandler.PASSWORD)).setValue("123");
		((Parameter<String>)pconfig.getParameter(TelfreeSmppHandler.USERNAME)).setValue("test");
		((Parameter<Integer>)pconfig.getParameter(TelfreeSmppHandler.PORT)).setValue(8065);
		((Parameter<String>)pconfig.getParameter(TelfreeSmppHandler.SERVICE_TYPE)).setValue("cmcat");
		((Parameter<String>)pconfig.getParameter(TelfreeSmppHandler.SYSTEM_TYPE)).setValue("communicate");
		
		channel = new Channel("TelfreeSMPP", ChannelType.OUT,
				pconfig.getResource(), null);
		ChannelConfig config = new ChannelConfig();
		config.setName("test config");
		config.setHandler(pconfig.getResource());
		config.setProperties(YamlUtils.dumpParameterList(pconfig.getParameters()));
		getGeneralDao().save(config);
		channel.setConfig(config);
		channel.setVoided(true);
		getGeneralDao().save(channel);
		
		channelManager.startServicesForChannel(channel);
		
		channelSelector.simulateMessageSending(false);
		
		// setup numberinfo so that messages get routed to the right channel
		Search search = new Search(NumberInfo.class);
		search.addFilterEqual(NumberInfo.PROP_PREFIX, "27");
		search.addFetch(NumberInfo.PROP_CHANNEL);
		NumberInfo info = (NumberInfo) getGeneralDao().searchUnique(search);
		if (info != null){
			info.setChannel(channel);
			getGeneralDao().saveOrUpdate(info);
		} else {
			getGeneralDao().save(new NumberInfo("telfree", "27", "", channel));
		}
	}
	
	@After
	public void tearDown() throws ChannelStateException{
		channelManager.stopServicesForChannel(channel);
	}
	
	private User getUser() {
		Search search = new Search(User.class);
		search.addFilterEqual(User.PROP_FIRST_NAME, "firstname 8");
		User user = (User) getGeneralDao().searchUnique(search);
		return user;
	}
	
	@Test(timeout=10000)
	public void testTelfreeSmpp() throws InterruptedException{

		Contact contact = getGeneralDao().findAll(Contact.class).get(0);
		String message = TestUtils.getLoremIpsum(50);
		SmsMt smsMT = new SmsMt(contact.getMsisdn(), message, "createdFor");
		smsMT.setOrganizationId(user.getOrganization().getId());
		smsMT.setUserId(user.getId());
		smsMT.setContactId(contact.getId());
		smsMT.setProcessCampaignCompletion(false);

		messageService.sendMessage(smsMT);
		boolean notDone = false;
		do{
			Thread.sleep(500);
			Search s = new Search(SmsLog.class);
			s.addFilterEqual(SmsLog.PROP_MSISDN, smsMT.getMsisdn());
			s.addFilterNotEmpty(SmsLog.PROP_WASP_STATUS);
			List<?> logs = getGeneralDao().search(s);
			notDone = logs.isEmpty();
		}while (notDone);

		Search s = new Search(SmsLog.class);
		s.addFilterEqual(SmsLog.PROP_MSISDN, contact.getMsisdn());
		@SuppressWarnings("unchecked")
		List<SmsLog> logs = getGeneralDao().search(s);
		Assert.assertEquals(1, logs.size());
	}
	
	@Test(timeout=10000)
	public void testTelfreeSmpp_failure() throws InterruptedException, ChannelStateException{
		Contact contact = getGeneralDao().findAll(Contact.class).get(0);
		
		// msisdn prefix doesn't match prefix for any active channels
		contact.setMsisdn("2635456");
		
		String message = TestUtils.getLoremIpsum(50);
		SmsMt smsMT = new SmsMt(contact.getMsisdn(), message, "createdFor");
		smsMT.setOrganizationId(user.getOrganization().getId());
		smsMT.setUserId(user.getId());
		smsMT.setContactId(contact.getId());
		smsMT.setProcessCampaignCompletion(false);

		messageService.sendMessage(smsMT);
		boolean notDone = false;
		do{
			Thread.sleep(500);
			Search s = new Search(SmsLog.class);
			s.addFilterEqual(SmsLog.PROP_MSISDN, smsMT.getMsisdn());
			s.addFilterEqual(SmsLog.PROP_STATUS, SmsStatus.QUEUE_FAIL);
			List<?> logs = getGeneralDao().search(s);
			notDone = logs.isEmpty();
		}while (notDone);

		Search s = new Search(SmsLog.class);
		s.addFilterEqual(SmsLog.PROP_MSISDN, contact.getMsisdn());
		@SuppressWarnings("unchecked")
		List<SmsLog> logs = getGeneralDao().search(s);
		Assert.assertEquals(1, logs.size());
	}
	
	@Test(timeout=10000)
	public void testTelfreeSmpp_failureIO() throws InterruptedException, ChannelStateException{
		smppServerSimulator.shutdown();
		
		Contact contact = getGeneralDao().findAll(Contact.class).get(0);
		String message = TestUtils.getLoremIpsum(50);
		SmsMt smsMT = new SmsMt(contact.getMsisdn(), message, "createdFor");
		smsMT.setOrganizationId(user.getOrganization().getId());
		smsMT.setUserId(user.getId());
		smsMT.setContactId(contact.getId());
		smsMT.setProcessCampaignCompletion(false);

		messageService.sendMessage(smsMT);
		boolean notDone = false;
		do{
			Thread.sleep(500);
			Search s = new Search(SmsLog.class);
			s.addFilterEqual(SmsLog.PROP_MSISDN, smsMT.getMsisdn());
			s.addFilterEqual(SmsLog.PROP_STATUS, SmsStatus.WASP_FAIL);
			List<?> logs = getGeneralDao().search(s);
			notDone = logs.isEmpty();
		}while (notDone);

		Search s = new Search(SmsLog.class);
		s.addFilterEqual(SmsLog.PROP_MSISDN, contact.getMsisdn());
		@SuppressWarnings("unchecked")
		List<SmsLog> logs = getGeneralDao().search(s);
		Assert.assertEquals(1, logs.size());
	}
	
	@Test(timeout=10000)
	public void testTelfreeSmpp_longMessage() throws InterruptedException{

		Contact contact = getGeneralDao().findAll(Contact.class).get(0);
		String message = TestUtils.getLoremIpsum(550);
		SmsMt smsMT = new SmsMt(contact.getMsisdn(), message, "createdFor");
		smsMT.setOrganizationId(user.getOrganization().getId());
		smsMT.setUserId(user.getId());
		smsMT.setContactId(contact.getId());
		smsMT.setProcessCampaignCompletion(false);

		messageService.sendMessage(smsMT);
		boolean notDone = false;
		do{
			Thread.sleep(500);
			Search s = new Search(SmsLog.class);
			s.addFilterEqual(SmsLog.PROP_MSISDN, smsMT.getMsisdn());
			s.addFilterNotEmpty(SmsLog.PROP_WASP_STATUS);
			List<?> logs = getGeneralDao().search(s);
			notDone = logs.isEmpty();
		}while (notDone);

		Search s = new Search(SmsLog.class);
		s.addFilterEqual(SmsLog.PROP_MSISDN, contact.getMsisdn());
		@SuppressWarnings("unchecked")
		List<SmsLog> logs = getGeneralDao().search(s);
		Assert.assertEquals(1, logs.size());
		String[] trackingNums = logs.get(0).getTrackingnumber().split(",");
		Assert.assertEquals("Expecting 4 tracking numbers for 4 part message",4, trackingNums.length);
		
	}
	
	@Test(timeout=50000)
	@Ignore("Used for profiling")
	public void testMessageService_bulkBatch() throws InterruptedException{
		ExecutorService executorService = Executors.newCachedThreadPool();
		final int numSends = 10;
		final int batchesPerSend = 30;
		final int recipientsPerBatch = 10;
		final int expectedTotalMessages = numSends * batchesPerSend * recipientsPerBatch;
		
		final List<? extends Messagable> recipients = getRecipients(null, recipientsPerBatch);
		
		for (int j = 0; j < numSends; j++) {
			final int snd = j;
			executorService.execute(new Runnable() {
				@Override
				public void run() {
					String correlationId = UUID.randomUUID().toString();
					SmsBatchConfig batch;
					for (int i = 0; i < batchesPerSend; i++) {
						batch = new SmsBatchConfig(correlationId, "createdFor" + i+snd,
								"message" + i, recipients, batchesPerSend
										* recipientsPerBatch, null,
								user.getId(), user.getOrganization().getId(),
								false);
						messageService.sendMessage(batch);
					}
				}
			});
		}
		
		boolean notDone = false;
		do{
			Thread.sleep(500);
			Search s = new Search(SmsLog.class);
			s.addFilterILike(SmsLog.PROP_CREATEDFOR, "createdFor%");
			s.addFilterNotEmpty(SmsLog.PROP_WASP_STATUS);
			List<?> logs = getGeneralDao().search(s);
			notDone = logs.size() < expectedTotalMessages;
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
	
}
