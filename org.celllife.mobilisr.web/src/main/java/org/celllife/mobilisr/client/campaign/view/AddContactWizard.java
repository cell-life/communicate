package org.celllife.mobilisr.client.campaign.view;

import java.util.Date;

import org.celllife.mobilisr.client.MobilisrEvents;
import org.celllife.mobilisr.client.contacts.view.ContactForm;
import org.celllife.mobilisr.client.view.gxt.FormUtil;
import org.celllife.mobilisr.client.view.gxt.ModelUtil;
import org.celllife.mobilisr.client.view.gxt.MyGXTRadio;
import org.celllife.mobilisr.client.view.gxt.StepValidator;
import org.celllife.mobilisr.client.view.gxt.WizardCard;
import org.celllife.mobilisr.client.view.gxt.WizardStepEvent;
import org.celllife.mobilisr.client.view.gxt.WizardWindow;
import org.celllife.mobilisr.domain.CampaignContact;

import com.extjs.gxt.ui.client.binding.FormBinding;
import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.data.ChangeEvent;
import com.extjs.gxt.ui.client.data.ChangeListener;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.RadioGroup;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;

public class AddContactWizard extends WizardWindow {
	
	private ContactForm contactForm;
	private FormBinding contactBindings;
	private WizardCard contactCard;
	private CampaignContact contact;
	private WizardCard messageTimesCard;

	public AddContactWizard(final CampaignContact contact, boolean isAdd) {
		super();
		this.contact = contact;
		
		/*
		 *  only show the contact card when 
		 *  	editing existing campaign contacts or
		 *  	adding new contacts
		 */
		if (!contact.getContact().isPersisted() 
				|| (contact.isPersisted() && contact.getEndDate() == null) ){
			addContactCard();
			contactForm.getMsisdnField().setEnabled(!contact.getContact().isPersisted());
			BeanModel contactModel = ModelUtil.convertEntityToBeanModel(contact.getContact());
			contactModel.addChangeListener(new ChangeListener() {
				@Override
				public void modelChanged(ChangeEvent event) {
					updateMessageTimesCardHeader(contact);
				}
			});
			contactBindings.bind(contactModel);
		}
		
		if (contact.getContactMsgTimes() != null && !contact.getContactMsgTimes().isEmpty()){
			addMessageTimesCard(contact);
		}
		
		if (isAdd && contact.getProgress() > 0) {
			addStartOverCard();
		}
	}

	private void addMessageTimesCard(CampaignContact contact) {
		messageTimesCard = new WizardCard("Contact message times");
		updateMessageTimesCardHeader(contact);
		FormPanel panel = new FormPanel();
		panel.setLabelWidth(150);		
		
		new CampaignContactForm(panel, contact);		
		messageTimesCard.setFormPanel(panel);
		addCard(messageTimesCard);
	}

	/**
	 * @param contact
	 */
	private void updateMessageTimesCardHeader(CampaignContact contact) {
		messageTimesCard.setHtmlText("<p><b>"+contact.getContact().toString()+"</b></p>" +
				"<p>Enter the times at which this contact will get the campaign messages</p>");
	}

	private void addContactCard() {
		contactCard = new WizardCard("Contact details");
		contactCard.setHtmlText("<p>Enter the contact details for the new contact</p>" +
				"<p>To change the <i>mobile number</i> open the contact in the contact editor.</p>");
		FormPanel panel = new FormPanel();
		panel.setLabelWidth(150);
		contactForm = new ContactForm(panel);
		contactForm.showAnchor(false);
		
		contactBindings = FormUtil.createFormBinding(panel, true);
		
		contactCard.setFormPanel(panel);
		
		addCard(contactCard);
	}
	
	private void addStartOverCard() {
		
		contactCard = new WizardCard("Campaign Progress");
		Date date = contact.getEndDate();
		String endDate = "Unknown";
		if (date != null){
			endDate = DateTimeFormat.getFormat(PredefinedFormat.DATE_MEDIUM).format(date);
		}
		contactCard.setHtmlText("<p>This contact has been on this campaign before. They were removed on "+endDate+
				".</p><br/><p>You can choose to restart them from the beginning of the campaign or to let them continue from where they left off</p>");
		
		RadioGroup startOverGroup = new RadioGroup("startOverYesNoGroup");
		
		final MyGXTRadio startOverButton = new MyGXTRadio("Start Over. This option will start the new recipient from day 0.");
		MyGXTRadio resumeButton = new  MyGXTRadio("Resume. This option will resume from where the recipient left off.");
		startOverButton.setFieldLabel("field label");
		
		startOverGroup.add(startOverButton);
		startOverGroup.add(resumeButton);
		startOverGroup.setValue(resumeButton);
		
		FormPanel panel = new FormPanel();
		panel.setLabelWidth(150);	
		panel.setLayout(new RowLayout());
		panel.add(resumeButton);
		panel.add(startOverButton);
		panel.setLabelSeparator("");
		
		contactCard.setFormPanel(panel);
		
		contactCard.addListener(MobilisrEvents.WizardStep, new Listener<WizardStepEvent>() {
			@Override
			public void handleEvent(WizardStepEvent we) {
				if (startOverButton.getValue()){
					contact.setProgress(0);
				}				
			}
		});
		
		addCard(contactCard);
		
	}
	
	public CampaignContact getContact() {
		return contact;
	}
	
	public void setContactValidator(StepValidator validator){
		contactCard.setValidator(validator);
	}
}
