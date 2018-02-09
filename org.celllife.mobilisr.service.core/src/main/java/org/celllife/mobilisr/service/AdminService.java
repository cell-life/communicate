package org.celllife.mobilisr.service;

import java.util.List;

import org.celllife.mobilisr.domain.NumberInfo;
import org.celllife.mobilisr.domain.User;
import org.celllife.mobilisr.service.gwt.OrganisationNotificationViewModel;

public interface AdminService extends BaseService {

	int getMailQueueCount();

	int getLostMessagesCount();

	void sendMailNow();

	void sendNewUserRequest(User user, String requestType, String requestText);

	List<NumberInfo> getNumberInfoList();

	void sendNewOrganizationNotification(OrganisationNotificationViewModel model);
}
