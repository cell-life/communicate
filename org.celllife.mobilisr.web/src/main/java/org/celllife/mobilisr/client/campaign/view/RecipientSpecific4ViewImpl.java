package org.celllife.mobilisr.client.campaign.view;

import java.util.ArrayList;
import java.util.List;

import org.celllife.mobilisr.client.Constants;
import org.celllife.mobilisr.client.Messages;
import org.celllife.mobilisr.client.MobilisrClientUtil;
import org.celllife.mobilisr.client.Resources;
import org.celllife.mobilisr.client.URLUtil;
import org.celllife.mobilisr.client.UserContext;
import org.celllife.mobilisr.client.campaign.RecipientSpecific4View;
import org.celllife.mobilisr.client.campaign.presenter.WizardPresenter;
import org.celllife.mobilisr.client.model.ViewModel;
import org.celllife.mobilisr.client.template.view.EntityListTemplateImpl;
import org.celllife.mobilisr.client.view.gxt.Action;
import org.celllife.mobilisr.client.view.gxt.ButtonGridCellRenderer;
import org.celllife.mobilisr.client.view.gxt.CsvFileDialog;
import org.celllife.mobilisr.client.view.gxt.GridModelEvent;
import org.celllife.mobilisr.client.view.gxt.MessageBoxWithIds;
import org.celllife.mobilisr.client.view.gxt.MobilisrAsyncCallback;
import org.celllife.mobilisr.client.view.gxt.MyGXTButton;
import org.celllife.mobilisr.client.view.gxt.MyGXTLabelField;
import org.celllife.mobilisr.client.view.gxt.RemoteStoreFilterField;
import org.celllife.mobilisr.client.view.gxt.grid.EntityIDColumnConfig;
import org.celllife.mobilisr.client.view.gxt.grid.MyGXTPaginatedGridSearch;
import org.celllife.mobilisr.constants.CampaignType;
import org.celllife.mobilisr.domain.Campaign;
import org.celllife.mobilisr.domain.CampaignMessage;
import org.celllife.mobilisr.domain.User;
import org.celllife.mobilisr.exception.ImportException;
import org.celllife.mobilisr.service.gwt.CampaignServiceAsync;
import org.celllife.mobilisr.service.gwt.ExportServiceAsync;
import org.celllife.mobilisr.service.gwt.ScheduleServiceAsync;
import org.celllife.mobilisr.service.gwt.ServiceAndUIConstants;

import com.extjs.gxt.ui.client.Style.ButtonScale;
import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.IconAlign;
import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.data.BeanModelFactory;
import com.extjs.gxt.ui.client.data.BeanModelLookup;
import com.extjs.gxt.ui.client.data.LoadEvent;
import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FormEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.LoadListener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.AdapterField;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.LabelField;
import com.extjs.gxt.ui.client.widget.form.StoreFilterField;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.grid.GridView;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;

