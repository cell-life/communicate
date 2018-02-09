package org.celllife.mobilisr.service;

import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.google.gwt.user.client.rpc.RemoteService;
import org.celllife.mobilisr.constants.CampaignType;
import org.celllife.mobilisr.domain.*;
import org.celllife.mobilisr.exception.InsufficientBalanceException;
import org.celllife.mobilisr.exception.MsisdnFormatException;
import org.celllife.mobilisr.exception.TransactionNotFoundException;
import org.celllife.mobilisr.service.exception.CampaignStateException;
import org.celllife.mobilisr.service.exception.UniquePropertyException;

import java.util.Date;
import java.util.List;

/**
 * Interface for CRUD operations on the Campaign
 * @author Vikram Bindal (e-mail: vikram@cell-life.org)
 * @author Amelia Sagoff
 */
public interface CampaignService extends RemoteService {

	/**
	 * Method that is responsible for saving JustSMS campaigns into the system
	 * @param campaign			Campaign that must be saved
	 * @throws UniquePropertyException
	 */
	Campaign saveOrUpdateFixedCampaign(Campaign campaign) throws UniquePropertyException;
	
	/**
	 * Save or update the campaign. If {@link Campaign#isRebuildMessages()} then
	 * the method will also regenerate all the campaign messages.
	 * 
	 * If campaignMessages is not null or empty the messages will be saved or
	 * updated before the campaign cost is re-calculated. This only happens if
	 * the campaign messages are not regenerated.
	 * 
	 * Note: The messages are passed in separately to avoid lazy loading exception
	 * when calling campaign.getMessages for an already persisted campaign.
	 * 
	 * @param campaign the campaign to save
	 * @param campaignMessages a list of messages to save / update
	 * @return the saved campaign. Note that it will not contain any messages.
	 * @throws UniquePropertyException if a campaign with the same name already exists
	 */
	Campaign saveOrUpdateCampaign(Campaign campaign, List<CampaignMessage> campaignMessages) throws UniquePropertyException;
	
	List<CampaignMessage> findCampMessageByCampaign(Campaign campaign);
	
	PagingLoadResult<CampaignMessage> findCampMessageByCampaign(Campaign campaign, PagingLoadConfig loadConfig);

	PagingLoadResult<Campaign> listAllCampaigns(Organization organization, CampaignType[] campaignTypes, PagingLoadConfig loadConfig, Boolean showVoided);

	Campaign loadCampaignWithMessages(Long campaignId);

	/**
	 * Convert a list of contacts to a list of campaign contacts. Checks is they
	 * exist already before creating new. New CampaignContacs are loaded with
	 * default message times if the campaign is a relative campaign.
	 * 
	 * @param contactList
	 * @param campaign
	 * @return
	 */
	List<CampaignContact> convertContactToCampaignContact(List<Contact> contactList, Campaign campaign);

	void rescheduleRelativeCampaign(Campaign campaign, User user)throws TransactionNotFoundException, InsufficientBalanceException, CampaignStateException;

	Campaign getCampaign(Long campaignId);

	Campaign importCampaignMessagesFromCSV(String filePath, Campaign campaign);

	PagingLoadResult<CampaignContact> getCampaignsByContact(Contact contact, PagingLoadConfig loadConfig);

	List<ContactMsgTime> getCampaignMessageTimes(Campaign campaign);

	Long getCampaignIdForName(String campaignName);

	/**
	 * Toggles the voided state of the campaign and saves it.
	 * 
	 * @param campaign
	 */
	void toggleCampaignVoidState(Campaign campaign);

	/**
	 * Returns the number of active campaigns for an organisation or for
	 * all organisations if the parameter is null.
	 *  
	 * @param org
	 * @return
	 */
	int getNumberActiveCampaigns(Organization org);

	/**
	 * Adds an existing group to a campaign.
	 * 
	 * @param contactGroupId
	 * @param campaign
	 * @param defaultTimes
	 * @param startOver
	 *
	 */
	void addGroupToCampaign(Long contactGroupId, Campaign campaign, List<ContactMsgTime> defaultTimes, Boolean startOver);
	
	/**
	 * Add a new campaign contact to a campaign or update an exising one.
	 * 
	 * @param campaignContact
	 * @return 
	 * @throws MsisdnFormatException
	 * @throws UniquePropertyException
	 */
	CampaignContact saveOrUpdateCampaignContact(CampaignContact campaignContact)
			throws MsisdnFormatException, UniquePropertyException;

	void removeGroupFromCampaign(Long contactGroupId, Campaign campaign);

	/**
	 * This method adds contacts from a CSV file to a campaign.
	 * 
	 * @param fieldOrder
	 * @param filePath
	 * @param campaign
	 * @param defaultTimes
	 * @param startOver
	 * 
	 */
	Long addCsvFileToCampaign(List<String> fieldOrder, String filePath, Campaign campaign, List<ContactMsgTime> defaultTimes, Boolean startOver);

	/**
	 * This method removes contacts in a CSV file from a campaign.
	 * 
	 * @param fieldOrder
	 * @param filePath
	 * @param campaign
	 * 
	 */
	void removeCsvFileFromCampaign(List<String> fieldOrder, String filePath, Campaign campaign);
	
	void removeAllContactsFromCampaign(Campaign campaign);

	void removeContactFromCampaign(Campaign campaign, Messagable contact);
	
	void removeContactsFromCampaign(Campaign campaign, List<? extends Messagable> removedContactList);

	/**
	 * This method adds all existing contacts to a campaign.
	 * 
	 * @param campaign
	 * @param contactMsgTimes
	 * @param startOver
	 */
	void addAllContactsToCampaign(Campaign campaign, List<ContactMsgTime> contactMsgTimes, Boolean startOver);

	/**
	 * This method is used to check whether a campaign contact exists on a campaign with a particular msisdn
	 * 
	 * @param campaign
	 * @param msisdn
	 * @return true if contact exists on campaign
	 */
	CampaignContact getCampaignContact(Campaign campaign, String msisdn);

    /**
     * This gets a list of links for the particular campaign.
     * @param campaignId The ID of the campaign to get links for.
     * @return A list of Long IDs of linked campaigns.
     */
    List<Long> getLinkedCampaignsForCampaign(Long campaignId);

    /**
     * Adds a contact to a campaign, or to the correct linked campaign.
     * @param contactList The list of Campaign Contacts to add.
     * @param campaign The campaign to add the contacts to.
     * @throws Exception
     */
    void addContactsToCampaignWithLinks(List<CampaignContact> contactList, Campaign campaign, Date joiningDate, List<Date> contactMessageTimes) throws Exception;

}
