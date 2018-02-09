package org.celllife.mobilisr.service;

import java.util.List;

import org.celllife.mobilisr.domain.Campaign;
import org.celllife.mobilisr.domain.CampaignContact;
import org.celllife.mobilisr.domain.Contact;
import org.celllife.mobilisr.domain.ContactGroup;
import org.celllife.mobilisr.domain.Organization;
import org.celllife.mobilisr.exception.MsisdnFormatException;
import org.celllife.mobilisr.service.exception.UniquePropertyException;
import org.celllife.mobilisr.service.gwt.ContactContactGroupViewModel;
import org.celllife.mobilisr.service.gwt.ContactGroupContactViewModel;
import org.celllife.mobilisr.service.gwt.CsvDataReport;
import org.celllife.mobilisr.utilbean.ContactExportSummary;

import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.google.gwt.user.client.rpc.RemoteService;

/**
 * Interface used in ContactManagment
 * @author Vikram Bindal (e-mail: vikram@cell-life.org)
 */
public interface ContactsService extends RemoteService {

	//------------------------------------- CONTACT RELATED ------------------------------------------------//
	/**
	 * Method responsible for persisting contact. This method also performs adding groups to the contact via
	 * configuration for ContactModel (addAll, removeAll, add selected, remove selected). For details
	 * @see ContactModel. 
	 * @param organization	Organisation to which the contact must be added
	 * @param contactModel	ContactModel containing the configuration for contact management of groups to contact
	 * @return				persisted Contact object
	 * @throws UniquePropertyException 
	 * @throws MsisdnFormatException 
	 */
	Contact saveOrUpdateContact(Organization organization, ContactContactGroupViewModel contactModel) throws UniquePropertyException, MsisdnFormatException;
	
	/**
	 * Method responsible for obtaining list of contacts that belong to an organisation via the configuration
	 * specified under PagingLoadConfig (max fetches, page number/offset, misc search keys).
	 * @param organization	Organisation for which the contact must be listed
	 * @param loadConfig	@see PagingloadConfig for details about configuration
	 * @return				Result containing the contact for organisation obtained adhering to PagingLoadConfig
	 */
	PagingLoadResult<Contact> listAllContactsForOrganization(Organization organization, PagingLoadConfig loadConfig);
		
	/**
	 * Method responsible for obtaining list of all the groups that belong to a particular contact
	 * @param organization	Organisation for which the contacts belonging to a particular group must be obtained for
	 * @param contact		Contact from which the groups must be obtained
	 * @param loadConfig	@see PagingloadConfig for details about configuration
	 * @return
	 */
	PagingLoadResult<ContactGroup> listAllGroupsForContact(Organization organization, Contact contact,PagingLoadConfig loadConfig);
	
	//------------------------------------- GROUPS RELATED ----------------------------------------------------//
	/**
	 * Method responsible for persisting contact. This method also performs adding groups to the contact via
	 * configuration for ContactModel (addAll, removeAll, add selected, remove selected). For details
	 * @see ContactModel. 
	 * @param organization	Organisation to which the contact must be added
	 * @param contactModel	ContactModel containing the configuration for contact management of contacts to group
	 * @return				persisted Contact object
	 * @throws UniquePropertyException 
	 */
	ContactGroup saveOrUpdateContactGroup(Organization organization, ContactGroupContactViewModel contactModel) throws UniquePropertyException;

	/**
	 * Method responsible for obtaining list of groups that belong to an organisation via the configuration
	 * specified under PagingLoadConfig (max fetches, page number/offset, misc search keys).
	 * @param organization	Organisation for which the group must be listed
	 * @param pagingLoadConfig	@see PagingloadConfig for details about configuration
	 * @return				Result containing the group for organisation obtained adhering to PagingLoadConfig
	 */
	PagingLoadResult<ContactGroup> listAllGroupsForOrganization(Organization organization, PagingLoadConfig pagingLoadConfig);

	/**
	 * Method responsible for obtaining list of all the contacts that belong to
	 * a particular group
	 * 
	 * @param contactGroup
	 *            ContactGroup from which the contacts must be obtained
	 * @param loadConfig
	 *            @see PagingLoadConfig for details about configuration
	 * @return
	 */
	PagingLoadResult<Contact> listAllContactsForGroup(ContactGroup contactGroup, PagingLoadConfig loadConfig);

