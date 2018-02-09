package org.celllife.mobilisr.service.action;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.chain.Context;
import org.apache.commons.chain.impl.ContextBase;
import org.celllife.mobilisr.dao.api.MobilisrGeneralDAO;
import org.celllife.mobilisr.domain.Contact;
import org.celllife.mobilisr.domain.ContactGroup;
import org.celllife.mobilisr.domain.MessageFilter;
import org.celllife.mobilisr.domain.Organization;
import org.celllife.mobilisr.domain.SmsLog;
import org.celllife.mobilisr.service.ContactsService;
import org.celllife.mobilisr.service.filter.Action;
import org.celllife.mobilisr.service.gwt.ContactContactGroupViewModel;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class GroupManagementCommandTest {
	
	@Mock
	private ContactsService contactService;
	
	@Mock
	private MobilisrGeneralDAO generalDao;
	
	private GroupManagementCommand addAction = new AddToGroupAction();
	private GroupManagementCommand removeAction = new RemoveFromGroupAction();
	
	@Before
	public void setup() {
		addAction.setDao(generalDao);
		addAction.setContactService(contactService);
		removeAction.setDao(generalDao);
		removeAction.setContactService(contactService);
	}

	@Test
	public void addToGroupHappyTest() throws Exception {	
		Context context = createContext("0768198075", "Cell-Life", 1L);
		
		// setup mock
		ContactGroup contactGroup = new ContactGroup("group", "test group");
		Long groupId = (Long)context.get(GroupManagementCommand.GROUP_ID);
		Mockito.when(generalDao.find(ContactGroup.class, groupId)).thenReturn(contactGroup);
		
		// run test
		addAction.execute(context);
		
		// verify
		List<ContactGroup> groupList = new ArrayList<ContactGroup>();
		groupList.add(contactGroup);
		Contact contact = (Contact)context.get(GetContactCommand.CONTACT);
		ContactContactGroupViewModel contactModel = new ContactContactGroupViewModel(
				contact, groupList, null, false, false);
		Mockito.verify(contactService).addGroupsToContact(contact.getOrganization(), contactModel);
	}
	
	@Test
	public void removeFromGroupHappyTest() throws Exception {
Context context = createContext("0768198075", "Cell-Life", 1L);
		
		// setup mock
		ContactGroup contactGroup = new ContactGroup("group", "test group");
		Long groupId = (Long)context.get(GroupManagementCommand.GROUP_ID);
		Mockito.when(generalDao.find(ContactGroup.class, groupId)).thenReturn(contactGroup);
		
		// run test
		removeAction.execute(context);
		
		// verify
		List<ContactGroup> groupList = new ArrayList<ContactGroup>();
		groupList.add(contactGroup);
		Contact contact = (Contact)context.get(GetContactCommand.CONTACT);
		ContactContactGroupViewModel contactModel = new ContactContactGroupViewModel(
				contact, null, groupList, false, false);
		Mockito.verify(contactService).addGroupsToContact(contact.getOrganization(), contactModel);
	}
	
	@SuppressWarnings("unchecked")
	protected Context createContext(String msisdn, String orgName, Long groupId) {
		Context context = new ContextBase();
		MessageFilter filter = new MessageFilter();
		context.put(Action.FILTER, filter);
		Organization org = new Organization();
		org.setName(orgName);
		SmsLog smsLog = new SmsLog();
		smsLog.setMsisdn(msisdn);
		smsLog.setOrganization(org);
		context.put(Action.SMS_LOG, smsLog);
		Contact contact = new Contact(msisdn, org);
		context.put(GetContactCommand.CONTACT, contact);
		context.put(GroupManagementCommand.GROUP_ID, groupId);
		return context;
	}
}
