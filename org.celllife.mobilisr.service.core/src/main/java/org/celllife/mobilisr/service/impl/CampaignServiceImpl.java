package org.celllife.mobilisr.service.impl;

import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.trg.search.Filter;
import com.trg.search.Search;
import com.trg.search.SearchResult;
import org.celllife.mobilisr.annotation.LogLevel;
import org.celllife.mobilisr.annotation.Loggable;
import org.celllife.mobilisr.api.validation.ValidationError;
import org.celllife.mobilisr.api.validation.ValidatorFactory;
import org.celllife.mobilisr.constants.CampaignStatus;
import org.celllife.mobilisr.constants.CampaignType;
import org.celllife.mobilisr.dao.api.CampaignDAO;
import org.celllife.mobilisr.dao.api.ContactDAO;
import org.celllife.mobilisr.dao.api.ContactGroupDAO;
import org.celllife.mobilisr.dao.api.MobilisrGeneralDAO;
import org.celllife.mobilisr.domain.*;
import org.celllife.mobilisr.exception.ImportException;
import org.celllife.mobilisr.exception.InsufficientBalanceException;
import org.celllife.mobilisr.exception.MsisdnFormatException;
import org.celllife.mobilisr.service.CampaignScheduleService;
import org.celllife.mobilisr.service.CampaignService;
import org.celllife.mobilisr.service.ContactsService;
import org.celllife.mobilisr.service.UserService;
import org.celllife.mobilisr.service.exception.CampaignStateException;
import org.celllife.mobilisr.service.exception.UniquePropertyException;
import org.celllife.mobilisr.service.gwt.ServiceAndUIConstants;
import org.celllife.mobilisr.service.utility.CSVUtil;
import org.celllife.mobilisr.service.utility.MobilisrUtility;
import org.celllife.mobilisr.service.utility.ServiceUtil;
import org.celllife.mobilisr.util.CommunicateHome;
import org.celllife.mobilisr.util.LogUtil;
import org.hibernate.exception.ConstraintViolationException;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;

import java.io.*;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service("crudCampaignService")
public class CampaignServiceImpl implements CampaignService {

	private static final long serialVersionUID = 8910982835245986905L;

	private static Logger log = LoggerFactory.getLogger(CampaignServiceImpl.class);

	@Autowired
	private CampaignDAO campaignDAO;

	@Autowired
	private ContactDAO contactDAO;
	
	@Autowired
	private ContactGroupDAO contactGroupDAO;

	@Autowired
	private MobilisrGeneralDAO mobilisrGeneralDAO;

	@Autowired
	private CampaignScheduleService campaignScheduleService;
	
	@Autowired
	private ValidatorFactory validatorFactory;
	
	@Autowired
	private ContactsService contactsService;
	
	@Autowired
	private UserService userService;

	@Loggable(LogLevel.TRACE)
	@Override
	public PagingLoadResult<Campaign> listAllCampaigns(Organization org, CampaignType[] types, PagingLoadConfig loadConfig, Boolean showVoided) {

		Search search;
		if (loadConfig !=null){
			search = ServiceUtil.getSearchFromLoadConfig(Campaign.class, loadConfig, Campaign.PROP_NAME);
		}else{
			search = new Search(Campaign.class);
			search.addSort(Campaign.PROP_NAME, true);
		}

		if (types!= null){
			List<Filter> filters = new ArrayList<Filter>();
			for (CampaignType campType : types){
				filters.add(Filter.equal(Campaign.PROP_TYPE, campType));
			}
			search.addFilterOr(filters.toArray(new Filter[filters.size()]));
		}

		if (org != null){
			search.addFilterEqual(Campaign.PROP_ORGANIZATION, org);
		}

		if (showVoided != null){
			search.addFilterEqual(Campaign.PROP_VOIDED, showVoided);
		}

		search.addFilterEqual(Campaign.PROP_ORGANIZATION+"."+Organization.PROP_VOIDED, false);

		SearchResult<Campaign> searchResult = campaignDAO.searchAndCount(search);

		return ServiceUtil.getPagingLoadResult(loadConfig, searchResult);
	}

	@Loggable(LogLevel.TRACE)
	@Override
	public PagingLoadResult<CampaignContact> getCampaignsByContact(Contact contact, PagingLoadConfig loadConfig) {
		Search search = ServiceUtil.getSearchFromLoadConfig(CampaignContact.class, loadConfig, CampaignContact.PROP_CAMPAIGN);

		if (contact != null){
			search.addFilterEqual(CampaignContact.PROP_CONTACT, contact);
		}
		@SuppressWarnings("unchecked")
		SearchResult<CampaignContact> searchResult = mobilisrGeneralDAO.searchAndCount(search);

		return ServiceUtil.getPagingLoadResult(loadConfig, searchResult);
	}

	@Loggable(LogLevel.TRACE)
	@Override
	public List<CampaignMessage> findCampMessageByCampaign(Campaign campaign){
		List<CampaignMessage> messageList = campaignDAO.getAllCampMessages(campaign);
		return messageList;
	}

