package org.celllife.mobilisr.client;

import org.celllife.mobilisr.constants.ChannelType;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.LocalizableResource.DefaultLocale;

@DefaultLocale("en")
public interface Messages extends com.google.gwt.i18n.client.Messages{

	public static final Messages INSTANCE =  GWT.create(Messages.class);

	@Key("campaign.action.add_new")
	String campaignAddNew();

	@Key("campaign.error.change_message_per_day_title")
	String campaignChangeMsgsPerDayErrorTitle();

	@Key("campaign.error.change_message_per_day")
	String campaignChangeMsgsPerDayError();

	@Key("campaign.list.header")
	String campaignListHeader();

	@Key("campaign.list.header_admin")
	String campaignListHeaderAdmin();

	@Key("campaign.message_log")
	String campaignMessageLog();

	@Key("campaign.label.name")
	String campaignName();

	@Key("campaign.label.progress")
	String campaignProgress();

	@Key("campaign.label.recipients")
	String campaignRecipients();

	@Key("campaign.warning.regenerate_messages")
	String campaignRegenerateMessagesWarning();

	@Key("campaign.warning.regenerate_messages_title")
	String campaignRegenerateMessagesTitle();
	
	@Key("campaign.action.request_new")
	String campaignRequestNew();

	@Key("campaign.action.select_contacts")
	String campaignSelectContacts();

	@Key("campaign.action.select_groups")
	String campaignSelectGroups();

	@Key("campaign.action.view_recipients")
	String campaignViewRecipients();

	@Key("label.cancel")
	String cancel();
	
	@Key("channel.confirmation.title.activate")
	String channelConfirmationActivateTitle();

	@Key("channel.confirmation.message.activate_out")
	String channelConfirmationActivateOutMessage();

	@Key("channel.action.add_new")
	String channelAddNew(ChannelType type);
	
	@Key("channelconfig.action.add_new")
	String channelConfigAddNew();
	
	@Key("channelconfig.list.header")
	String channelConfigListHeader();
	
	@Key("channelconfig.label.name")
	String channelConfigName();
	
	@Key("channel.confirmation.title.deactivate")
	String channelConfirmationDeactivateTitle();
	
	@Key("channel.confirmation.message.deactivate_in")
	String channelConfirmationDeactivateInMessage();

	@Key("channel.create.header")
	String channelCreateHeader(ChannelType type);

	@Key("channel.list.header")
	String channelListHeader();

	@Key("channel.message.save_success")
	String channelSaveSucess(String channelName);

	@Key("channel.label.name")
	String channelName();

	@Key("channel.label.name_example")
	String channelNameExample();

	@Key("channel.label.shortcode")
	String channelShortcode();
	
	@Key("channel.label.shortcode_example")
	String channelShortcodeExample();
	
	@Key("channel.warning.title.active_filters")
	String channelWarningActiveFiltersTitle();

	@Key("channel.warning.message.active_filters")
	String channelWarningActiveFiltersMessage(Integer num);
	
	@Key("label.clear")
	String clear();
	
	@Key("label.compulsory_marker")
	String compulsory();

	@Key("contact.action.add_new")
	String contactAddNew();

	@Key("contact.action.add_to_all_groups")
	String contactAddToAllGroups();

	@Key("contact.create.header")
	String contactCreateHeader();

	@Key("contact.label.first_name")
	String contactFirstName();

	@Key("contact.label.last_name")
	String contactLastName();

    @Key("contact.label.invalid")
    String invalid();

	@Key("contact.list.header")
	String contactListHeader();

	@Key("contact.list.header.for_group")
	@Meaning("The header of the contact list when it is only showing contacts in a specific group")
	String contactListHeader_for_group(String groupName);

	@Key("contact.action.manage_groups")
	String contactManageGroups();

	@Key("contact.label.mobile_network")
	String contactMobileNetwork();

	@Key("contact.label.mobile_number")
	String contactMobileNumber();
	
