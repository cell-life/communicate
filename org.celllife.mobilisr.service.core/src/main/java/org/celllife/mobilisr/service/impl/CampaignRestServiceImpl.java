package org.celllife.mobilisr.service.impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.celllife.mobilisr.annotation.LogLevel;
import org.celllife.mobilisr.annotation.Loggable;
import org.celllife.mobilisr.api.rest.CampaignDto;
import org.celllife.mobilisr.api.rest.ContactDto;
import org.celllife.mobilisr.api.rest.MessageDto;
import org.celllife.mobilisr.api.rest.MessageStatusDto;
import org.celllife.mobilisr.api.rest.PagedListDto;
import org.celllife.mobilisr.constants.ApiVersion;
import org.celllife.mobilisr.constants.CampaignStatus;
import org.celllife.mobilisr.constants.CampaignType;
import org.celllife.mobilisr.converter.DtoConverterFactory;
import org.celllife.mobilisr.converter.EntityDtoConverterFactory;
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
import org.celllife.mobilisr.domain.SmsLog;
import org.celllife.mobilisr.domain.User;
import org.celllife.mobilisr.exception.MobilisrException;
import org.celllife.mobilisr.service.CampaignRestService;
import org.celllife.mobilisr.service.CampaignScheduleService;
import org.celllife.mobilisr.service.CampaignService;
import org.celllife.mobilisr.service.exception.CampaignStateException;
import org.celllife.mobilisr.service.exception.ObjectNotFoundException;
import org.celllife.mobilisr.service.utility.RestUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.trg.search.Search;
import com.trg.search.SearchResult;

@Service("campaignRestService")
public class CampaignRestServiceImpl implements CampaignRestService {

	private static Logger log = LoggerFactory.getLogger(CampaignRestServiceImpl.class);

	@Autowired
	CampaignScheduleService campaignScheduleService;
	
	@Autowired
	CampaignService campaignService;
	
	@Autowired
	MobilisrGeneralDAO generalDao;

	@Autowired
	ContactDAO contactDao;

	@Autowired
	CampaignDAO campaignDao;

	@Override
	@Loggable(LogLevel.TRACE)
	public PagedListDto<CampaignDto> getCampaigns(User user, Search search, ApiVersion ver) {
		Validate.notNull(user, "User is null");
		Validate.notNull(search, "Search config is null");

		search.setSearchClass(Campaign.class);
		search.addFilterEqual(Campaign.PROP_ORGANIZATION, user.getOrganization());
		@SuppressWarnings("unchecked")
		SearchResult<Campaign> searchResult = generalDao.searchAndCount(search);
		PagedListDto<CampaignDto> listDto = RestUtil.getPagedList(CampaignDto.class, search,
				searchResult, ver);
		return listDto;
	}
	
	@Override
	@Loggable(LogLevel.TRACE)
	public PagedListDto<MessageStatusDto> getCampaignMessageLogs(User user, Long campaignId, Search search, ApiVersion ver) throws ObjectNotFoundException {
		Validate.notNull(campaignId, "Campaign ID is null");
		Validate.notNull(search, "Search config is null");
		
		Campaign campaign = getCampaignInternal(user, campaignId);

		search.setSearchClass(SmsLog.class);
		search.addFilterEqual(SmsLog.PROP_CREATEDFOR, campaign.getIdentifierString());
		@SuppressWarnings("unchecked")
		SearchResult<SmsLog> searchResult = generalDao.searchAndCount(search);
		PagedListDto<MessageStatusDto> listDto = RestUtil.getPagedList(MessageStatusDto.class,
				search, searchResult, ver);
		return listDto;
	}

	@Loggable(LogLevel.TRACE)
	@Override
	public CampaignDto getCampaign(User user, Long id, ApiVersion ver)
			throws MobilisrException {
		Validate.notNull(user, "User is null");
		Validate.notNull(id, "Campaign ID is null");
		
		Campaign campaign = getCampaignInternal(user, id);

		EntityDtoConverterFactory factory = DtoConverterFactory.getInstance();
		return factory.toDto(campaign, CampaignDto.class,ver);
	}

