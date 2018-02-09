package org.celllife.mobilisr.client.campaign.presenter;

import java.util.ArrayList;
import java.util.List;

import org.celllife.mobilisr.client.Constants;
import org.celllife.mobilisr.client.MobilisrEvents;
import org.celllife.mobilisr.client.URLUtil;
import org.celllife.mobilisr.client.app.presenter.DirtyPresenter;
import org.celllife.mobilisr.client.campaign.CampaignEventBus;
import org.celllife.mobilisr.client.campaign.ManageRecipientsView;
import org.celllife.mobilisr.client.campaign.view.AddContactWizard;
import org.celllife.mobilisr.client.campaign.view.BulkAddContactWizard;
import org.celllife.mobilisr.client.campaign.view.BulkAddContactWizard.BulkType;
import org.celllife.mobilisr.client.campaign.view.ManageRecipientsViewImpl;
import org.celllife.mobilisr.client.model.ViewModel;
import org.celllife.mobilisr.client.reporting.EntityStoreProvider;
import org.celllife.mobilisr.client.reporting.EntityStoreProviderImpl;
import org.celllife.mobilisr.client.reporting.PConfigDialog;
import org.celllife.mobilisr.client.view.gxt.BusyIndicator;
import org.celllife.mobilisr.client.view.gxt.CsvFileDialog;
import org.celllife.mobilisr.client.view.gxt.GridModelEvent;
import org.celllife.mobilisr.client.view.gxt.MessageBoxWithIds;
import org.celllife.mobilisr.client.view.gxt.MobilisrAsyncCallback;
import org.celllife.mobilisr.client.view.gxt.ModelUtil;
import org.celllife.mobilisr.client.view.gxt.MyGXTButton;
import org.celllife.mobilisr.client.view.gxt.StepValidator;
import org.celllife.mobilisr.client.view.gxt.WizardCard;
import org.celllife.mobilisr.client.view.gxt.grid.MyGXTPaginatedGridSearch;
import org.celllife.mobilisr.constants.CampaignType;
import org.celllife.mobilisr.domain.Campaign;
import org.celllife.mobilisr.domain.CampaignContact;
import org.celllife.mobilisr.domain.Contact;
import org.celllife.mobilisr.domain.ContactGroup;
import org.celllife.mobilisr.domain.ContactMsgTime;
import org.celllife.mobilisr.service.gwt.AdminServiceAsync;
import org.celllife.mobilisr.service.gwt.CampaignServiceAsync;
import org.celllife.mobilisr.service.gwt.ContactServiceAsync;
import org.celllife.mobilisr.service.gwt.CsvDataReport;
import org.celllife.mobilisr.service.gwt.ExportServiceAsync;
import org.celllife.pconfig.model.EntityParameter;
import org.celllife.pconfig.model.Pconfig;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FormEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.store.Store;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Label;
import com.google.inject.Inject;
import com.mvp4g.client.annotation.Presenter;
import com.mvp4g.client.history.NavigationEventCommand;

@Presenter(view = ManageRecipientsViewImpl.class)
public class ManageRecipientsPresenter extends DirtyPresenter<ManageRecipientsView,CampaignEventBus> {

	@Inject
	private AdminServiceAsync adminService;

	@Inject
	private ContactServiceAsync contactService;

	@Inject
	private CampaignServiceAsync campaignService;
	
	@Inject 
	private ExportServiceAsync exportService;

	private MyGXTPaginatedGridSearch<Contact> gridSearchAvailable;
	private MyGXTPaginatedGridSearch<CampaignContact> gridSearchSelected;
	private EntityStoreProvider entityStoreProvider;
	protected Campaign campaign;

	protected List<ContactMsgTime> defaultMsgTimes;

	private Listener<AppEvent> completionListener;

	private Window window;

	private Listener<BaseEvent> windowHideListener;

