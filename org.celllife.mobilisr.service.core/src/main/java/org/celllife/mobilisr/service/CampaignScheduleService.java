package org.celllife.mobilisr.service;

import org.celllife.mobilisr.constants.CampaignStatus;
import org.celllife.mobilisr.domain.Campaign;
import org.celllife.mobilisr.domain.User;
import org.celllife.mobilisr.exception.InsufficientBalanceException;
import org.celllife.mobilisr.exception.MsisdnFormatException;
import org.celllife.mobilisr.service.exception.CampaignStateException;
import org.celllife.mobilisr.service.exception.MobilisrSchedulingException;

import com.google.gwt.user.client.rpc.RemoteService;

/**
 * Interface responsible for managing program specific operations
 * @author Vikram Bindal (e-mail: vikram@cell-life.org)
 */
public interface CampaignScheduleService extends RemoteService {

	void scheduleCampaign(Campaign campaign, User user)	throws InsufficientBalanceException, CampaignStateException;
	
	CampaignStatus stopCampaign(Campaign campaign, User user) throws MobilisrSchedulingException;

	void sendTestSMS(Campaign campaign, User user, String number, String smsMsg) throws InsufficientBalanceException, MsisdnFormatException;

	void sendTestSMS(Long campaignID, User user, String number, String smsMsg) throws InsufficientBalanceException, MsisdnFormatException;
	
	void sendWelcomeMessages(Campaign campaign, User user);

	void processCampaignFinish(Campaign campaign);

	void processCampaignFinish();

	void rescheduleAllRelativeCampaigns(User user);

}