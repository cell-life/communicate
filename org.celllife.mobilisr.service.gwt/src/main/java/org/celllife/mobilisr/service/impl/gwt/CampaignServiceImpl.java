package org.celllife.mobilisr.service.impl.gwt;

import java.util.List;

import javax.servlet.ServletException;

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
import org.celllife.mobilisr.service.gwt.CampaignService;

import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import com.extjs.gxt.ui.client.data.PagingLoadResult;

public class CampaignServiceImpl extends AbstractMobilisrService implements
		CampaignService {

	private static final long serialVersionUID = 4357718911295325066L;

	private org.celllife.mobilisr.service.CampaignService service;

	@Override
	public void init() throws ServletException {
		super.init();
		service = (org.celllife.mobilisr.service.CampaignService) getBean("crudCampaignService");
	}

	@Override
	public Campaign saveOrUpdateFixedCampaign(Campaign campaign) throws MobilisrException {
		return service.saveOrUpdateFixedCampaign(campaign);
	}

	@Override
	public Campaign saveOrUpdateCampaign(Campaign campaign, List<CampaignMessage> messages) throws MobilisrException {
		return service.saveOrUpdateCampaign(campaign, messages);
	}

	@Override
	public List<CampaignMessage> findCampMessageByCampaign(Campaign campaign)
			throws MobilisrException {
		return service.findCampMessageByCampaign(campaign);
	}

	@Override
	public PagingLoadResult<CampaignMessage> findCampMessageByCampaign(
			Campaign campaign, PagingLoadConfig loadConfig)
			throws MobilisrException, MobilisrRuntimeException {
		return service.findCampMessageByCampaign(campaign, loadConfig);
	}

	@Override
	public PagingLoadResult<Campaign> listAllCampaigns(Organization org,
			CampaignType[] campaignTypes, PagingLoadConfig loadConfig,
			Boolean showVoided) throws MobilisrException {
		return service.listAllCampaigns(org, campaignTypes, loadConfig, showVoided);
	}

	@Override
	public Campaign loadCampaignWithMessages(Long campaignId)
			throws MobilisrException {
		return service.loadCampaignWithMessages(campaignId);
	}

	@Override
	public void rescheduleRelativeCampaign(Campaign campaign, User user) throws MobilisrException {
		service.rescheduleRelativeCampaign(campaign, user);
	}

	@Override
	public Campaign importCampaignMessagesFromCSV(String filePath,
			Campaign campaign) throws MobilisrException {
		return service.importCampaignMessagesFromCSV(filePath, campaign);
	}

	@Override
	public PagingLoadResult<CampaignContact> listCampaignsByContact(Contact contact, PagingLoadConfig loadConfig) {
		return service.getCampaignsByContact(contact, loadConfig);
	}

	@Override
	public List<ContactMsgTime> getCampaignMessageTimes(Campaign campaign) {
		return service.getCampaignMessageTimes(campaign);
	}

	@Override
	public Long getCampaignIdForName(String campaignName){
		return service.getCampaignIdForName(campaignName);
	}

	@Override
	public void toggleCampaignVoidState(Campaign campaign){
		service.toggleCampaignVoidState(campaign);
	}

	@Override
	public int getNumberActiveCampaigns(Organization org){
		return service.getNumberActiveCampaigns(org);
	}

	@Override
	public void addGroupToCampaign(Long contactGroupId, Campaign campaign, List<ContactMsgTime> msgTimes, boolean startOver)
			throws MobilisrException, MobilisrRuntimeException {
		service.addGroupToCampaign(contactGroupId, campaign, msgTimes, startOver);

	}

	@Override
	public CampaignContact saveOrUpdateCampaignContact(CampaignContact campaignContact)
			throws MobilisrException, MobilisrRuntimeException {
		return service.saveOrUpdateCampaignContact(campaignContact);
	}
	
	@Override
	public void removeGroupFromCampaign(Long contactGroupId, Campaign campaign)
			throws MobilisrException, MobilisrRuntimeException {
		service.removeGroupFromCampaign(contactGroupId, campaign);
	}
	
	@Override
	public void removeContactFromCampaign(Campaign campaign, Messagable contact) throws MobilisrException,
			MobilisrRuntimeException {
		service.removeContactFromCampaign(campaign, contact);
	}
	
	@Override
	public void addAllContactsToCampaign(Campaign campaign, List<ContactMsgTime> contactMsgTimes, boolean startOver)
			throws MobilisrException, MobilisrRuntimeException {
		service.addAllContactsToCampaign(campaign, contactMsgTimes, startOver);
	}
	
	@Override
	public void removeAllContactsFromCampaign(Campaign campaign)
			throws MobilisrException, MobilisrRuntimeException {
		service.removeAllContactsFromCampaign(campaign);
	}
	
	@Override
	public CampaignContact getCampaignContact(Campaign campaign, String msisdn)
			throws MobilisrException, MobilisrRuntimeException {
		return service.getCampaignContact(campaign, msisdn);
	}
	
	@Override
	public Long addCsvFileToCampaign(List<String> fieldOrder, String filePath, Organization org, Campaign campaign, List<ContactMsgTime> defaultTimes, boolean startOver) 
			throws UniquePropertyException 	{
		return service.addCsvFileToCampaign(fieldOrder, filePath, campaign, defaultTimes, startOver);
	}
	
	@Override
	public void removeCsvFileFromCampaign(List<String> fieldOrder, String filePath, Campaign campaign) 
	throws UniquePropertyException 	{
		service.removeCsvFileFromCampaign(fieldOrder, filePath, campaign);
	}

}