	@Loggable(LogLevel.TRACE)
	@Override
	public void addContactsToCampaign(User user, Long id,
			List<ContactDto> contactDtos, ApiVersion ver) throws MobilisrException {
		Validate.notNull(user, "User is null");
		Validate.notNull(id, "Campaign ID is null");

		Campaign campaign = getCampaignInternal(user, id);
		if (!campaign.isActive()){
			throw new CampaignStateException("Campaign is not active. You can only add or remove contacts from a running campaign.");
		}

        List<Long> linkedCampaigns = campaignService.getLinkedCampaignsForCampaign(campaign.getId());

		EntityDtoConverterFactory factory = DtoConverterFactory.getInstance();

		for (ContactDto contactDto : contactDtos) {
			Contact contact = contactDao.searchByOrganizationAndMSISDN(user.getOrganization(), contactDto.getMsisdn());
			if (contact != null){
				mergeContactDtoWithContact(contactDto, contact);
			} else {
				contact = factory.fromDto(contactDto, Contact.class, ver);
				contact.setOrganization(user.getOrganization());
			}
			contactDao.saveOrUpdate(contact);

			List<CampaignContact> campaignContacts = campaignService
					.convertContactToCampaignContact(
							Arrays.asList(new Contact[] { contact }), campaign);

			CampaignContact campaignContact = campaignContacts.get(0);

            if (linkedCampaigns.isEmpty()) {

			if (!campaignContact.isPersisted() || campaignContact.getEndDate() != null) {
				// ignore contact that are already active on the campaign
				if (CampaignType.DAILY.equals(campaign.getType())) {
					List<Date> contactMessageTimes = contactDto.getContactMessageTimes();
					setMessageTimesForContact(campaign, campaignContact, contactMessageTimes);
				}

				campaignContact.setEndDate(null);
				campaignContact.setProgress(0);

                if (contactDto.getStartDate() != (null)) {
                    Date joiningDate;
                     try {
                         joiningDate = new SimpleDateFormat("yyyy-MM-dd").parse(contactDto.getStartDate());
                         campaignContact.setJoiningDate(joiningDate);
                     } catch (Exception e) {
                         log.warn("Could not convert joining date " + contactDto.getStartDate() + " for contact " + contactDto.getMsisdn());
                     }
                }

				campaignService.saveOrUpdateCampaignContact(campaignContact);
			}

            } else {

                    Date joiningDate = null;
                    if (contactDto.getStartDate() != (null)) {
                        try {
                            joiningDate = new SimpleDateFormat("yyyy-MM-dd").parse(contactDto.getStartDate());
                        } catch (Exception e) {
                            log.warn("Could not convert joining date " + contactDto.getStartDate() + " for contact " + contactDto.getMsisdn());
                        }
                    }

                    try {
                        List<CampaignContact> campaignContactsToPassOn = new ArrayList<CampaignContact>();
                        campaignContactsToPassOn.add(campaignContact);
                        campaignService.addContactsToCampaignWithLinks(campaignContactsToPassOn, campaign, joiningDate, contactDto.getContactMessageTimes());
                    } catch (Exception e) {
                        throw new MobilisrException(e.getMessage(), e.getCause());
                    }
                }
        }

        if (linkedCampaigns.isEmpty()) {
		    campaignService.rescheduleRelativeCampaign(campaign, user);
	    }
    }

