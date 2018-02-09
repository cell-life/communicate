package org.celllife.mobilisr.service.action;

import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.contains;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.Properties;

import org.apache.commons.chain.Context;
import org.apache.commons.chain.impl.ContextBase;
import org.apache.velocity.app.VelocityEngine;
import org.celllife.mobilisr.domain.Channel;
import org.celllife.mobilisr.domain.MessageFilter;
import org.celllife.mobilisr.domain.Organization;
import org.celllife.mobilisr.domain.SmsLog;
import org.celllife.mobilisr.service.MailService;
import org.celllife.mobilisr.service.SettingService;
import org.celllife.mobilisr.service.constants.SettingsEnum;
import org.celllife.mobilisr.service.filter.Action;
import org.celllife.mobilisr.service.impl.TemplateServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EmailActionTest {
	
	@Mock
	private MailService mailService;
	
	private TemplateServiceImpl templateService;
	
	private EmailAction action = new EmailAction();

	@Mock
	private SettingService settingService;
	
	@Before
	public void setup() {
		templateService = new TemplateServiceImpl();
		Properties p = new Properties();
		p.put("resource.loader", "class");
		p.put("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
		templateService.setVelocityEngine(new VelocityEngine(p));
		templateService.setSettingService(settingService);
		
		action.setMailService(mailService);
		action.setTemplateService(templateService);
		
		when(settingService.getSettingValue(SettingsEnum.USER_REQUEST_EMAIL))
				.thenReturn("support@test.com");
	}

	@Test
	public void testEmailAction() throws Exception {	
		final String msisdn = "0768198075";
		String orgName = "Cell-Life";
		final String messageText = "test message content";
		String mailTo = "test@test.com";
		Context context = createContext(msisdn, orgName,
				messageText, mailTo, "");
		
		// run test
		action.execute(context);
		
		// verify
		verify(mailService).enqueueMail(eq(mailTo), contains(msisdn), argThat(new ArgumentMatcher<String>() {
			@Override
			public boolean matches(Object argument) {
				String message = (String) argument;
				return message.contains(msisdn) &&
					message.contains(messageText);
			}
		}));
	}
	
	@SuppressWarnings("unchecked")
	protected Context createContext(String msisdn, String orgName, String messageText, String mailTo, String template) {
		Context context = new ContextBase();
		MessageFilter filter = new MessageFilter();
		Channel channel = new Channel();
		channel.setShortCode("1234");
		filter.setChannel(channel);
		context.put(Action.FILTER, filter);
		Organization org = new Organization();
		org.setName(orgName);
		SmsLog smsLog = new SmsLog();
		smsLog.setMsisdn(msisdn);
		smsLog.setOrganization(org);
		smsLog.setDatetime(new Date());
		smsLog.setMessage(messageText);
		context.put(Action.SMS_LOG, smsLog);
		context.put(EmailAction.MAIL_TO, mailTo);
		context.put(EmailAction.MAIL_TEMPLATE, template);
		return context;
	}
}
