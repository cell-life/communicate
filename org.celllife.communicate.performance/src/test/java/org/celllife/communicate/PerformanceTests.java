package org.celllife.communicate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang.RandomStringUtils;
import org.celllife.communicate.dao.PerfDao;
import org.celllife.mobilisr.api.rest.ContactDto;
import org.celllife.mobilisr.api.validation.MsisdnCountryRule;
import org.celllife.mobilisr.api.validation.ValidatorFactoryImpl;
import org.celllife.mobilisr.client.CampaignService;
import org.celllife.mobilisr.client.MobilisrClient;
import org.celllife.mobilisr.client.impl.MobilisrClientImpl;
import org.celllife.mobilisr.constants.ChannelType;
import org.celllife.mobilisr.constants.SmsStatus;
import org.celllife.mobilisr.domain.Channel;
import org.celllife.mobilisr.domain.ContactGroup;
import org.celllife.mobilisr.domain.FilterAction;
import org.celllife.mobilisr.domain.MessageFilter;
import org.celllife.mobilisr.domain.Organization;
import org.celllife.mobilisr.domain.SmsLog;
import org.celllife.mobilisr.service.action.AddToGroupAction;
import org.celllife.mobilisr.service.action.EmailAction;
import org.celllife.mobilisr.service.filter.KeywordFilter;
import org.celllife.mobilisr.service.wasp.SMPPServerSimulator;
import org.celllife.mobilisr.service.wasp.TelfreeSmppHandler;
import org.celllife.mobilisr.service.yaml.YamlUtils;
import org.celllife.mobilisr.test.AbstractDBTest;
import org.celllife.mobilisr.test.TestUtils;
import org.celllife.pconfig.model.EntityParameter;
import org.celllife.pconfig.model.Pconfig;
import org.celllife.pconfig.model.StringParameter;
import org.jfree.util.Log;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.dumbster.smtp.SimpleSmtpServer;
import com.trg.search.Filter;
import com.trg.search.Search;

/**
 * Convenience class for running performance tests.
 * 
 * @author Simon Kelly
 *
 */
public class PerformanceTests extends AbstractDBTest{
	
	public enum MessageType {
		MESSAGES_RECEIVED, MESSAGE_RESPONSES, MESSAGES_SENT

	}

	private static final int RECEIPT_DELAY = 0;
	private static final int RESPONSE_DELAY = 0;
	
	private static final String BASE_URL = "http://localhost:8181/communicate";
	private IntegratSimulationServer integratServer;
	private SMPPServerSimulator smppServer;
	
	@Autowired
	private PerfDao perfDao;
	
	@SuppressWarnings("unused")
	private SimpleSmtpServer smtp;

	@BeforeClass
	public static void verifyPropertiesOverride() {
		// Make sure this property is set so that we don't clean out the wrong database
		if (System.getProperty("propertiesOverride") == null){
			String msg = "System property propertiesOverride is not set";
			throw new RuntimeException(msg);
		}
	}
	

	@Before
	public void setup(){
		smtp = SimpleSmtpServer.start(9085);
	}
	
	@Test
	public void testTelfreeSingleCampaign() throws Exception{
		startSmppServer(true);
		
		getGeneralDao().save(new Channel("telfree", ChannelType.OUT, new TelfreeSmppHandler().getConfigDescriptor().getResource()
				, "0"));
		
		createAndRunCampaign(100, 500);
		
		waitForSmppMessages(100*2, MessageType.MESSAGE_RESPONSES);
		
		wait_ms(2000);
		
		generateDataStats();
		
		stopSmppServer();
	}

	@Test
	public void testTelfreeMultiCampaign() throws Exception{
		startSmppServer(true);
		
		getGeneralDao().save(new Channel("telfree", ChannelType.OUT, new TelfreeSmppHandler().getConfigDescriptor().getResource()
				,"0"));
		
		for (int i = 0; i < 10; i++) {
			createAndRunCampaign(100, 500);
		}
		
		waitForSmppMessages(1000*2, MessageType.MESSAGE_RESPONSES);
		
		wait_ms(2000);
		
		generateDataStats();
		
		stopSmppServer();
	}
	
	private void wait_ms(int i) {
		System.out.println("Waiting for " + i + "ms");
		try {Thread.sleep(i);} catch (InterruptedException e) {}
	}


