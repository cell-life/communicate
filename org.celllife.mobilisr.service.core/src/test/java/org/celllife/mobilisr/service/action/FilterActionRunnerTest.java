package org.celllife.mobilisr.service.action;

import java.util.Date;
import java.util.List;

import org.celllife.mobilisr.api.mock.MockUtils;
import org.celllife.mobilisr.constants.SmsStatus;
import org.celllife.mobilisr.domain.Channel;
import org.celllife.mobilisr.domain.Contact;
import org.celllife.mobilisr.domain.ContactGroup;
import org.celllife.mobilisr.domain.Contact_ContactGroup;
import org.celllife.mobilisr.domain.FilterAction;
import org.celllife.mobilisr.domain.MailMessage;
import org.celllife.mobilisr.domain.MessageFilter;
import org.celllife.mobilisr.domain.Organization;
import org.celllife.mobilisr.domain.SmsLog;
import org.celllife.mobilisr.domain.mock.DomainMockFactory;
import org.celllife.mobilisr.service.filter.KeywordFilter;
import org.celllife.mobilisr.service.filter.MatchAllFilter;
import org.celllife.mobilisr.service.filter.RegexFilter;
import org.celllife.mobilisr.service.yaml.YamlUtils;
import org.celllife.mobilisr.test.AbstractServiceTest;
import org.celllife.pconfig.model.EntityParameter;
import org.celllife.pconfig.model.Parameter;
import org.celllife.pconfig.model.Pconfig;
import org.celllife.pconfig.model.StringParameter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.trg.search.Search;

public class FilterActionRunnerTest extends AbstractServiceTest {
	
	@Autowired
	private FilterActionRunner runner;
	private Organization organization;
	
	@Before
	public void setup(){
		organization = getGeneralDao().findAll(Organization.class).get(0);
	}
	
	@Test
	public void testFilterRunner_noFilters() throws Exception {
		Channel channel = getChannel();
		
		SmsLog smsLog = new SmsLog(MockUtils.createMsisdn(0), "", "text", "", SmsStatus.RX_SUCCESS, null, null, 
				new Date(), channel, null);
		smsLog.setDir(SmsLog.SMS_DIR_IN);
		
		runner.processMessage(smsLog);
		
		// verify smslog created
		Search s = new Search(SmsLog.class);
		s.addFilterEqual(SmsLog.PROP_MSISDN, smsLog.getMsisdn());
		@SuppressWarnings("unchecked")
		List<SmsLog> logs = getGeneralDao().search(s);
		Assert.assertEquals(1, logs.size());
		Assert.assertNull(logs.get(0).getOrganization());
	}
	
	@Test
	public void testFilterRunner_noActions() throws Exception {
		Channel channel = getChannel();
		
		getFilter(channel, MatchAllFilter.BEAN_NAME, (Parameter<?>[])null);
		
		String msisdn = MockUtils.createMsisdn(0);
		String message = "text";
		SmsLog smsLog = getSmsLog(channel, msisdn, message);
		
		runner.processMessage(smsLog);
		
		// verify smslog created
		Search s = new Search(SmsLog.class);
		s.addFilterEqual(SmsLog.PROP_MSISDN, smsLog.getMsisdn());
		@SuppressWarnings("unchecked")
		List<SmsLog> logs = getGeneralDao().search(s);
		Assert.assertEquals(1, logs.size());
		Assert.assertEquals(organization.getId(), logs.get(0).getOrganization().getId());
	}
	
	@Test
	public void testFilterRunner_keywordFilter_match() throws Exception {
		Channel channel = getChannel();
		
		StringParameter parameter = new StringParameter(KeywordFilter.KEYWORD, "");
		parameter.setValue("test");
		getFilter(channel, KeywordFilter.BEAN_NAME, parameter);
		
		String msisdn = MockUtils.createMsisdn(0);
		String message = "test message";
		SmsLog smsLog = getSmsLog(channel, msisdn, message);
		
		runner.processMessage(smsLog);
		
		// verify smslog created for correct organisation
		Search s = new Search(SmsLog.class);
		s.addFilterEqual(SmsLog.PROP_MSISDN, smsLog.getMsisdn());
		@SuppressWarnings("unchecked")
		List<SmsLog> logs = getGeneralDao().search(s);
		Assert.assertEquals(1, logs.size());
		Assert.assertEquals(organization.getId(), logs.get(0).getOrganization().getId());
	}
	