public class RecipientSpecific4ViewImpl extends EntityListTemplateImpl
		implements RecipientSpecific4View, WizardStep {

	private LabelField lblfldNowYouNeed;
	private LabelField lblCampaignCost;
	private Anchor addNewLink;

	private MyGXTButton importMsgsBtn;
	private MyGXTButton exportMsgsBtn;

	private MyGXTButton btnBack;
	private MyGXTButton btnSave;
	private MyGXTButton btnNext;
	private WizardStepper parent;
	private Campaign campaign;
	private MyGXTPaginatedGridSearch<CampaignMessage> gridSearch;

	private SmsTestDialog smsTestDialog;
	private MessageEditorDialog editMessageDialog;
	private CampaignServiceAsync crudCampaignServiceAsync = null;
	private List<BeanModel> newOrChangedMessages = new ArrayList<BeanModel>();
	private ExportServiceAsync exportServiceAsync;

	public RecipientSpecific4ViewImpl() {
	}

	public RecipientSpecific4ViewImpl(WizardViewImpl parent) {
		this.parent = parent;

		createView();
		getPagingToolBar().bind(gridSearch.getLoader());
		buildWidget(gridSearch.getStore(), gridSearch.getFilter());
	}

	@Override
	public void createView() {
		lblfldNowYouNeed = new LabelField(
		"Now you need to specify the messages, times and schedules for 'Recipient-Specific Timing' campaign. You can do these individually, or import messages from a CSV file.");
		lblCampaignCost = new MyGXTLabelField("Campaign Cost: n Credits");
		addNewLink = new Anchor("Add New Message","#");
		
		btnBack = new MyGXTButton("backButton", Messages.INSTANCE.wizardBack());
		btnSave = new MyGXTButton("saveButton", Messages.INSTANCE.save());
		btnNext = new MyGXTButton("nextButton","Finish");

		importMsgsBtn = new MyGXTButton("csvImport", "Import Messages",
				Resources.INSTANCE.csv(), IconAlign.LEFT, ButtonScale.MEDIUM);
		exportMsgsBtn = new MyGXTButton("csvExport", "Export Messages",
				Resources.INSTANCE.csv(), IconAlign.LEFT, ButtonScale.MEDIUM);

		smsTestDialog = new SmsTestDialog();
		smsTestDialog.setMyMsisdn(UserContext.getUser().getMsisdn());
		editMessageDialog = new MessageEditorDialog();
		
		crudCampaignServiceAsync = (CampaignServiceAsync) parent.getItem(WizardPresenter.CAMPAIGN_SERVICE_ASYNC);
		exportServiceAsync = (ExportServiceAsync) parent.getItem(WizardPresenter.EXPORT_SERVICE_ASYNC);

		Button[] buttons = new Button[] {btnBack, btnSave, btnNext};

		addListeners();

		gridSearch = new MyGXTPaginatedGridSearch<CampaignMessage>(
				CampaignMessage.PROP_CAMPAIGN,
				Constants.INSTANCE.pageSize()) {

			@Override
			public void rpcListServiceCall(PagingLoadConfig pagingLoadConfig,
					AsyncCallback<PagingLoadResult<CampaignMessage>> callback) {
				crudCampaignServiceAsync.findCampMessageByCampaign(campaign, pagingLoadConfig, callback);
			}
		};
		gridSearch.getStore().setMonitorChanges(true);
		gridSearch.getLoader().addLoadListener(new LoadListener(){
			@Override
			public void loaderBeforeLoad(final LoadEvent le) {
				// although this isn't really necessary it could look weird if you went ahead
				// a page and then came back and your new / edited message wasn't there.
				if (!newOrChangedMessages.isEmpty()){
					le.setCancelled(true);

					MessageBoxWithIds.confirm("Save your changes", "You have made some changes, " +
							"do you want to save them before continuing?", new Listener<MessageBoxEvent>() {
						@Override
						public void handleEvent(MessageBoxEvent be) {
							if (be.getButtonClicked().getItemId()
									.equals(Dialog.YES)) {
								save();
							} else {
								newOrChangedMessages.clear();
								gridSearch.getLoader().load(le.getConfig());
							}
						}
					});
				}
			}
		});

		layoutListTemplate(
				"Create New Campaign: Recipient-Specific Timing (Step 4 of 4)",
				null, true);
		titleLabel.setId("wizardStepTitle");
		titleLabel.getElement().setId("wizardStepTitle");
		listContentPanel.setHeight(310);
		buttonBar.setAlignment(HorizontalAlignment.CENTER);
		super.addButtonBar(buttons);
	}

	private void addListeners() {
		importMsgsBtn.addSelectionListener(new SelectionListener<ButtonEvent>() {
					@Override
					public void componentSelected(ButtonEvent ce) {
						CsvFileDialog dialog = new CsvFileDialog(){
							@Override
							protected void createFormContents(
									FormPanel formPanel) {
								super.createFormContents(formPanel);
								Anchor anchor = new Anchor("Download template file", "#");
								anchor.addClickHandler(new ClickHandler() {
									@Override
									public void onClick(ClickEvent arg0) {
										exportMessages();
									}
								});
								formPanel.add(anchor);
							}
						};
						dialog.setHeading("Import Campaign Messages");
						dialog.setInfoText("The CSV file must contain the following columns:<br/>" +
						"<b>Flexi campaigns:</b> Message text, Message day, Message time</br>" +
						"<b>Daily campaigns:</b> Message text");
						dialog.setSubmitListener(new Listener<FormEvent>() {
							@Override
							public void handleEvent(FormEvent be) {
								readDataFromCsvFile(be);
							}
						});
						dialog.show();
					}
				});
		
		exportMsgsBtn.addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				exportMessages();
			}
		});

		btnBack.addSelectionListener(new SelectionListener<ButtonEvent>() {

			@Override
			public void componentSelected(ButtonEvent ce) {
				goPrevious();
			}
		});
		btnSave.addSelectionListener(new SelectionListener<ButtonEvent>() {

			@Override
			public void componentSelected(ButtonEvent ce) {
				save();
			}
		});

		btnNext.addSelectionListener(new SelectionListener<ButtonEvent>() {

			@Override
			public void componentSelected(ButtonEvent ce) {
				goNext();
			}
		});

		addNewLink.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent arg0) {
				addNewCampaignMessageToCampaign();
			}

		});
	}
	
	private void readDataFromCsvFile(FormEvent fe) {
		String data = fe.getResultHtml();
		String[] split = data.split(";");
		String filePath = split[0];
		
		crudCampaignServiceAsync.importCampaignMessagesFromCSV(filePath, 
				campaign, new MobilisrAsyncCallback<Campaign>() {
			@Override
			public void onSuccess(Campaign campaign) {
				reloadMessages(campaign);	
			}
			@Override
			public void onFailure(Throwable error) {
				if ( (error instanceof ImportException)
				  && (error.getMessage().contains("Disallowed characters")) ) {
					String delimiter = "fileName:";
					String[] temp = error.getMessage().split(delimiter);
					String fileName = temp[1];
					URLUtil.getTextFile(fileName);
					Throwable e = new ImportException(
							temp[0] + "\nSee returned log file for details.\n\n"
							+ ServiceAndUIConstants.VALIDATION_TEXT_MESSAGE);
					super.onFailure(e);
				} else {
					super.onFailure(error);
				}
			}
		});
	}
	
	private void exportMessages() {
		final MessageBox wait = MessageBox.wait("Exporting", "Exporting messages", null);
		exportServiceAsync.exportCampaignMessages(campaign.getId(), new MobilisrAsyncCallback<String>() {
			@Override
			public void onFailure(Throwable error) {
				wait.close();
				super.onFailure(error);
			}
			
			@Override
			public void onSuccess(String result) {
				wait.close();
				URLUtil.getTextFile(result);
			}
		});
	}

	private void addNewCampaignMessageToCampaign() {
		CampaignMessage campaignMessage = new CampaignMessage("message", 1, null, campaign);
		// set the id to a negative number force uniqueness for equals method
		// so that it will get added to the nowOrChangedList after editing
		campaignMessage.setId(Long.valueOf(-1-newOrChangedMessages.size()));
		BeanModel model = convertEntityToBeanModel(campaignMessage);
		launchEditMessageDialog(model,true);

	}

	@Override
	protected void addMessageLabel() {
		super.addMessageLabel();
		add(lblfldNowYouNeed, new RowData(1, -1, new Margins(10, 10, 5, 10)));
		add(listContentPanel, new RowData(1, -1, new Margins(10, 10, 0, 10)));
	}

	private List<ColumnConfig> createColumnConfigs() {
		List<ColumnConfig> configs = new ArrayList<ColumnConfig>();

		configs.add(new EntityIDColumnConfig(CampaignMessage.PROP_MESSAGE, "Message",
				250, "campaignMessage"));

		ColumnConfig lengthCounter = new ColumnConfig("numOfMsgs",
				"No Messages", 75);
		lengthCounter.setRenderer(new GridCellRenderer<BeanModel>() {

			@Override
			public Object render(BeanModel model, String property,
					ColumnData config, int rowIndex, int colIndex,
					ListStore<BeanModel> store, Grid<BeanModel> grid) {

				CampaignMessage cm = model.getBean();
				return MobilisrClientUtil.calculateNumberSMSs(cm.getMessage());
			}
		});
		configs.add(lengthCounter);
		configs.add(new ColumnConfig(CampaignMessage.PROP_MSG_DAY,
				"Day To Send", 100));
		ColumnConfig e = new ColumnConfig(CampaignMessage.PROP_MSG_TIME,
				"Time To Send", 100);
		e.setDateTimeFormat(DateTimeFormat.getFormat(PredefinedFormat.TIME_SHORT));
		configs.add(e);

		ColumnConfig testMessage = new ColumnConfig("tstMsg", "Actions", 180);
		ButtonGridCellRenderer renderer = new ButtonGridCellRenderer();
		testMessage.setRenderer(renderer);
		renderer.addAction(new Action("Edit Message", null,
				Resources.INSTANCE.messageEdit(), "edit",
				new SelectionListener<GridModelEvent>() {
					@Override
					public void componentSelected(GridModelEvent ce) {
						launchEditMessageDialog(ce.getModel(), false);
					}
				}));
		renderer.addAction(new Action("Test SMS", null,
				Resources.INSTANCE.messageTest(), "test",
				new SelectionListener<GridModelEvent>() {
			@Override
			public void componentSelected(GridModelEvent ce) {
				CampaignMessage cm = ce.getModel().getBean();
				launchTestSMSDialog(cm);
			}
		}));

		// Delete messages not allowed for DAILY campaigns (MOBILISR-382).
		if ( (campaign != null) && (campaign.getType()!= CampaignType.DAILY) ) {
			renderer.addAction(new Action("Delete Message", null,
					Resources.INSTANCE.delete(), "delete",
					new SelectionListener<GridModelEvent>() {
				@Override
				public void componentSelected(GridModelEvent ce) {
					BeanModel bm = ce.getModel();
					deleteMessage(bm);
				}
			}));
		}

		configs.add(testMessage);
		return configs;
	}

	protected void deleteMessage(BeanModel bm) {
		// Safety (see MOBILISR-382)
		if (campaign.getType() == CampaignType.DAILY) {
			MessageBoxWithIds.alert("Delete invalid",
					"Delete message not valid for this campaign type", null);
			return;
		}

		CampaignMessage campaignMessage = (CampaignMessage) bm.getBean();
		if (!newOrChangedMessages.contains(bm)) {
			campaignMessage.setMarkedForDeletion(true);
			newOrChangedMessages.add(bm);
		}
		gridSearch.getStore().remove(bm);
	}

	private void launchEditMessageDialog(final BeanModel beanModel, final boolean addToTable) {
		if (campaign.getType() == CampaignType.FLEXI) {
			editMessageDialog.setDayTimeEnabled(true);
		} else{
			editMessageDialog.setDayTimeEnabled(false);
		}
		CampaignMessage msg = (CampaignMessage) beanModel.getBean();
		final int originalLength = msg.getMessage().length();

		editMessageDialog.bind(beanModel);
		editMessageDialog.show();

		editMessageDialog.setSaveListener(new Listener<ButtonEvent>() {
			public void handleEvent(ButtonEvent be) {
				CampaignMessage message = (CampaignMessage) beanModel.getBean();
				if (!newOrChangedMessages.contains(beanModel)) {
					newOrChangedMessages.add(beanModel);
				}

				if (addToTable){
					gridSearch.getStore().add(beanModel);
				}

				int newLength = message.getMessage().length();
				if (newLength != originalLength){
					adjustCampaignCost(originalLength, newLength);
				}
			}
		});
	}

	public BeanModel convertEntityToBeanModel(CampaignMessage entityobject) {
		BeanModelFactory beanModelFactory = BeanModelLookup.get().getFactory(
				entityobject.getClass());
		BeanModel beanModel = beanModelFactory.createModel(entityobject);
		return beanModel;
	}

	protected Object convertBeanModelToEntity(BeanModel beanModel) {
		Object bean = beanModel.getBean();
		return bean;
	}

	private void adjustCampaignCost(int originalLength, int newLength) {
		if (campaign != null){
			int cost = campaign.getCost();
			int origNum = MobilisrClientUtil.calcualteNumberSMSs(originalLength);
			int newNum = MobilisrClientUtil.calcualteNumberSMSs(newLength);
			int newCost = cost - origNum + newNum;
			campaign.setCost(newCost);
			lblCampaignCost.setText("Campaign Cost: " + newCost + " Credits");
		}
	}

	private void launchTestSMSDialog(final CampaignMessage cm) {
		smsTestDialog.show();
		smsTestDialog.setMyMsisdn(UserContext.getUser().getMsisdn());
		smsTestDialog.getSaveButton().addListener(Events.Select,
				new Listener<ButtonEvent>() {
					@Override
					public void handleEvent(ButtonEvent be) {
						sendTestSMS(cm, smsTestDialog.getSelectedMsisdn());
					}
				});

		smsTestDialog.getCancelButton().addListener(Events.Select,
				new Listener<ButtonEvent>() {
					@Override
					public void handleEvent(ButtonEvent be) {
						smsTestDialog.hide();
					}
				});
	}

	private void sendTestSMS(CampaignMessage cm, String smsTestNumber) {
		User user = UserContext.getUser();
		String campaignTestMessage = cm.getMessage();
		ScheduleServiceAsync schedCampService = (ScheduleServiceAsync) parent
				.getItem(WizardPresenter.CAMPAIGN_SCHEDULE_SERVICE_ASYNC);

		schedCampService.sendTestSMS(campaign, user, smsTestNumber,
				campaignTestMessage, new MobilisrAsyncCallback<Void>() {
					@Override
					public void onSuccess(Void result) {
						MessageBoxWithIds.info("Success",
								"Message successfully sent.", null);
					}
				});
	}

	@Override
	public void buildWidget(ListStore<BeanModel> store,
			RemoteStoreFilterField<BeanModel> filter) {
		List<ColumnConfig> configs = createColumnConfigs();

		addNewLink.setName("addNewMessage");
		renderEntityListGrid(store, filter, configs, addNewLink, importMsgsBtn, exportMsgsBtn);
	}

	private void renderEntityListGrid(ListStore<BeanModel> store,
			StoreFilterField<BeanModel> filter, List<ColumnConfig> configs,
			Anchor anchor, MyGXTButton button1, MyGXTButton button2) {

		ColumnModel cm = new ColumnModel(configs);

		entityList = new Grid<BeanModel>(store, cm);

		GridView gridView = new GridView();
		gridView.setAutoFill(true);
		gridView.setForceFit(true);
		gridView.setEmptyText("No records currently exist");

		entityList.setLoadMask(true);
		entityList.setView(gridView);
		entityList.setBorders(true);
		entityList.setHeight("250%");
		entityList.setStripeRows(true);

		listContentPanel.add(entityList);

		ToolBar topToolBar = new ToolBar();
		topToolBar.setAlignment(HorizontalAlignment.LEFT);
		topToolBar.setIntStyleAttribute("padding", 5);
		topToolBar.setSpacing(10);
		lblCampaignCost.setId("campaignCost");
		topToolBar.add(new AdapterField(lblCampaignCost));
		topToolBar.add(new SeparatorToolItem());

		topToolBar.add(new AdapterField(anchor));
		topToolBar.add(new SeparatorToolItem());
		topToolBar.add(new AdapterField(button1));
		topToolBar.add(new SeparatorToolItem());
		topToolBar.add(new AdapterField(button2));

		if (filter != null){
			filter.bind(store);
			filter.setEmptyText("");
		}

		listContentPanel.setTopComponent(topToolBar);
	}

	@Override
	public ViewModel<Campaign> getFormObject() {
		ViewModel<Campaign> vem = new ViewModel<Campaign>();
		List<CampaignMessage> list = new ArrayList<CampaignMessage>();
		for (BeanModel bm : newOrChangedMessages) {
			CampaignMessage bean = bm.getBean();
			// remove negative id's
			if (bean.getId() < 0){
				bean.setId(null);
			}
			list.add(bean);
		}
		campaign.setCampaignMessages(list);
		vem.setModelObject(campaign);
		newOrChangedMessages.clear();
		return vem;
	}

	@Override
	public void setFormObject(ViewModel<Campaign> vem) {
		campaign = vem.getModelObject();
		gridSearch.getLoader().load(0, Constants.INSTANCE.pageSize());
		newOrChangedMessages.clear();
		reconfigureGrid(createColumnConfigs());
		updateTitle();
	}

	private void updateTitle() {
		String type = "";

		if (campaign.getType()==CampaignType.DAILY){
			addNewLink.setVisible(false);
			type = "Recipient-Specific";
		}else if (campaign.getType() == CampaignType.FLEXI){
			addNewLink.setVisible(true);
			addNewLink.setEnabled(!campaign.isActive());
			type = "Generic";
		}else if (campaign.getType() == CampaignType.FIXED){
			addNewLink.setVisible(true);
			addNewLink.setVisible(!campaign.isActive());
			type = "Fixed";
		}

		String title = (campaign.isPersisted() ? "Editing Campaign '" + campaign.getName()
				+ "'" : "Create New "+type+" Campaign")
				+ ": Step 4 of 4";
		titleLabel.setText(title);

		lblCampaignCost.setText("Campaign Cost: " + campaign.getCost() + " Credits");
	}

	@Override
	public void goNext() {
		parent.goNext(getFormObject());
	}

	@Override
	public void cancel() {
		parent.cancel();
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
	public void resetForm() {
	}

	@Override
	public Campaign getCampaign() {
		return campaign;
	}

	@Override
	public void reloadMessages(Campaign campaign) {
		this.campaign = campaign;
		gridSearch.getLoader().load(0, Constants.INSTANCE.pageSize());
		lblCampaignCost.setText("Campaign Cost: " + campaign.getCost() + " Credits");
	}
}