	@Key("contact.action.remove_all_groups")
	String contactRemoveAllGroups();

	@Key("delete")
	String delete();

	@Key("dialog.title.unsaved_changes")
	String dialogUnsavedChangesTitle();

	@Key("dialog.message.unsaved_changes")
	String dialogUnsavedChangesMessage();

	@Key("label.done")
	String done();
	
	@Key("label.end_date")
	String endDate();

	@Key("filter.label.action")
	String filterAction();

	@Key("filter.label.action_details")
	String filterActionDetails();

	@Key("filter.actions")
	String filterActions();

	@Key("filter.action.add_new")
	String filterAddNew();

	@Key("filter.action.add_new_action")
	String filterAddNewAction();

	@Key("filter.label.channel")
	String filterChannel();

	@Key("filter.column.channel")
	String filterColumnChannel();

	@Key("filter.column.name")
	String filterColumnName();

	@Key("filter.column.type")
	String filterColumnType();

	@Key("filter.create.header")
	String filterCreateHeader();

	@Key("filter.action.deactivate")
	String filterDeactivate();

	@Key("filter.list.header")
	String filterListHeader();

	@Key("filter.list.header_admin")
	String filterListHeaderAdmin();

	@Key("filter.label.filter_management")
	String filterManagement();

	@Key("filter.label.name")
	String filterName();

	@Key("filter.label.organisation")
	String filterOrganisation();

	@Key("filter.action.request_new")
	String filterRequestNew();

	@Key("filter.label.search")
	String filterSearch();

	@Key("filter.label.type")
	String filterType();

	@Key("filter.action.view_inbox")
	String filterViewInbox();

	@Key("footer.label")
	String footerLabel(String version);

	@Key("group.action.add")
	String groupAdd();

	@Key("group.action.add_new")
	String groupAddNew();

	@Key("group.create.header")
	String groupCreateHeader();

	@Key("group.label.description")
	String groupDescription();

	@Key("group.list.header")
	String groupListHeader();

	@Key("group.action.manage_contacts")
	String groupManageContacts();

	@Key("group.label.name")
	String groupName();

	@Key("import_contact.heading")
	String importContactHeading();

	@Key("just_sms.create.header")
	String justSmsCreateHeader();

	@Key("just_sms.list.header")
	String justSmsListHeader();

	@Key("just_sms.list.header_admin")
	String justSmsListHeaderAdmin();

	@Key("just_sms.label.send_at_date")
	String justSmsSendAtDate();

	@Key("just_sms.label.send_now")
	String justSmsSendNow();

	@Key("just_sms.error.time_to_soon")
	String justSmsTimeTooSoon();

	@Key("lost_messages.header")
	String lostMessagesHeader();

	@Key("lost_messages.action.reprocess")
	String lostMessagesReprocess();

	@Key("manage_recipients.available")
	String manageRecipientsAvailable();

	@Key("manage_recipients.selected")
	String manageRecipientsSelected();

	@Key("manage_recipients.bulkadd")
	String manageRecipientsBulkAdd();

	@Key("manage_recipients.bulkremove")
	String manageRecipientsBulkRemove();

	@Key("menu.campaigns")
	String menuCampaigns();

	@Key("menu.dashboard")
	String menuDashboard();

	@Key("menu.filters")
	String menuFilters();

	@Key("menu.just_sms")
	String menuJustSms();

	@Key("menu.settings")
	String menuSettings();
	
	@Key("numberinfo.list.header")
	String numberInfoListHeader();
	
	@Key("numberinfo.action.add_new")
	String numberInfoAddNew();

	@Key("numberinfo.label.name")
	String numberInfoName();
	
	@Key("numberinfo.label.prefix")
	String numberInfoPrefix();
	
	@Key("numberinfo.label.regex")
	String numberInfoRegex();
	
	@Key("numberinfo.label.channel")
	String numberInfoChannel();
	
	@Key("numberinfo.confirmation.title.deactivate")
	String numberInfoConfirmationDeactivateTitle();