	@Override
	public void bindView() {
		entityStoreProvider = new EntityStoreProviderImpl(adminService);

		gridSearchAvailable = new MyGXTPaginatedGridSearch<Contact>(
				Contact.PROP_MSISDN, Constants.INSTANCE.pageSize()) {
			@Override
			public void rpcListServiceCall(PagingLoadConfig config,
					AsyncCallback<PagingLoadResult<Contact>> callback) {
				contactService.listAllContactsForOrganization(
						campaign.getOrganization(), config, callback);
			}
		};

		gridSearchSelected = new MyGXTPaginatedGridSearch<CampaignContact>(
				CampaignContact.PROP_MSISDN, Constants.INSTANCE.pageSize()) {
			@Override
			public void rpcListServiceCall(PagingLoadConfig config,
					AsyncCallback<PagingLoadResult<CampaignContact>> callback) {
				contactService.listAllCampaignContactsForCampaign(campaign,
						config, true, false, callback);
			}
		};

		Listener<BaseEvent> selectedStoreListener = new Listener<BaseEvent>() {
			@Override
			public void handleEvent(BaseEvent be) {
				updateBulkRemoveState();
			}
		};
		gridSearchSelected.getStore().addListener(Store.DataChanged,selectedStoreListener);
		gridSearchSelected.getStore().addListener(Store.Add,selectedStoreListener);
		gridSearchSelected.getStore().addListener(Store.Remove,selectedStoreListener);

		getView().buildWidgets(gridSearchAvailable, gridSearchSelected);

		getView().getAddContactAction().setListener(new SelectionListener<GridModelEvent>() {
			@Override
			public void componentSelected(GridModelEvent ce) {
				final Contact contact = ce.getModel().getBean();	
				addSingleContact(campaign, contact);				
			}
		});		

		getView().getRemoveRecipientAction().setListener(new SelectionListener<GridModelEvent>() {
			@Override
			public void componentSelected(GridModelEvent ce) {
				final BeanModel beanModel = ce.getModel();
				final CampaignContact contact = beanModel.getBean();
				MessageBoxWithIds.confirm("Remove contact",
					"Are you sure you want to remove this contact?",
					new Listener<MessageBoxEvent>() {
						@Override
						public void handleEvent(MessageBoxEvent be) {
							if (be.getButtonClicked().getItemId() == Dialog.YES) {
								getView().setDirty(true);
								BusyIndicator.showBusyIndicator();															
								campaignService.removeContactFromCampaign(campaign,contact,
									new MobilisrAsyncCallback<Void>() {
										@Override
										public void onSuccess(Void result) {										
											BusyIndicator.hideBusyIndicator();
											getView().setDirty(true);
											gridSearchSelected.getStore().remove(beanModel);
										}
								});
							}
						}
					});
			}
		});

		getView().getAddNewContactButton().addSelectionListener(
				new SelectionListener<ButtonEvent>() {
					@Override
					public void componentSelected(ButtonEvent ce) {
						CampaignContact contact = new CampaignContact(campaign,new Contact());
						contact.setContactMsgTimes(getContactMessageTimes(contact));
						addEditContact(contact, true);
					}
				});

		getView().getDoneButton().addSelectionListener(
				new SelectionListener<ButtonEvent>() {
					@Override
					public void componentSelected(ButtonEvent ce) {
						handleActionCompetion(null);
					}
				});

		getView().getMsisdnAnchor().setSelectionListener(
				new SelectionListener<GridModelEvent>() {
					@Override
					public void componentSelected(GridModelEvent ce) {
						CampaignContact contact = ce.getModel().getBean();
						addEditContact(contact, false);
					}
				});

		createAddMenuButtons();
		createRemoveMenuButtons();
	}
	
	protected void updateBulkRemoveState() {
		int count = gridSearchSelected.getStore().getCount();
		getView().enableBulkRemove(count > 0);
	}

	protected List<ContactMsgTime> getContactMessageTimes(
			CampaignContact contact) {
		if (defaultMsgTimes == null) {
			return null;
		}

		List<ContactMsgTime> times = new ArrayList<ContactMsgTime>(
				defaultMsgTimes.size());
		for (ContactMsgTime msg : defaultMsgTimes) {
			times.add(new ContactMsgTime(msg.getMsgTime(), msg.getMsgSlot(),
					contact, campaign));
		}

		return times;
	}

