package org.celllife.mobilisr.service.impl;

import static org.celllife.mobilisr.test.MobilisrMockito.listOfSize;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import junit.framework.Assert;

import org.celllife.mobilisr.api.mock.DtoMockFactory;
import org.celllife.mobilisr.api.rest.CampaignDto;
import org.celllife.mobilisr.api.rest.ContactDto;
import org.celllife.mobilisr.api.rest.MessageDto;
import org.celllife.mobilisr.api.rest.PagedListDto;
import org.celllife.mobilisr.constants.ApiVersion;
import org.celllife.mobilisr.constants.CampaignStatus;
import org.celllife.mobilisr.constants.CampaignType;
import org.celllife.mobilisr.dao.api.CampaignDAO;
import org.celllife.mobilisr.dao.api.ContactDAO;
import org.celllife.mobilisr.dao.api.MobilisrGeneralDAO;
import org.celllife.mobilisr.domain.Campaign;
import org.celllife.mobilisr.domain.CampaignContact;
import org.celllife.mobilisr.domain.CampaignMessage;
import org.celllife.mobilisr.domain.Contact;
import org.celllife.mobilisr.domain.ContactGroup;
import org.celllife.mobilisr.domain.ContactMsgTime;
import org.celllife.mobilisr.domain.Organization;
import org.celllife.mobilisr.domain.User;
import org.celllife.mobilisr.domain.mock.DomainMockFactory;
import org.celllife.mobilisr.exception.MobilisrException;
import org.celllife.mobilisr.service.CampaignScheduleService;
import org.celllife.mobilisr.service.CampaignService;
import org.celllife.mobilisr.service.exception.CampaignStateException;
import org.celllife.mobilisr.service.exception.ObjectNotFoundException;
import org.celllife.mobilisr.test.BaseTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.trg.search.Search;
import com.trg.search.SearchResult;

@RunWith(MockitoJUnitRunner.class)
public class CampaignRestServiceImplTest extends BaseTest {

	@Mock
	private CampaignScheduleService campaignScheduleService;

	@Mock
	private CampaignService campaignService;
	
	@Mock
	private MobilisrGeneralDAO generalDao;

	@Mock
	private CampaignDAO campaignDao;

	@Mock
	private ContactDAO contactDao;

	private CampaignRestServiceImpl service;

	private User loggedInUser;

