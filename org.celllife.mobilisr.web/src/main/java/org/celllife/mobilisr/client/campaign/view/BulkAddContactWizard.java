package org.celllife.mobilisr.client.campaign.view;

import org.celllife.mobilisr.client.MobilisrEvents;
import org.celllife.mobilisr.client.reporting.EntityStoreProvider;
import org.celllife.mobilisr.client.reporting.PconfigParamterFieldFactory;
import org.celllife.mobilisr.client.view.gxt.MyGXTRadio;
import org.celllife.mobilisr.client.view.gxt.StepValidator;
import org.celllife.mobilisr.client.view.gxt.WizardCard;
import org.celllife.mobilisr.client.view.gxt.WizardStepEvent;
import org.celllife.mobilisr.client.view.gxt.WizardWindow;
import org.celllife.mobilisr.domain.CampaignContact;
import org.celllife.mobilisr.domain.ContactGroup;
import org.celllife.pconfig.model.EntityParameter;

import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FormEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.FileUploadField;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.RadioGroup;
import com.extjs.gxt.ui.client.widget.form.FormPanel.Encoding;
import com.extjs.gxt.ui.client.widget.form.FormPanel.Method;
import com.extjs.gxt.ui.client.widget.form.Validator;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class BulkAddContactWizard extends WizardWindow {

	public enum BulkType {
		ALL,GROUP,CSV_FILE;
	}
	
	private final CampaignContact contact;
	private long numRecords;
	private String path;
	private Long selGroup;
	private EntityStoreProvider store;
	private boolean startOver;

	public BulkAddContactWizard(CampaignContact contact, EntityStoreProvider store, BulkType type) {	
		super();
		
		this.contact = contact;		
		this.store = store;
		this.startOver = false;
		setHideOnFinish(true);
		
		switch (type) {
		case ALL:
			break;
		case GROUP:
			addGroupsContactsCard();
			break;
		case CSV_FILE:
			addCSVContactsCard();
			break;
		default:
			break;
		}
		
		if (contact.getContactMsgTimes() != null && !contact.getContactMsgTimes().isEmpty()){
			addMessageTimesCard();
		}		
		
		addStartOverCard();
	}

	private void addMessageTimesCard() {
		WizardCard card = new WizardCard("Contact Message Times");
		card.setHtmlText("<p>Enter the times at which this contact will get the campaign messages.</p>");
		
		final FormPanel panel = new FormPanel();
		panel.setLabelWidth(150);
		new CampaignContactForm(panel, contact);				
		card.setFormPanel(panel);		
		addCard(card);	
	}

	private void addCSVContactsCard() {
		WizardCard contactCard = new WizardCard("Add Contacts from CSV File");
		contactCard.setHtmlText("The CSV File must have three columns: First Name, Last Name and Contact Number" + 
								"<br>" + "<br>" +
								"Select the CSV File to upload from:");	
		
		final FormPanel panel = new FormPanel();		
		panel.setLabelWidth(150);		
		panel.setLayout(new RowLayout());
		panel.setEncoding(Encoding.MULTIPART);
		panel.setMethod(Method.POST);
		panel.setAction(GWT.getModuleBaseURL() + "readCsv");
		
		final FileUploadField newField = new FileUploadField();
		newField.setWidth(250);
		newField.setName("csvFile");
		newField.setId("csvFile");
		newField.setFieldLabel("File");
		newField.setValidator(new Validator() {
			@Override
			public String validate(Field<?> field, String value) {
				String fileNm = value.toLowerCase();
				if (!fileNm.endsWith(".csv")) {
					return "Only .csv files are accepted";
				}
				return null;
			}
		});
		panel.add(newField);
		
		contactCard.setValidator(new StepValidator() {
			@Override
			public void validate(WizardCard wizardCard, int currentStep, final AsyncCallback<Void> callback) {
				panel.addListener(Events.Submit, new Listener<FormEvent>() {

					@Override
					public void handleEvent(FormEvent fe) {
						String data = fe.getResultHtml();
						String[] split = data.split(";");
						path = split[0];
						numRecords = Long.parseLong(split[1]);
						callback.onSuccess(null);
					}
				});
				panel.submit();
			}
		});
		
		contactCard.setFormPanel(panel);		
		addCard(contactCard);
	}
	
	private void addGroupsContactsCard() {

		WizardCard groupCard = new WizardCard("Group To Add");
		groupCard.setHtmlText("Select the group to add.");

		final FormPanel panel = new FormPanel();
		panel.setLabelWidth(150);
		panel.setLayout(new RowLayout());

		final EntityParameter group = new EntityParameter("group", "Group:");
		group.setDisplayProperty(ContactGroup.PROP_GROUP_NAME);
		group.setValueProperty(ContactGroup.PROP_ID);
		group.setEntityClass(ContactGroup.class.getName());

		Field<?> field = null;
		field = PconfigParamterFieldFactory.getField(group, store, false);
		field.setWidth(300);
		field.setFieldLabel("Group Name"); // FIXME why does this label not show?
		panel.add(field, new RowData(1, 1, new Margins(5)));

		groupCard.addListener(MobilisrEvents.WizardStep, new Listener<WizardStepEvent>() {
			@Override
			public void handleEvent(WizardStepEvent we) {
				selGroup = Long.valueOf(group.getValue());
			}
		});

		groupCard.setFormPanel(panel);
		addCard(groupCard);

	}
	
	private void addStartOverCard() {
		
		WizardCard startOverCard = new WizardCard("Campaign Progress");
		startOverCard.setHtmlText("If any contacts had previously participated in this campaign, would you like them to resume from where they left off?");
		
		RadioGroup startOverGroup = new RadioGroup("startOverYesNoGroup");
		
		final MyGXTRadio startOverButton = new MyGXTRadio("Start Over. This option will start the new recipient from Day 0.");
		MyGXTRadio resumeButton = new  MyGXTRadio("Resume. This option will resume from where the recipient left off.");
		
		startOverGroup.add(startOverButton);
		startOverGroup.add(resumeButton);
		startOverGroup.setValue(resumeButton);
		
		FormPanel panel = new FormPanel();
		panel.setLabelWidth(150);	
		panel.setLayout(new RowLayout());
		panel.add(resumeButton);
		panel.add(startOverButton);
		panel.setLabelSeparator("");
		
		startOverCard.setFormPanel(panel);
		
		startOverCard.addListener(MobilisrEvents.WizardStep, new Listener<WizardStepEvent>() {
			@Override
			public void handleEvent(WizardStepEvent we) {
				
				if (startOverButton.getValue()){
					startOver = true;
				}				
			}
		});
		
		addCard(startOverCard);
		
	}
	
	public long getNumRecords() {
		return numRecords;
	}
	
	public String getPath() {
		return path;
	}

	public Long getSelGroup() {
		return selGroup;
	}
	
	public Boolean getStartOver() {
		return startOver;
	} 
	
}