	@Loggable(LogLevel.TRACE)
	@Override
	public PagingLoadResult<CampaignMessage> findCampMessageByCampaign(Campaign campaign, PagingLoadConfig loadConfig){
		Search search = ServiceUtil.getSearchFromLoadConfig(CampaignMessage.class, loadConfig, CampaignMessage.PROP_MSG_DAY);
		search.clearSorts();
		search.addSortAsc(CampaignMessage.PROP_MSG_DAY);
		search.addSortAsc(CampaignMessage.PROP_MSG_TIME);

		search.addFilterEqual(CampaignMessage.PROP_CAMPAIGN, campaign);

		@SuppressWarnings("unchecked")
		SearchResult<CampaignMessage> searchResult = mobilisrGeneralDAO.searchAndCount(search);
		return ServiceUtil.getPagingLoadResult(loadConfig, searchResult);
	}

	@Loggable(LogLevel.TRACE)
	@Override
	public List<ContactMsgTime> getCampaignMessageTimes(Campaign campaign){
		List<CampaignMessage> list = campaignDAO.findDefaultTimesForRelativeCampaign(campaign);
		List<ContactMsgTime> times = new ArrayList<ContactMsgTime>(list.size());
		for (CampaignMessage msg : list) {
			times.add(new ContactMsgTime(msg.getMsgTime(), msg.getMsgSlot(), null, null));
		}
		return times;
	}

	@Loggable(LogLevel.TRACE)
	@Override
	public Campaign saveOrUpdateFixedCampaign(Campaign campaign) throws UniquePropertyException {
		try {
			campaignDAO.saveOrUpdate(campaign);

			List<CampaignMessage> messages = campaign.getCampaignMessages();
			saveCampaignMessages(campaign, messages, false);

			int count = campaignDAO.countNumberOfContactsForCampaign(campaign, true);
			campaign.setContactCount(count);

			int totalMsgs = MobilisrUtility.countTotalNumberOfMessages(messages, campaign);
			campaign.setMessageCount(totalMsgs);
			campaign.setCost(count * totalMsgs);
			campaignDAO.saveOrUpdate(campaign);

		} catch (ConstraintViolationException e) {
			throw new UniquePropertyException("Campaign with name \'" + campaign.getName() + "\' already exist");
		}

		 return campaign;
	}

	@Override
	@Secured({"PERM_CAMPAIGNS_ADMIN_MANAGE","PERM_CAMPAIGNS_MANAGE_RECIPIENTS"})
	public void addGroupToCampaign(Long contactGroupId, Campaign campaign, final List<ContactMsgTime> defaultTimes, Boolean startOver) {

		List<Long> groupIdList = new ArrayList<Long>();
		groupIdList.add(contactGroupId);

		campaignDAO.addContactGroupsToCampaignById(campaign, groupIdList, startOver);
		
		if (CampaignType.DAILY.equals(campaign.getType())){
			campaignDAO.createDefaultMessageTimesForContacts(campaign, defaultTimes);
		}
	}
	
	@Override
	@Secured({"PERM_CAMPAIGNS_ADMIN_MANAGE","PERM_CAMPAIGNS_MANAGE_RECIPIENTS"})
	public void removeGroupFromCampaign(Long contactGroupId, Campaign campaign) {
		List<Long> groupsList = new ArrayList<Long>();
		groupsList.add(contactGroupId);
		campaignDAO.setEndDateForCampaignContactsByGroup(campaign, groupsList);
	} 
	
	@Override
	@Loggable(LogLevel.TRACE)
	@Secured({"PERM_CAMPAIGNS_ADMIN_MANAGE","PERM_CAMPAIGNS_MANAGE_RECIPIENTS"})
	public void addAllContactsToCampaign(Campaign campaign, List<ContactMsgTime> contactMsgTimes, Boolean startOver){
		campaignDAO.addAllContactsToCampaign(campaign, startOver);
		if (CampaignType.DAILY.equals(campaign.getType())){
			campaignDAO.createDefaultMessageTimesForContacts(campaign, contactMsgTimes);
		}
	}
	
	@Override
	@Loggable(LogLevel.TRACE)
	@Secured({ "PERM_CAMPAIGNS_ADMIN_MANAGE", "PERM_CAMPAIGNS_MANAGE_RECIPIENTS" })
	public Long addCsvFileToCampaign(List<String> fieldOrder, final String filePath, final Campaign campaign, final List<ContactMsgTime> defaultTimes, final Boolean startOver) {

		Organization org = campaign.getOrganization();
		
		SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy-HH:mm:ss");
		String date = formatter.format(new Date());
		String groupName = "CSV_Addition" + "_" + date;
		final ContactGroup newGroup = new ContactGroup(groupName, "Contacts from Import");
		newGroup.setOrganization(org);
		contactGroupDAO.save(newGroup);

		List<ContactGroup> groupList = new ArrayList<ContactGroup>();
		groupList.add(newGroup);

		final int numberOfLines = CSVUtil.countLines(filePath);
		
		if (numberOfLines <= 0){
			throw new ImportException("Import file is empty");
		}

		final Long saveCSVJobId = contactsService.saveCSVContacts(fieldOrder, filePath, org, groupList);

		Thread saveContacts = new Thread(new Runnable() {
			@Override
			public void run() {				
				try {
					int secondsPassed = 0;
					while (!contactsService.isJobComplete(saveCSVJobId) && (secondsPassed < (numberOfLines / 50) + 2)) {
						Thread.sleep(1000);
						secondsPassed++;
					}
					addGroupToCampaign(newGroup.getId(),campaign, defaultTimes, startOver);									
				} catch (InterruptedException e1) {
					log.warn("addCsvFileToCampaign thread interrupted", e1);
				}
			}
		},"addCsvFileToCampaign" + date);
		saveContacts.start();
		
		return saveCSVJobId;
		
	}
	