	@Before
	public void setup() {
		service = new CampaignRestServiceImpl();
		service.setGeneralDao(generalDao);
		service.setCampaignDao(campaignDao);
		service.setContactDao(contactDao);
		service.setCampaignService(campaignService);
		service.setCampaignScheduleService(campaignScheduleService);
		
		loggedInUser = DomainMockFactory._().on(User.class).withMode(DomainMockFactory.MODE_LOAD).create();
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetCampaigns_nullUser() {
		service.getCampaigns(null, new Search(), ApiVersion.getLatest());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetCampaigns_nullSearch() {
		service.getCampaigns(new User(), null, ApiVersion.getLatest());
	}

	@Test
	public void testGetCampaigns() {
		// setup mocking
		int total = 59;
		int limit = 7;
		int offset = 3;
		SearchResult<Campaign> value = new SearchResult<Campaign>();
		value.setResult(DomainMockFactory._().on(Campaign.class).create(limit));
		value.setTotalCount(total);
		when(generalDao.searchAndCount(any(Search.class))).thenReturn(value);
		
		// call test method
		PagedListDto<CampaignDto> campaigns = service.getCampaigns(loggedInUser, getSearch(limit, offset), ApiVersion.getLatest());
		
		// verify results
		Assert.assertEquals(limit, campaigns.size());
		Assert.assertEquals(59, campaigns.getTotal().intValue());
		Assert.assertEquals(offset, campaigns.getOffset().intValue());
		Assert.assertEquals(limit, campaigns.getLimit().intValue());

		verify(generalDao).searchAndCount(
				eq(getSearch(limit, offset).addFilterEqual(Campaign.PROP_ORGANIZATION,
						loggedInUser.getOrganization())));
	}
	
	@Test
	public void testAddContactsToCampaign_all_aready_exist() throws MobilisrException {
		final List<ContactDto> contacts = DtoMockFactory._().on(ContactDto.class).create(7);

		// setup mocking
		Campaign campaign = DomainMockFactory._().on(Campaign.class)
				.withMode(DomainMockFactory.MODE_LOAD).create();
		campaign.setStatus(CampaignStatus.ACTIVE);
		campaign.setOrganization(loggedInUser.getOrganization());
		when(campaignDao.find(eq(campaign.getId()))).thenReturn(campaign);
		
		final List<CampaignContact> campContacts = DomainMockFactory._()
			.on(CampaignContact.class).withMode(DomainMockFactory.MODE_LOAD).create(1);
		when(campaignService.convertContactToCampaignContact(
				anyListOf(Contact.class), eq(campaign))).thenReturn(
						campContacts);
		
		// call test method
		service.addContactsToCampaign(loggedInUser, campaign.getId(), contacts, ApiVersion.getLatest());
		
		// verify results
		verify(campaignService, never()).saveOrUpdateCampaignContact(any(CampaignContact.class));
		verify(campaignService).rescheduleRelativeCampaign(eq(campaign), eq(loggedInUser));
	}
	
	@Test
	public void testAddContactsToCampaign() throws MobilisrException {
		// setup mocking
		final List<ContactDto> contacts = DtoMockFactory._()
				.on(ContactDto.class).create(7);
		Campaign campaign = DomainMockFactory._().on(Campaign.class)
				.withMode(DomainMockFactory.MODE_LOAD).create();
		campaign.setStatus(CampaignStatus.ACTIVE);
		campaign.setOrganization(loggedInUser.getOrganization());
		when(campaignDao.find(eq(campaign.getId()))).thenReturn(campaign);
		
		final List<CampaignContact> campContacts = DomainMockFactory._()
			.on(CampaignContact.class).create(1);
		when(campaignService.convertContactToCampaignContact(
				anyListOf(Contact.class), eq(campaign))).thenReturn(
						campContacts);
	
		// call test method
		service.addContactsToCampaign(loggedInUser, campaign.getId(), contacts, ApiVersion.getLatest());
		
		// verify results
		verify(campaignService, times(7)).saveOrUpdateCampaignContact(any(CampaignContact.class));
		verify(campaignService).rescheduleRelativeCampaign(eq(campaign), eq(loggedInUser));
	}
	
	@Test(expected=CampaignStateException.class)
	public void testAddContactsToCampaign_campaignNotRunning() throws MobilisrException {
		// setup mocking
		final List<ContactDto> contacts = DtoMockFactory._().on(ContactDto.class).create(7);

		Campaign campaign = DomainMockFactory._().on(Campaign.class)
				.withMode(DomainMockFactory.MODE_LOAD).create();
		campaign.setOrganization(loggedInUser.getOrganization());
		when(campaignDao.find(eq(campaign.getId()))).thenReturn(campaign);
		
		// call test method
		service.addContactsToCampaign(loggedInUser, campaign.getId(), contacts, ApiVersion.getLatest());
	}
	
	@Test
	public void testAddContactsToCampaign_customMessageTimes() throws MobilisrException{
		// setup mocking
		final List<ContactDto> contacts = DtoMockFactory._().on(ContactDto.class).create(1);
		final List<Date> msgTimes = Arrays.asList(new Date(), new Date());
		for (ContactDto contactDto : contacts) {
			contactDto.setContactMessageTimes(msgTimes);
		}
		
		Campaign campaign = DomainMockFactory._().on(Campaign.class)
				.withMode(DomainMockFactory.MODE_LOAD).create();
		campaign.setOrganization(loggedInUser.getOrganization());
		campaign.setType(CampaignType.DAILY);
		campaign.setStatus(CampaignStatus.ACTIVE);
		when(campaignDao.find(eq(campaign.getId()))).thenReturn(campaign);
		
		final List<CampaignContact> campContacts = DomainMockFactory._()
			.on(CampaignContact.class).create(1);
		campContacts.get(0).setMsisdn(contacts.get(0).getMsisdn());
		campContacts.get(0)
				.getContactMsgTimes()
				.add(new ContactMsgTime(new Date(), 1, campContacts.get(0),
						campaign));
		campContacts.get(0)
				.getContactMsgTimes()
				.add(new ContactMsgTime(new Date(), 1, campContacts.get(0),
						campaign));
		when(campaignService.convertContactToCampaignContact(
				anyListOf(Contact.class), eq(campaign))).thenReturn(
						campContacts);
	
		// call test method
		service.addContactsToCampaign(loggedInUser, campaign.getId(), contacts, ApiVersion.getLatest());
		
		verify(campaignService).saveOrUpdateCampaignContact(argThat(new ArgumentMatcher<CampaignContact>() {
			@Override
			public boolean matches(Object argument) {
				CampaignContact contact = (CampaignContact) argument;
				Assert.assertEquals("Incorrect contact msisdn", contacts.get(0).getMsisdn(),contact.getMsisdn());
				Assert.assertEquals("Expected 2 contact message times", 2, contact.getContactMsgTimes().size());
				
				List<ContactMsgTime> contactMsgTimes = contact.getContactMsgTimes();
				for (int i = 0; i < contactMsgTimes.size(); i++) {
					Assert.assertEquals("Message times do not match", msgTimes.get(i),contactMsgTimes.get(i).getMsgTime());
				}
				return true;
			}
		}));
		verify(campaignService).rescheduleRelativeCampaign(eq(campaign), eq(loggedInUser));
	}
	
	@Test
	public void testRemoveContactsFromCampaign() throws MobilisrException {
		// setup mocking
		Campaign campaign = DomainMockFactory._().on(Campaign.class)
				.withMode(DomainMockFactory.MODE_LOAD).create();
		campaign.setOrganization(loggedInUser.getOrganization());
		campaign.setStatus(CampaignStatus.ACTIVE);
		when(campaignDao.find(campaign.getId())).thenReturn(campaign);
		Contact contact = DomainMockFactory._().on(Contact.class).create();
		contact.setOrganization(loggedInUser.getOrganization());
		when(contactDao.searchByOrganizationAndMSISDN(loggedInUser.getOrganization(), contact.getMsisdn())).thenReturn(contact);
		
		// call test method
		service.removeContactFromCampaign(loggedInUser, campaign.getId(), contact.getMsisdn());
		
		// verify result
		verify(campaignService).removeContactFromCampaign(eq(campaign), any(CampaignContact.class));
		verify(campaignService).rescheduleRelativeCampaign(eq(campaign), eq(loggedInUser));
	}
	
	@Test(expected=CampaignStateException.class)
	public void testRemoveContactsFromCampaign_campaignNotRunning() throws MobilisrException {
		// setup mocking
		Campaign campaign = DomainMockFactory._().on(Campaign.class)
				.withMode(DomainMockFactory.MODE_LOAD).create();
		campaign.setOrganization(loggedInUser.getOrganization());
		when(campaignDao.find(eq(campaign.getId()))).thenReturn(campaign);
		
		// call test method
		service.removeContactFromCampaign(loggedInUser, campaign.getId(), "msisdn");
	}
	
	public void testManageCampaignContacts_add(){
		// TODO	implement test method	
	}
	
	public void testManageCampaignContacts_remove(){
		// TODO	implement test method
	}
	
	public void testManageCampaignContacts_balanceNotFound(){
		// TODO	implement test method
	}
	
	public void testManageCampaignContacts_bnsifficientBalance(){
		// TODO	implement test method
	}
	
	@Test
	public void testGetCampaignInternal() throws MobilisrException {
		// setup mocking
		Campaign campaign = DomainMockFactory._().on(Campaign.class).create();
		campaign.setOrganization(loggedInUser.getOrganization());
		when(campaignDao.find(eq(campaign.getId()))).thenReturn(campaign);
		
		// call test method
		service.getCampaignInternal(loggedInUser, campaign.getId());
		
		// verify results
		verify(campaignDao).find(campaign.getId());
	}
	
	@Test(expected=ObjectNotFoundException.class)
	public void testGetCampaignInternal_notFound() throws MobilisrException {
		when(campaignDao.find(any(Long.class))).thenReturn(null);
		service.getCampaignInternal(null, 7L);
	}
	
	@Test(expected=ObjectNotFoundException.class)
	public void testGetCampaignInternal_restricted() throws MobilisrException {
		Campaign campaign = DomainMockFactory._().on(Campaign.class)
				.withMode(DomainMockFactory.MODE_LOAD).create();
		when(campaignDao.find(eq(campaign.getId()))).thenReturn(campaign);
		service.getCampaignInternal(loggedInUser, campaign.getId());
	}
	
	@Test
	public void testGetContactInternal() throws MobilisrException {
		// setup mocking
		Contact contact = DomainMockFactory._().on(Contact.class).create();
		contact.setOrganization(loggedInUser.getOrganization());
		when(contactDao.searchByOrganizationAndMSISDN(loggedInUser.getOrganization(), contact.getMsisdn())).thenReturn(contact);
		
		// call test method
		service.getContactInternal(loggedInUser, contact.getMsisdn());
		
		// verify results
		verify(contactDao).searchByOrganizationAndMSISDN(eq(loggedInUser.getOrganization()), eq(contact.getMsisdn()));
	}
	
	@Test(expected=ObjectNotFoundException.class)
	public void testGetContactInternal_notFound() throws MobilisrException {
		when(contactDao.searchByOrganizationAndMSISDN(any(Organization.class), any(String.class))).thenReturn(null);
		service.getContactInternal(loggedInUser, "msisdn");
	}
	
	@Test(expected=MobilisrException.class)
	public void testCreateAndRunCampaign_emptyMessages() throws MobilisrException{
		// setup mocking
		CampaignDto campDto = DtoMockFactory._().on(CampaignDto.class).withMode(DtoMockFactory.MODE_POST).create();
		List<MessageDto> emptyList = Collections.emptyList();
		campDto.setMessages(emptyList);
		
		// call test method
		service.createAndRunCampaign(loggedInUser, campDto, ApiVersion.getLatest());
	}
	
	@Test(expected=MobilisrException.class)
	public void testCreateAndRunCampaign_emptyContacts() throws MobilisrException{
		// setup mocking
		CampaignDto campDto = DtoMockFactory._().on(CampaignDto.class).withMode(DtoMockFactory.MODE_POST).create();
		List<ContactDto> emptyList = Collections.emptyList();
		campDto.setContacts(emptyList);
		
		// call test method
		service.createAndRunCampaign(loggedInUser, campDto, ApiVersion.getLatest());
	}
	
	@Test
	public void testCreateAndRunCampaign() throws MobilisrException{
		// setup mocking
		CampaignDto campDto = DtoMockFactory._().on(CampaignDto.class).withMode(DtoMockFactory.MODE_POST).create();
		Campaign campaign = DomainMockFactory._().on(Campaign.class).create();
		when(campaignService.saveOrUpdateCampaign(any(Campaign.class), anyListOf(CampaignMessage.class))).thenReturn(campaign);
		List<Contact> savedContacts = DomainMockFactory._().on(Contact.class).create(campDto.getContacts().size());
		when(contactDao.batchSaveContact(eq(loggedInUser.getOrganization()),
				anyListOf(Contact.class), anyListOf(ContactGroup.class)))
				.thenReturn(savedContacts);
		
		// call test method
		service.createAndRunCampaign(loggedInUser, campDto, ApiVersion.getLatest());
		
		// verify
		verify(campaignService).saveOrUpdateCampaign(any(Campaign.class), 
				listOfSize(CampaignMessage.class, 1));
		verify(campaignDao).addContactsToCampaign(eq(campaign), 
				eq(loggedInUser.getOrganization()),
				listOfSize(Contact.class, campDto.getContacts().size()), eq(false));
		verify(campaignScheduleService).scheduleCampaign(eq(campaign), eq(loggedInUser));
	}
	
	@Test
	public void testUpdateContact() throws MobilisrException{
		// setup mocking
		ContactDto contactDto = DtoMockFactory._().on(ContactDto.class).create();
		Contact contact = DomainMockFactory._().on(Contact.class).withMode(DomainMockFactory.MODE_LOAD).create();
		String oldMsisdn = contact.getMsisdn();
		when(contactDao.searchByOrganizationAndMSISDN(eq(loggedInUser.getOrganization()), eq(oldMsisdn))).thenReturn(contact);
		
		// call test method
		service.updateContactDetails(loggedInUser, oldMsisdn , contactDto);
		
		// verify
		Assert.assertEquals(contactDto.getMsisdn(), contact.getMsisdn());
		verify(contactDao).saveOrUpdate(contact);
		verify(campaignDao).updateCampaignContactMsisdn(eq(contact.getId()), eq(contactDto.getMsisdn()));
	}
	
	@Test(expected=ObjectNotFoundException.class)
	public void testUpdateContact_nonExistant() throws MobilisrException{
		// setup mocking
		ContactDto contactDto = DtoMockFactory._().on(ContactDto.class).create();
		Contact contact = DomainMockFactory._().on(Contact.class).create();
		String oldMsisdn = contact.getMsisdn();
		when(contactDao.searchByOrganizationAndMSISDN(eq(loggedInUser.getOrganization()), eq(oldMsisdn))).thenReturn(null);
		
		// call test method
		service.updateContactDetails(loggedInUser, oldMsisdn , contactDto);
	}
	
	private Search getSearch(int limit, int offset) {
		return new Search(Campaign.class).setMaxResults(limit).setFirstResult(offset);
	}
}
