package org.celllife.mobilisr.client.campaign.view;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.celllife.mobilisr.client.Messages;
import org.celllife.mobilisr.client.Resources;
import org.celllife.mobilisr.client.UserContext;
import org.celllife.mobilisr.client.campaign.JustSMSCreateView;
import org.celllife.mobilisr.client.model.ViewModel;
import org.celllife.mobilisr.client.template.view.EntityCreateTemplateImpl;
import org.celllife.mobilisr.client.view.gxt.MyGXTButton;
import org.celllife.mobilisr.client.view.gxt.MyGXTDateField;
import org.celllife.mobilisr.client.view.gxt.MyGXTSmsBox;
import org.celllife.mobilisr.client.view.gxt.MyGXTTextField;
import org.celllife.mobilisr.client.view.gxt.MyGXTTimeField;
import org.celllife.mobilisr.constants.CampaignStatus;
import org.celllife.mobilisr.constants.CampaignType;
import org.celllife.mobilisr.domain.Campaign;
import org.celllife.mobilisr.domain.CampaignMessage;
import org.celllife.mobilisr.domain.User;

import com.extjs.gxt.ui.client.Style.ButtonScale;
import com.extjs.gxt.ui.client.Style.IconAlign;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.AdapterField;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.FieldSet;
import com.extjs.gxt.ui.client.widget.form.FormButtonBinding;
import com.extjs.gxt.ui.client.widget.form.LabelField;
import com.extjs.gxt.ui.client.widget.form.Time;
import com.extjs.gxt.ui.client.widget.form.Validator;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;

public class JustSMSCreateViewImpl extends EntityCreateTemplateImpl<Campaign> implements JustSMSCreateView {

	private Button campaignListButton;
	private MyGXTTextField campaignNameField;
	private MyGXTButton manageRecipientsButton;
	private LabelField numContactsLabel;
	private MyGXTDateField sendDateField;
	private MyGXTTimeField sendTimeField;
	private MyGXTSmsBox smsBox;

	private Campaign campaign;

	private FieldSet fieldSet;

	@Override
	public void createView() {
		super.createView();
		
		campaignListButton = new MyGXTButton("campaignListButton",
				"Show 'Just Send SMS' Campaign list", Resources.INSTANCE.folderTable(), IconAlign.LEFT,
				ButtonScale.SMALL);
		
		submitButton.setText(Messages.INSTANCE.wizardSaveAndContinue());
		cancelButton.setText(Messages.INSTANCE.clear());

		layoutCreateTemplate(Messages.INSTANCE.justSmsCreateHeader());
		
		campaignNameField = new MyGXTTextField(Messages.INSTANCE.compulsory() + 
				Messages.INSTANCE.campaignName(),
				Campaign.PROP_NAME, false, "");
		campaignNameField.setId("campaignName");
		campaignNameField.setMinLength(1);
		campaignNameField.setMaxLength(100);
		addDirtyListenerToField(campaignNameField);
		
		manageRecipientsButton = new MyGXTButton("selectContacts",
				Messages.INSTANCE.campaignSelectContacts());
		new FormButtonBinding(formPanel){
			protected boolean checkPanel() {
				 boolean v = formPanel.isValid(true);
				     if (v != manageRecipientsButton.isEnabled()) {
				    	 manageRecipientsButton.setEnabled(v);
				    	 manageRecipientsButton.setText(v ? Messages.INSTANCE.campaignSelectContacts() : 
				    		 Messages.INSTANCE.campaignSelectContacts() + 
				    		 " (complete compulsory fields to enable)");
				     }
				    return v;
			};
		};
		
		numContactsLabel = new LabelField("0 contact(s) selected");
		numContactsLabel.setId("numContactsLabel");
		
		sendDateField = new MyGXTDateField(Messages.INSTANCE.compulsory() + 
				"Date to send", CampaignMessage.PROP_MSG_DATE, false, false,
				new Date());
		sendDateField.setId("sendDate");
		addDirtyListenerToField(sendDateField);
		
		sendTimeField = new MyGXTTimeField(Messages.INSTANCE.compulsory() + 
				"Time to send", CampaignMessage.PROP_MSG_TIME, 30, false, true,
				false);
		sendTimeField.setId("sendTime");
		addDirtyListenerToField(sendTimeField);
		
		fieldSet = new FieldSet();
		fieldSet.setId("setDateTime");
		fieldSet.setHeading("Set time to send message");
		fieldSet.setCollapsible(true);
		fieldSet.setCheckboxToggle(true);
		fieldSet.collapse();

		FormLayout fieldSetlayout = new FormLayout();
		fieldSetlayout.setLabelWidth(150);
		fieldSetlayout.setDefaultWidth(300);
		fieldSet.setLayout(fieldSetlayout);

		fieldSet.add(sendDateField, new FormData("-20"));
		fieldSet.add(sendTimeField, new FormData("-20"));
		fieldSet.addListener(Events.Expand, new Listener<BaseEvent>() {
			@Override
			public void handleEvent(BaseEvent be) {
				enableDateTime(true);
			}
		});
		fieldSet.addListener(Events.Collapse, new Listener<BaseEvent>() {
			@Override
			public void handleEvent(BaseEvent be) {
				enableDateTime(false);
			}
		});

		smsBox = new MyGXTSmsBox(true);
		smsBox.getToolBar().setId("smsBoxToolBar");
		addDirtyListenerToField(smsBox.getMsgTxtArea());

		// initialize form elements
		initialize();

		// make the form logic, and add the form to the root container
		createForm();
		

		Map<Button, Boolean> formButtons = new LinkedHashMap<Button, Boolean>();
		formButtons.put(submitButton, true);
		formButtons.put(cancelButton, false);
		addAndConfigFormButtons(formButtons, false);
		
	}
	
