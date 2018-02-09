package org.celllife.mobilisr.service.impl;

import java.util.List;

import org.celllife.mobilisr.constants.SmsStatus;
import org.celllife.mobilisr.domain.MailMessage;
import org.celllife.mobilisr.domain.NumberInfo;
import org.celllife.mobilisr.domain.SmsLog;
import org.celllife.mobilisr.domain.User;
import org.celllife.mobilisr.service.AdminService;
import org.celllife.mobilisr.service.MailService;
import org.celllife.mobilisr.service.exception.MobilisrSchedulingException;
import org.celllife.mobilisr.service.gwt.OrganisationNotificationViewModel;
import org.celllife.mobilisr.service.qrtz.BackgroundJobs;
import org.celllife.mobilisr.service.qrtz.QuartzService;
import org.celllife.mobilisr.util.LogUtil;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.trg.search.Search;

@Service("adminService")
public class AdminServiceImpl extends BaseServiceImpl implements AdminService {

	private static final Logger log = LoggerFactory.getLogger(AdminServiceImpl.class);

	@Autowired
	private MailService mailService;

	@Autowired
	private QuartzService quartzService;

	@Override
	public int getMailQueueCount() {
		Search s = new Search(MailMessage.class);
		s.addFilterEqual(MailMessage.PROP_EMAILED, false);
		int count = getGeneralDao().count(s);
		return count;
	}

	@Override
	public int getLostMessagesCount() {
		Search s = new Search(SmsLog.class);
		s.addFilterEqual(SmsLog.PROP_VOIDED, false);
		s.addFilterIn(SmsLog.PROP_STATUS, SmsStatus.RX_CHANNEL_FAIL, SmsStatus.RX_FILTER_FAIL);
		int count = getGeneralDao().count(s);
		return count;
	}

	@Override
	public void sendMailNow() {
		try {
			quartzService.fireBackgroundJob(BackgroundJobs.PROCESS_MAIL_QUEUE);
		} catch (SchedulerException e) {
			log.error(LogUtil.getMarker_notifyAdmin(),
					"Error firing backgourd job: " + BackgroundJobs.PROCESS_MAIL_QUEUE, e);
			throw new MobilisrSchedulingException("Error sending mail. See log for details.");
		}
	}

	@Override
	public void sendNewUserRequest(User user, String requestType, String requestText){
		mailService.sendNewUserRequest(user, requestType, requestText);
	}
	
	@Override
	public void sendNewOrganizationNotification(OrganisationNotificationViewModel model){
		mailService.sendNewOrganizationNotification(model);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<NumberInfo> getNumberInfoList(){
		Search s = new Search(NumberInfo.class);
		s.addFilterEqual(NumberInfo.PROP_VOIDED, false);
		return getGeneralDao().search(s);
	}
}
