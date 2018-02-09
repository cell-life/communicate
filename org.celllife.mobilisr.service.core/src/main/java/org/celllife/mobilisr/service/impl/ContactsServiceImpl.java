package org.celllife.mobilisr.service.impl;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.celllife.mobilisr.annotation.LogLevel;
import org.celllife.mobilisr.annotation.Loggable;
import org.celllife.mobilisr.api.validation.ValidationError;
import org.celllife.mobilisr.api.validation.ValidatorFactory;
import org.celllife.mobilisr.dao.api.CampaignDAO;
import org.celllife.mobilisr.dao.api.ContactDAO;
import org.celllife.mobilisr.dao.api.ContactGroupDAO;
import org.celllife.mobilisr.dao.api.MobilisrGeneralDAO;
import org.celllife.mobilisr.domain.Campaign;
import org.celllife.mobilisr.domain.CampaignContact;
import org.celllife.mobilisr.domain.Contact;
import org.celllife.mobilisr.domain.ContactGroup;
import org.celllife.mobilisr.domain.Contact_ContactGroup;
import org.celllife.mobilisr.domain.Organization;
import org.celllife.mobilisr.exception.ImportException;
import org.celllife.mobilisr.exception.MsisdnFormatException;
import org.celllife.mobilisr.service.ContactsService;
import org.celllife.mobilisr.service.exception.UniquePropertyException;
import org.celllife.mobilisr.service.gwt.ContactContactGroupViewModel;
import org.celllife.mobilisr.service.gwt.ContactGroupContactViewModel;
import org.celllife.mobilisr.service.gwt.CsvDataReport;
import org.celllife.mobilisr.service.utility.CSVUtil;
import org.celllife.mobilisr.service.utility.ServiceUtil;
import org.celllife.mobilisr.service.writer.CsvDataJobBean;
import org.celllife.mobilisr.service.writer.CsvDataWriter;
import org.celllife.mobilisr.service.writer.CustomLineTokenizer;
import org.celllife.mobilisr.util.LogUtil;
import org.celllife.mobilisr.utilbean.ContactExportSummary;
import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.trg.search.Filter;
import com.trg.search.Search;
import com.trg.search.SearchResult;

/**
 * Default implementation for the CrudContactsService
 * @author Vikram Bindal (e-mail: vikram@cell-life.org)
 *
 */
@Service("crudContactsService")
public class ContactsServiceImpl implements ContactsService{

	private static final long serialVersionUID = 2441461832376493921L;
	private static Logger log = LoggerFactory.getLogger(ContactsServiceImpl.class);

	@Autowired
	private ContactDAO contactDAO;
	
	@Autowired
	private CampaignDAO campaignDAO;
	
	@Autowired
	private ContactGroupDAO contactGroupDAO;
	
	@Autowired
	private JobLauncher jobLauncher;
	
	@Autowired
	private MobilisrGeneralDAO mobilisrGeneralDAO;

	@Autowired
	private Job job;
	
	@Autowired
	private CustomLineTokenizer customTokenizer;
	
	@Autowired
	private ValidatorFactory validatorFactory;
	
	private static Map<String, JobExecution> jobExecutionMap;
	
	private void initJobMap() {
		if(jobExecutionMap == null){
			jobExecutionMap = new HashMap<String, JobExecution>();
		}
	}

	@Loggable(LogLevel.TRACE)
	@Override
	public PagingLoadResult<Contact> listAllContactsForOrganization(Organization organization, PagingLoadConfig loadConfig) {
		
		Search search = ServiceUtil.getSearchFromLoadConfig(Contact.class, loadConfig, Contact.PROP_MSISDN);
		search.addFilterEqual(Contact.PROP_ORGANIZATION, organization);
	
		SearchResult<Contact> searchResult = contactDAO.searchAndCount(search);
		return ServiceUtil.getPagingLoadResult(loadConfig, searchResult);
	}