	private void createAddMenuButtons() {
		Menu menu = new Menu();

		MenuItem addGroupItem = new MenuItem("Add group");
		addGroupItem.addSelectionListener(new SelectionListener<MenuEvent>() {
			@Override
			public void componentSelected(MenuEvent ce) {
				addGroup();
			}
		});
		menu.add(addGroupItem);

		MenuItem addCsvItem = new MenuItem("Add from CSV");
		addCsvItem.addSelectionListener(new SelectionListener<MenuEvent>() {
			@Override
			public void componentSelected(MenuEvent ce) {
				addCSV();
			}
		});
		menu.add(addCsvItem);

		MenuItem addAllItem = new MenuItem("Add all");
		addAllItem.addSelectionListener(new SelectionListener<MenuEvent>() {
			@Override
			public void componentSelected(MenuEvent ce) {
				addAll();
			}
		});
		menu.add(addAllItem);

		getView().getBulkAddButton().setMenu(menu);
	}
	
	protected void addGroup() {
		
		final CampaignContact contact = new CampaignContact(null,new Contact());
		contact.setContactMsgTimes(getContactMessageTimes(contact));
		final BulkAddContactWizard bulkAddContactWizard = new BulkAddContactWizard(contact,entityStoreProvider, BulkType.GROUP);
				
		bulkAddContactWizard.addListener(MobilisrEvents.WizardFinish,
			new Listener<BaseEvent>() {
				@Override
				public void handleEvent(BaseEvent be) {
					
					List<ContactMsgTime> contactMsgTimes = contact.getContactMsgTimes();					
					final Campaign campaign = getView().getCampaign();
					BusyIndicator.showBusyIndicator("Adding Group To Campaign");
					
					campaignService.addGroupToCampaign(bulkAddContactWizard.getSelGroup(), campaign, contactMsgTimes, bulkAddContactWizard.getStartOver(),
							new MobilisrAsyncCallback<Void>() {
								@Override
								public void onSuccess(Void result) {
									BusyIndicator.hideBusyIndicator();
									getView().setDirty(true);
									gridSearchSelected.getLoader().load(0,Constants.INSTANCE.pageSize());
								}
							});
					
				}
			});
		
		bulkAddContactWizard.show();
	}
	
	protected void addAll() {
		
		final CampaignContact contact = new CampaignContact(null,new Contact());
		contact.setContactMsgTimes(getContactMessageTimes(contact));
		final BulkAddContactWizard bulkAddContactWizard = new BulkAddContactWizard(contact, entityStoreProvider, BulkType.ALL);
				
		Listener<BaseEvent> finishListener = new Listener<BaseEvent>() {
			@Override
			public void handleEvent(BaseEvent be) {
				
				List<ContactMsgTime> contactMsgTimes = contact.getContactMsgTimes();					
				final Campaign campaign = getView().getCampaign();
				BusyIndicator.showBusyIndicator("Adding All Contacts To Campaign");
				
				campaignService.addAllContactsToCampaign(campaign, contactMsgTimes, bulkAddContactWizard.getStartOver(), new MobilisrAsyncCallback<Void>() {
							@Override
							public void onSuccess(Void result) {
								BusyIndicator.hideBusyIndicator();
								getView().setDirty(true);
								gridSearchSelected.getLoader().load(0,Constants.INSTANCE.pageSize());
							}
						});
				
			}
		};
		
		if (bulkAddContactWizard.getCards().size() > 0){
			bulkAddContactWizard.addListener(MobilisrEvents.WizardFinish, finishListener);
			bulkAddContactWizard.show();
		} else {
			finishListener.handleEvent(null);
		}
	}
	
