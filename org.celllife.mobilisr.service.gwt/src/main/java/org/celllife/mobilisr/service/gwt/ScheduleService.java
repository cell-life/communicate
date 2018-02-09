package org.celllife.mobilisr.service.gwt;

import org.celllife.mobilisr.constants.CampaignStatus;
import org.celllife.mobilisr.domain.Campaign;
import org.celllife.mobilisr.domain.User;
import org.celllife.mobilisr.exception.MobilisrException;
import org.celllife.mobilisr.exception.MobilisrRuntimeException;
import org.celllife.mobilisr.service.CampaignScheduleService;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * @see CampaignScheduleService
 */
@RemoteServiceRelativePath("schedCampaign.rpc")
public interface ScheduleService extends RemoteService {

	/**
	 * @see CampaignScheduleService#scheduleCampaign(Campaign, User)
	 */
	void scheduleCampaign(Campaign campaign, User user)	throws MobilisrException, MobilisrRuntimeException;
	
	/**
	 * @see CampaignScheduleService#stopCampaign(Campaign)
	 */
	CampaignStatus stopCampaign(Campaign campaign, User user) throws MobilisrException, MobilisrRuntimeException;

	/**
	 * @see CampaignScheduleService#sendTestSMS(Campaign, User, String, String)
	 */
	void sendTestSMS(Campaign campaign, User user, String number, String smsMsg) throws MobilisrException, MobilisrRuntimeException;

	/**
	 * @see CampaignScheduleService#sendTestSMS(Long, User, String, String)
	 */
	void sendTestSMS(Long campaignID, User user, String number, String smsMsg) throws MobilisrException, MobilisrRuntimeException;

	/**
	 * @see CampaignScheduleService#rescheduleAllRelativeCampaigns(User)
	 */
	void rescheduleAllRelativeCampaigns(User user) throws MobilisrException, MobilisrRuntimeException;
}