	@Test
	public void testTelfreeReceiveMessages_peakVolume() throws Exception{
		startSmppServer(true);
		
		String keyword = "HELP";
		String desinationAddress = "12456";
		
		setupChannelAndFilter(keyword, desinationAddress);
		
		waitForBind();
		
		int errorCount = 0;
		int num = 10000;
		String message = keyword + " test message " + RandomStringUtils.randomNumeric(10);
//		do {
			System.out.println("<----------- starting send of " + num + " messages ----------->");
			List<ContactDto> contacts = getContacts(num);
			for (ContactDto dto : contacts) {
				smppServer.sendMessage(message, dto.getMsisdn(), desinationAddress);
			}
			
			waitForSmppMessages(num, MessageType.MESSAGES_SENT);
			
			smppServer.resetStats();
			
			errorCount = smppServer.getErrorCount();
			System.out.println("<----------- finished send of " + num
					+ " messages (" + errorCount + " errors) ----------->");
			
//		} while (errorCount <= 0);
		
		stopSmppServer();
			
		waitForProcessedSmsLogs(num - errorCount, message);
		
		generateDataStats();
		
	}
	
	@Test
	public void testTelfreeReceiveMessages_sustainedFlow() throws Exception{
		startSmppServer(false);
		
		String keyword = "HELP";
		String desinationAddress = "12456";
		
		setupChannelAndFilter(keyword, desinationAddress);
		
		waitForBind();
		
		int count = 0;
		int totalNum = 10000;
		int batchSize = 100;
		String message = keyword + " test message " + RandomStringUtils.randomNumeric(10);
		do {
			List<ContactDto> contacts = getContacts(batchSize);
			for (ContactDto dto : contacts) {
				smppServer.sendMessage(message, dto.getMsisdn(), desinationAddress);
			}
			count += batchSize;
			
			wait_ms(1000);
			
			System.out.println("Status breakdown: " + perfDao.getSmsLogStatusStats());
		} while (count < totalNum);
		
		stopSmppServer();
			
		waitForProcessedSmsLogs(totalNum, message);
		
		generateDataStats();
	}


	private void waitForProcessedSmsLogs(int num, String message) {
		int count = 0;
		int timeout = 0;
		int prevCount = 0;
		do {
			try {Thread.sleep(1000);} catch (InterruptedException e) {}
		
			Search s = new Search(SmsLog.class);
			s.addFilterEqual(SmsLog.PROP_MESSAGE, message);
			s.addFilterOr(Filter.equal(SmsLog.PROP_STATUS, SmsStatus.RX_SUCCESS),
					Filter.equal(SmsLog.PROP_STATUS, SmsStatus.RX_ACTION_FAIL),
					Filter.equal(SmsLog.PROP_STATUS, SmsStatus.RX_FILTER_FAIL),
					Filter.equal(SmsLog.PROP_STATUS, SmsStatus.RX_CHANNEL_FAIL));
			count = getGeneralDao().count(s);
			int persec = count - prevCount;
			prevCount = count;
			System.out.println("Messages processed: total " + count + ", pre second " + persec);
			
			System.out.println("Status breakdown: " + perfDao.getSmsLogStatusStats());
			
			if (persec == 0) {
				timeout++;
			} else {
				timeout = 0;
			}
		} while (count < num && timeout < 10);
		
		if(timeout == 10){
			System.out.println("Timout while waiting for processed smslogs");
		} else {
			System.out.println("==================== All messages processed ====================");
		}
	}


	private void setupChannelAndFilter(String keyword, String desinationAddress) {
		getGeneralDao().save(new Channel("telfree out", ChannelType.OUT, new TelfreeSmppHandler().getConfigDescriptor().getResource()
				, "0"));
		
		Channel channel = new Channel("telfree in", ChannelType.IN, new TelfreeSmppHandler().getConfigDescriptor().getResource()
				, desinationAddress);
		getGeneralDao().save(channel);
		
		Organization organization = getGeneralDao().findAll(Organization.class).get(0);

		MessageFilter keyFilter = new MessageFilter("KeywordFilter", "");
		keyFilter.setName("Keyword Filter");
		keyFilter.setOrganization(organization);
		StringParameter keywordP = new StringParameter(KeywordFilter.KEYWORD, "");
		keywordP.setValue(keyword);
		keyFilter.setProps(YamlUtils.dumpParameterList(keywordP));
		keyFilter.setChannel(channel);
		getGeneralDao().save(keyFilter);
		
		ContactGroup group = new ContactGroup();
		group.setGroupName("test group");
		group.setOrganization(organization);
		getGeneralDao().save(group);
		
		// create add to group action
		Pconfig addtogroup = new AddToGroupAction().getConfigDescriptor();
		((EntityParameter) addtogroup.getParameter(AddToGroupAction.GROUP_ID))
				.setValue(group.getId().toString());

		FilterAction action = new FilterAction(AddToGroupAction.BEAN_NAME, YamlUtils
				.dumpParameterList(addtogroup.getParameters()));
		action.setFilter(keyFilter);
		getGeneralDao().save(
				action);
		
		// create email to group action
		Pconfig email = new EmailAction().getConfigDescriptor();
		((StringParameter)email.getParameter(EmailAction.MAIL_TO)).setValue("test@test.com");
		
		FilterAction action2 = new FilterAction(EmailAction.BEAN_NAME, YamlUtils
				.dumpParameterList(email.getParameters()));
		action2.setFilter(keyFilter);
		getGeneralDao().save(
				action2);
	}
	
