package org.celllife.mobilisr.service.impl;

import static org.mockito.Mockito.when;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.velocity.app.VelocityEngine;
import org.celllife.mobilisr.domain.Organization;
import org.celllife.mobilisr.service.SettingService;
import org.celllife.mobilisr.service.constants.SettingsEnum;
import org.celllife.mobilisr.service.utility.MapBuilder;
import org.celllife.mobilisr.test.TestUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TemplateServiceImplTest {
	
	private TemplateServiceImpl service;
	
	@Mock
	private SettingService settingService;
	
	@Before
	public void setup() {
		service = new TemplateServiceImpl();
		Properties p = new Properties();
		p.put("resource.loader", "class");
		p.put("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
		service.setVelocityEngine(new VelocityEngine(p));
		service.setSettingService(settingService);
		
		when(settingService.getSettingValue(SettingsEnum.USER_REQUEST_EMAIL))
				.thenReturn("support@test.com");
	}
	
	@Test
	public void testBasicTemplate(){
		Organization organization = new Organization();
		organization.setBalance(17);
		organization.setReserved(12);
		organization.setName("test org name");
		Map<String, Object> model = MapBuilder.stringObject().put("organization", organization).getMap();
		String generated = service.generateContent(model, "testTemplate.vm");
		Assert.assertEquals(organization.getName()+":"+organization.getAvailableBalance(), generated.trim());
	}
	
	@Test
	public void testAdvancedTemplate(){
		Date dateVar = new Date();
		Map<String, Object> model = MapBuilder.stringObject().put("dateVar", dateVar).getMap();
		String generated = service.generateContent(model, "testTemplateAdvanced.vm");
		String expected = SimpleDateFormat.getDateTimeInstance(DateFormat.MEDIUM,
				DateFormat.MEDIUM).format(dateVar);
		Assert.assertEquals(
				expected, generated.trim());
	}
	
	@Test
	public void testDynamicTemplate(){
		Date date = new Date();
		String receiver = "123456789";
		String messageText = TestUtils.getLoremIpsum(100);
		String sender = "27755555555";
		
		Map<String, Object> map = MapBuilder.stringObject()
		.put("sender", sender)
		.put("messageText", messageText)
		.put("dateReceived", date)
		.put("receiver", receiver)
		.getMap();
		
		String template = "$display.truncate(\"$date.format('yyyy-MM-dd H:m',$dateReceived), into '${receiver}', from '${sender}', ${messageText}\", 100)";
		String message = service.generateDynamicContent(map,template );
		
		String expectedDate = new SimpleDateFormat("yyyy-MM-dd H:m").format(date);
		
		String expectedMessage = expectedDate + ", into '" + receiver + "', from '"
				+ sender + "', " + messageText;
		Assert.assertEquals(StringUtils.abbreviate(expectedMessage, 100), message);
	}
}
