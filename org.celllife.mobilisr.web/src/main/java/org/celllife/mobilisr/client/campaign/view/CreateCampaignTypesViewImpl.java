package org.celllife.mobilisr.client.campaign.view;

import java.util.HashMap;
import java.util.Map;

import org.celllife.mobilisr.client.Constants;
import org.celllife.mobilisr.client.Messages;
import org.celllife.mobilisr.client.campaign.CreateCampaignTypesView;
import org.celllife.mobilisr.client.model.ViewModel;
import org.celllife.mobilisr.client.template.view.EntityCreateTemplateImpl;
import org.celllife.mobilisr.client.view.gxt.MyGXTButton;
import org.celllife.mobilisr.client.view.gxt.MyGXTRadio;
import org.celllife.mobilisr.constants.CampaignStatus;
import org.celllife.mobilisr.constants.CampaignType;
import org.celllife.mobilisr.domain.Campaign;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.form.Radio;
import com.extjs.gxt.ui.client.widget.form.RadioGroup;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.user.client.ui.Widget;

public class CreateCampaignTypesViewImpl extends EntityCreateTemplateImpl<Campaign> implements CreateCampaignTypesView, WizardStep {

	private MyGXTButton btnCancel;
	private MyGXTButton btnNext;

	private Label lblWeHaveThree;

	private Map<CampaignType, Radio> campTypeData = new HashMap<CampaignType, Radio>();
	private Radio relativeCampRadioBtn;
	private Radio genericCampRadioBtn;

	private ButtonBar buttonBar = new ButtonBar();
	private WizardStepper parent;
	private Campaign campaign;
	private RadioGroup radioGroup;

	public CreateCampaignTypesViewImpl() {
	}

	public CreateCampaignTypesViewImpl(WizardStepper parent) {
		this.parent = parent;
		createView();
	}
	
	@Override
	public void createView() {
		super.createView();
		
		btnCancel = new MyGXTButton("cancelButton", Messages.INSTANCE.cancel());
		btnNext = new MyGXTButton("nextButton", Messages.INSTANCE.wizardNext());

		lblWeHaveThree = new Label(
				"We have three types of campaigns and they relate to the pattern of messages that the recipients will receive. Which describes your campaign best?");

		relativeCampRadioBtn = new MyGXTRadio("Daily Campaigns - Recipients can choose times for their daily SMS.");
		relativeCampRadioBtn.setId(Campaign.PROP_TYPE + "-" + CampaignType.DAILY);
		genericCampRadioBtn = new MyGXTRadio("Flexi Campaigns - Recipients are sent SMSs at predetermined times (not necessarily daily).");
		genericCampRadioBtn.setId(Campaign.PROP_TYPE + "-" + CampaignType.FLEXI);
		
		campTypeData.put(CampaignType.FLEXI, genericCampRadioBtn);
		campTypeData.put(CampaignType.DAILY, relativeCampRadioBtn);

		relativeCampRadioBtn.setValue(false);
		genericCampRadioBtn.setEnabled(true);
		
		radioGroup = new RadioGroup();
		radioGroup.setOrientation(Orientation.VERTICAL);
		radioGroup.add(relativeCampRadioBtn);
		radioGroup.add(genericCampRadioBtn);
		radioGroup.addListener(Events.Change, new Listener<BaseEvent>() {
			@Override
			public void handleEvent(BaseEvent be) {
				setDirty();
				btnNext.enable();
			}
		});
		

		buttonBar.setAlignment(HorizontalAlignment.CENTER);
		buttonBar.setWidth(400);

		addListeners();

		buttonBar.add(btnCancel);
		buttonBar.add(btnNext);
		btnNext.disable();

		layoutWizardTemplate("Create New Campaign: Step 1 of 4");
		
		formElementContainer.add(radioGroup);
		formElementContainer.add(buttonBar, new FormData("100%"));
	}

