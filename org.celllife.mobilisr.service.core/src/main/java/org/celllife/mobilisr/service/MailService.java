package org.celllife.mobilisr.service;

import java.io.File;
import java.util.List;

import org.celllife.mobilisr.domain.MailMessage;
import org.celllife.mobilisr.domain.Organization;
import org.celllife.mobilisr.domain.User;
import org.celllife.mobilisr.service.gwt.OrganisationNotificationViewModel;

public interface MailService {

	void sendQueuedMail();
	
	void sendResetPasswordEmail(User user, String newPassword);
	
	void sendNewUserRequest(User user, String requestType, String requestText);
	
	void sendBalanceAlert(User user, String message, int reserveAmount, Organization organization);

	void sendSystemAlert(String message);

	void sendBalanceLowAlert(Organization organization);

	void enqueueMail(MailMessage message);

	void enqueueMail(String address, String subject, String text);

	void enqueueMail(String address, String subject, String text,
			File attachment);

	void enqueueMail(String address, String subject, String text,
			List<File> attachments);

	void enqueueMail(String address, String subject, String text, File attachment, String attachmentName);

	void sendNewOrganizationNotification(OrganisationNotificationViewModel model);

	void sendCreditNotification(Organization organization, User user,
			int amount, String message);

}