	protected void addCSV() {
		final CampaignContact contact = new CampaignContact(null,new Contact());
		contact.setContactMsgTimes(getContactMessageTimes(contact));
		final BulkAddContactWizard bulkAddContactWizard = new BulkAddContactWizard(contact,entityStoreProvider,BulkType.CSV_FILE);
				
		bulkAddContactWizard.addListener(MobilisrEvents.WizardFinish,
			new Listener<BaseEvent>() {
				@Override
				public void handleEvent(BaseEvent be) {
					List<ContactMsgTime> contactMsgTimes = contact.getContactMsgTimes();
					
					final String path = bulkAddContactWizard.getPath();
					final long numRecords = bulkAddContactWizard.getNumRecords();
					final Campaign campaign = getView().getCampaign();
	
					final List<String> fieldOrder = new ArrayList<String>();
					fieldOrder.add(Contact.PROP_FIRST_NAME);
					fieldOrder.add(Contact.PROP_LAST_NAME);
					fieldOrder.add(Contact.PROP_MSISDN);
	
					BusyIndicator.showBusyIndicator("Adding CSV Contacts To Campaign");
					
					campaignService.addCsvFileToCampaign(fieldOrder, path, campaign.getOrganization(), campaign, contactMsgTimes, bulkAddContactWizard.getStartOver(), new MobilisrAsyncCallback<Long>() {
						
						@Override
						public void onSuccess(final Long jobId) {
							BusyIndicator.hideBusyIndicator();
							startCsvProgressDialog(jobId, numRecords, path, new MobilisrAsyncCallback<CsvDataReport>() {
								@Override
								public void onSuccess(CsvDataReport result) {
									getView().setDirty(true);
									gridSearchSelected.getLoader().load(0, Constants.INSTANCE.pageSize());		
									showCSVSummary(result.getNumOfRecordsStored(), result.getNumOfErrors(), jobId, path);
								}
							});
						}
					});
					
				}
			});
		
		bulkAddContactWizard.show();
	}
	
	private void startCsvProgressDialog(final Long jobId, final Long totalRecords, final String filePath, final MobilisrAsyncCallback<CsvDataReport> mobilisrAsyncCallback) {

		final MessageBox box = MessageBox.progress("Please wait", "Saving " + totalRecords + " Contacts", "Initializing...");
		box.setModal(false);
		box.getProgressBar().setAutoWidth(true);

		final Timer t = new Timer() {
			
			Integer numOfRecordsStored = 0;
			CsvDataReport returnedReport = new CsvDataReport();
			
			/* 
			 * a count of the number of records still to be processed
			 */
			long recordsLeft;
			
			/*
			 * keeps track of how many times the timer has run where recordsLeft == 1 
			 * 
			 * We need this in case the last line of the file is blank, then the line count
			 * will always be 1 greater than the number processed.
			 */
			int noChange = 0;
			
			@Override
			public void run() {

				if (numOfRecordsStored < totalRecords) {
					recordsLeft = totalRecords - numOfRecordsStored;
					if (recordsLeft == 1) {
						noChange++;
					}
					
					if (noChange > 3){
						cancel();
						box.close();
						mobilisrAsyncCallback.onSuccess(returnedReport);
					} else {
						contactService.getNumOfRecordsStoredForCsvImport(filePath, jobId, new MobilisrAsyncCallback<CsvDataReport>() {
	
							@Override
							public void onSuccess(CsvDataReport csvDataReport) {
								returnedReport = csvDataReport;
								numOfRecordsStored = csvDataReport.getNumOfRecordsStored();
								box.getProgressBar().updateProgress(((double) numOfRecordsStored / (double) totalRecords), "Stored " + numOfRecordsStored + " contacts");
								box.getProgressBar().setVisible(true);
							}
	
							@Override
							public void onFailure(Throwable cause) {
								cancel();
								super.onFailure(cause);
								box.close();
							}
						});
					}

				}
				
				else {
					cancel();
					box.close();
					mobilisrAsyncCallback.onSuccess(returnedReport);
				}
			}
		};
		
		t.scheduleRepeating(2000);
	}