	@Test
	public void testFilterRunner_keywordFilter_noMatch() throws Exception {
		Channel channel = getChannel();
		
		StringParameter parameter = new StringParameter(KeywordFilter.KEYWORD, "");
		parameter.setValue("test1");
		getFilter(channel, KeywordFilter.BEAN_NAME, parameter);
		
		String msisdn = MockUtils.createMsisdn(0);
		String message = "test message";
		SmsLog smsLog = getSmsLog(channel, msisdn, message);
		
		runner.processMessage(smsLog);
		
		// verify smslog created with no organisation
		Search s = new Search(SmsLog.class);
		s.addFilterEqual(SmsLog.PROP_MSISDN, smsLog.getMsisdn());
		@SuppressWarnings("unchecked")
		List<SmsLog> logs = getGeneralDao().search(s);
		Assert.assertEquals(1, logs.size());
		Assert.assertNull(logs.get(0).getOrganization());
	}
	
	@Test
	public void testFilterRunner_regexFilter_match() throws Exception {
		Channel channel = getChannel();
		
		StringParameter parameter = new StringParameter(RegexFilter.REGEX, "");
		parameter.setValue("^[a-z]*$");
		getFilter(channel, RegexFilter.BEAN_NAME, parameter);
		
		String msisdn = MockUtils.createMsisdn(0);
		String message = "testmessage";
		SmsLog smsLog = getSmsLog(channel, msisdn, message);
		
		runner.processMessage(smsLog);
		
		// verify smslog created for correct organisation
		Search s = new Search(SmsLog.class);
		s.addFilterEqual(SmsLog.PROP_MSISDN, smsLog.getMsisdn());
		@SuppressWarnings("unchecked")
		List<SmsLog> logs = getGeneralDao().search(s);
		Assert.assertEquals(1, logs.size());
		Assert.assertEquals(organization.getId(), logs.get(0).getOrganization().getId());
	}
	
	@Test
	public void testFilterRunner_regexFilter_noMatch() throws Exception {
		Channel channel = getChannel();
		
		StringParameter parameter = new StringParameter(RegexFilter.REGEX, "");
		parameter.setValue("^[a-z]*$");
		getFilter(channel, RegexFilter.BEAN_NAME, parameter);
		
		String msisdn = MockUtils.createMsisdn(0);
		String message = "123message";
		SmsLog smsLog = getSmsLog(channel, msisdn, message);
		
		runner.processMessage(smsLog);
		
		// verify smslog created with no organisation
		Search s = new Search(SmsLog.class);
		s.addFilterEqual(SmsLog.PROP_MSISDN, smsLog.getMsisdn());
		@SuppressWarnings("unchecked")
		List<SmsLog> logs = getGeneralDao().search(s);
		Assert.assertEquals(1, logs.size());
		Assert.assertNull(logs.get(0).getOrganization());
	}
	
	/**
	 * Test to see that keyword filter gets processed even though regex also
	 * matches.
	 * 
	 * Test filter processing order (based on MessageFilter.type ordering)
	 */
	@Test
	public void testFilterRunner_multipleFilters_keyword() throws Exception {
		Channel channel = getChannel();
		
		StringParameter parameter = new StringParameter(RegexFilter.REGEX, "");
		parameter.setValue("^[a-z]*$");
		getFilter(channel, RegexFilter.BEAN_NAME, parameter);

		parameter = new StringParameter(KeywordFilter.KEYWORD, "");
		parameter.setValue("test");
		MessageFilter keyword = getFilter(channel, KeywordFilter.BEAN_NAME, parameter);
		
		
		String msisdn = MockUtils.createMsisdn(0);
		String message = "test message";
		SmsLog smsLog = getSmsLog(channel, msisdn, message);
		
		runner.processMessage(smsLog);
		
		// verify smslog created with no organisation
		Search s = new Search(SmsLog.class);
		s.addFilterEqual(SmsLog.PROP_MSISDN, smsLog.getMsisdn());
		@SuppressWarnings("unchecked")
		List<SmsLog> logs = getGeneralDao().search(s);
		Assert.assertEquals(1, logs.size());
		Assert.assertEquals(keyword.getIdentifierString(), logs.get(0).getCreatedfor());
	}
	