	@Override
	protected void addTitleLabel(String titleLabelText) {
		super.addTitleLabel(titleLabelText);
		add(campaignListButton, new RowData(240,-1, new Margins(10)));
	}

	private void initialize() {
		// Validate that the selected date/time is at least 15mins from current date/time
		sendTimeField.setValidator(new Validator() {

			@Override
			public String validate(Field<?> field, String value) {
				Date now = new Date();
				if (sendDateField.getValue().before(now)) {
					Time timeValue = sendTimeField.getValue();
					int hour = timeValue.getHour();
					int minute = timeValue.getMinutes();

					DateTimeFormat dtf = DateTimeFormat.getFormat("HH");
					String hf = dtf.format(now);
					DateTimeFormat dtmf = DateTimeFormat.getFormat("mm");
					String mf = dtmf.format(now);
					int presentHour = Integer.parseInt(hf);
					int presentMinute = Integer.parseInt(mf);
					// check to validate selected time is at least 15 mins from now
					if ((hour - presentHour) * 60 + minute - presentMinute < 15) {
						return (Messages.INSTANCE.justSmsTimeTooSoon());
					}
				}
				return null;
			}
		});
	}

	private void createForm() {
		formElementContainer.setBorders(false);
		formElementContainer.add(campaignNameField);
		formElementContainer.add(smsBox.getMsgTxtArea());
		formElementContainer.add(new AdapterField(smsBox.getToolBar()));

		AdapterField contactsBtnAdapter = new AdapterField(manageRecipientsButton);
		contactsBtnAdapter.setId("contactsBtnAdapter");
		formElementContainer.add(contactsBtnAdapter);

		formElementContainer.add(numContactsLabel);
		FormData data = new FormData();
		data.setMargins(new Margins(0,0,0,-10));
		formElementContainer.add(fieldSet, data);
	}
	
	protected void configureFormElementContainer() {
		FormLayout formLayout = new FormLayout();
		formLayout.setLabelSeparator("");
		formLayout.setLabelWidth(150);
		formLayout.setDefaultWidth(300);
		formElementContainer.setLayout(formLayout);
		formElementContainer.setAutoWidth(true);
		formElementContainer.setAutoHeight(true);
	}

