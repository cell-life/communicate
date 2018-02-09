package org.celllife.mobilisr.service.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.mail.internet.MimeMessage;

import org.apache.commons.io.FileUtils;
import org.celllife.mobilisr.annotation.LogLevel;
import org.celllife.mobilisr.annotation.Loggable;
import org.celllife.mobilisr.dao.api.MobilisrGeneralDAO;
import org.celllife.mobilisr.domain.AlertType;
import org.celllife.mobilisr.domain.MailMessage;
import org.celllife.mobilisr.domain.Organization;
import org.celllife.mobilisr.domain.User;
import org.celllife.mobilisr.service.MailService;
import org.celllife.mobilisr.service.SettingService;
import org.celllife.mobilisr.service.TemplateService;
import org.celllife.mobilisr.service.UserService;
import org.celllife.mobilisr.service.constants.SettingsEnum;
import org.celllife.mobilisr.service.constants.Templates;
import org.celllife.mobilisr.service.gwt.OrganisationNotificationViewModel;
import org.celllife.mobilisr.service.utility.MapBuilder;
import org.celllife.mobilisr.service.utility.MobilisrUtility;
import org.celllife.mobilisr.util.CommunicateHome;
import org.celllife.mobilisr.util.LogUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Service;

import com.trg.search.Search;

@Service("mailService")
public class MailServiceImpl implements MailService {

	private static final Logger log = LoggerFactory.getLogger(MailServiceImpl.class);

	protected static final String ATTACHMENTS_FOLDER = "attachments";

	@Autowired
	private MobilisrGeneralDAO generalDao;

	@Autowired
	private JavaMailSender mailSender;

	@Autowired
	private TemplateService templateService;

	@Autowired
	private SettingService settingService;
	
	@Autowired
	private UserService userService;
	
	/**
	 * Override property for development and test environments
	 */
	@Value("${enableMailSending}")
	private boolean enableMailSending;

	/**
	 * @param to		A comma delimited list of email addresses
	 * @param subject	Message subject
	 * @param body		Message body
	 */
	private void sendEmail(final String to, final String subject, final String message) {
		List<String> addresses = new ArrayList<String>();

		String[] split = to.split(",");
		for (String string : split) {
			addresses.add(string);
		}

		sendEmail(addresses, subject, message);
	}