	/**
	 * Used when adding a contact to a campaign. This method creates the ContactMsgTimes for the CampaignContact
	 * based on the default times for the campaign and a list of supplied times for the contact.
	 * 
	 * If the supplied times are not present or do not match the default times in number they are ignored.
	 * 
	 * @param campaign
	 * @param campaignContact
	 * @param customContactMessageTimes list of message times to be set for the campaign contact
	 */
	/* package private*/ void setMessageTimesForContact(Campaign campaign,
			CampaignContact campaignContact,
			List<Date> customContactMessageTimes) {
		//TODO: write unit tests for setMessageTimesForContact

		List<ContactMsgTime> defaultTimeCampaignMsgs = campaignContact.getContactMsgTimes();
        if (customContactMessageTimes == null) {
            log.debug("No message times supplied for campaign contact with id " + campaignContact.getId() + ". Default campaign message times will be used.");
            customContactMessageTimes = null;
        } else if (customContactMessageTimes.size() != defaultTimeCampaignMsgs.size()) {
            log.warn("There are " + defaultTimeCampaignMsgs.size() + " default message times for campaign contact " + campaignContact.getId() + ", but " +  customContactMessageTimes.size() + " supplied message times. The default campaign message times will be used.");
			customContactMessageTimes = null;
		} else {
			Collections.sort(customContactMessageTimes);
		}
		
		List<ContactMsgTime> contactMsgTimes = new ArrayList<ContactMsgTime>();
		for (int i = 0; i < defaultTimeCampaignMsgs.size(); i++) {
			Date msgTime = customContactMessageTimes == null ? defaultTimeCampaignMsgs.get(i).getMsgTime() : customContactMessageTimes.get(i);
			int msgSlot = defaultTimeCampaignMsgs.get(i).getMsgSlot();
			contactMsgTimes.add(new ContactMsgTime(msgTime,	msgSlot, campaignContact, campaign));
		}
		campaignContact.setContactMsgTimes(contactMsgTimes);
	}

	@Loggable(LogLevel.TRACE)
	@Override
	public void removeContactFromCampaign(User user, Long campaignId,
			String msisdn) throws MobilisrException {
		Validate.notNull(user, "User is null");
		Validate.notNull(campaignId, "Campaign ID is null");
		Validate.notNull(msisdn, "MSISDN is null");
		
		Campaign campaign = getCampaignInternal(user, campaignId);
		if (!campaign.isActive()){
			throw new CampaignStateException("Campaign is not active. You can only add or remove contacts from a running campaign.");
		}
		
		Contact contact = getContactInternal(user, msisdn);
		campaignService.removeContactFromCampaign(campaign, contact);
		campaignService.rescheduleRelativeCampaign(campaign, user);
	}
	
	@Loggable(LogLevel.TRACE)
	@Override
	public Campaign createAndRunCampaign(User user, CampaignDto dto, ApiVersion ver) throws MobilisrException {
		EntityDtoConverterFactory factory = DtoConverterFactory.getInstance();

		List<MessageDto> messages = dto.getMessages();
		if (messages == null || messages.isEmpty()){
			throw new MobilisrException("No messages in campaign");
		}
		
		List<ContactDto> contactDtos = dto.getContacts();
		if (contactDtos == null || contactDtos.isEmpty()){
			throw new MobilisrException("No contacts in campaign");
		}
		
		Organization organization = user.getOrganization();

		Campaign campaign = new Campaign();
		campaign.setName(dto.getName());
		campaign.setDescription(dto.getDescription());
		campaign.setOrganization(organization);
		campaign.setStatus(CampaignStatus.INACTIVE);
		campaign.setType(CampaignType.FIXED);
		campaign.setStartDate(new Date());
		campaign.setContactCount(contactDtos.size());
		campaign.setMessageCount(1);
		campaign.setCost(1);

		CampaignMessage message = factory.fromDto(messages.get(0), CampaignMessage.class, ver);
		if (message.getMsgDate() == null){
			message.setMsgDate(new Date());
		}
		if (message.getMsgTime() == null){
			message.setMsgTime(new Date());
		}
		campaign = campaignService.saveOrUpdateCampaign(campaign, Arrays.asList(message));
		
		List<Contact> contacts = saveContacts(organization, contactDtos, ver);
		campaignDao.addContactsToCampaign(campaign, organization, contacts, false);
		
		campaignScheduleService.scheduleCampaign(campaign, user);
		return campaign;
	}