	@Override 
	@Loggable(LogLevel.TRACE)
	@Secured({"PERM_CAMPAIGNS_ADMIN_MANAGE","PERM_CAMPAIGNS_MANAGE_RECIPIENTS"})
	public void removeCsvFileFromCampaign(List<String> fieldOrder, String filePath, Campaign campaign) {
		int numberOfLines = CSVUtil.countLines(filePath);
		if (numberOfLines <= 0){
			throw new ImportException("Import file is empty");
		}
		
		List<List<String>> data = CSVUtil.readCSVFixedRecordLength(filePath, -1);
		
		List<String> sample = data.get(0);
		if (sample.size() < fieldOrder.size()){
			String msg = "Incorrect number of columns in import file. Expecting: ";
			for (String field : fieldOrder) {
				msg += field + ", ";
			}
			throw new ImportException(msg.substring(0, msg.length()-2));
		}
		
		List<String> numbers = new ArrayList<String>();
		
		for (int i = 0; i < fieldOrder.size(); i++) {
			if (fieldOrder.get(i).equals(Contact.PROP_MSISDN))
				for (int j = 0; j < data.size(); j++) {
					numbers.add(data.get(j).get(i));
				}
		}			
		campaignDAO.setCampaignContactsEndDateByMsisdn(campaign, numbers);
	}
	
	@Override
	@Loggable(LogLevel.TRACE)
	public Campaign loadCampaignWithMessages(Long campaignId){
		Search s = new Search();
		s.addFilterEqual(Campaign.PROP_ID, campaignId);
		s.addFetch(Campaign.PROP_MSGS);
		Campaign fullCampaign = campaignDAO.searchUnique(s);
		return fullCampaign;
	}

	@Override
	@Loggable(LogLevel.TRACE)
	public List<CampaignContact> convertContactToCampaignContact(List<Contact> contactList, Campaign campaign ){
		List<CampaignContact> campContactList = new ArrayList<CampaignContact>();
		List<CampaignMessage> campMsgs = null;
		if (CampaignType.DAILY.equals(campaign.getType())) {
			campMsgs = campaignDAO.findDefaultTimesForRelativeCampaign(campaign);
		}

		for (Contact contact : contactList) {
			CampaignContact campContact = campaignDAO.getCampaignContact(campaign, contact);
			if (campContact == null){
				campContact = createNewCampContact(campaign, contact, campMsgs);
			}
			campContactList.add(campContact);
		}

		return campContactList;
	}

	private CampaignContact createNewCampContact(Campaign campaign, Contact contact, List<CampaignMessage> campMsgs) {

		CampaignContact campContact =  new CampaignContact(campaign, contact);

		if (campMsgs != null){
			List<ContactMsgTime> contactMsgTimes = new ArrayList<ContactMsgTime>();
			for (CampaignMessage campaignMessage : campMsgs) {
				contactMsgTimes.add(new ContactMsgTime(campaignMessage.getMsgTime(), campaignMessage.getMsgSlot(), campContact, campaign));
			}
			campContact.setContactMsgTimes(contactMsgTimes);
		}

		return campContact;
	}

	@Override
	@Loggable(LogLevel.TRACE)
	@Secured({"PERM_CAMPAIGNS_ADMIN_MANAGE","PERM_CAMPAIGNS_MANAGE_RECIPIENTS"})
	public void rescheduleRelativeCampaign(Campaign campaign, User user ) throws InsufficientBalanceException, CampaignStateException{

        if (!campaign.isActive()){
			throw new CampaignStateException("Campaign is not ACTIVE, can not update schedule");
		}

        campaignScheduleService.sendWelcomeMessages(campaign, user);
        int count = campaignDAO.countNumberOfContactsForCampaign(campaign, true);
        campaignDAO.setContactCount(campaign, count);

		campaignScheduleService.scheduleCampaign(campaign, user);

	}
	
	@Override
	@Loggable(LogLevel.TRACE)
	@Secured({"PERM_CAMPAIGNS_ADMIN_MANAGE","PERM_CAMPAIGNS_MANAGE_RECIPIENTS"})
	public void removeAllContactsFromCampaign(Campaign campaign) {
		campaignDAO.setAllCampaignContactsEndDates(campaign);
	}

	private void saveUpdateCampContactMsgTime(CampaignContact campaignContact) {
		List<ContactMsgTime> contactMsgTimes = campaignContact.getContactMsgTimes();
		if (contactMsgTimes != null){
			for (ContactMsgTime contactMsgTime : contactMsgTimes) {
				mobilisrGeneralDAO.saveOrUpdate(contactMsgTime);
			}
		}
	}

