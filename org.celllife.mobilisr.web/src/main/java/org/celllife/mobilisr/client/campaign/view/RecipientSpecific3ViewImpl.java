package org.celllife.mobilisr.client.campaign.view;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.celllife.mobilisr.client.Constants;
import org.celllife.mobilisr.client.Messages;
import org.celllife.mobilisr.client.campaign.RecipientSpecific3View;
import org.celllife.mobilisr.client.campaign.presenter.WizardPresenter;
import org.celllife.mobilisr.client.model.ViewModel;
import org.celllife.mobilisr.client.template.view.EntityCreateTemplateImpl;
import org.celllife.mobilisr.client.validator.BeforeTimeFieldValidator;
import org.celllife.mobilisr.client.view.gxt.MessageBoxWithIds;
import org.celllife.mobilisr.client.view.gxt.MobilisrAsyncCallback;
import org.celllife.mobilisr.client.view.gxt.MyGXTButton;
import org.celllife.mobilisr.client.view.gxt.MyGXTNumberField;
import org.celllife.mobilisr.client.view.gxt.MyGXTSmsBox;
import org.celllife.mobilisr.client.view.gxt.MyGXTTimeField;
import org.celllife.mobilisr.client.view.gxt.grid.MyGXTPaginatedGridSearch;
import org.celllife.mobilisr.constants.CampaignType;
import org.celllife.mobilisr.domain.Campaign;
import org.celllife.mobilisr.domain.CampaignMessage;
import org.celllife.mobilisr.domain.ContactMsgTime;
import org.celllife.mobilisr.service.gwt.CampaignServiceAsync;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.binding.FieldBinding;
import com.extjs.gxt.ui.client.binding.FormBinding;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.form.AdapterField;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.FieldSet;
import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;
import com.extjs.gxt.ui.client.widget.layout.CenterLayout;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;