	private List<Contact> saveContacts(Organization org, List<ContactDto> contactDtos, ApiVersion ver) {
		EntityDtoConverterFactory factory = DtoConverterFactory.getInstance();
		List<Contact> contactList = factory.fromDto(contactDtos, Contact.class, ver);
		return contactDao.batchSaveContact(org, contactList, new ArrayList<ContactGroup>());
	}

	Contact getContactInternal(User user, String msisdn) throws ObjectNotFoundException {
		Contact contact = contactDao.searchByOrganizationAndMSISDN(user.getOrganization(), msisdn);
		if (contact == null) {
			throw new ObjectNotFoundException("Contact (" + msisdn
					+ ") does not exist");
		}
		return contact;
	}

	Campaign getCampaignInternal(User user, Long id)
			throws ObjectNotFoundException {

		Campaign campaign = campaignDao.find(id);
		if (campaign == null) {
			throw new ObjectNotFoundException("Campaign " + id
					+ " does not exist");
		} else {
			Organization org = user.getOrganization();
			if (!campaign.getOrganization().getId()
					.equals(org.getId())) {
				throw new ObjectNotFoundException("Campaign " + id
						+ " does not exist for organisation " + org.getName());
			}
		}
		return campaign;
	}
	
	@Loggable(LogLevel.TRACE)
	@Override
	public void updateContactDetails(User user, String oldMsisdn, ContactDto contact)
			throws MobilisrException {
		Validate.notNull(user, "User is null");
		Validate.notNull(oldMsisdn, "oldMsisdn is null");
		Validate.notNull(contact, "Contact is null");
		
		Contact existingContact = getContactInternal(user, oldMsisdn);
		existingContact.setMsisdn(contact.getMsisdn());
		
		mergeContactDtoWithContact(contact, existingContact);

		contactDao.saveOrUpdate(existingContact);
		// does not commit without flushing but works in tests, confused?
		contactDao.flush();
		campaignDao.updateCampaignContactMsisdn(existingContact.getId(), contact.getMsisdn());
	}
	
	@Loggable(LogLevel.TRACE)
	@Override
	public PagedListDto<MessageStatusDto> getContactMessageLogs(User user,
			String msisdn, Search search, ApiVersion ver) throws ObjectNotFoundException {
		Validate.notNull(user, "User is null");
		Validate.notNull(msisdn, "msisdn is null");
		Validate.notNull(search, "search config is null");
		
		Contact contact = getContactInternal(user, msisdn);
		
		search.setSearchClass(SmsLog.class);
		search.addFilterEqual(SmsLog.PROP_CONTACT, contact);
		@SuppressWarnings("unchecked")
		SearchResult<SmsLog> searchResult = generalDao.searchAndCount(search);
		PagedListDto<MessageStatusDto> listDto = RestUtil.getPagedList(MessageStatusDto.class, 
				search, searchResult, ver);
		return listDto;
	}

	private void mergeContactDtoWithContact(ContactDto contact, Contact existingContact) {
		String firstName = contact.getFirstName();
		if(firstName != null && !firstName.isEmpty()){
			existingContact.setFirstName(firstName);
		}
		
		String lastName = contact.getLastName();
		if(lastName != null && !lastName.isEmpty()){
			existingContact.setLastName(lastName);
		}
	}
	
	/*
	 * setters for testing
	 * -------------------
	 */
	void setGeneralDao(MobilisrGeneralDAO generalDao) {
		this.generalDao = generalDao;
	}

	void setCampaignDao(CampaignDAO campaignDao) {
		this.campaignDao = campaignDao;
	}

	void setContactDao(ContactDAO contactDao) {
		this.contactDao = contactDao;
	}

	void setCampaignService(CampaignService campaignService) {
		this.campaignService = campaignService;
	}

	void setCampaignScheduleService(CampaignScheduleService campaignScheduleService) {
				this.campaignScheduleService = campaignScheduleService;
	}
}