	@Loggable(LogLevel.TRACE)
	private void saveCampaignMessages(Campaign campaign, List<CampaignMessage> messages, boolean replaceExisting){
		if (messages == null || messages.isEmpty()){
			return;
		}

		if (replaceExisting){
			campaignDAO.removeMessagesFromCampaign(campaign,  null);
		}

		for (CampaignMessage campaignMessage : messages) {
			if (log.isTraceEnabled()){
				log.trace("Saving campaign message: [{}]", campaignMessage.getMessage());
			}
			campaignMessage.setCampaign(campaign);
			mobilisrGeneralDAO.saveOrUpdate(campaignMessage);
		}
	}
	
	@Override
	@Loggable(LogLevel.TRACE)
	@Secured({"PERM_CAMPAIGNS_ADMIN_MANAGE","PERM_CAMPAIGNS_MANAGE_RECIPIENTS"})
	public void removeContactFromCampaign(Campaign campaign, Messagable contact){
		if (contact != null){
			ArrayList<Messagable> list = new ArrayList<Messagable>(1);
			list.add(contact);
			campaignDAO.setCampaignContactsEndDate(campaign, list);
		}
	}
	
	@Override
	@Loggable(LogLevel.TRACE)
	@Secured({"PERM_CAMPAIGNS_ADMIN_MANAGE","PERM_CAMPAIGNS_MANAGE_RECIPIENTS"})
	public void removeContactsFromCampaign(Campaign campaign, List<? extends Messagable> removedContactList){
		campaignDAO.setCampaignContactsEndDate(campaign, removedContactList);
	}
		
	@Override
	@Loggable(LogLevel.TRACE)
	@Secured({"PERM_CAMPAIGNS_ADMIN_MANAGE","PERM_CAMPAIGNS_MANAGE_RECIPIENTS"})
	public CampaignContact saveOrUpdateCampaignContact(CampaignContact campaignContact) throws MsisdnFormatException, UniquePropertyException{
		Campaign campaign = campaignContact.getCampaign();
		Contact contact = campaignContact.getContact();
		
		if (!contact.isPersisted()){
			String msisdn = contact.getMsisdn();
			ValidationError error = validatorFactory.validateMsisdn(msisdn);
			if (error != null){
				throw new MsisdnFormatException(msisdn);
			}

			Organization organization = campaign.getOrganization();
			contact.setOrganization(organization);
			
			Contact existing = contactDAO.searchByOrganizationAndMSISDN(organization, msisdn);;
			if (existing != null){
				throw new UniquePropertyException("A contact with mobile number \'" + msisdn + "\' already exist");
			}

            contactDAO.saveOrUpdate(contact);
		}
		
		campaignContact.setContact(contact);

		if (!campaignContact.isPersisted()){
			Search s = new Search(CampaignContact.class);
			s.addFilterEqual(CampaignContact.PROP_CAMPAIGN, campaign);
			s.addFilterEqual(CampaignContact.PROP_CONTACT, contact);
			CampaignContact existingContact = (CampaignContact) mobilisrGeneralDAO.searchUnique(s);
			if (existingContact != null){
				existingContact.setContactMsgTimes(campaignContact.getContactMsgTimes());
				existingContact.setEndDate(campaignContact.getEndDate());
				existingContact.setProgress(campaignContact.getProgress());

				campaignContact = existingContact;
			}
		}
	
		mobilisrGeneralDAO.saveOrUpdate(campaignContact);
		
		if (CampaignType.DAILY.equals(campaign.getType()))
			saveUpdateCampContactMsgTime(campaignContact);
		
		return campaignContact;
	}

	@Override
	@Loggable(LogLevel.TRACE)
	@Secured({"PERM_CAMPAIGNS_ADMIN_MANAGE","PERM_CAMPAIGNS_VOID"})
	public void toggleCampaignVoidState(Campaign campaign){
		campaign.setVoided(!campaign.getVoided());
		campaignDAO.saveOrUpdate(campaign);
	}