	@Loggable(LogLevel.TRACE)
	@Override
	public PagingLoadResult<ContactGroup> listAllGroupsForOrganization(Organization organization, PagingLoadConfig loadConfig) {

		Search search = ServiceUtil.getSearchFromLoadConfig(ContactGroup.class, loadConfig, ContactGroup.PROP_GROUP_NAME);
		search.addFilterEqual(ContactGroup.PROP_ORGANIZATION, organization);
		SearchResult<ContactGroup> searchResult = contactGroupDAO.searchAndCount(search);
		return ServiceUtil.getPagingLoadResult(loadConfig, searchResult);
	}

		
	@Loggable(LogLevel.TRACE)
	@Override
	public PagingLoadResult<ContactGroup> listAllGroupsForContact(Organization organization, Contact contact, PagingLoadConfig loadConfig){
		
		Search m2msearch = new Search(Contact_ContactGroup.class);
		m2msearch.addFilterEqual(Contact_ContactGroup.PROP_CONTACT, contact);
		m2msearch.addField(Contact_ContactGroup.PROP_CONTACT_GROUP + ".id");
		@SuppressWarnings("unchecked")
		List<Long> contactGroups = mobilisrGeneralDAO.search(m2msearch);
		
		Search search = ServiceUtil.getSearchFromLoadConfig(ContactGroup.class, loadConfig, ContactGroup.PROP_GROUP_NAME);
		search.addFilterEqual(ContactGroup.PROP_ORGANIZATION, organization);
		search.addFilterIn(ContactGroup.PROP_ID, contactGroups);
		
		SearchResult<ContactGroup> searchResult = contactGroupDAO.searchAndCount(search);
		return ServiceUtil.getPagingLoadResult(loadConfig, searchResult);
	}
	
	@Loggable(LogLevel.TRACE)
	@Override
	public PagingLoadResult<Contact> listAllContactsForGroup( ContactGroup contactGroup, PagingLoadConfig loadConfig) {
		Search search = ServiceUtil.getSearchFromLoadConfig(Contact_ContactGroup.class, loadConfig, 
				Contact_ContactGroup.PROP_CONTACT + "." + Contact.PROP_MSISDN);
		search.addFilterEqual(Contact_ContactGroup.PROP_CONTACT_GROUP, contactGroup);
		search.addField(Contact_ContactGroup.PROP_CONTACT);

		@SuppressWarnings("unchecked")
		SearchResult<Contact> searchResult = mobilisrGeneralDAO.searchAndCount(search);
		return ServiceUtil.getPagingLoadResult(loadConfig, searchResult);
	}
	
	@Loggable(LogLevel.TRACE)
	@Override
	public Contact saveOrUpdateContact(Organization organization, ContactContactGroupViewModel contactModel) throws UniquePropertyException, MsisdnFormatException {
		Contact contact = contactModel.getParentObject();
		
		String msisdn = contact.getMsisdn();
		ValidationError error = validatorFactory.validateMsisdn(msisdn);
		if (error != null){
			throw new MsisdnFormatException(msisdn);
		}

		contact.setOrganization(organization);
		
		if (contact.isPersisted()){ // check if contact has already been saved (different MSISDN)
			log.debug("contact.isPersisted(): true");
			Contact oldContact = contactDAO.find(contact.getId());
			if (oldContact != null && !oldContact.getMsisdn().equals(msisdn)){
				log.debug("Differing MSISDN Numbers: updating campaign contacts");
				contact.setInvalid(false);
				campaignDAO.updateCampaignContactMsisdn(contact.getId(), msisdn);
			}
		} else {
			Contact existing = contactDAO.searchByOrganizationAndMSISDN(organization, msisdn);;
			if (existing != null){
				throw new UniquePropertyException("Contact with mobile number \'" + msisdn + "\' already exist");
			}
		}
			
		contactDAO.saveOrUpdate(contact);	
		addGroupsToContact(organization, contactModel);

		return contact;
	}

	@Loggable(LogLevel.TRACE)
	@Override
	public Contact addGroupsToContact(Organization organization, ContactContactGroupViewModel contactModel){
		
		Contact contact = (Contact) contactModel.getParentObject();
		List<ContactGroup> addedGroupList = contactModel.getAddedChildList();
		List<ContactGroup> removedGroupList = contactModel.getRemovedChildList();
		boolean addAll = contactModel.isAddAll();
		boolean removeAll = contactModel.isRemoveAll();
		
		if( addAll ){
			contactGroupDAO.removeAllGroupFromContact(contact);
			contactGroupDAO.addAllGroupsToContact(organization, contact, removedGroupList);
		}else if( removeAll){		
			contactGroupDAO.removeAllGroupFromContact(contact);
			
			//Check if there are any groups that the user wanted to add as well or not
			if((addedGroupList != null) && (!addedGroupList.isEmpty())){
				contactGroupDAO.addGroupsToContact(addedGroupList, contact);
			}			
		}else{
			//User doesn't want to remove all the groups, neither addAll
			//Check if there are groups that user wanted to add
			if((addedGroupList != null) && (!addedGroupList.isEmpty())){
				contactGroupDAO.addGroupsToContact(addedGroupList, contact);
			}
			
			if((removedGroupList != null) && (!removedGroupList.isEmpty())){
				//There are groups that user wanted to remove as well
				contactGroupDAO.removeGroupsFromContact(contact, removedGroupList);
			}
		}
		return contact;
		
	}
	