	private void sendEmail(final List<String> to, final String subject, final String message) {
		if (!enableMailSending)
			return;
		
		try {
			mailSender.send(new MimeMessagePreparator(){
				@Override
				public void prepare(MimeMessage mimeMessage) throws Exception {
					MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false);

					for (String address : to) {
						helper.addTo(address);
					}

					helper.setSubject(subject);
					helper.setText(message, true);
				}
			});
		} catch (MailException e) {
			log.error(LogUtil.getMarker_notifyAdmin(),
					"Error composing email",e);
		}
	}

	@Loggable(LogLevel.TRACE)
	@Override
	public void enqueueMail(MailMessage message){
		generalDao.save(message);
	}

	@Loggable(LogLevel.TRACE)
	@Override
	public void enqueueMail(String address, String subject, String text){
		generalDao.save(new MailMessage(address, subject, text, AlertType.USER_EMAIL, null));
	}
	
	@Loggable(LogLevel.TRACE)
	@Override
	public void enqueueMail(String address, String subject, String text, File attachment){
		MailMessage message = new MailMessage(address, subject, text, AlertType.USER_EMAIL, null);
		
		copyAttachment(attachment);
		
		message.setAttachments(attachment.getName());
		generalDao.save(message);
	}
	
	@Loggable(LogLevel.TRACE)
	@Override
	public void enqueueMail(String address, String subject, String text, File attachment, String attachmentName){
		MailMessage message = new MailMessage(address, subject, text, AlertType.USER_EMAIL, null);
		
		try {
			File folder = CommunicateHome.getAttachmentsFolder();
			FileUtils.copyFile(attachment, new File(folder.getAbsolutePath() + File.separator + attachmentName));
		} catch (IOException e) {
			log.error("Unable to copy attachment", e);
		}
		
		message.setAttachments(attachmentName);
		enqueueMail(message);
	}

	@Loggable(LogLevel.TRACE)
	@Override
	public void enqueueMail(String address, String subject, String text, List<File> attachments){
		MailMessage message = new MailMessage(address, subject, text, AlertType.USER_EMAIL, null);
		
		List<String> attachementList = new ArrayList<String>();
		for (File file : attachments) {
			copyAttachment(file);
			attachementList.add(file.getName());
		}
		message.setAttachmentList(attachementList);
		enqueueMail(message);
	}
	
	private boolean copyAttachment(File attachment) {
		try {
			File folder = CommunicateHome.getAttachmentsFolder();
			File dest = new File(folder.getAbsolutePath() + File.separator + attachment.getName());
			FileUtils.copyFile(attachment, dest);
			return true;
		} catch (IOException e) {
			log.error("Unable to copy attachment", e);
			return false;
		}
	}

	@Loggable(LogLevel.TRACE)
	@Override
	public void sendResetPasswordEmail(User user, String newPassword){

		String mailTo = user.getEmailAddress();
		String subject = "New Communicate Password";
		String text = templateService.generateContent(MapBuilder.stringObject()
					.put("firstname",user.getFirstName())
					.put("lastname",user.getLastName())
					.put("password",newPassword)
				.getMap(), Templates.NEW_PASSWORD);

		// mail must go straight away so don't save to queue
		sendEmail(mailTo, subject, text);
	}

	@Loggable(LogLevel.TRACE)
	@Override
	public void sendNewUserRequest(User user, String requestType, String requestText){
		String mailTo = settingService.getSettingValue(SettingsEnum.USER_REQUEST_EMAIL);
		String subject = "New Campaign: " + user.getOrganization().getName();
		String text = templateService.generateContent(MapBuilder.stringObject()
					.put("firstName",user.getFirstName())
					.put("lastName",user.getLastName())
					.put("organizationName",user.getOrganization().getName())
					.put("userMsisdn",user.getMsisdn())
					.put("userEmailAddress",user.getEmailAddress())
					.put("requestText",requestText)
					.put("requestType",requestType)
				.getMap(),
						Templates.USER_REQUEST);

		MailMessage message = new MailMessage(mailTo, subject, text, AlertType.USER_EMAIL, user.getOrganization());
		enqueueMail(message);
	}
	
	@Override
	public void sendNewOrganizationNotification(OrganisationNotificationViewModel model) {
		if (model.getTestEmail() != null && !model.getTestEmail().isEmpty()){
			log.debug("Sending test organisation notification\nSubject: {}\nMessage: {}\nEmail: {}",
					new Object[]{model.getSubject(), model.getTestEmail()});
			
			User currentLoggedInUser = userService.getCurrentLoggedInUser();
			
			String subject = "Communicate notification: " + model.getSubject();
			String text = templateService.generateContent(MapBuilder.stringObject()
						.put("contactName",currentLoggedInUser.getFullName())
						.put("organisationName",currentLoggedInUser.getOrganization().getName())
						.put("organisation",currentLoggedInUser.getOrganization())
						.put("notificationText", model.getMessage())
					.getMap(), Templates.NOTIFICATION_EMAIL);
			
			// mail must go straight away so don't save to queue
			sendEmail(model.getTestEmail(), subject, text);
			return;
		}
		
		log.debug("Sending organisation notification\nSubject: {}\nMessage: {}\nOrgs: {}",
				new Object[]{model.getSubject(), model.getMessage(), 
				model.isSendToAll() ? "ALL" : model.getOrganisationList()});
		
		Search search = new Search(Organization.class);
		search.addFilterEqual(Organization.PROP_VOIDED, false);
		if (!model.isSendToAll())
			search.addFilterIn(Organization.PROP_ID, model.getOrganisationList());
		
		@SuppressWarnings("unchecked")
		List<Organization> orgs = generalDao.search(search);
		
		Set<String> emailAddresses = new HashSet<String>();
		List<MailMessage> messages = new ArrayList<MailMessage>();
		for (Organization org : orgs) {
			if (org.getContactEmail() == null || org.getContactEmail().isEmpty()) {
				log.warn("Missing contact email for organisation: {}", org.getName());
				continue;
			}
			
			String email = org.getContactEmail();
			if (emailAddresses.contains(email)){
				continue;
			}
			
			String subject = "Communicate notification: " + model.getSubject();
			String mailto = settingService.getSettingValue(SettingsEnum.USER_REQUEST_EMAIL);
			String text = templateService.generateContent(MapBuilder.stringObject()
						.put("contactName",org.getContactName())
						.put("organisationName",org.getName())
						.put("organisation",org)
						.put("mailTo", mailto)
						.put("notificationText", model.getMessage())
					.getMap(), Templates.NOTIFICATION_EMAIL);

			MailMessage m = new MailMessage(email, subject, text, AlertType.USER_EMAIL, org);
			messages.add(m);
			emailAddresses.add(email);
			
			if (model.isIncludeUsers()){
				List<User> users = userService.getUsersForOrganisation(org);
				for (User user : users) {
					if (user.isVoided()){
						continue;
					}
					
					email = user.getEmailAddress();
					if (emailAddresses.contains(email)){
						continue;
					}
					
					text = templateService.generateContent(MapBuilder.stringObject()
								.put("contactName",user.getFullName())
								.put("organisationName",org.getName())
								.put("organisation",org)
								.put("mailTo", mailto)
								.put("notificationText", model.getMessage())
							.getMap(), Templates.NOTIFICATION_EMAIL);

					m = new MailMessage(email, subject, text, AlertType.USER_EMAIL, org);
					messages.add(m);
					emailAddresses.add(email);
				}
			}
		}
		generalDao.save(messages.toArray());
	}

	@Loggable(LogLevel.TRACE)
	@Override
	public void sendQueuedMail() {
		Search s = new Search(MailMessage.class);
		s.addFilterEqual(MailMessage.PROP_EMAILED, false);
		@SuppressWarnings("unchecked")
		List<MailMessage> messages = generalDao.search(s);

		List<MimeMessagePreparator> msgs = new ArrayList<MimeMessagePreparator>(messages.size());
		for (final MailMessage message : messages) {
			if (enableMailSending) {
				msgs.add(new MimeMessagePreparator() {
					@Override
					public void prepare(MimeMessage mimeMessage) throws Exception {
						MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, 
							message.hasAttachements());
						
						String[] addresses = message.getAddress().split(",");
						helper.setTo(addresses);
						
						helper.setSubject(message.getSubject() + " (" + MobilisrUtility.getHostname() + ")");
						helper.setText(message.getText(), true);
						
						List<String> attachmentList = message.getAttachmentList();
						if (!attachmentList.isEmpty()){
							
							File folder = CommunicateHome.getAttachmentsFolder();
							for (String attachment : attachmentList) {
								String path = folder.getAbsolutePath() + File.separator + attachment;
								File file = new File(path);
								if (file.exists() && file.isFile()){
									helper.addAttachment(file.getName(), file);
								}
							}
						}
					}
				});
			}
			message.setEmailed(true);
			generalDao.saveOrUpdate(message);
		}

		if (!msgs.isEmpty()) {
			mailSender.send(msgs.toArray(new MimeMessagePreparator[messages.size()]));
		}
	}

	@Loggable(LogLevel.TRACE)
	@Override
	public void sendBalanceAlert(User user, String action, int requestedAmount, Organization organization) {
		String contactEmail = organization.getContactEmail();
		if (contactEmail == null || contactEmail.isEmpty()){
			log.warn("Empty contact email for organisation [{}]", organization.getName());
			return;
		}

		String subject = "Communicate: insufficient balance alert for " + organization.getName();
		String message = templateService.generateContent(MapBuilder.stringObject().put("action", action)
						.put("requestedAmount", requestedAmount)
						.put("organization", organization).getMap(),
						Templates.INSUFFICIENT_BALANCE);
		MailMessage alert = new MailMessage(contactEmail, subject, message, AlertType.BALANCE_RUNTIME, organization);
		enqueueMail(alert);
	}


	@Loggable(LogLevel.TRACE)
	@Override
	public void sendBalanceLowAlert(Organization organization){
		String contactEmail = organization.getContactEmail();
		if (contactEmail == null || contactEmail.isEmpty()){
			log.warn("Empty contact email for organisation [{}]", organization.getName());
			return;
		}

		Date midnight = MobilisrUtility.getBeginningOfDay(new Date());

		Search s = new Search(MailMessage.class);
		s.addFilterEqual(MailMessage.PROP_TYPE, AlertType.BALANCE_RUNTIME);
		s.addFilterEqual(MailMessage.PROP_ORGANIZATION, organization);
		s.addFilterGreaterThan(MailMessage.PROP_DATETIME, midnight);
		List<?> search = generalDao.search(s);
		if (search.isEmpty()){
			// only send one alert per day
			String subject = "Communicate: balance low alert for " + organization.getName();
			String message = templateService.generateContent(MapBuilder.stringObject()
							.put("organization", organization).getMap(),
							Templates.BALANCE_LOW);
			MailMessage alert = new MailMessage(contactEmail,subject, message, AlertType.BALANCE_RUNTIME, organization);
			enqueueMail(alert);
		}
	}

	@Loggable(LogLevel.TRACE)
	@Override
	public void sendSystemAlert(String message) {
		log.warn("SYSTEM ALERT: " + message);

		String mailto = settingService.getSettingValue(SettingsEnum.SYSTEM_NOTIFICATIONS_EMAIL);

		if (mailto == null || mailto.isEmpty()){
			log.warn("Empty system alert email");
			return;
		}

		String subject = "Communicate: system alert";
		MailMessage alert = new MailMessage(mailto, subject, message, AlertType.SYSTEM_ALERT, null);
		enqueueMail(alert);
	}
	
	@Override
	public void sendCreditNotification(Organization organization, User user,
			int amount, String transactionMessage) {
		String address = settingService.getSettingValue(SettingsEnum.CREDIT_NOTIFICATIONS_EMAIL);
		if (address == null || address.isEmpty()){
			return;
		}
		
		String subject = "Communicate notification: credit added to "
			+ organization.getName() + " by " + user.getFullName();
		String message = templateService.generateContent(MapBuilder.stringObject()
						.put("amount", amount)
						.put("transactionMessage", transactionMessage)
						.put("organization", organization)
						.put("user", user)
						.getMap(),
						Templates.CREDIT_NOTIFICATION);
		MailMessage alert = new MailMessage(address, subject, message, AlertType.BALANCE_RUNTIME, null);
		enqueueMail(alert);
	}
	
	public void enableMailSending(boolean enableMailSending) {
		this.enableMailSending = enableMailSending;
	}
}