	@Override
	@Loggable(LogLevel.TRACE)
	@Secured({"PERM_CAMPAIGNS_ADMIN_MANAGE","PERM_CAMPAIGNS_CREATE","PERM_CAMPAIGNS_EDIT"})
	public Campaign saveOrUpdateCampaign(Campaign campaign, List<CampaignMessage> campaignMessages) throws UniquePropertyException {

        if (campaign.getOrganization() == null) {
            campaign.setOrganization(userService.getCurrentLoggedInUser().getOrganization());
        }

		try {
			if (campaign.isRebuildMessages() && campaign.getMessageTimes() != null){
				campaign.setRebuildMessages(false);

				List<CampaignMessage> messages = new ArrayList<CampaignMessage>();
				List<Date> messageTimes = campaign.getMessageTimes();
				int timesPerDay = campaign.getTimesPerDay();

				if (timesPerDay != messageTimes.size()){
					log.error(LogUtil.getMarker_notifyAdmin(),
							"Unable to regenerate campaign messages since " +
							"timesPerDay is not equal to messageTimes list size. [campaignId={}]",
							campaign.getId());
				} else {
					int counter = 1;
					for (int i = 0; i < campaign.getDuration(); i++) {
						for (int j = 0; j < timesPerDay; j++) {
							int msgSlot = j + 1;
							int msgDay = i + 1;
							CampaignMessage cm = new CampaignMessage("Message "
									+ (counter++) + " text...", msgDay,
									messageTimes.get(j), msgSlot, campaign);
							cm.setCampaign(campaign);
							messages.add(cm);
						}
					}

					if (campaign.isPersisted()){
						campaignDAO.removeMessagesFromCampaign(campaign, null);
					}
					MobilisrUtility.recalculateCostAndDuration(campaign, messages);
					campaignDAO.saveOrUpdate(campaign);
					mobilisrGeneralDAO.save(messages.toArray());
                    log.info("Rebuilt and saved campaign with name " + campaign.getName());
				}
			} else {
				if (!campaign.isPersisted()){
					campaignDAO.save(campaign);
				}

				List<CampaignMessage> deletedMessages = new ArrayList<CampaignMessage>();
				if (campaignMessages != null){
					for (CampaignMessage campaignMessage : campaignMessages){
						if (campaignMessage.isMarkedForDeletion()){
							deletedMessages.add(campaignMessage);
						}
					}
					campaignMessages.removeAll(deletedMessages);
					deleteCampaignMessages(campaign,deletedMessages);
					saveCampaignMessages(campaign, campaignMessages, false);
				}

				recalculateCampaignCostAndDuration(campaign);
				campaignDAO.saveOrUpdate(campaign);
                log.info("Saved Campaign with name " + campaign.getName());

			}
		} catch (ConstraintViolationException e) {
            log.error("An error occurred while trying to save the campaign.\n" + e.getLocalizedMessage().toString() + "\n" + e.getStackTrace().toString());
			throw new UniquePropertyException("There was a problem saving this campaign. If the problem persists, please contact support@cell-life.org");
		}

		return campaign;
	}

	private void deleteCampaignMessages(Campaign campaign, List<CampaignMessage> deletedMessages) {
		if (deletedMessages != null && !deletedMessages.isEmpty()) {
			campaignDAO.removeMessagesFromCampaign(campaign, deletedMessages);
		}
	}

	private void recalculateCampaignCostAndDuration(Campaign campaign) {
		List<CampaignMessage> msgInfo = campaignDAO.getCampaignMessageLengthsAndDay(campaign.getId());
		MobilisrUtility.recalculateCostAndDuration(campaign, msgInfo);
	}

	@Override
	@Loggable(LogLevel.TRACE)
	public Campaign getCampaign(Long campaignId) {
		return campaignDAO.find(campaignId);
	}

	@Override
	@Loggable(LogLevel.TRACE)
	public Campaign importCampaignMessagesFromCSV(String filePath,
			Campaign campaign) throws ImportException {
		CampaignType campaignType = campaign.getType();
		List<List<String>> data = CSVUtil.readCSVFixedRecordLength(filePath,-1);
		List<CampaignMessage> messages = new ArrayList<CampaignMessage>();
		if (data.isEmpty()){
			throw new ImportException("Import file contained no data or " +
					"import file could not be read.");
		}
		checkMessagesForAllowedCharacters(data);
		switch (campaignType) {
		case FIXED:
			// not currently supported
			break;
		case DAILY:
			messages = getRelativeCampaignMessages(campaign, data);
			break;
		case FLEXI:
			messages = getGenericCampaignMessages(campaign, data);
			break;
		}

		saveCampaignMessages(campaign, messages, true);
		recalculateCampaignCostAndDuration(campaign);
		campaignDAO.saveOrUpdate(campaign);
		return campaign;
	}

	/**
	 * Helper method to check imported text messages for allowed characters - i.e. characters
	 * that will display on cell-phones (loosely based on GSM 03.38).
	 * If a message is found with a dis-allowed character, an ImportException is thrown.
	 *
	 * @see 'http://en.wikipedia.org/wiki/GSM_03.38'
	 * @see 'http://www.cs.sfu.ca/~ggbaker/reference/characters/'
	 *
	 * @param data	List of list of strings; The import data.
	 * @throws FileNotFoundException
	 */
	/*package private*/ void checkMessagesForAllowedCharacters(List<List<String>> data) throws ImportException {
		DateTime tNow = new DateTime();
		String fileName = "BadCharacters_" + tNow.toString("yyy-MM-dd_HHmmss") + ".txt";
		Pattern pattern = Pattern.compile(ServiceAndUIConstants.REGEX_TEXT_MESSAGE);
		boolean noBadChars = true;
		File badCharsFile = null;
		PrintWriter badCharsLog = null;

		for (int i = 1; i < data.size(); i++) { // Start at 2nd line (skip header).
			List<String> list = data.get(i);

			String messageText = list.get(0); // Message text is in 1st column.
			int messageLength = messageText.length();
			Matcher matcher = pattern.matcher(messageText);
			if (!matcher.matches()) {
				if (noBadChars) { // If this is true, then we just hit our first bad char
					try {
						badCharsFile = createBadCharLogFile(fileName);
						badCharsLog = new PrintWriter(new FileOutputStream(badCharsFile, false));
					} catch (IOException e) {
						throw new ImportException("Could not create log file: \n" + e);
					}
					badCharsLog.write("Bad characters:\n");
					noBadChars = false;
				}
				matcher.reset();
				while (matcher.find()) {
					int start = matcher.start();
					int end = matcher.end();
					if ((start != messageLength) && (start == end)) {
						int contextStart = start - 5;
						if (contextStart < 0) contextStart = 0;
						int contextEnd = end + 6;
						if (contextEnd > messageLength) contextEnd = messageLength;
						badCharsLog.write("'" + messageText.substring(start, end+1)
								+ "' at line " + (i+1)
								+ ", character " + (start + 1) + " in message. (Context: '"
								+ messageText.substring(contextStart, contextEnd) + "')\n");
					}
				}
			}
		}
		if (!noBadChars) {
			badCharsLog.close();
			throw new ImportException("Disallowed characters in file.\n\n"
					+ "\n\nfileName:" + fileName);
		}
	}

