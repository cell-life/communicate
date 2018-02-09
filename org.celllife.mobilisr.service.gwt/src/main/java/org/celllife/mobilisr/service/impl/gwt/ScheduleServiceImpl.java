package org.celllife.mobilisr.service.impl.gwt;

import javax.servlet.ServletException;

import org.celllife.mobilisr.constants.CampaignStatus;
import org.celllife.mobilisr.domain.Campaign;
import org.celllife.mobilisr.domain.User;
import org.celllife.mobilisr.exception.MobilisrException;
import org.celllife.mobilisr.exception.MobilisrRuntimeException;
import org.celllife.mobilisr.service.CampaignScheduleService;
import org.celllife.mobilisr.service.gwt.ScheduleService;

public class ScheduleServiceImpl extends AbstractMobilisrService implements
		ScheduleService {

	private static final long serialVersionUID = 7937722652449660027L;
	private CampaignScheduleService service;
	
	@Override
	public void init() throws ServletException {
		super.init();
		service = (CampaignScheduleService) getBean("campaignService");
	}

	@Override
	public void scheduleCampaign(Campaign campaign, User user)
			throws MobilisrException {
		service.scheduleCampaign(campaign, user);
	}

	@Override
	public CampaignStatus stopCampaign(Campaign campaign, User user) throws MobilisrException {
		return service.stopCampaign(campaign, user);
	}

	@Override
	public void sendTestSMS(Campaign campaign, User user, String number,
			String smsMsg) throws MobilisrException {
		service.sendTestSMS(campaign, user, number, smsMsg);
	}
	
	@Override
	public void sendTestSMS(Long campaignID, User user, String number,
			String smsMsg) throws MobilisrException {
		service.sendTestSMS(campaignID, user, number, smsMsg);
	}
	
	@Override
	public void rescheduleAllRelativeCampaigns(User user)
			throws MobilisrException, MobilisrRuntimeException {
		service.rescheduleAllRelativeCampaigns(user);
	}
}