	@Loggable(LogLevel.TRACE)
	@Override
	public ContactGroup saveOrUpdateContactGroup(Organization organization, ContactGroupContactViewModel contactModel) throws UniquePropertyException {
		
		ContactGroup contactGroup = contactModel.getParentObject();
		contactGroup.setOrganization(organization);
		try{
			contactGroupDAO.saveOrUpdate(contactGroup);
			contactModel.setParentObject(contactGroup);
			addContactsToGroup(organization, contactModel);
		}catch(ConstraintViolationException e){
			//Will occur when same groupname already exist in records for a given org
			throw new UniquePropertyException("Group with name \'" + contactGroup.getGroupName() + "\' already exist");
		}
		return contactGroup;
	}

	@Override
	public boolean deleteContactGroup(ContactGroup contactGroup) throws UniquePropertyException{
		boolean result = contactGroupDAO.remove(contactGroup);
		return result;
	}
	
	@Override
	public Long countContactsForGroup(ContactGroup group) {
		return contactDAO.countContactsInGroups(Arrays.asList(group));
	}
	
	private ContactGroup addContactsToGroup(Organization organization, ContactGroupContactViewModel contactModel){
		
		ContactGroup contactGroup = contactModel.getParentObject();
		List<Contact> addedContactList = contactModel.getAddedChildList();
		List<Contact> removedContactList = contactModel.getRemovedChildList();
		boolean addAll = contactModel.isAddAll();
		boolean removeAll = contactModel.isRemoveAll();
		
		if( addAll ){
			contactGroupDAO.removeAllContactsFromGroup(contactGroup);
			contactGroupDAO.addAllContactsToGroup(organization, contactGroup, removedContactList);
		}else if( removeAll){		
			
			log.debug("CrudContactsServiceImpl: addContactsToGroup( removeAll = TRUE )" );
			contactGroupDAO.removeAllContactsFromGroup(contactGroup);
			
			if((addedContactList != null) && (!addedContactList.isEmpty())){
				log.debug("CrudContactsServiceImpl: addContactsToGroup( removeAll = TRUE, addedContactList NOT EMPTY )" );
				contactGroupDAO.addContactsToGroup(addedContactList, contactGroup);
			}			
		}else{
			if((addedContactList != null) && (!addedContactList.isEmpty())){
				log.debug("CrudContactsServiceImpl: addContactsToGroup( addedContactList NOT EMPTY )" );
				contactGroupDAO.addContactsToGroup(addedContactList, contactGroup);
			}
			if((removedContactList != null) && (!removedContactList.isEmpty())){
				log.debug("CrudContactsServiceImpl: addContactsToGroup( removedContactList NOT EMPTY )" );
				contactGroupDAO.removeContactsFromGroup(contactGroup, removedContactList);
			}
		}
		return contactGroup;
	}

	@Loggable(LogLevel.TRACE)
	@Override
	public List<List<String>> readCSVFixedRecordLength(String filePath, int maxRead) {
		return CSVUtil.readCSVFixedRecordLength(filePath, maxRead);
	}

	@Loggable(LogLevel.TRACE)
	@Override
	public Long saveCSVContacts(List<String> fieldOrder, String filePath, Organization organization, List<ContactGroup> groupList) {
		List<List<String>> data = CSVUtil.readCSVFixedRecordLength(filePath, 1);
		
		List<String> sample = data.get(0);
		if (sample.size() < fieldOrder.size()){
			String msg = "Incorrect number of columns in import file. Expecting: ";
			for (String field : fieldOrder) {
				msg += field + ", ";
			}
			throw new ImportException(msg.substring(0, msg.length()-2));
		}
		
		JobExecution jobExecution = null;
		
		StringBuffer stringBuffer = new StringBuffer();
		for (String string : fieldOrder) {
			stringBuffer.append(string);
			stringBuffer.append(",");
		}
		String fieldOrderData = stringBuffer.toString();
		fieldOrderData = fieldOrderData.substring(0, fieldOrderData.length()-1);
		
		Map<String, JobParameter> jobMap = new HashMap<String, JobParameter>();
		
		String fileNamePath = "file:" + filePath;
		jobMap.put("inputFile", new JobParameter(fileNamePath));
		jobMap.put("fieldOrder", new JobParameter(fieldOrderData));
		JobParameters jobParameters = new JobParameters(jobMap);	
		
		CsvDataJobBean csvDataJobBean = new CsvDataJobBean(organization, groupList);
		CsvDataWriter.storeJobData(fileNamePath, csvDataJobBean);
		customTokenizer.setNames(fieldOrderData.split(","));
		
		try {			
			jobExecution = jobLauncher.run(job, jobParameters);
			initJobMap();
			jobExecutionMap.put(String.valueOf(jobExecution.getJobId()), jobExecution);
		} catch (Exception e){
			log.error(LogUtil.getMarker_notifyAdmin(),
					"Error running contact import job",e);
		}
		return jobExecution.getJobId();
	}
	