	private void showCSVSummary(int numOfContactsStores, int numOfErrors, final long jobId, final String filePath) {

		final Window csvSummaryWindow = new Window();		
		csvSummaryWindow.setHeading("Saving Contacts");
		csvSummaryWindow.setLayout(new RowLayout());
		csvSummaryWindow.setWidth(350);
		csvSummaryWindow.setHeight(200);
		csvSummaryWindow.setPlain(true);
		csvSummaryWindow.setButtonAlign(HorizontalAlignment.CENTER);
		
		Anchor exportDataAnchor = new Anchor("Export Data", "#");
		exportDataAnchor.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent arg0) {
				final MessageBox wait = MessageBox.wait("Exporting", "Exporting import errors", null);
				exportService.exportContactImportErrors(filePath, jobId, new MobilisrAsyncCallback<String>() {
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
		});
		
		
		MyGXTButton doneButton = new MyGXTButton("Close");		
		doneButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				csvSummaryWindow.hide();
			}
		});

		Label msgSuccesStoredLabel = new Label(numOfContactsStores + " contact(s) have succesfully been imported.");
		csvSummaryWindow.add(msgSuccesStoredLabel, new RowData(1, 1, new Margins(5)));
		
		if (numOfErrors > 0) {
			String numOfErrorText = "We found "
					+ numOfErrors
					+ " errors in your data. To see a detailed list of these please select the link below to export the data";
			Label msgErrorStoredLabel = new Label(numOfErrorText);			
			csvSummaryWindow.add(msgErrorStoredLabel, new RowData(1, 1, new Margins(5)));
			csvSummaryWindow.add(exportDataAnchor, new RowData(1, 1, new Margins(5)));
		}

		csvSummaryWindow.addButton(doneButton);
		csvSummaryWindow.setVisible(true);
	}
		
	private void createRemoveMenuButtons() {
		Menu menu = new Menu();

		MenuItem removeGroupItem = new MenuItem("Remove group");
		removeGroupItem.addSelectionListener(new SelectionListener<MenuEvent>() {
			@Override
			public void componentSelected(MenuEvent ce) {
				removeGroup(new MobilisrAsyncCallback<Long>() {
					@Override
					public void onSuccess(Long result) {
						Campaign campaign = getView().getCampaign();
						BusyIndicator.showBusyIndicator("Removing Group From Campaign");
						campaignService.removeGroupFromCampaign(result,campaign,
							new MobilisrAsyncCallback<Void>() {
								@Override
								public void onSuccess(Void result) {
									BusyIndicator.hideBusyIndicator();
									getView().setDirty(true);
									gridSearchSelected.getLoader().load(0,Constants.INSTANCE.pageSize());
								}
							});
					}
				}, false);
			}
		});
		menu.add(removeGroupItem);

		MenuItem removeCsvItem = new MenuItem("Remove by CSV");
		removeCsvItem.addSelectionListener(new SelectionListener<MenuEvent>() {
			@Override
			public void componentSelected(MenuEvent ce) {
				removeCSV();
			}
		});
		menu.add(removeCsvItem);

		MenuItem removeAllItem = new MenuItem("Remove all");
		removeAllItem.addSelectionListener(new SelectionListener<MenuEvent>() {
			@Override
			public void componentSelected(MenuEvent ce) {
				MessageBoxWithIds.confirm("Remove all",
					"Are you sure you want to remove all the contacts from this campaign?",
					new Listener<MessageBoxEvent>() {
						@Override
						public void handleEvent(MessageBoxEvent be) {
							if (be.getButtonClicked().getItemId() == Dialog.YES) {
								BusyIndicator.showBusyIndicator("Removing All Contacts From Campaign");
								campaignService.removeAllContactsFromCampaign(campaign,
									new MobilisrAsyncCallback<Void>() {
										@Override
										public void onSuccess(Void result) {
											BusyIndicator.hideBusyIndicator();
											getView().setDirty(true);
											gridSearchSelected.getLoader().load(0,Constants.INSTANCE.pageSize());
										}
									});
							}
						}
					});
			}
		});
		menu.add(removeAllItem);

		getView().getBulkRemoveButton().setMenu(menu);
	}
		
	public void removeGroup(final AsyncCallback<Long> callback,
			boolean isAdd) {
		Pconfig config = new Pconfig(null, "AddGroupToCampaign");
		final EntityParameter group = new EntityParameter("group", "Group:");
		group.setDisplayProperty(ContactGroup.PROP_GROUP_NAME);
		group.setValueProperty(ContactGroup.PROP_ID);
		group.setEntityClass(ContactGroup.class.getName());
		config.addParameter(group);

		final PConfigDialog dialog = new PConfigDialog(entityStoreProvider,
				config, false);
		dialog.getSaveButton().setText(isAdd ? "Add group" : "Remove group");
		dialog.getSaveButton().addSelectionListener(
				new SelectionListener<ButtonEvent>() {
					@Override
					public void componentSelected(ButtonEvent ce) {
						callback.onSuccess(Long.valueOf(group.getValue()));
						dialog.hide();
					}
				});
		dialog.show();
	}

	protected void removeCSV() {		
		CsvFileDialog dialog = new CsvFileDialog();
		dialog.setHeading("Upload CSV File");
		dialog.setInfoText("The CSV file must contain the following only one column: Mobile Number");
				
		dialog.setSubmitListener(new Listener<FormEvent>() {
			@Override
			public void handleEvent(FormEvent fe) {
				String data = fe.getResultHtml();
				String[] split = data.split(";");
				final String path = split[0]; 
				final Long numRecords = Long.parseLong(split[1]);
				
				Campaign campaign = getView().getCampaign();
				BusyIndicator.showBusyIndicator("Removing CSV Contacts From Campaign");
				
				List<String> fieldOrder = new ArrayList<String>();
				fieldOrder.add(Contact.PROP_MSISDN);
				
				campaignService.removeCsvFileFromCampaign(fieldOrder, path, campaign, new MobilisrAsyncCallback<Void>() {
					@Override
					public void onSuccess(Void csvReport) {
						BusyIndicator.hideBusyIndicator();
						getView().setDirty(true);
						gridSearchSelected.getLoader().load(0, Constants.INSTANCE.pageSize());
						MessageBox.info("CSV Remove Complete", numRecords + " Contacts Processed", null);						
					}
				});				
			}
		});
		dialog.show();
	}
	
	protected void addSingleContact(final Campaign campaign, final Contact contact) {
		if (contact.isInvalid()){
			MessageBoxWithIds
					.alert("Invalid Contact",
							"<p>This contact has an invalid mobile number.</p>" +
							"<p>Please change the mobile number before adding " +
							"them to the campaign.</p>",
							null);
			return;
		}
		BusyIndicator.showBusyIndicator();
		campaignService.getCampaignContact(campaign,
				contact.getMsisdn(),
				new MobilisrAsyncCallback<CampaignContact>() {
					@Override
					public void onSuccess(CampaignContact result) {
						BusyIndicator.hideBusyIndicator();
						if (result == null) {
							CampaignContact campaignContact = new CampaignContact(campaign, contact);
							campaignContact.setContactMsgTimes(getContactMessageTimes(campaignContact));
							addEditContact(campaignContact, true);
						} else if (result.getEndDate() != null){
							addEditContact(result, true);
						} else {
							MessageBoxWithIds.alert("Contact is already on the campaign", 
									"This contact is already on the campaign.", null);
						}
					}
				});
	}
	
	public void addEditContact(CampaignContact contact, boolean isAdd) {

		 /* don't show the wizard if 
		  * 	we are adding an existing contact to a FIXED campaign
		  * 	we are adding an existing contact to a FLEXI campaign that has not been added before
		  */
		boolean isExistingContact = contact.getContact().isPersisted();
		boolean dontShowWizard = isExistingContact && CampaignType.FIXED.equals(campaign.getType());
		dontShowWizard |= isExistingContact && CampaignType.FLEXI.equals(campaign.getType()) && contact.getEndDate() == null;
		if (isAdd && dontShowWizard){
			saveCampaignContact(contact);
		} else {			
			final AddContactWizard contactWizard = new AddContactWizard(contact, isAdd);
			contactWizard.addListener(MobilisrEvents.WizardFinish,
					new Listener<BaseEvent>() {
						@Override
						public void handleEvent(BaseEvent be) {
							saveCampaignContact(contactWizard.getContact());
						}
					});
			if (!contact.getContact().isPersisted()) {
				addContactExistsValidator(contactWizard);
			}
			contactWizard.show();
		}
	}

	private void saveCampaignContact(final CampaignContact campaignContact) {
		BusyIndicator.showBusyIndicator();
		
		final boolean isNewCampaignContact = !campaignContact.isPersisted() || campaignContact.getEndDate() != null;
		final boolean isEditContact = campaignContact.getContact().isPersisted();
		
		campaignContact.setEndDate(null);
		
		campaignService.saveOrUpdateCampaignContact(campaignContact,
			new MobilisrAsyncCallback<CampaignContact>() {
				@Override
				public void onSuccess(CampaignContact savedContact) {

					getView().setDirty(true);

					BeanModel contactModel = ModelUtil.convertEntityToBeanModel(savedContact
									.getContact());
					if (isEditContact) {
						gridSearchAvailable.getStore().update(contactModel);
					} else {
						gridSearchAvailable.getStore().insert(contactModel, 0);
					}

					BeanModel campaignContactModel = ModelUtil
							.convertEntityToBeanModel(savedContact);
					if (isNewCampaignContact) {
						gridSearchSelected.getStore().insert(campaignContactModel, 0);
					} else {
						gridSearchSelected.getStore().update(campaignContactModel);
					}
					
					BusyIndicator.hideBusyIndicator();
				}
			});
	}

	private void addContactExistsValidator(final AddContactWizard contactWizard) {

		contactWizard.setContactValidator(new StepValidator() {
			@Override
			public void validate(WizardCard wizardCard, int currentStep, final AsyncCallback<Void> callback) {

				final String msisdn = contactWizard.getContact().getContact().getMsisdn();
				BusyIndicator.showBusyIndicator();
				contactService.checkMsisdnExists(campaign.getOrganization(), msisdn, new MobilisrAsyncCallback<Boolean>() {
					@Override
					public void onFailure(Throwable error) {

						super.onFailure(error);
						callback.onFailure(null);
					}

					@Override
					public void onSuccess(Boolean exists) {

						BusyIndicator.hideBusyIndicator();
						if (exists) {
							callback.onFailure(null);
							contactWizard.hide();
							MessageBox.confirm("Contact Exists", "This contact already exists. Would you like to add him/her to the campaign?", new Listener<MessageBoxEvent>() {
								@Override
								public void handleEvent(MessageBoxEvent be) {

									if (be.getButtonClicked().getItemId().equals(Dialog.YES)) {	
										//add contact to campaign, since we know it already exists
										addSingleContact(campaign, contactWizard.getContact().getContact());
									}
								}
							});
						}
						else {
							callback.onSuccess(null);
						}
							

					}
				});
			}
		});
	}

	public void onShowCampRecipientManage(ViewModel<Campaign> viewEntityModel, 
			Listener<AppEvent> completionListener, boolean showInWindow) {
		this.completionListener = completionListener;
		getEventBus().setNavigationConfirmation(this);
		isAdminView(viewEntityModel);
		getView().setFormObject(viewEntityModel);

		campaign = viewEntityModel.getModelObject();
		entityStoreProvider.restrictResultsToOrganization(campaign
				.getOrganization());
		gridSearchAvailable.getLoader().load(0, Constants.INSTANCE.pageSize());
		gridSearchSelected.getLoader().load(0, Constants.INSTANCE.pageSize());

		if (CampaignType.DAILY.equals(campaign.getType())) {
			campaignService.getCampaignMessageTimes(campaign,
					new MobilisrAsyncCallback<List<ContactMsgTime>>() {
						@Override
						public void onSuccess(List<ContactMsgTime> result) {
							defaultMsgTimes = result;
						}
					});
		} else {
			defaultMsgTimes = null;
		}

		if (showInWindow){
			window = new Window();
			window.setHeading("Manage recipients");
			window.setModal(true);
			window.setSize(1000, 600);
			window.add(getView().getViewWidget());
			windowHideListener = new Listener<BaseEvent>() {
				@Override
				public void handleEvent(BaseEvent be) {
					handleActionCompetion(null);
				}
			};
			window.addListener(Events.Hide, windowHideListener);
			window.show();
		} else {
			windowHideListener = null;
			window = null;
			getEventBus().setRegionRight(this);
		}
	}

	@Override
	public void confirm(final NavigationEventCommand event) {
		handleActionCompetion(event);
	}

	private void handleActionCompetion(final NavigationEventCommand event) {
		AppEvent appEvent = new AppEvent(MobilisrEvents.NavigationChange);
		appEvent.setData(campaign);
		appEvent.setData("dirty", getView().isDirty());
		if (event != null)
			appEvent.setData("navigationEvent", event);
		
		getView().setDirty(false);
		completionListener.handleEvent(appEvent);
		if (window != null && window.isVisible()){
			if (windowHideListener != null)	
				window.removeListener(Events.Hide, windowHideListener);
			window.hide();
		}
	}
	
}