	/**
	 * Test to make sure that regex filter gets processed when keyword doesn't match
	 */
	@Test
	public void testFilterRunner_multipleFilters_regex() throws Exception {
		Channel channel = getChannel();
		
		StringParameter parameter = new StringParameter(RegexFilter.REGEX, "");
		parameter.setValue("^[a-z]*$");
		MessageFilter regex = getFilter(channel, RegexFilter.BEAN_NAME, parameter);

		parameter = new StringParameter(KeywordFilter.KEYWORD, "");
		parameter.setValue("test");
		getFilter(channel, KeywordFilter.BEAN_NAME, parameter);
		
		String msisdn = MockUtils.createMsisdn(0);
		String message = "message";
		SmsLog smsLog = getSmsLog(channel, msisdn, message);
		
		runner.processMessage(smsLog);
		
		// verify smslog created with no organisation
		Search s = new Search(SmsLog.class);
		s.addFilterEqual(SmsLog.PROP_MSISDN, smsLog.getMsisdn());
		@SuppressWarnings("unchecked")
		List<SmsLog> logs = getGeneralDao().search(s);
		Assert.assertEquals(1, logs.size());
		Assert.assertEquals(regex.getIdentifierString(), logs.get(0).getCreatedfor());
	}
	
	/**
	 * Test that match all filter gets processed when no other filter matches
	 */
	@Test
	public void testFilterRunner_multipleFilters_matchAll_matches() throws Exception {
		Channel channel = getChannel();
		
		MessageFilter matchall = getFilter(channel, MatchAllFilter.BEAN_NAME, (Parameter<?>[])null);

		StringParameter parameter = new StringParameter(KeywordFilter.KEYWORD, "");
		parameter.setValue("test");
		getFilter(channel, KeywordFilter.BEAN_NAME, parameter);
		
		parameter = new StringParameter(RegexFilter.REGEX, "");
		parameter.setValue("^[a-z]*$");
		getFilter(channel, RegexFilter.BEAN_NAME, parameter);
		
		String msisdn = MockUtils.createMsisdn(0);
		String message = "123";
		SmsLog smsLog = getSmsLog(channel, msisdn, message);
		
		runner.processMessage(smsLog);
		
		// verify smslog created with no organisation
		Search s = new Search(SmsLog.class);
		s.addFilterEqual(SmsLog.PROP_MSISDN, smsLog.getMsisdn());
		@SuppressWarnings("unchecked")
		List<SmsLog> logs = getGeneralDao().search(s);
		Assert.assertEquals(1, logs.size());
		Assert.assertEquals(matchall.getIdentifierString(), logs.get(0).getCreatedfor());
	}
	
	/**
	 * Test to make sure that a match all filter does not get processed
	 * when another filter also matches the message
	 */
	@Test
	public void testFilterRunner_multipleFilters_matchAll_noMatch() throws Exception {
		Channel channel = getChannel();
		
		getFilter(channel, MatchAllFilter.BEAN_NAME, (Parameter<?>[])null);

		StringParameter parameter = new StringParameter(KeywordFilter.KEYWORD, "");
		parameter.setValue("test");
		MessageFilter keyword = getFilter(channel, KeywordFilter.BEAN_NAME, parameter);
		
		parameter = new StringParameter(RegexFilter.REGEX, "");
		parameter.setValue("^[a-z]*$");
		getFilter(channel, RegexFilter.BEAN_NAME, parameter);
		
		String msisdn = MockUtils.createMsisdn(0);
		String message = "test";
		SmsLog smsLog = getSmsLog(channel, msisdn, message);
		
		runner.processMessage(smsLog);
		
		// verify smslog created with no organisation
		Search s = new Search(SmsLog.class);
		s.addFilterEqual(SmsLog.PROP_MSISDN, smsLog.getMsisdn());
		@SuppressWarnings("unchecked")
		List<SmsLog> logs = getGeneralDao().search(s);
		Assert.assertEquals(1, logs.size());
		Assert.assertEquals(keyword.getIdentifierString(), logs.get(0).getCreatedfor());
	}

	@Test
	public void testFilterRunner_addToGroup() throws Exception {
		Channel channel = getChannel();
		
		MessageFilter filter = getFilter(channel, MatchAllFilter.BEAN_NAME, (Parameter<?>[])null);
		
		ContactGroup group = DomainMockFactory._().on(ContactGroup.class).create();
		group.setOrganization(organization);
		getGeneralDao().save(group);
		
		EntityParameter parameter = new EntityParameter(AddToGroupAction.GROUP_ID, "");
		parameter.setValueType(Long.class.getSimpleName());
		parameter.setValue(group.getId().toString());
		
		String type = AddToGroupAction.BEAN_NAME;
		createFilterAction(type, filter, parameter);
		
		String msisdn = MockUtils.createMsisdn(0);
		String message = "text";
		SmsLog smsLog = getSmsLog(channel, msisdn, message);
		
		runner.processMessage(smsLog);
		
		// verify smslog created
		Search s = new Search(SmsLog.class);
		s.addFilterEqual(SmsLog.PROP_MSISDN, smsLog.getMsisdn());
		@SuppressWarnings("unchecked")
		List<SmsLog> logs = getGeneralDao().search(s);
		Assert.assertEquals(1, logs.size());
		Assert.assertEquals(organization.getId(), logs.get(0).getOrganization().getId());
		
		// verify contact added to group
		s = new Search(Contact.class);
		s.addFilterEqual(Contact.PROP_MSISDN, smsLog.getMsisdn());
		s.addFetch(Contact.PROP_CONTACT_GROUPS);
		@SuppressWarnings("unchecked")
		List<Contact> contacts = getGeneralDao().search(s);
		Assert.assertEquals(1, contacts.size());
		List<ContactGroup> groups = contacts.get(0).getContactGroups();
		Assert.assertEquals(1, groups.size());
		Assert.assertEquals(group.getGroupName(), groups.get(0).getGroupName());
	}
	
