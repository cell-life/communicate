package org.celllife.mobilisr.service.gwt;

import java.util.List;

import org.celllife.mobilisr.domain.Campaign;
import org.celllife.mobilisr.domain.CampaignContact;
import org.celllife.mobilisr.domain.Contact;
import org.celllife.mobilisr.domain.ContactGroup;
import org.celllife.mobilisr.domain.Organization;
import org.celllife.mobilisr.exception.MobilisrException;
import org.celllife.mobilisr.exception.MobilisrRuntimeException;
import org.celllife.mobilisr.service.ContactsService;

import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * @see ContactsService
 */
@RemoteServiceRelativePath("crudContacts.rpc")
public interface ContactService extends RemoteService {

	/**
	 * @see ContactsService#saveOrUpdateContact(Organization, ContactContactGroupViewModel)
	 */
	Contact saveOrUpdateContact(Organization organization, ContactContactGroupViewModel contactModel) throws MobilisrException, MobilisrRuntimeException;
	
	/**
	 * @see ContactsService#listAllContactsForOrganization(Organization, PagingLoadConfig)
	 */
	PagingLoadResult<Contact> listAllContactsForOrganization(Organization organization, PagingLoadConfig loadConfig) throws MobilisrException, MobilisrRuntimeException;
	
	/**
	 * @see ContactsService#listAllGroupsForContact(Organization, Contact, PagingLoadConfig)
	 */
	PagingLoadResult<ContactGroup> listAllGroupsForContact(Organization organization, Contact contact,PagingLoadConfig loadConfig) throws MobilisrException, MobilisrRuntimeException;
	
	/**
	 * @see ContactsService#saveOrUpdateContactGroup(Organization, ContactGroupContactViewModel)
	 */
	ContactGroup saveOrUpdateContactGroup(Organization organization, ContactGroupContactViewModel contactModel) throws MobilisrException, MobilisrRuntimeException;

	/**
	 * @see ContactsService#listAllGroupsForOrganization(Organization, PagingLoadConfig)
	 */
	PagingLoadResult<ContactGroup> listAllGroupsForOrganization(Organization organization, PagingLoadConfig pagingLoadConfig) throws MobilisrException, MobilisrRuntimeException;
	
	/**
	 * @see ContactsService#listAllContactsForGroup(ContactGroup, PagingLoadConfig)
	 */
	PagingLoadResult<Contact> listAllContactsForGroup(ContactGroup contactGroup, PagingLoadConfig loadConfig) throws MobilisrException, MobilisrRuntimeException;
	
	/**
	 * @param includeComplete
	 * @see ContactsService#listAllCampaignContactsForCampaign(Campaign, PagingLoadConfig, boolean, boolean)
	 */
	PagingLoadResult<CampaignContact> listAllCampaignContactsForCampaign(Campaign campaign, PagingLoadConfig pagingLoadConfig, boolean includeComplete, boolean includeRemoved) throws MobilisrException, MobilisrRuntimeException;

	/**
	 * @see ContactsService#readCSVFixedRecordLength(String, int)
	 */
	List<List<String>> readCSVFixedRecordLength(String filePath, int maxRead) throws MobilisrException, MobilisrRuntimeException;
	
	/**
	 * @see ContactsService#saveCSVContacts(List, String, Organization, List)
	 */
	Long saveCSVContacts(List<String> fieldOrder, String filePath, Organization organization, List<ContactGroup> groupList) throws MobilisrException, MobilisrRuntimeException;

	/**
	 * @see ContactsService#getNumOfRecordsStoredForCsvImport(String, Long)
	 */
	CsvDataReport getNumOfRecordsStoredForCsvImport(String filePath, Long jobId) throws MobilisrException, MobilisrRuntimeException;
	
	/**
	 * @see ContactsService#deleteContactGroup(ContactGroup)
	 */
	boolean deleteContactGroup(ContactGroup contactGroup) throws MobilisrException, MobilisrRuntimeException;
	
	/**
	 * @see ContactsService#checkMsisdnExists(Organization, String)
	 */
	boolean checkMsisdnExists(Organization organization, String msisdn) throws MobilisrException, MobilisrRuntimeException;

    /**
     * @see ContactsService#listAllCampaignsForContact(String msisdn)
     */
    List<Long> listAllCampaignsForContact(String msisdn);
}