	/**
	 * @param campaign
	 *            the campaign to list contacts for
	 * @param pagingLoadConfig
	 *            paging configuration
	 * @param includeComplete
	 *            true to include contacts that have completed the campaign
	 * @param includeRemoved
	 *            true to include contacts that were removed from the campaign
	 * @return
	 */
	PagingLoadResult<CampaignContact> listAllCampaignContactsForCampaign(Campaign campaign, PagingLoadConfig pagingLoadConfig, boolean includeComplete, boolean includeRemoved);

	//------------------------------------- CSV IMPORT RELATED ----------------------------------------------------//
	
	/**
	 * Method responsible for reading sample number of data from the csv file
	 * @param filePath	CSV File path
	 * @param maxRead	Maximum number of lines to read
	 * @return			ArrayList containing the data from the csv file with each column in a row as String
	 */
	List<List<String>> readCSVFixedRecordLength(String filePath, int maxRead);
	
	/**
	 * Method responsible for saving the records from the csv file into the database. For performance Spring Batch is used
	 * to save csv contacts into the database.
	 * @param fieldOrder	ArrayList containing the type of field for a contact from a csv column (e.g: is column 1 MSISDN ? etc)
	 * @param filePath		CSV File path
	 * @param organization	Organization under which the contact records in the CSV file must be saved
	 * @param groupList		List of groups to which the contact must be added. CSV import feature allows adding contacts to groups
	 * @return				Job Id from Spring Batch that is running the current task. This ID is usefull in obtaining the 
	 * 						progress information from spring batch for the particular task
	 */
	Long saveCSVContacts(List<String> fieldOrder, String filePath, Organization organization, List<ContactGroup> groupList);

	/**
	 * Method responsible for obtaining the progrss information (number of contacts stored/failed with list of contacts)
	 * so that user can be notified while the csv file save is in progress
	 * @param filePath		CSV File path
	 * @param jobId			Job Id from Spring Batch that is currently running the csv save task.@see saveCSVContacts method
	 * @return				@see CsvDataReport for full details on the various attributes that can be obtained
	 */
	CsvDataReport getNumOfRecordsStoredForCsvImport(String filePath, Long jobId);

	/**
	 * Method responsible for obtaining the list of contacts that couldn't be saved or had errors in them.
	 * Most likely due to bad contact mobile number
	 * @param filePath		Csv File path
	 * @param jobId			Job id from Spring Batch that was running the csv store task
	 * @return				List of contacts that couldn't be saved during csv import
	 */	
	List<Contact> generateCsvErrorFile(String filePath, Long jobId);

	Contact addGroupsToContact(Organization organization,
			ContactContactGroupViewModel contactModel);

	boolean deleteContactGroup(ContactGroup contactGroup) throws UniquePropertyException;

	Long countContactsForGroup(ContactGroup group);
	
	//------------------------------------- CSV EXPORT RELATED ----------------------------------------------------//
	
	/**
	 * Method responsible for retrieving the Contacts (in batches) for an Organisation formatted for export purposes
	 * @param organization Organisation for which the contact must be listed
	 * @param batch Integer number of the current batch (e.g. 0 is first batch, 1 is the second)
	 * @param batchSize Integer maximum number of Contacts returned in the batch
	 * @return List of ContactExportSummary
	 */
	List<ContactExportSummary> listAllExportContactsForOrganization(Organization organization, Integer batch, Integer batchSize);

	/**
	 * @param jobId
	 * @return true if job is complete
	 */
	boolean isJobComplete(Long jobId);

	/**
	 * Checks to see if a contact exists for the given organisation with the given msisdn.
	 * @param organization
	 * @param msisdn
	 * @return
	 */
	boolean checkMsisdnExists(Organization organization, String msisdn);
	
	/**
	 * Returns a ContactGroup given an identifier
	 * @param contactGroupId
	 * @return
	 */
	ContactGroup getContactGroup(Long contactGroupId);

    /**
     * @param contactGroup The group to export
     * @param batch Integer number of the current batch (e.g. 0 is first batch, 1 is the second)
     * @param batchSize Integer maximum number of Contacts returned in the batch
     * @return List of contacts in the group.
     */
    List<ContactExportSummary> listAllExportContactsForGroup(ContactGroup contactGroup, Integer batch, Integer batchSize);

    /**
     * Returns a list of campaign contacts for a particular MSISDN.
     * @param msisdn The MSISDN to retrieve campaign contacts for.
     * @return The list of campaign contacts.
     */
    List<Long> listAllCampaignsForContact(String msisdn);

}