	@Test
	public void testFilterRunner_removeFromGroup() throws Exception {
		Channel channel = getChannel();
		
		MessageFilter filter = getFilter(channel, MatchAllFilter.BEAN_NAME, (Parameter<?>[])null);
		
		ContactGroup group = DomainMockFactory._().on(ContactGroup.class).create();
		group.setOrganization(organization);
		getGeneralDao().save(group);
		
		String msisdn = MockUtils.createMsisdn(0);
		Contact contact = DomainMockFactory._().on(Contact.class).create();
		contact.setMsisdn(msisdn);
		contact.setOrganization(organization);
		getGeneralDao().save(contact);
		
		getGeneralDao().save(new Contact_ContactGroup(contact, group));
		
		EntityParameter parameter = new EntityParameter(AddToGroupAction.GROUP_ID, "");
		parameter.setValueType(Long.class.getSimpleName());
		parameter.setValue(group.getId().toString());
		
		String type = RemoveFromGroupAction.BEAN_NAME;
		createFilterAction(type, filter, parameter);
		
		String message = "text";
		SmsLog smsLog = getSmsLog(channel, msisdn, message);
		
		runner.processMessage(smsLog);
		
		// verify smslog created
		Search s = new Search(SmsLog.class);
		s.addFilterEqual(SmsLog.PROP_MSISDN, smsLog.getMsisdn());
		@SuppressWarnings("unchecked")
		List<SmsLog> logs = getGeneralDao().search(s);
		Assert.assertEquals(1, logs.size());
		Assert.assertEquals(organization.getId(), logs.get(0).getOrganization().getId());

		// verify contact removed from group
		s = new Search(Contact.class);
		s.addFilterEqual(Contact.PROP_MSISDN, smsLog.getMsisdn());
		s.addFetch(Contact.PROP_CONTACT_GROUPS);
		@SuppressWarnings("unchecked")
		List<Contact> contacts = getGeneralDao().search(s);
		Assert.assertEquals(1, contacts.size());
		List<ContactGroup> groups = contacts.get(0).getContactGroups();
		Assert.assertEquals(0, groups.size());
	}
	
	@Test
	public void testFilterRunner_emailAction() throws Exception {
		Channel channel = getChannel();
		
		MessageFilter filter = getFilter(channel, MatchAllFilter.BEAN_NAME, (Parameter<?>[])null);
		
		ContactGroup group = DomainMockFactory._().on(ContactGroup.class).create();
		group.setOrganization(organization);
		getGeneralDao().save(group);
		
		String msisdn = MockUtils.createMsisdn(0);
		Contact contact = DomainMockFactory._().on(Contact.class).create();
		contact.setMsisdn(msisdn);
		contact.setOrganization(organization);
		getGeneralDao().save(contact);
		
		getGeneralDao().save(new Contact_ContactGroup(contact, group));
		
		Pconfig email = new EmailAction().getConfigDescriptor();
		((StringParameter)email.getParameter(EmailAction.MAIL_TO)).setValue("test@test.com");
		
		String type = EmailAction.BEAN_NAME;
		createFilterAction(type, filter, email.getParameter(EmailAction.MAIL_TO));
		
		String message = "text";
		SmsLog smsLog = getSmsLog(channel, msisdn, message);
		
		runner.processMessage(smsLog);
		
		// verify smslog created
		Search s = new Search(SmsLog.class);
		s.addFilterEqual(SmsLog.PROP_MSISDN, smsLog.getMsisdn());
		@SuppressWarnings("unchecked")
		List<SmsLog> logs = getGeneralDao().search(s);
		Assert.assertEquals(1, logs.size());
		Assert.assertEquals(organization.getId(), logs.get(0).getOrganization().getId());

		// verify mail queued
		s = new Search(MailMessage.class);
		s.addFilterEqual(MailMessage.PROP_ADDRESS, "test@test.com");
		@SuppressWarnings("unchecked")
		List<MailMessage> mails = getGeneralDao().search(s);
		Assert.assertEquals(1, mails.size());
	}
	