public class RecipientSpecific3ViewImpl extends EntityCreateTemplateImpl<Campaign>
		implements RecipientSpecific3View, WizardStep {

	private final class ConfigChangeListener implements Listener<FieldEvent> {

		private Field<?> field;
		private Listener<FieldEvent> successCallback;

		private ConfigChangeListener(Field<?> field) {
			this.field = field;
		}

		private ConfigChangeListener(Field<?> field, Listener<FieldEvent> successCallback) {
			this.field = field;
			this.successCallback = successCallback;
		}


		@Override
		public void handleEvent(final FieldEvent be) {
			if (campaign != null && campaign.isPersisted()){
				if (field.equals(numMsgsPerDay) && campaign.getContactCount() > 0){
					MessageBoxWithIds.alert(Messages.INSTANCE.campaignChangeMsgsPerDayErrorTitle(), 
							Messages.INSTANCE.campaignChangeMsgsPerDayError(),null);
					field.reset();
					return;
				}

				MessageBoxWithIds.confirm(Messages.INSTANCE.campaignRegenerateMessagesTitle(), 
						Messages.INSTANCE.campaignRegenerateMessagesWarning(),
										new Listener<MessageBoxEvent>() {

					@Override
					public void handleEvent(MessageBoxEvent e) {
						boolean cancelled = !e.getButtonClicked().getItemId()
									.equals(Dialog.YES);
						be.setCancelled(cancelled);
						regenerateMessages = !cancelled;
						if (cancelled){
							field.reset();
						} else if (successCallback != null){
							successCallback.handleEvent(be);
						}
					}
				});
			} else {
				regenerateMessages = true;
				if (successCallback != null) {
					successCallback.handleEvent(be);
				}
			}
		}
	}

	private static final int NUM_TIME_FIELDS = 4;
	private MyGXTSmsBox welcomeMessage;
	private MyGXTNumberField numDays;
	private SimpleComboBox<Integer> numMsgsPerDay;
	private MyGXTTimeField[] timeFields = new MyGXTTimeField[4];

	private ButtonBar buttonBar = new ButtonBar();

	private MyGXTButton btnBack;
	private MyGXTButton btnSave;
	private MyGXTButton btnCancel;
	private MyGXTButton btnNext;
	private FieldSet fldstWhatTimesDo;
	private WizardStepper parent;
	private Campaign campaign;

	MyGXTPaginatedGridSearch<CampaignMessage> gridSearch;
	protected boolean regenerateMessages;

	public RecipientSpecific3ViewImpl() {
	}

	public RecipientSpecific3ViewImpl(WizardStepper parent) {
		this.parent = parent;
		createView();
	}

	@Override
	public void createView() {
		super.createView();
		
		btnBack = new MyGXTButton("backButton", Messages.INSTANCE.wizardBack());
		btnSave = new MyGXTButton("saveButton", Messages.INSTANCE.save());
		btnCancel = new MyGXTButton("cancelButton", Messages.INSTANCE.cancel());
		btnNext = new MyGXTButton("nextButton", Messages.INSTANCE.wizardNext());
		
		Map<Button, Boolean> formButtons = new LinkedHashMap<Button, Boolean>();
		formButtons.put(btnBack, false);
		formButtons.put(btnSave, true);
		formButtons.put(btnCancel, false);
		formButtons.put(btnNext, true);
		
		welcomeMessage = new MyGXTSmsBox();
		numDays = new MyGXTNumberField(
				"* How many days do you want the program to run?",
				Campaign.PROP_DURATION, false, "", 0);
		numMsgsPerDay = new SimpleComboBox<Integer>();

		createContainer(formElementContainer);

		layoutWizardTemplate("Create New Campaign: Step 3 of 4", formButtons,
				true);
		
	}

	public void layoutWizardTemplate(String titleLabelText,
			Map<Button, Boolean> formButtons, boolean isAutoBind) {

		setIntStyleAttribute("margin", 0);
		setScrollMode(Scroll.AUTOY);
		setLayout(new RowLayout(Orientation.VERTICAL));

		List<Integer> numbers = new ArrayList<Integer>();
		for (int i = 0; i < 4; i++) {
			numbers.add(new Integer(i + 1));
		}
		numMsgsPerDay.setId("messagesPerDay");
		numMsgsPerDay.setName(Campaign.PROP_TIMES_PER_DAY);
		numMsgsPerDay.add(numbers);
		numMsgsPerDay.setTriggerAction(TriggerAction.ALL);
		numMsgsPerDay.addListener(Events.Select,
				new ConfigChangeListener(numMsgsPerDay, new Listener<FieldEvent>() {
					@Override
					public void handleEvent(FieldEvent ce) {
						Integer value = numMsgsPerDay.getSimpleValue();
						enableTimeFields(value);
					}
				}));

		numDays.setId("numDays");
		numDays.setMinValue(1);
		numDays.addListener(Events.Change, new ConfigChangeListener(numDays));

		for (int i = 1; i < timeFields.length; i++) {
			BeforeTimeFieldValidator bft = new BeforeTimeFieldValidator(
					timeFields[i - 1]);
			MyGXTTimeField timeField = timeFields[i];
			timeField.setValidator(bft);
			timeField.addListener(Events.Select,
					new ConfigChangeListener(timeField));
		}

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
		formLayout.setLabelWidth(300);
		formLayout.setDefaultWidth(250);
		formElementContainer.setLayout(formLayout);
		formElementContainer.setAutoHeight(true);

		formCenterContainer.setLayout(new CenterLayout());
		formCenterContainer.setAutoWidth(true);
		formCenterContainer.setHeight("65%");
		formCenterContainer.add(formElementContainer);

		formPanel
				.add(formCenterContainer, new RowData(-1, -1, new Margins(10)));

		addAndConfigFormButtons(formButtons, false);
		createFormBinding(formPanel, isAutoBind);

		// remove binding for time fields since their values are set manually
		FormBinding formBinding = getFormBinding();
		for (MyGXTTimeField field : timeFields) {
			FieldBinding binding = formBinding.getBinding(field);
			formBinding.removeFieldBinding(binding);
		}
		add(titleLabel, new RowData(1, -1, new Margins(0, 10, 10, 10)));
		add(msgLabel, new RowData(1, -1, new Margins(10)));
		add(formPanel, new RowData(1, 1, new Margins(10)));

	}

	public void createContainer(LayoutContainer lc) {

		fldstWhatTimesDo = new FieldSet();
		fldstWhatTimesDo
				.setHeading("What times do you want to send these messages?");
		fldstWhatTimesDo.setCollapsible(true);

		FormLayout msgTimeFormLayout = new FormLayout();
		msgTimeFormLayout.setLabelSeparator("");
		fldstWhatTimesDo.setLayout(msgTimeFormLayout);

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

		numMsgsPerDay.setFieldLabel("* How many messages will be sent per day ?");

		welcomeMessage.getMsgTxtArea().setAllowBlank(true);
		welcomeMessage.getMsgTxtArea().setFieldLabel("Welcome Message");
		welcomeMessage.getMsgTxtArea().setName(Campaign.PROP_WELCOME_MSG);
		welcomeMessage.getMsgTxtArea().setHeight(100);

		lc.add(welcomeMessage.getMsgTxtArea());
		formElementContainer.add(new AdapterField(welcomeMessage.getToolBar()));
		numDays.setPropertyEditorType(Integer.class);
		lc.add(numDays);
		lc.add(numMsgsPerDay);
		for (int i = 0; i < NUM_TIME_FIELDS; i++) {

			timeFields[i] = new MyGXTTimeField(
					"On each day, what default time would like to send Message "
							+ (i + 1) + " ?", i + "", 30, false, true, false);
			timeFields[i].disable();
			timeFields[i].setId("time"+i);
			timeFields[i].setForceSelection(true);
			timeFields[i].setTriggerAction(TriggerAction.ALL);
			lc.add(timeFields[i]);
		}

		lc.add(buttonBar, new FormData("100%"));
		lc.setLayout(new FitLayout());
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

	@Override
	public ViewModel<Campaign> getFormObject() {

		ViewModel<Campaign> viewEntityModel = super.getFormObject();
		campaign = (Campaign) viewEntityModel.getModelObject();

		campaign.setWelcomeMsg(welcomeMessage.getSmsMsg());

		if (regenerateMessages){
			int timesPerDay = 0;
			int duration = 0;
			
			timesPerDay = numMsgsPerDay.getValue().getValue().intValue();
			duration = this.numDays.getValue().intValue();
			
			campaign.setTimesPerDay(timesPerDay);
			campaign.setDuration(duration);
			campaign.setRebuildMessages(true);
			List<Date> messageTimes = new ArrayList<Date>();
			for (int i = 0; i < timesPerDay; i++) {
				messageTimes.add(timeFields[i].getDateValue());
			}
			campaign.setMessageTimes(messageTimes);
		}

		ViewModel<Campaign> vem = new ViewModel<Campaign>();
		vem.setModelObject(campaign);

		return vem;
	}

	@Override
	public void setFormObject(ViewModel<Campaign> vem) {
		super.setFormObject(vem);
		regenerateMessages = false;

		campaign = vem.getModelObject();

		boolean active = campaign.getStatus().isActiveState();
		numDays.setEnabled(!active);
		numMsgsPerDay.setEnabled(!active);
		for (int i = 0; i < timeFields.length; i++) {
			timeFields[i].setEnabled(!active&& (i < campaign.getTimesPerDay()));
		}

		updateTitle();

		CampaignServiceAsync campaignServiceAsync = (CampaignServiceAsync) parent.getItem(WizardPresenter.CAMPAIGN_SERVICE_ASYNC);

		if (campaign.isPersisted()){
			campaignServiceAsync.getCampaignMessageTimes(campaign, new MobilisrAsyncCallback<List<ContactMsgTime>>() {
				@Override
				public void onSuccess(List<ContactMsgTime> campaignMessages) {
					populateTimeFields(campaignMessages);
				}
			});
		}
	}

	private void updateTitle() {
		String type = "";
		if (campaign.getType()==CampaignType.DAILY){
			type = "Recipient-Specific" ;
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
		numMsgsPerDay.clear();
		numDays.clear();
		for (MyGXTTimeField timefield : timeFields) {
			timefield.clearSelections();
		}
	}

	private void populateTimeFields(List<ContactMsgTime> campaignMessages) {
		for (int i = 0; (i < campaignMessages.size()); i++) {
			Date d = campaignMessages.get(i).getMsgTime();
			timeFields[i].setDateValue(d);
		}
	}

	private void enableTimeFields(Integer value) {
		for (int i = 0; i < NUM_TIME_FIELDS; i++) {
			if (i < value) {
				timeFields[i].enable();
				timeFields[i].addListener(Events.Select,new SelectionListener<ComponentEvent>() {
					@Override
					public void componentSelected(ComponentEvent be) {
						for (int j = 0; j < timeFields.length; j++) {
							if (timeFields[j].isEnabled())
								timeFields[j].validate();
						}

					}
				});

			} else {
				if (timeFields[i].isEnabled())
					timeFields[i].clear();
				timeFields[i].disable();
			}
		}
	}
}