	private void addListeners() {
		btnCancel.addSelectionListener(new SelectionListener<ButtonEvent>() {

			@Override
			public void componentSelected(ButtonEvent ce) {
				cancel();
			}
		});

		btnNext.addSelectionListener(new SelectionListener<ButtonEvent>() {

			@Override
			public void componentSelected(ButtonEvent ce) {
				goNext();
			}
		});

	}

	public void layoutWizardTemplate(String titleLabelText) {

		setIntStyleAttribute("margin", 0);
		setScrollMode(Scroll.AUTOY);
		setLayout(new RowLayout(Orientation.VERTICAL));
		setHeight("100%");

		titleLabel.setText(titleLabelText);
		titleLabel.setId("wizardStepTitle");
		titleLabel.getElement().setId("wizardStepTitle");
		titleLabel.setStyleName(Constants.INSTANCE.styleFont14());

		formPanel.setPadding(5);
		formPanel.setBorders(true);
		formPanel.setButtonAlign(HorizontalAlignment.CENTER);
		formPanel.setLayout(new RowLayout());
		formPanel.setAutoWidth(true);
		formPanel.setAutoHeight(true);
		formPanel.setScrollMode(Scroll.AUTOY);

		FormLayout formLayout = new FormLayout();
		formLayout.setLabelSeparator("");
		formElementContainer.setLayout(formLayout);
		formElementContainer.setAutoHeight(true);
		
		formPanel.add(lblWeHaveThree, new RowData(1, 1, new Margins(10)));
		formPanel.add(formElementContainer, new RowData(1, -1, new Margins(50, 10, 10, 10)));

		add(titleLabel, new RowData(1, -1, new Margins(0, 10, 10, 10)));
		add(formPanel, new RowData(1, 1, new Margins(5)));
	}

	@Override
	public Widget getViewWidget() {
		return this;
	}

	@Override
	public void cancel() {
		parent.cancel();
	}

	@Override
	public void goNext() {
		parent.goNext(getFormObject());
	}

	@Override
	public void goPrevious() {
		// not necessary cos this is the first step.
	}

	@Override
	public void save() {
		// do nothing.
	}

	@Override
	public void setFormObject(ViewModel<Campaign> viewEntityModel) {
		super.setFormObject(viewEntityModel);
		
		campaign = (Campaign) viewEntityModel.getModelObject();
		CampaignType campaignType = campaign.getType();
		if(campaignType != null){
			relativeCampRadioBtn.setEnabled(false);
			genericCampRadioBtn.setEnabled(false);
			
			Radio campTypeRadioBtn = campTypeData.get(campaignType);
			if(campTypeRadioBtn != null){
				radioGroup.setValue(campTypeRadioBtn);
				btnNext.enable();
			}else{
				// unselect ALL options	
				relativeCampRadioBtn.setValue(false);
				genericCampRadioBtn.setValue(false);
			}
		}else{
			relativeCampRadioBtn.setEnabled(true);
			genericCampRadioBtn.setEnabled(true);
			relativeCampRadioBtn.setValue(false);
			genericCampRadioBtn.setValue(false);
		}
		
		updateTitle();
	}
	
	private void updateTitle() {
		String title = ( campaign.isPersisted() ? "Editing Campaign '" + campaign.getName() +"'" : "Create New Campaign" ) + ": Step 1 of 4";
		titleLabel.setText(title);
	}

	@Override
	public ViewModel<Campaign> getFormObject() {

		ViewModel<Campaign> vem = super.getFormObject();	
		
		campaign = (Campaign) vem.getModelObject();
		if (campaign==null){ campaign = new Campaign(); }
		if (relativeCampRadioBtn.getValue()) {
			campaign.setType(CampaignType.DAILY);
		} else if (genericCampRadioBtn.getValue()) {
			campaign.setType(CampaignType.FLEXI);
		}

		CampaignStatus status = campaign.getStatus();
		status = (status==null ? CampaignStatus.INACTIVE : status);
		campaign.setStatus(status);
		vem.setModelObject(campaign);

		return vem;
	}

	@Override
	public void resetForm() {
		relativeCampRadioBtn.setValue(false);
		genericCampRadioBtn.setValue(false);
	}

}
