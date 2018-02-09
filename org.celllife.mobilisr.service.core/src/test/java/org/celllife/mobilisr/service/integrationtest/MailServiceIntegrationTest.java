package org.celllife.mobilisr.service.integrationtest;

import java.io.File;
import java.util.Iterator;

import junit.framework.Assert;

import org.celllife.mobilisr.domain.AlertType;
import org.celllife.mobilisr.domain.MailMessage;
import org.celllife.mobilisr.domain.Organization;
import org.celllife.mobilisr.domain.User;
import org.celllife.mobilisr.domain.mock.DomainMockFactory;
import org.celllife.mobilisr.service.MailService;
import org.celllife.mobilisr.service.constants.SettingsEnum;
import org.celllife.mobilisr.service.impl.MailServiceImpl;
import org.celllife.mobilisr.test.AbstractServiceTest;
import org.celllife.mobilisr.test.MailTestUtils;
import org.celllife.mobilisr.test.TestUtils;
import org.celllife.pconfig.model.Parameter;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import com.dumbster.smtp.SimpleSmtpServer;
import com.dumbster.smtp.SmtpMessage;

public class MailServiceIntegrationTest extends AbstractServiceTest {

	@Autowired
	private ApplicationContext applicationContext;
	
	@Autowired
	private MailService mailService;
	
	private SimpleSmtpServer smtp;

	@SuppressWarnings("unchecked")
	@Before
	public void setup() throws Exception{
		smtp = SimpleSmtpServer.start(9085);
		MailTestUtils.reconfigureMailSenders(applicationContext, 9085);
		((Parameter<Boolean>)SettingsEnum.ENABLE_MAIL_QUEUE_PROCESSING.getConfig()).setValue(true);
		TestUtils.getTargetObject(mailService, MailServiceImpl.class).enableMailSending(true);
		Assert.assertEquals(0, smtp.getReceivedEmailSize());
	}
	
	@After
	public void tearDown(){
		smtp.stop();
	}
	
	@Test
	public void testSendMail(){
		// flush the mail queue
		mailService.sendQueuedMail();
		int emailSize = smtp.getReceivedEmailSize();
		
		Organization org1 = DomainMockFactory._().on(Organization.class).create();
		getGeneralDao().save(org1);
		MailMessage clientAlert1 = new MailMessage(org1.getContactEmail(),null, "test message1", AlertType.BALANCE_PROCESSING, org1);
		getGeneralDao().save(clientAlert1);
		
		Organization org2 = DomainMockFactory._().on(Organization.class).create();
		getGeneralDao().save(org2);
		MailMessage clientAlert2 = new MailMessage(org2.getContactEmail(),null, "test message2", AlertType.SYSTEM_ALERT, org2);
		getGeneralDao().save(clientAlert2);

		getGeneralDao().flush();
		
		mailService.sendQueuedMail();
		
		Assert.assertEquals(emailSize+2, smtp.getReceivedEmailSize());
		
		@SuppressWarnings("unchecked")
		Iterator<SmtpMessage> receivedEmail = smtp.getReceivedEmail();
		SmtpMessage msg = receivedEmail.next();
		Assert.assertEquals(clientAlert1.getText(), msg.getBody());
		
		msg = receivedEmail.next();
		Assert.assertEquals(clientAlert2.getText(), msg.getBody());
	}

	@Test
	public void testSendMailMultipleRecipients(){
		// flush the mail queue
		mailService.sendQueuedMail();
		int emailSize = smtp.getReceivedEmailSize();
		
		Organization org1 = DomainMockFactory._().on(Organization.class).create();
		getGeneralDao().save(org1);
		Organization org2 = DomainMockFactory._().on(Organization.class).create();
		getGeneralDao().save(org2);		
		String to = org1.getContactEmail() + ", " + org2.getContactEmail();
		MailMessage clientAlert1 = new MailMessage(to,"test", "test message", AlertType.BALANCE_PROCESSING, org1);
		getGeneralDao().save(clientAlert1);
		getGeneralDao().flush();
		
		mailService.sendQueuedMail();
		
		Assert.assertEquals(emailSize+1, smtp.getReceivedEmailSize());
		
		@SuppressWarnings("unchecked")
		Iterator<SmtpMessage> receivedEmail = smtp.getReceivedEmail();
		SmtpMessage msg = receivedEmail.next();
		Assert.assertEquals(to, msg.getHeaderValue("To"));
		Assert.assertEquals(clientAlert1.getText(), msg.getBody());
		
	}
	
	@Test
	public void testSendMail_withAtachment(){
		Organization org1 = DomainMockFactory._().on(Organization.class).create();
		getGeneralDao().save(org1);
		MailMessage clientAlert1 = new MailMessage(org1.getContactEmail(),null, "test message1", AlertType.BALANCE_PROCESSING, org1);
		
		String path = this.getClass().getClassLoader().getResource("logback.groovy").getFile();
		
		// flush the mail queue
		mailService.sendQueuedMail();
		int emailSize = smtp.getReceivedEmailSize();
		
		// run the test
		mailService.enqueueMail(org1.getContactEmail(), null, "test message1", new File(path));
		mailService.sendQueuedMail();
		Assert.assertEquals(emailSize+1, smtp.getReceivedEmailSize());
		
		@SuppressWarnings("unchecked")
		Iterator<SmtpMessage> receivedEmail = smtp.getReceivedEmail();
		SmtpMessage msg = receivedEmail.next();
		Assert.assertTrue(msg.getBody().contains(clientAlert1.getText()));
		Assert.assertTrue(msg.getBody().contains("logback.groovy"));
	}
	
	@Test
	@Ignore("For testing how email appears in email clients")
	public void testSendMailForReal(){
		User user = new User();
		user.setEmailAddress("me@cell-life.org");
		user.setFirstName("Test");
		user.setLastName("User");
		mailService.sendResetPasswordEmail(user, "newPassword");
		mailService.sendQueuedMail();
	}
}
