package org.celllife.mobilisr.dao.api;

import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import org.celllife.mobilisr.constants.CampaignStatus;
import org.celllife.mobilisr.domain.*;
import org.celllife.mobilisr.exception.MsisdnFormatException;

import java.util.Date;
import java.util.List;

/**
 * Generic interface for the DAO operations related to Campaign
 * @author Vikram Bindal (e-mail: vikram@cell-life.org)
 */
/**
 * @author Simon
 *
 */

public interface CampaignDAO extends BaseDAO<Campaign, Long> {


	public List<ContactMsgTime> getMsgTimesForCampaignFromContacts(
			Campaign campaign);
	/**
	 * Get the campaign message for the supplied campaign with the given message slot and
	 * message date.
	 *
	 * @param campaign
	 * @param msgSlot
	 * @param msgDay
	 * @return the campaign message
	 */
	public CampaignMessage getCampMsgForDailyCampaign( Campaign campaign, int msgSlot, int msgDay );

	public List<CampaignMessage> getCampMsgForFlexiCampaign(Campaign campaign, Date msgTime);

	public List<CampaignMessage> getAllCampMessages(Campaign campaign);

	/**
	 * Gets the first message for a campaign. Used particularly for getting the message for fixed
	 * campaigns which only have one message.
	 *
	 * @param campaign
	 * @return the first message text or null if there are no messages
	 */
	public String getCampMsgForCampaign(Campaign campaign);

	public List<CampaignContact> getContactsToProcessForFlexiCampaign(Campaign campaign, int progress, Date currentDate);

	/**
	 * Get all CampaignContacts for the campaign that may need messages sent to them.
	 *
	 * @param campaign
	 * @param msgTime
	 * @param msgSlot
	 * @param currentDate to allow for testing, use null in production
	 *
	 * @return
	 */
	public List<CampaignContact> getContactsToProcessForDailyCampaign(Campaign campaign, Date msgTime, int msgSlot, Date currentDate);

	public List<CampaignContact> getContactsInPaginationForCampaign(Campaign campaign, PagingLoadConfig pagingLoadConfig);

	public int countNumberOfContactsForCampaign(Campaign campaign, boolean includeInvalid);

	public void updateCampaignContactsProgress(Campaign campaign);

	public abstract void removeMessagesFromCampaign(Campaign campaign,  List<CampaignMessage> deletedMessages);

	public abstract void addAllContactsToCampaign(Campaign campaign, Boolean startOver);

	public abstract void addContactsToCampaign(Campaign campaign, Organization organization, List<Contact> addedContactList, Boolean startOver);

	public void addContactGroupsToCampaignById(Campaign campaign, List<Long> groupIdList, Boolean startOver);

	public boolean isAllContactsProcessedForCampaign(Campaign campaign);

	public List<CampaignMessage> findDefaultTimesForRelativeCampaign(Campaign campaign);

	/**
	 * Update all the CampaignContacts for the given organization that have the
	 * given msisdn.
	 *
	 * @param contactId
	 * @param newMsisdn
	 * @return the number of CampaignContacts updated
	 * @throws MsisdnFormatException
	 */
	int updateCampaignContactMsisdn(long contactId, String newMsisdn) throws MsisdnFormatException;

	/**
	 * Get a list of CampaignConatacts that have not yet received the welcome message;
	 *
	 * @param campaign
	 * @return
	 */
	public List<CampaignContact> getCampaignContactsNeedingWelcomeMessage(Campaign campaign);

	/**
	 * Mark all CampaignContacts in the list as having received the welcome message
	 * @param contacts
	 */
	public void markCampaignContactsAsReceivedWelcomeMessage(List<CampaignContact> contacts);

	/**
	 * Loads the CampaignContact for the given campaign and contact
	 *
	 * @param campaign
	 * @param contact
	 * @return
	 */
	CampaignContact getCampaignContact(Campaign campaign, Contact contact);
	
	/**
	 * Loads the CampaignContact for the given campaign and msisdn
	 * 
	 * @param campaign
	 * @param msisdn
	 * @return
	 */
	CampaignContact getCampaignContact(Campaign campaign, String msisdn);

	/**
	 * Get all campaign that may need to be marked as finished. i.e. those
	 * with a non-null end date and a status != FINISHED
	 *
	 * @return list of campaigns
	 */
	public List<Campaign> getRunningRelativeCampaignsWithEndDate();

	/**
	 * Get the status for a campaign
	 *
	 * @param campaignId
	 * @return the current status or null campaign with supplied id not found
	 */
	public CampaignStatus getCampaignStatus(Long campaignId);

	/**
	 * @param campaignId
	 * @return List<CampaignMessage> with only the msgDay and msgLenght populated
	 */
	public List<CampaignMessage> getCampaignMessageLengthsAndDay(Long campaignId);

	public Campaign getCampaign(String campaignName);

	public void updateCampaignStatus(Long campaignId, CampaignStatus newStatus);

	/**
	 * For each campaign contact that does not have message times this method
	 * will create message times based on the default times give.
	 * 
	 * @param campaign
	 * @param defaultTimes
	 */
	void createDefaultMessageTimesForContacts(Campaign campaign, List<ContactMsgTime> defaultTimes);
	
	void setCampaignContactsEndDate(Campaign campaign, List<? extends Messagable> contacts);
	
	void setEndDateForCampaignContactsByGroup(Campaign campaign, List<Long> groupIdList);
	
	void setCampaignContactsEndDateByMsisdn(Campaign campaign, List<String> msisdnList);
	void setAllCampaignContactsEndDates(Campaign campaign);

    /**
     * Sets the campaign ID for the campaign to be linked to this one.
     * @param campaignId The campaign ID.
     * @param linkedCampaignId The ID for the linked campaign.
     */
    void setLinkedCampaignId(Long campaignId, Long linkedCampaignId);

    /**
     * Sets the end date for a list of contacts in a campaign.
     * @param campaign Campaign object.
     * @param msisdnList List of msisdn numbers.
     * @param endDate The end date for the contact.
     */
    void setCampaignContactsEndDateByMsisdn(Campaign campaign, List<String> msisdnList, Date endDate);

    void setCampaignContactsDateLastMessage(Campaign campaign, List<? extends Messagable> contacts);

    void setCampaignContactsDateLastMessageByMsisdn(Campaign campaign, List<String> msisdnList, Date lastDate);

    void setContactCount(Campaign campaign, int contactCount);

}