	/**
	 * Helper method.
	 * @throws IOException
	 */
	private File createBadCharLogFile(String fileName) throws IOException {
		log.debug("fileName: " + fileName);
		File directory = CommunicateHome.getDownloadsFolder();
		File destFile = new File(directory.getAbsolutePath() + File.separator + fileName);
		return destFile;
	}

	/*package private*/  List<CampaignMessage> getGenericCampaignMessages(Campaign campaign,
			List<List<String>> data) {
		List<CampaignMessage> messages = new ArrayList<CampaignMessage>();
		data.remove(0); // skip heading line

		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");

		Date msgDate = new Date();
		for (int i = 0; i < data.size(); i++) {
			List<String> list = data.get(i);
			if (list.size()>=3){
				try {
					CampaignMessage message = new CampaignMessage();
					message.setCampaign(campaign);
					message.setMsgDate(msgDate);
					message.setMessage(list.get(0));
					message.setMsgDay(Integer.parseInt(list.get(1)));
					message.setMsgTime(sdf.parse(list.get(2)));
					messages.add(message);
				} catch (ParseException e) {
					String message = MessageFormat.format("There was an converting a date in the import file. " +
							"Please check the file is correct and try again.\n\n" +
							"Error message: {0}", e.getMessage());
					throw new ImportException(message, e);
				} catch (NumberFormatException e){
					String message = MessageFormat.format("There was an converting a day in the import file. " +
							"Please check the file is correct and try again.\n\n" +
							"Error message: {0}", e.getMessage());
					throw new ImportException(message, e);
				}
			} else {
				throw new ImportException("Incorrect data for import. Expecting three columns in the import file.");
			}
		}
		return messages;
	}

	/*package private*/ List<CampaignMessage> getRelativeCampaignMessages(Campaign campaign, List<List<String>> data) {
		List<CampaignMessage> messages = new ArrayList<CampaignMessage>();
		List<CampaignMessage> msgTimes = campaignDAO.findDefaultTimesForRelativeCampaign(campaign);
		int duration = campaign.getDuration();
		int timesPerDay = campaign.getTimesPerDay();

		if (duration ==0 || timesPerDay == 0){
			log.warn("Attempt to import campaign messages for campaign with invalid"
							+ " duration or timesPerDay [duration={}] [timesPerDay={}]",
					duration, timesPerDay);
			return null;
		}

		if (timesPerDay != msgTimes.size()){
			throw new ImportException("The times per day field in the " +
					"campaign does not match the existing times in the campaign" +
					" messages.");
		}

		int progress = 0;
		data.remove(0); // skip heading line
		for (int i = 0; i < data.size(); i++) {
			int msgSlot = (i % timesPerDay) + 1;
			if (msgSlot == 1){
				progress++;
				if (progress > duration){
					break;
				}
			}

			List<String> list = data.get(i);
			if (list.size()>=1){
				CampaignMessage message = new CampaignMessage();
				message.setCampaign(campaign);
				message.setMsgDate(new Date());
				message.setMsgDay(progress);
				message.setMsgTime(msgTimes.get(msgSlot-1).getMsgTime());
				message.setMsgSlot(msgSlot);
				message.setMessage(list.get(0));
				messages.add(message);
			}
		}
		return messages;
	}

	@Loggable(LogLevel.TRACE)
	@Override
	public int getNumberActiveCampaigns(Organization org){
		Search s = new Search();
		s.addFilterOr(Filter.equal(Campaign.PROP_STATUS, CampaignStatus.ACTIVE),
				Filter.equal(Campaign.PROP_STATUS, CampaignStatus.RUNNING),
				Filter.equal(Campaign.PROP_STATUS, CampaignStatus.STOPPING),
				Filter.equal(Campaign.PROP_STATUS, CampaignStatus.SCHEDULED));
		s.addFilterEqual(Campaign.PROP_VOIDED, false);
		if (org != null)
			s.addFilterEqual(Campaign.PROP_ORGANIZATION, org);

		int count = campaignDAO.count(s);
		return count;
	}

	@Override
	public Long getCampaignIdForName(String campaignName) {
		Campaign campaign = campaignDAO.getCampaign(campaignName);
		return campaign == null ? null : campaign.getId();
	}
	
	@Loggable(LogLevel.TRACE)
	@Override
	public CampaignContact getCampaignContact(Campaign campaign, String msisdn){
		return campaignDAO.getCampaignContact(campaign, msisdn);
	}

	/*package private*/ void setCampaignDao(CampaignDAO campaignDao) {
		campaignDAO = campaignDao;
	}