	@Test
	public void testFilterRunner_multiAction() throws Exception {
		Channel channel = getChannel();
		
		MessageFilter filter = getFilter(channel, MatchAllFilter.BEAN_NAME, (Parameter<?>[])null);
		
		ContactGroup group = DomainMockFactory._().on(ContactGroup.class).create();
		group.setOrganization(organization);
		getGeneralDao().save(group);
		
		String msisdn = MockUtils.createMsisdn(0);
		Contact contact = DomainMockFactory._().on(Contact.class).create();
		contact.setMsisdn(msisdn);
		contact.setOrganization(organization);
		getGeneralDao().save(contact);
		
		getGeneralDao().save(new Contact_ContactGroup(contact, group));
		
		Pconfig email = new EmailAction().getConfigDescriptor();
		((StringParameter)email.getParameter(EmailAction.MAIL_TO)).setValue("test@test.com");
		createFilterAction(EmailAction.BEAN_NAME, filter,
				email.getParameter(EmailAction.MAIL_TO));

		EntityParameter parameter = new EntityParameter(AddToGroupAction.GROUP_ID, "");
		parameter.setValueType(Long.class.getSimpleName());
		parameter.setValue(group.getId().toString());
		createFilterAction(AddToGroupAction.BEAN_NAME, filter, parameter);
		
		String message = "text";
		SmsLog smsLog = getSmsLog(channel, msisdn, message);
		
		runner.processMessage(smsLog);
		
		// verify smslog created
		Search s = new Search(SmsLog.class);
		s.addFilterEqual(SmsLog.PROP_MSISDN, smsLog.getMsisdn());
		@SuppressWarnings("unchecked")
		List<SmsLog> logs = getGeneralDao().search(s);
		Assert.assertEquals(1, logs.size());
		Assert.assertEquals(organization.getId(), logs.get(0).getOrganization().getId());

		// verify contact added to group
		s = new Search(Contact.class);
		s.addFilterEqual(Contact.PROP_MSISDN, smsLog.getMsisdn());
		s.addFetch(Contact.PROP_CONTACT_GROUPS);
		@SuppressWarnings("unchecked")
		List<Contact> contacts = getGeneralDao().search(s);
		Assert.assertEquals(1, contacts.size());
		List<ContactGroup> groups = contacts.get(0).getContactGroups();
		Assert.assertEquals(1, groups.size());
		Assert.assertEquals(group.getGroupName(), groups.get(0).getGroupName());
		
		// verify mail queued
		s = new Search(MailMessage.class);
		s.addFilterEqual(MailMessage.PROP_ADDRESS, "test@test.com");
		@SuppressWarnings("unchecked")
		List<MailMessage> mails = getGeneralDao().search(s);
		Assert.assertEquals(1, mails.size());
	}
	
	private SmsLog getSmsLog(Channel channel, String msisdn, String message) {
		SmsLog smsLog = new SmsLog(msisdn, "", message, "", SmsStatus.RX_SUCCESS, null, null, 
				new Date(), channel, null);
		smsLog.setDir(SmsLog.SMS_DIR_IN);
		return smsLog;
	}

	private void createFilterAction(String type, MessageFilter filter, Parameter<?>... params) {
		FilterAction action = new FilterAction(type, YamlUtils.dumpParameterList(params));
		action.setFilter(filter);
		getGeneralDao().save(action);
	}
	
	private Channel getChannel() {
		return getGeneralDao().findAll(Channel.class).get(0);
	}

	private MessageFilter getFilter(Channel channel, String type, Parameter<?>... params) {
		MessageFilter filter = new MessageFilter(type, YamlUtils.dumpParameterList(params));
		filter.setChannel(channel);
		filter.setName("test filter");
		filter.setOrganization(organization);
		filter.setActive(true);
		if (KeywordFilter.BEAN_NAME.equals(type))
			filter.setRank(1);
		else if (RegexFilter.BEAN_NAME.equals(type))
			filter.setRank(2);
		else if (MatchAllFilter.BEAN_NAME.equals(type))
			filter.setRank(3);
		
		getGeneralDao().save(filter);
		return filter;
	}
	
}
