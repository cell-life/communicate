package org.celllife.mobilisr.client.campaign.view;

import java.util.LinkedHashMap;
import java.util.Map;

import org.celllife.mobilisr.client.Constants;
import org.celllife.mobilisr.client.Messages;
import org.celllife.mobilisr.client.campaign.RecipientSpecific3View;
import org.celllife.mobilisr.client.model.ViewModel;
import org.celllife.mobilisr.client.template.view.EntityCreateTemplateImpl;
import org.celllife.mobilisr.client.view.gxt.MyGXTButton;
import org.celllife.mobilisr.client.view.gxt.MyGXTSmsBox;
import org.celllife.mobilisr.client.view.gxt.grid.MyGXTPaginatedGridSearch;
import org.celllife.mobilisr.constants.CampaignType;
import org.celllife.mobilisr.domain.Campaign;
import org.celllife.mobilisr.domain.CampaignMessage;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.form.AdapterField;
import com.extjs.gxt.ui.client.widget.layout.CenterLayout;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;

public class Generic3ViewImpl  extends EntityCreateTemplateImpl<Campaign> implements RecipientSpecific3View, WizardStep {
	
	private MyGXTSmsBox welcomeMessage;
	private ButtonBar buttonBar = new ButtonBar();
	private MyGXTButton btnBack;
	private MyGXTButton btnSave;
	private MyGXTButton btnCancel;
	private MyGXTButton btnNext;
	private WizardStepper parent;

	MyGXTPaginatedGridSearch<CampaignMessage> gridSearch;

	public Generic3ViewImpl() {

	}

	public Generic3ViewImpl(WizardStepper parent) {
		this.parent = parent;
		createView();
	}

	@Override
	public void createView() {
		super.createView();
		
		btnBack = new MyGXTButton("backButton", Messages.INSTANCE.wizardBack());
		btnSave = new MyGXTButton("saveButton", Messages.INSTANCE.save());
		btnCancel = new MyGXTButton("cancelButton",Messages.INSTANCE.cancel());
		btnNext = new MyGXTButton("nextButton",Messages.INSTANCE.wizardNext());
		
		Map<Button, Boolean> formButtons = new LinkedHashMap<Button, Boolean>();
		formButtons.put(btnBack, false);
		formButtons.put(btnSave, true);
		formButtons.put(btnCancel, false);
		formButtons.put(btnNext, true);

		layoutWizardTemplate("Create New Campaign: Step 3 of 4");
		
		welcomeMessage = new MyGXTSmsBox();
		welcomeMessage.getMsgTxtArea().setHeight(150);
		
		createContainer();
		
		addAndConfigFormButtons(formButtons, false);
		createFormBinding(formPanel, true);
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
		formLayout.setLabelWidth(160);
		formLayout.setDefaultWidth(300);
		formElementContainer.setLayout(formLayout);
		formElementContainer.setAutoHeight(true);

		formCenterContainer.setAutoWidth(true);
		formCenterContainer.setHeight("65%");
		formCenterContainer.setLayout(new CenterLayout());
		formCenterContainer.add(formElementContainer);

		Label tmp = new Label("In 'Generic Timing' campaigns, you specify the time on each day the recipient should receive their messages. For example, on Day 1, Message 1 will go out at 8am, Message 2 will go out at 11am, etc. All recipients will receive a 'Welcome to the Campaign' message at the time of enrollment, and 'Day 1' of the message schedule will commence on the following day.");
		tmp.setWidth(200);
		formPanel.add(tmp,new RowData(-1, -1, new Margins(20)));
		
		formPanel.add(formCenterContainer, new RowData(-1, -1, new Margins(10)));

		add(titleLabel, new RowData(1, -1, new Margins(0, 10, 10, 10)));
		add(msgLabel, new RowData(1, -1, new Margins(10)));
		add(formPanel, new RowData(1, 1, new Margins(10)));
	}

	public void createContainer() {
		btnSave.addSelectionListener(new SelectionListener<ButtonEvent>() {

			@Override
			public void componentSelected(ButtonEvent ce) {
				save();
			}
		});

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
		btnBack.addSelectionListener(new SelectionListener<ButtonEvent>() {

			@Override
			public void componentSelected(ButtonEvent ce) {
				goPrevious();
			}
		});

		buttonBar.setAlignment(HorizontalAlignment.CENTER);
		buttonBar.add(btnBack);
		buttonBar.add(btnSave);
		buttonBar.add(btnCancel);
		buttonBar.add(btnNext);

		welcomeMessage.getMsgTxtArea().setAllowBlank(true);
		welcomeMessage.getMsgTxtArea().setFieldLabel("Welcome Message");
		welcomeMessage.getMsgTxtArea().setName(Campaign.PROP_WELCOME_MSG);

		FormData formData = new FormData();
		formData.setMargins(new Margins(10));
		
		formElementContainer.add(welcomeMessage.getMsgTxtArea(),formData);
		formElementContainer.add(new AdapterField(welcomeMessage.getToolBar()),formData);
		formElementContainer.add(buttonBar, formData);
	}

	@Override
	public void cancel() {
		parent.cancel();
	}

	@Override
	public void goNext() {
		parent.save(true);
	}

	@Override
	public void goPrevious() {
		parent.goPrevious(getFormObject());
	}

	@Override
	public void save() {
		parent.save(false);
	}

	public ViewModel<Campaign> getFormObject() {

		ViewModel<Campaign> viewEntityModel = super.getFormObject();
		Campaign campaign = (Campaign) viewEntityModel.getModelObject();
		viewEntityModel.setModelObject(campaign);

		return viewEntityModel;
	}

	@Override
	public void setFormObject(ViewModel<Campaign> vem) {
		super.setFormObject(vem);
		Campaign campaign = (Campaign) vem.getModelObject();

		final int timesPerDay = campaign.getTimesPerDay();
		System.out.println("RS3VI.setFormObject() Times per day: " + timesPerDay);

		updateTitle(campaign);
	}

	private void updateTitle(Campaign campaign) {
		String type = "";
		
		if (campaign.getType()==CampaignType.DAILY){
			type = "Recipient-Specific";
		}else if (campaign.getType() == CampaignType.FLEXI){
			type = "Generic";
		}else if (campaign.getType() == CampaignType.FIXED){
			type = "Fixed"; 
		}
		
		String title = (campaign.isPersisted() ? "Editing Campaign '" + campaign.getName() + "'" : "Create New "+type+" Campaign") + ": Step 3 of 4";
		titleLabel.setText(title);
	}

	@Override
	public void resetForm() {
	}
	

}
