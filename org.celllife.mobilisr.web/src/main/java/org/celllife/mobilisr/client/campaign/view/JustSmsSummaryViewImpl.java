package org.celllife.mobilisr.client.campaign.view;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.celllife.mobilisr.client.Messages;
import org.celllife.mobilisr.client.UserContext;
import org.celllife.mobilisr.client.campaign.JustSmsSummaryView;
import org.celllife.mobilisr.client.model.ViewModel;
import org.celllife.mobilisr.client.template.view.EntityCreateTemplateImpl;
import org.celllife.mobilisr.client.view.gxt.MyGXTButton;
import org.celllife.mobilisr.client.view.gxt.MyGXTFormPanel;
import org.celllife.mobilisr.client.view.gxt.MyGXTLabelField;
import org.celllife.mobilisr.client.view.gxt.MyGXTTextArea;
import org.celllife.mobilisr.constants.CampaignStatus;
import org.celllife.mobilisr.domain.Campaign;
import org.celllife.mobilisr.domain.CampaignMessage;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Status;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.AdapterField;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.extjs.gxt.ui.client.widget.toolbar.FillToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.Widget;

public class JustSmsSummaryViewImpl extends EntityCreateTemplateImpl<Campaign> implements JustSmsSummaryView {

	private MyGXTLabelField lblRecipients;
	private MyGXTLabelField lblSchedule;
	private MyGXTLabelField lblCostPerRecipient;
	private MyGXTLabelField lblCostOfCampaign;
	private MyGXTLabelField lblCurrentBalance;
	private MyGXTLabelField lblBalanceAfterCampaign;

	private MyGXTButton btnCheckCampaign;
	private MyGXTButton btnSchedule;
	private MyGXTButton btnClose;
	private MyGXTButton btnEditCampaign;
	private MyGXTButton btnPrevious;
	private MyGXTButton btnNext;
	
	private Status statusParts;

	private Campaign campaign;
	private MyGXTTextArea textArea;
	
	private int currentMessageIndex = 0;
	private List<String> smsList;

	private SmsTestDialog smsTestDialog;

	private Long numberContacts = 0l;
	
	private String statusScheduleMsg;

	@Override
	public void createView() {
		super.createView();

		layoutCreateTemplate("Campaign Summary");
		
		btnSchedule = new MyGXTButton("btnSave", "Schedule");
		btnClose = new MyGXTButton("btnCancel", "Close");
		btnEditCampaign = new MyGXTButton("btnEditCampaign", "<< Edit Campaign");
		
		Map<Button, Boolean> formButtons = new LinkedHashMap<Button, Boolean>();
		formButtons.put(btnEditCampaign, false);
		formButtons.put(btnSchedule, false);
		formButtons.put(btnClose, false);
		addAndConfigFormButtons(formButtons, false);
		
		lblRecipients = new MyGXTLabelField("----", "Message Recipients");
		lblSchedule = new MyGXTLabelField("----", "Schedule");
		lblCostPerRecipient = new MyGXTLabelField("----", "Cost Per Recipient");
		lblCostOfCampaign = new MyGXTLabelField("----", "Cost of Campaign");
		lblCurrentBalance = new MyGXTLabelField("----", "Current Balance");
		lblBalanceAfterCampaign = new MyGXTLabelField("----", "Balance after Campaign");

		btnCheckCampaign = new MyGXTButton("btnCheckCampaign", "Check your campaign by sending a test message");
		
		statusParts = new Status();
		
		initNonEditableTextFields(formElementContainer);
		initButtons(formElementContainer);
		
		smsTestDialog = new SmsTestDialog();
		smsTestDialog.setMyMsisdn(UserContext.getUser().getMsisdn());
	}

	private void initNonEditableTextFields(LayoutContainer fp) {

		FormData fData = new FormData("150");
		fp.add(lblRecipients, fData);
		fp.add(lblSchedule, fData);
		fp.add(new AdapterField(createStatusMessageBox()), fData);
		fp.add(lblCostPerRecipient, fData);
		fp.add(lblCostOfCampaign, fData);
		fp.add(lblCurrentBalance, fData);
		fp.add(lblBalanceAfterCampaign, fData);

	}

	public void updateMessageButtons() {
		int numCampaignMessages = smsList.size();
		if (numCampaignMessages == 0) {
			btnNext.setEnabled(false);
			btnPrevious.setEnabled(false);
			return;
		}

		currentMessageIndex = (currentMessageIndex % numCampaignMessages);

		statusParts.setText("Part " + (currentMessageIndex + 1) + " of "
				+ numCampaignMessages);

		textArea.setValue(smsList.get(currentMessageIndex));
		if (numCampaignMessages == 1) {
			btnNext.setEnabled(false);
			btnPrevious.setEnabled(false);

		} else {
			if (currentMessageIndex == 0) {
				btnPrevious.setEnabled(false);
				btnNext.setEnabled(true);

			} else if (currentMessageIndex == (numCampaignMessages - 1)) {
				btnPrevious.setEnabled(true);
				btnNext.setEnabled(false);

			} else {
				btnPrevious.setEnabled(true);
				btnNext.setEnabled(true);
			}
		}
	}