	@Key("numberinfo.confirmation.message.deactivate")
	String numberInfoConfirmationDeactivateMessage(String prefix);
	
	@Key("organisation.action.add_new")
	String orgAddNew();

	@Key("organisation.label.address")
	String orgAddress();

	@Key("organisation.label.balance_threshold")
	String orgBalanceThreshold();

	@Key("organisation.label.contact_person_email")
	String orgContactEmail();

	@Key("organisation.label.contact_person_name")
	String orgContactName();

	@Key("organisation.label.contact_person_number")
	String orgContactNumber();

	@Key("organisation.create.header")
	String orgCreateHeader();

	@Key("organisation.action.credit_account")
	String orgCreditAccount();

	@Key("organisation.label.credits")
	String orgCredits();

	@Key("organisation.label.current_balance")
	String orgCurrentBalance();

	@Key("organisation.list.adjust_balance_message")
	String orgListAdjustBalanceMessage(int amount, int newBalance);

	@Key("organisation.list.header")
	String orgListHeader();

	@Key("organisation.label.name")
	String orgName();

	@Key("organisation.action.send_notification")
	String orgSendNotification();

	@Key("report.action.generate")
	String reportGenerate();

	@Key("report.generated.header")
	String reportGeneratedHeader(String reportName);

	@Key("repport.list.header")
	String reportListHeader();

	@Key("report.label.name")
	String reportName();

	@Key("report.action.reload_cache")
	String reportReloadCache();

	@Key("report.scheduled.header")
	String reportScheduledHeader(String reportName);

	@Key("report.scheduled.header_all")
	String reportScheduledHeaderAll();

	@Key("role.action.add_new")
	String roleAddNew();

	@Key("role.create.header")
	String roleCreateHeader();
	
	@Key("user.action.delete_selected")
	String roleDeleteSelected();

	@Key("role.list.header")
	String roleListHeader();

	@Key("role.label.name")
	String roleName();

	@Key("role.label.permissions")
	String rolePermissions();

	@Key("save")
	String save();

	@Key("security.access_denied")
	String securityAccessDenied(String reason);

	@Key("send")
	String send();
	
	@Key("sending")
	String sending();
	
	@Key("send_test_message")
	String sendTestMessage();

	@Key("setting.create.header")
	String settingCreateHeader(String settingName);

	@Key("setting.list.header")
	String settingListHeader();

	@Key("smsbox.label.characters_left")
	String smsboxCharsLeft(@PluralCount int count);

	@Key("smsbox.default.message_text")
	String smsboxDefaultMessageText();

	@Key("smsbox.label.message")
	String smsboxMessage();

	@Key("smsbox.lable.num_messages")
	String smsboxMessage(@PluralCount int count);

	@Key("label.start_date")
	String startDate();

	@Key("update")
	String update();

	@Key("user.action.add_new")
	String userAddNew();
	
	@Key("user.action.add_new_apikey")
	String userAddNewApiKey();

	@Key("user.action.assign_roles")
	String userAssignRoles();

	@Key("user.label.confirm_password")
	String userConfirmPassword();

	@Key("user.create.header")
	String userCreateHeader();

	@Key("user.lable.email_address")
	String userEmailAddress();

	@Key("user.label.first_name")
	String userFirstName();

	@Key("user.label.last_name")
	String userLastName();

	@Key("user.list.header")
	String userListHeader();

	@Key("user.label.mobile_number")
	String userMobileNumber();

	@Key("user.label.new_password")
	String userNewPassword();

	@Key("user.label.organisation")
	String userOrganisation();

	@Key("user.label.password")
	String userPassword();

	@Key("user.label.username")
	String userUsername();

	@Key("validation.email")
	String validationEmail();

	@Key("wizard.back")
	String wizardBack();

	@Key("wizard.next")
	String wizardNext();

	@Key("wizard.save_and_continue")
	String wizardSaveAndContinue();

    @Key("linkedCampaign.label.name")
    String linkedCampaignLabelName();

}