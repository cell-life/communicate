package org.celllife.mobilisr.service.gwt;

import java.util.List;

import org.celllife.mobilisr.constants.CampaignStatus;
import org.celllife.mobilisr.constants.CampaignType;
import org.celllife.mobilisr.domain.Campaign;
import org.celllife.mobilisr.domain.CampaignContact;
import org.celllife.mobilisr.domain.CampaignMessage;
import org.celllife.mobilisr.domain.Contact;
import org.celllife.mobilisr.domain.ContactMsgTime;
import org.celllife.mobilisr.domain.Messagable;
import org.celllife.mobilisr.domain.Organization;
import org.celllife.mobilisr.domain.User;
import org.celllife.mobilisr.exception.MobilisrException;
import org.celllife.mobilisr.exception.MobilisrRuntimeException;
import org.celllife.mobilisr.service.exception.UniquePropertyException;

import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * @see org.celllife.mobilisr.service.CampaignService
 */
@RemoteServiceRelativePath("crudCampaign.rpc")
public interface CampaignService extends RemoteService {

	/**
	 * @see org.celllife.mobilisr.service.CampaignService#saveOrUpdateFixedCampaign(Campaign)
	 */
	Campaign saveOrUpdateFixedCampaign(Campaign campaign) throws MobilisrException, MobilisrRuntimeException;

	/**
	 * @see org.celllife.mobilisr.service.CampaignService#saveOrUpdateCampaign(Campaign)
	 */
	Campaign saveOrUpdateCampaign(Campaign campaign, List<CampaignMessage> messages) throws MobilisrException, MobilisrRuntimeException;

	/**
	 * @see org.celllife.mobilisr.service.CampaignService#findCampMessageByCampaign(Campaign)
	 */
	List<CampaignMessage> findCampMessageByCampaign(Campaign campaign) throws MobilisrException, MobilisrRuntimeException;

	/**
	 * @see org.celllife.mobilisr.service.CampaignService#findCampMessageByCampaign(Campaign, PagingLoadConfig)
	 */
	PagingLoadResult<CampaignMessage> findCampMessageByCampaign(Campaign campaign, PagingLoadConfig loadConfig) throws MobilisrException, MobilisrRuntimeException;

	/**
	 * @see org.celllife.mobilisr.service.CampaignService#listAllCampaigns(User, CampaignType, CampaignStatus, PagingLoadConfig)
	 */
	PagingLoadResult<Campaign> listAllCampaigns(Organization org, CampaignType[] campaignTypes, PagingLoadConfig loadConfig, Boolean showVoided) throws MobilisrException, MobilisrRuntimeException;

	/**
	 * @see org.celllife.mobilisr.service.CampaignService#loadCampaignWithMessages(Long)
	 */
	Campaign loadCampaignWithMessages(Long campaignId) throws MobilisrException, MobilisrRuntimeException;

	/**
	 * @see org.celllife.mobilisr.service.CampaignService#rescheduleRelativeCampaign(Campaign, User)
	 */
	void rescheduleRelativeCampaign(Campaign campaign, User user) throws MobilisrException, MobilisrRuntimeException;

	/**
	 * @return
	 * @see org.celllife.mobilisr.service.CampaignService#importCampaignMessagesFromCSV(String, Campaign)
	 */
	Campaign importCampaignMessagesFromCSV(String filePath, Campaign campaign) throws MobilisrException, MobilisrRuntimeException;

	/**
	 * @see org.celllife.mobilisr.service.CampaignService#listCampaignsByContact(Contact)
	 */
	PagingLoadResult<CampaignContact> listCampaignsByContact(Contact contact, PagingLoadConfig loadConfig) throws MobilisrException, MobilisrRuntimeException;

	/**
	 * @see org.celllife.mobilisr.service.CampaignService#getCampaignMessageTimes(Campaign)
	 */
	List<ContactMsgTime> getCampaignMessageTimes(Campaign campaign) throws MobilisrException, MobilisrRuntimeException;

	/**
	 * @see org.celllife.mobilisr.service.CampaignService#getCampaignIdForName(String)
	 */
	Long getCampaignIdForName(String campaignName) throws MobilisrException, MobilisrRuntimeException;

	/**
	 * @see org.celllife.mobilisr.service.CampaignService#toggleCampaignVoidState(Campaign)
	 */
	void toggleCampaignVoidState(Campaign campaign) throws MobilisrException, MobilisrRuntimeException;

	/**
	 * @see org.celllife.mobilisr.service.CampaignService#getNumberActiveCampaigns(Organization)
	 */
	int getNumberActiveCampaigns(Organization org);

	/**
	 * @return 
	 * @see org.celllife.mobilisr.service.CampaignService#saveOrUpdateCampaignContact(CampaignContact)
	 */
	CampaignContact saveOrUpdateCampaignContact(CampaignContact campaignContact) throws MobilisrException, MobilisrRuntimeException;

	/**
	 * @see org.celllife.mobilisr.service.CampaignService#removeGroupFromCampaign(Long, Campaign)
	 */
	void removeGroupFromCampaign(Long contactGroupId, Campaign campaign) throws MobilisrException, MobilisrRuntimeException;
	
	/**
	 * @see org.celllife.mobilisr.service.CampaignService#removeContactFromCampaign(Campaign, Messagable)
	 */
	void removeContactFromCampaign(Campaign campaign, Messagable contact) throws MobilisrException, MobilisrRuntimeException;
	
	/**
	 * @see org.celllife.mobilisr.service.CampaignService#removeAllContactsFromCampaign(Campaign)
	 */
	void removeAllContactsFromCampaign(Campaign campaign) throws MobilisrException, MobilisrRuntimeException;
	
	/**
	 * @see org.celllife.mobilisr.service.CampaignService#getCampaignContact(Campaign, String)
	 */
	CampaignContact getCampaignContact(Campaign campaign, String msisdn) throws MobilisrException, MobilisrRuntimeException;

	Long addCsvFileToCampaign(List<String> fieldOrder, String filePath, Organization org, Campaign campaign, List<ContactMsgTime> defaultTimes, boolean startOver) throws UniquePropertyException;

	void removeCsvFileFromCampaign(List<String> fieldOrder, String filePath,
			Campaign campaign) throws UniquePropertyException;

	void addGroupToCampaign(Long contactGroupId, Campaign campaign, List<ContactMsgTime> msgTimes, boolean startOver) throws MobilisrException, MobilisrRuntimeException;

	void addAllContactsToCampaign(Campaign campaign, List<ContactMsgTime> contactMsgTimes, boolean startOver) throws MobilisrException,
			MobilisrRuntimeException;

}