	private void updateCampaignMessage() {
		Date dateValue;
		Date timeValue;
		if (!fieldSet.isExpanded()) {
			dateValue = new Date();
			timeValue = dateValue;
		} else {
			dateValue = sendDateField.getValue();
			timeValue = sendTimeField.getDateValue();
		}
		List<CampaignMessage> messages = campaign.getCampaignMessages();
		String smsMsg = smsBox.getSmsMsg();
		smsMsg = smsMsg == null ? "" : smsMsg;
		if (messages != null && !messages.isEmpty()){
			CampaignMessage message = messages.get(0);
			message.setMessage(smsMsg);
			message.setMsgDate(dateValue);
			message.setMsgTime(timeValue);
		} else {
			if (messages == null){
				messages = new ArrayList<CampaignMessage>();
			}
			CampaignMessage message = new CampaignMessage(smsMsg,
					dateValue, timeValue, campaign);
			messages.add(message);
			campaign.setCampaignMessages(messages);
		}
	}

	private Date getStartDate() {
		if(!fieldSet.isExpanded()){
			return new Date();
		}else{
			return sendDateField.getValue() == null ? new Date() : sendDateField.getValue();
		}
	}

	@Override
	public Campaign getCampaign() {
		campaign.setType(CampaignType.FIXED);
		if(!campaign.isPersisted()){
			campaign.setStatus(CampaignStatus.INACTIVE);
		}
		campaign.setTimesPerDay(1);
		User user = UserContext.getUser();

		campaign.setName(campaignNameField.getValue());
		campaign.setStartDate(getStartDate());
		campaign.setOrganization(user.getOrganization());
		campaign.setSendNow(!fieldSet.isExpanded());

		updateCampaignMessage();

		campaign.setCost(new Long(campaign.getContactCount() * smsBox.getNumMesgs()).intValue());
		// campaign only has one message
		campaign.setDuration(1);
		return campaign;
	}

	public MyGXTButton getCancelButton() {
		return cancelButton;
	}

	@Override
	public void setFormObject(ViewModel<Campaign> viewEntityModel) {
		campaign = viewEntityModel.getModelObject();
		super.setFormObject(viewEntityModel);
		if (!campaign.isPersisted()) {
			clearUnboundFields();
		} else {
			fillUnboundFields();
		}
		campaignNameField.focus();
		submitButton.setText(Messages.INSTANCE.wizardSaveAndContinue());
	}

	private void clearUnboundFields() {
		numContactsLabel.setText("0 contact(s) selected");
		DateTimeFormat format = DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_MEDIUM);
		campaignNameField.setValue(format.format(new Date()));
		campaignNameField.selectAll();
		smsBox.resetSmsBox();
		fieldSet.collapse();
		sendTimeField.clear();
		sendDateField.clear();
		sendTimeField.disable();
		sendDateField.disable();
	}

	private void fillUnboundFields() {
		List<CampaignMessage> messages = campaign.getCampaignMessages();
		CampaignMessage message = messages.get(0);

		Date date = message.getMsgDate();
		enableDateTime(!campaign.isSendNow());
		if(campaign.isSendNow()) {
			fieldSet.collapse();
			sendTimeField.clear();
			sendDateField.clear();
		} else {
			fieldSet.expand();
			//pull out date and time
			sendDateField.setValue(date);
			Date timeValue = message.getMsgTime();
			sendTimeField.setDateValue(timeValue);
		}
		numContactsLabel.setText(campaign.getContactCount() + " contact(s) selected");
		campaignNameField.setValue(campaign.getName());
		smsBox.setSmsMsg(message.getMessage());
		numContactsLabel.setText(campaign.getContactCount() + " contact(s) selected");
		if(campaign.isRunning()){
			submitButton.disable();
			manageRecipientsButton.disable();
		}
	}

	private void enableDateTime(boolean enable) {
		sendDateField.clearInvalid();
		sendDateField.setEnabled(enable);
		sendDateField.setAllowBlank(!enable);

		sendTimeField.clearInvalid();
		sendTimeField.setEnabled(enable);
		sendTimeField.setAllowBlank(!enable);
	}

	@Override
	public Button getManageRecipientsButton() {
		return manageRecipientsButton;
	}
	
	@Override
	public Button getCampaignListButton(){
		return campaignListButton;
	}
}