	@Override
	public boolean isJobComplete(Long jobId)
	{
		JobExecution jobExecution = jobExecutionMap.get(String.valueOf(jobId));		
		
		if (jobExecution.getStatus() == BatchStatus.COMPLETED)
			return true;
		else
			return false;		
	}	
	
	@Loggable(LogLevel.TRACE)
	@SuppressWarnings("unchecked")
	@Override
	public CsvDataReport getNumOfRecordsStoredForCsvImport(String filePath, Long jobId){
		CsvDataReport csvDataReport = null;
		try{
			initJobMap();
			JobExecution jobExecution = jobExecutionMap.get(String.valueOf(jobId));
			ExecutionContext executionContext = jobExecution.getExecutionContext();
			Integer numOfRecords = (Integer) executionContext.get("file:" + filePath);
			List<Contact> errorContactList = (List<Contact>) executionContext.get("ERRfile:" + filePath);
			if(errorContactList != null){
				Integer errors = errorContactList.size();
				Integer successful = numOfRecords - errors;
				csvDataReport = new CsvDataReport(successful, errors);
			}else{
				csvDataReport = new CsvDataReport(numOfRecords, 0);
			}
		}catch(Exception e) {
			log.error(LogUtil.getMarker_notifyAdmin(),"Error getNumOfRecordsStoredForCsvImport", e);
		}
		return csvDataReport;		
	}
	
	@Loggable(LogLevel.TRACE)
	@SuppressWarnings("unchecked")
	@Override
	public List<Contact> generateCsvErrorFile(String filePath, Long jobId){
		initJobMap();
		JobExecution jobExecution = jobExecutionMap.get(String.valueOf(jobId));
		ExecutionContext executionContext = jobExecution.getExecutionContext();
		List<Contact> errorContactList = (List<Contact>) executionContext.get("ERRfile:" + filePath);	
		return errorContactList;
	
	}

	@Loggable(LogLevel.TRACE)
	@SuppressWarnings("unchecked")
	@Override
	public PagingLoadResult<CampaignContact> listAllCampaignContactsForCampaign(Campaign campaign, PagingLoadConfig loadConfig, boolean includeComplete, boolean includeRemoved) {
		Search search = ServiceUtil.getSearchFromLoadConfig(CampaignContact.class, loadConfig, CampaignContact.PROP_JOINING_DATE);
		search.addFilterEqual(CampaignContact.PROP_CAMPAIGN, campaign);				
		search.addFetch(CampaignContact.PROP_CONTACT);		
		
		if (!includeComplete){
			search.addFilterLessOrEqual(CampaignContact.PROP_PROGRESS, campaign.getDuration());
		}
		
		if (!includeRemoved){
			search.addFilterNull(CampaignContact.PROP_END_DATE);
		}
		
		SearchResult<CampaignContact> searchResult = mobilisrGeneralDAO.searchAndCount(search);
		
		return ServiceUtil.getPagingLoadResult(loadConfig, searchResult);
	}

	@Loggable(LogLevel.TRACE)
	@Override
	public List<ContactExportSummary> listAllExportContactsForOrganization(Organization organization, Integer batch, Integer batchSize) {
		return contactDAO.getExportContactsForOrganization(organization, batch, batchSize);
	}
	
	@Loggable(LogLevel.TRACE)
	@Override
	public ContactGroup getContactGroup(Long contactGroupId) {
		return contactGroupDAO.find(contactGroupId);
	}

	@Loggable(LogLevel.TRACE)
	@Override
    public List<ContactExportSummary> listAllExportContactsForGroup(ContactGroup contactGroup, Integer batch, Integer batchSize) {
        return contactDAO.getExportContactsForGroup(contactGroup, batch, batchSize);
    }
	
	@Loggable(LogLevel.TRACE)
	@Override
	public boolean checkMsisdnExists(Organization organization, String msisdn){
		Search search = new Search(Contact.class);
		search.addFilterAnd(Filter.equal(Contact.PROP_MSISDN, msisdn), 
				Filter.equal(Contact.PROP_ORGANIZATION, organization));
		
		int count = contactDAO.count(search);
		return count > 0;
	}

    @Loggable(LogLevel.TRACE)
    @Override
    public List<Long> listAllCampaignsForContact(String msisdn) {
        return contactDAO.findCampaignsForContact(msisdn);
    }

}