	/*package private*/ void setCampaignScheduleService(CampaignScheduleService scheduleService) {
		campaignScheduleService = scheduleService;
	}

	/*package private*/ void setGeneralDAO(MobilisrGeneralDAO generalDAO) {
		this.mobilisrGeneralDAO = generalDAO;
	}

    @Loggable(LogLevel.TRACE)
    @Override
    public List<Long> getLinkedCampaignsForCampaign(Long campaignId) {
        List<Long> linkedCampaigns = new ArrayList<Long>();
        linkedCampaigns.add(campaignId);

        if (campaignId != null) {
            Campaign campaign = getCampaign(campaignId);
            if (campaign != null) {
                while ((campaign.getLinkedCampaignId() != null) && (!linkedCampaigns.contains(campaign.getLinkedCampaignId()))) {
                    if (!linkedCampaigns.contains(campaign.getLinkedCampaignId())) {
                    linkedCampaigns.add(campaign.getLinkedCampaignId());
                    }
                    campaign = getCampaign(campaign.getLinkedCampaignId());
                }
            }
        }

        return linkedCampaigns;
    }

    @Loggable(LogLevel.TRACE)
    @Override
    public void addContactsToCampaignWithLinks(List<CampaignContact> contactList, Campaign originalCampaign, Date joiningDate, List<Date> contactMessageTimes) throws Exception {

        List<Long> linkedCampaigns = getLinkedCampaignsForCampaign(originalCampaign.getId());
        List<Campaign> campaignsToUpdate = new ArrayList<Campaign>();
        Campaign actualCampaign;
        CampaignContact actualCampaignContact;
        Boolean stayOnLastCampaign = false;

        for (CampaignContact originalCampaignContact : contactList) {

            Long lastCampaignParticipated = null;
            Long currentCampaignId;
            List<Long> campaignsForContact = contactsService.listAllCampaignsForContact(originalCampaignContact.getMsisdn());
            Integer numberOfCampaignsParticipatedIn = 0;

            // Determine the last campaign the recipient participated in.
            if (!campaignsForContact.isEmpty()) {

                for (Long campaignId : linkedCampaigns) {
                    if (campaignsForContact.contains(campaignId)) {
                        numberOfCampaignsParticipatedIn = numberOfCampaignsParticipatedIn + 1;
                        lastCampaignParticipated = campaignId;
                    }
                }

                // If the recipient has participated in ALL the campaigns, we have to check the dates.
                if (numberOfCampaignsParticipatedIn == linkedCampaigns.size()) {

                    Date lastCampaignEndDate = this.getCampaignContact(this.getCampaign(linkedCampaigns.get(0)), originalCampaignContact.getMsisdn()).getDateLastMessage();
                    if (lastCampaignEndDate == null) {

                        lastCampaignParticipated = linkedCampaigns.get(0);
                        stayOnLastCampaign = true;

                    } else {

                        for (Long campaignId : linkedCampaigns) {
                            CampaignContact linkedCampaignContact = this.getCampaignContact(this.getCampaign(campaignId), originalCampaignContact.getMsisdn());
                            if (linkedCampaignContact.getDateLastMessage() == null) {
                                lastCampaignParticipated = campaignId;
                                stayOnLastCampaign = true;
                                break;
                            } else if ((linkedCampaignContact.getDateLastMessage() != null) && (linkedCampaignContact.getDateLastMessage().compareTo(lastCampaignEndDate) >= 0)) {
                                lastCampaignParticipated = campaignId;
                            }
                        }
                    }
                }

                log.debug("The last campaign the user " + originalCampaignContact.getMsisdn() + " participated in was campaign with ID " + lastCampaignParticipated);
            }

            actualCampaignContact = getCampaignContactToAddForLinkedCampaign(lastCampaignParticipated, stayOnLastCampaign, originalCampaignContact, joiningDate);
            if (actualCampaignContact == null) {
                return; // we don't need to add a new campaign contact
            }

            // Add campaign so it can be rescheduled.
            campaignsToUpdate.add(actualCampaignContact.getCampaign());

            // Save the Campaign Contact
            actualCampaignContact.setEndDate(null);
            actualCampaignContact.setProgress(0);
            actualCampaignContact.setReceivedWelcome(false);

            if ((actualCampaignContact.getCampaign().getType() == CampaignType.DAILY) && (contactMessageTimes != null) ) {
                List<ContactMsgTime> contactMsgTimes = convertMessageTimes(actualCampaignContact, contactMessageTimes);
                actualCampaignContact.setContactMsgTimes(contactMsgTimes);
            } else if (!originalCampaignContact.getContactMsgTimes().isEmpty()) {
                actualCampaignContact.setContactMsgTimes(copyMessageTimesFromOldContact(originalCampaignContact,actualCampaignContact,actualCampaignContact.getCampaign()));
            } else {
                List<ContactMsgTime> contactMsgTimes = convertMessageTimes(actualCampaignContact, null);
                actualCampaignContact.setContactMsgTimes(contactMsgTimes);
            }

            this.saveOrUpdateCampaignContact(actualCampaignContact);

        }

        // Reschedule the Relevant Campaigns
        for (Campaign campaignToUpdate : campaignsToUpdate) {
            if (campaignToUpdate.isActive()) {
                this.rescheduleRelativeCampaign(campaignToUpdate, (User) null);
                log.debug("Rescheduling campaign " + campaignToUpdate.getId() + " " + campaignToUpdate.getName());
            }
        }
    }