	private void generateDataStats() {
		List<GroupStats> stats = perfDao.getSmsLogAttemptStats();
		System.out.println("Attempts: " + stats);
		
		stats = perfDao.getSmsLogStatusStats();
		System.out.println("Status: " + stats);
		
		stats = perfDao.getSmsLogWaspStatusStats();
		System.out.println("Wasp Status: " + stats);
	}

	public void startSmppServer(boolean withTrafficWatcher) throws Exception{
		smppServer = new SMPPServerSimulator(8088, withTrafficWatcher);
		smppServer.setResponseDelay(RESPONSE_DELAY);
		smppServer.setDeliveryReceiptDelay(RECEIPT_DELAY);
		smppServer.run();
	}
	
	public void stopSmppServer(){
		smppServer.shutdown();
	}
	
	public void startIntegratServer() throws Exception{
		integratServer = new IntegratSimulationServer(8065, 20);
		integratServer.setBaseUrl(BASE_URL);
		integratServer.setResponseDelay(RESPONSE_DELAY);
		integratServer.setDeliveryReceiptDelay(RECEIPT_DELAY);
		integratServer.start();
	}
	
	public void stopIntegratServer() throws Exception{
		integratServer.stop();
	}
	

	private void waitForSmppMessages(int i, MessageType type) {
		int count = 0;
		int errcount = 0;
		int previousCount = 0;
		int noActivity = 0;
		int totalCount = 0;
		do {
			previousCount = totalCount;
			switch(type){
			case MESSAGES_RECEIVED:
				count = smppServer.getTotalRequestCount();
				break;
			case MESSAGE_RESPONSES:
				count = smppServer.getTotalResponseCount();
				break;
			case MESSAGES_SENT:
				count = smppServer.getTotalSentCount();
				break;
			}
			
			errcount = smppServer.getErrorCount();
			totalCount = count + errcount;
			
			System.out.println("Message count (" + type + ") = " + count + " (errors = " + errcount + ")");
			if (totalCount == previousCount){
				noActivity++;
			} else {
				noActivity = 0;
			}
			try {Thread.sleep(1000);} catch (InterruptedException e) {}
		} while(totalCount < i && noActivity < 10);
		
		if (noActivity > 0){
			Log.warn("Timeout while waiting for messages");
		}
	}


	private void waitForBind() {
		int i = 0;
		while (!smppServer.isSessionBound() && i < 10){
			try {Thread.sleep(1000);} catch (InterruptedException e) {}
			i++;
		}
		
		if (i == 10){
			System.out.println("Timeout while waiting for bind");
		}
	}
	
	public void createAndRunCampaign(int numContacts, int messageLength) throws Exception {
		ValidatorFactoryImpl vfactory = new ValidatorFactoryImpl();
		vfactory.setCountryRules(Arrays.asList(new MsisdnCountryRule("SA", "27", "^27[1-9][0-9]{8}$")));
		MobilisrClient client = new MobilisrClientImpl(BASE_URL, "admin", "admin",vfactory);
		CampaignService campaignService = client.getCampaignService();
		List<ContactDto> contacts = getContacts(numContacts);
		campaignService.createNewCampaign("test performance campaign"
				+ UUID.randomUUID().toString(), "", TestUtils.getLoremIpsum(messageLength),
				contacts);
	}

	private static List<ContactDto> getContacts(int num) {
		List<ContactDto> list = new ArrayList<ContactDto>();
		for (int i = 0; i < num; i++) {
			ContactDto dto = new ContactDto();
			dto.setMsisdn(String.format("27%09d", i));
			list.add(dto);
		}
		return list;
	}

}