	private LayoutContainer createStatusMessageBox() {

		LayoutContainer body = new LayoutContainer();
		ToolBar toolBar = new ToolBar();

		btnPrevious = new MyGXTButton("<< Previous");
		btnPrevious.addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				currentMessageIndex--;
				updateMessageButtons();
			}

		});
		btnNext = new MyGXTButton(Messages.INSTANCE.wizardNext());
		btnNext.addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				currentMessageIndex++;
				updateMessageButtons();
			}

		});

		toolBar.add(btnPrevious);
		
		statusParts.setText("Part");
		statusParts.setWidth(120);
		toolBar.add(statusParts);
		toolBar.add(new FillToolItem());
		toolBar.add(btnNext);

		MyGXTFormPanel form = new MyGXTFormPanel();
		form.setHeading("Message");
		form.setSize(250, 150);
		form.setPadding(0);

		form.setBottomComponent(toolBar);
		
		textArea = new MyGXTTextArea("Message","Message",false,"");
		textArea.setStyleAttribute("font-weight", "900");
		textArea.setHideLabel(true);
		textArea.disable();
		form.add(textArea, new FormData("100% 100%"));
		
		body.add(form, new FormData("-20"));
		
		return body;
		
	}	

	public void showSendSmsTest() {
		smsTestDialog.show();
	}

	@Override
	public MyGXTButton getFormCancelButton() {
		return btnClose;
	}

	@Override
	public ViewModel<Campaign> getFormObject() {
		ViewModel<Campaign> c = new ViewModel<Campaign>();
		c.setModelObject(campaign);
		return c;
	}

	@Override
	public MyGXTButton getFormSubmitButton() {
		return btnSchedule;
	}

	@Override
	public void setErrorMessage(String errorMsg) {
		super.setErrorMessage(null);
	}

	@Override
	public Widget getViewWidget() {
		return this;
	}

	@Override
	public void setFormObject(ViewModel<Campaign> o) {
		this.campaign = (Campaign) o.getModelObject();
		if(campaign.getStatus().equals(CampaignStatus.SCHEDULED)){
			statusScheduleMsg = "Campaign: " + campaign.getName()+ " has been Updated and Scheduled successfully";
		}else{ 
			statusScheduleMsg = "Campaign: " + campaign.getName()+ " scheduled successfully";
		};
		populateFields();
	}

	public void populateFields() {
		this.numberContacts = (long) campaign.getContactCount();
		titleLabel.setText("Campaign Summary: " + campaign.getName());

		this.smsList = createMultiPartSms(campaign.getCampaignMessages().get(0).getMessage());
		textArea.setValue(smsList.get(0));
		lblCurrentBalance.setText( (int)(getCurrentBalance()) + " credits");
		lblCostOfCampaign.setText(campaign.getCost() + " credits");
		lblCostPerRecipient.setText(getNumSMSPerRecipient() + " credits");
		CampaignMessage campaignMessage = campaign.getCampaignMessages().get(0);
		Date msgDate = campaignMessage.getMsgDate();
		Date msgTime = campaignMessage.getMsgTime();
		String date = DateTimeFormat.getFormat("dd MMM yyyy").format(msgDate);
		String time = DateTimeFormat.getFormat("HH:mm").format(msgTime);
		lblSchedule.setText(date + " at " + time);
		lblRecipients.setText(numberContacts + " contacts");
		lblBalanceAfterCampaign.setText( (int)(getBalanceAfterCampaign()) + " credits");

		currentMessageIndex = 0;
		updateMessageButtons();
	}

	public double getBalanceAfterCampaign() {

		double currentBalance = getCurrentBalance();
		double cost = getTotalCostForCampaign();
		
		btnCheckCampaign.setEnabled(currentBalance >= (smsList.size()));
		boolean campaignEditable = !campaign.getStatus().isActiveState()
			&& !campaign.getStatus().equals(CampaignStatus.FINISHED);
		
		btnSchedule.setVisible(campaignEditable);
		btnSchedule.setEnabled(cost <= currentBalance);
		
		btnEditCampaign.setVisible(campaignEditable);
		btnCheckCampaign.setVisible(campaignEditable);
		
		return (currentBalance - cost);
	}	

	private double getCurrentBalance() {
		return campaign.getOrganization().getAvailableBalance();
	}

	public List<String> createMultiPartSms(String message) {
		List<String> smses = new ArrayList<String>();
		int singleSmsLength = 160;
		int multipartSmsLength = 153;
		if (message.length() <= singleSmsLength) {
			smses.add(message);
			return smses;
		} else {
			int numFullSmses = (message.length() / multipartSmsLength);
			for (int i = 0; i < numFullSmses; i++) {
				smses.add(message.substring(i * multipartSmsLength,
						((i + 1) * multipartSmsLength)));
			}
			smses.add(message.substring(multipartSmsLength * numFullSmses));
			return smses;
		}
	}

	private void initButtons(LayoutContainer fp) {
	
		AdapterField checkCampaignAdapter = new AdapterField(btnCheckCampaign);
		checkCampaignAdapter.setId("checkCampaignAdapter");
		fp.add(checkCampaignAdapter);
		
	}

	public Campaign getCampaign(){
		return campaign;
	}

	public int getNumSMSPerRecipient() {
		return campaign.getTimesPerDay()
				* this.smsList.size();
	}

	@Override
	public double getTotalCostForCampaign() {
		return getNumSMSPerRecipient() * numberContacts;
	}

	@Override
	public MyGXTButton getBtnCheckCampaign() {
		return btnCheckCampaign;
	}
	
	
	@Override
	public MyGXTButton getBtnEditCampaign() {
		return btnEditCampaign;
	}

	@Override
	public MyGXTButton getBtnSchedule() {
		return btnSchedule;
	}
	
	@Override
	public MyGXTButton getBtnCloseCampaignSummary(){
		return btnClose;
	}

	@Override
	public String getSmsTestNumber() {
		return smsTestDialog.getSelectedMsisdn();
	}
	
	@Override
	public MyGXTButton getSmsSendButton() {
		return smsTestDialog.getSaveButton();
	}

	@Override
	public int getNumSMSParts() {
		return smsList.size();
	}
	
	@Override
	public String getCampSchedStatusMsg(){
		return statusScheduleMsg;
	}
}