    private CampaignContact getCampaignContactToAddForLinkedCampaign(Long lastCampaignParticipated, Boolean stayOnLastCampaign, CampaignContact originalCampaignContact, Date joiningDate) {

        CampaignContact lastCampaignParticipatedContact = null;
        CampaignContact actualCampaignContact;
        Campaign actualCampaign;

        if (lastCampaignParticipated != null) {
            lastCampaignParticipatedContact = getCampaignContact(getCampaign(lastCampaignParticipated), originalCampaignContact.getMsisdn());
        }

        // Get the "next" campaign and campaign contact.
        if (lastCampaignParticipated == null) {

            actualCampaign = originalCampaignContact.getCampaign();
            actualCampaignContact = originalCampaignContact;
            log.info("Contact has not participated in any of the linked campaigns. Adding contact " + actualCampaignContact.getMsisdn() + " to campaign " + actualCampaign.getName());

        } else if ((lastCampaignParticipatedContact.getProgress() < lastCampaignParticipatedContact.getCampaign().getDuration()) && (lastCampaignParticipatedContact.getEndDate() == null)) {

            log.info("Contact " + originalCampaignContact.getMsisdn() + " is currently active on the last campaign, and will not be added to a new one.");
            return null;

            } else if (stayOnLastCampaign) {

            actualCampaign = this.getCampaign(lastCampaignParticipated);
            actualCampaignContact = this.getCampaignContact(actualCampaign, originalCampaignContact.getMsisdn());
            if (actualCampaignContact == null) {
                actualCampaignContact = createNewCampaignContactForLinkedCampaign(originalCampaignContact, actualCampaign, joiningDate);
            }
            log.info("Adding contact " + actualCampaignContact.getMsisdn() + " on campaign " + actualCampaign.getName() + " because the last message date is null. ");

        } else {

            if (this.getCampaign(lastCampaignParticipated).getLinkedCampaignId() == null) {
                log.info("The last campaign the user participated in did not have a linked campaign. The contact " + originalCampaignContact.getMsisdn() + " was not added to any new campaign.");
                return null;
            } else {
                Long currentCampaignId = this.getCampaign(lastCampaignParticipated).getLinkedCampaignId();
                actualCampaign = this.getCampaign(currentCampaignId);
                actualCampaignContact = this.getCampaignContact(actualCampaign, originalCampaignContact.getMsisdn());
                if (actualCampaignContact == null) {
                    actualCampaignContact = createNewCampaignContactForLinkedCampaign(originalCampaignContact, actualCampaign, joiningDate);
                }
                log.info("Adding contact " + actualCampaignContact.getMsisdn() + " to campaign " + actualCampaign.getName());
            }
        }

        if (joiningDate != null) {
            actualCampaignContact.setJoiningDate(joiningDate);
        }
        return actualCampaignContact;

    }

    private List<ContactMsgTime> convertMessageTimes(CampaignContact campaignContact, List<Date> customContactMessageTimes) {

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

            Date msgTime;
            if (customContactMessageTimes == null) {
                msgTime = defaultTimeCampaignMsgs.get(i).getMsgTime();
            } else {
                msgTime = customContactMessageTimes.get(i);
            }
            int msgSlot = defaultTimeCampaignMsgs.get(i).getMsgSlot();
            contactMsgTimes.add(new ContactMsgTime(msgTime, msgSlot, campaignContact, campaignContact.getCampaign()));

        }

        return contactMsgTimes;
    }

    private CampaignContact createNewCampaignContactForLinkedCampaign(CampaignContact oldCampaignContact, Campaign campaign, Date joiningDate) {

        CampaignContact actualCampaignContact = new CampaignContact(campaign,oldCampaignContact.getContact(),oldCampaignContact.getMobileNetwork(),0,new Date());
        actualCampaignContact.setCampaign(campaign);
        if (joiningDate != null) {
            actualCampaignContact.setJoiningDate(joiningDate);
        }
        actualCampaignContact.setContactMsgTimes(copyMessageTimesFromOldContact(oldCampaignContact, actualCampaignContact, campaign));

        return actualCampaignContact;
    }

    private List<ContactMsgTime> copyMessageTimesFromOldContact(CampaignContact oldCampaignContact, CampaignContact newCampaignContact, Campaign campaign) {

        List<ContactMsgTime> newContactMsgTimes = new ArrayList<ContactMsgTime>();
        for (ContactMsgTime contactMsgTime : oldCampaignContact.getContactMsgTimes()) {
            ContactMsgTime newContactMsgTime = new ContactMsgTime();
            newContactMsgTime.setCampaign(campaign);
            newContactMsgTime.setCampaignContact(newCampaignContact);
            newContactMsgTime.setMsgSlot(contactMsgTime.getMsgSlot());
            newContactMsgTime.setMsgTime(contactMsgTime.getMsgTime());
            newContactMsgTimes.add(newContactMsgTime);
        }

        return newContactMsgTimes;
    }
		

}
