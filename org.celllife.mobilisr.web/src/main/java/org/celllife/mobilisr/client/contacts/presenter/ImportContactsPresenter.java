package org.celllife.mobilisr.client.contacts.presenter;

import java.util.ArrayList;
import java.util.List;

import org.celllife.mobilisr.client.Constants;
import org.celllife.mobilisr.client.URLUtil;
import org.celllife.mobilisr.client.UserContext;
import org.celllife.mobilisr.client.app.presenter.MobilisrBasePresenter;
import org.celllife.mobilisr.client.contacts.ContactEventBus;
import org.celllife.mobilisr.client.contacts.GenericContactManagementView;
import org.celllife.mobilisr.client.contacts.ImportContactsView;
import org.celllife.mobilisr.client.contacts.handler.GenericContactManagementEventHandler;
import org.celllife.mobilisr.client.contacts.handler.GroupToContactEventHandler;
import org.celllife.mobilisr.client.contacts.view.ImportContactsViewImpl;
import org.celllife.mobilisr.client.view.gxt.BusyIndicator;
import org.celllife.mobilisr.client.view.gxt.ChangeAwareListStore;
import org.celllife.mobilisr.client.view.gxt.MobilisrAsyncCallback;
import org.celllife.mobilisr.domain.ContactGroup;
import org.celllife.mobilisr.service.gwt.ContactServiceAsync;
import org.celllife.mobilisr.service.gwt.CsvDataReport;
import org.celllife.mobilisr.service.gwt.ExportServiceAsync;


import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FormEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Timer;
import com.google.inject.Inject;
import com.mvp4g.client.annotation.Presenter;

@Presenter(view=ImportContactsViewImpl.class)
public class ImportContactsPresenter extends MobilisrBasePresenter<ImportContactsView, ContactEventBus> {
	
	private GenericContactManagementEventHandler<ContactGroup> groupManagementPresenter;
	
	@Inject
	private ContactServiceAsync ContactServiceAsync;
	
	@Inject 
	private ExportServiceAsync exportService;
	
	private String filePath;
	private int totalRecords;
	private Integer numOfRecordsStored = 0;
	private Long jobId;
	Integer numOfErrors = 0;	
	
	@Override
	public void bindView() {
		GenericContactManagementView<ContactGroup> groupManagement = getView().getAddPopup();
		groupManagementPresenter = new GroupToContactEventHandler(groupManagement);
		
		getView().getFormPanel().addListener(Events.BeforeSubmit, new Listener<FormEvent>() {
			@Override
			public void handleEvent(FormEvent fe) {
				BusyIndicator.showBusyIndicator("Uploading file");
			}
		});
		
		//This event gets called after the form has been submitted
		//It is in this event, that we start reading n amount of records from the file and 
		//add records to the grid for user to view sample and select field heading for respective columns
		getView().getFormPanel().addListener(Events.Submit, new Listener<FormEvent>() {
			@Override
			public void handleEvent(FormEvent fe) {
				BusyIndicator.hideBusyIndicator();
				readSamplesFromCsvFile(fe);
			}
		});
		
		
		getView().getCancelButton().addSelectionListener(new SelectionListener<ButtonEvent>() {

			@Override
			public void componentSelected(ButtonEvent ce) {
				getView().setErrorMessage(null);
				getEventBus().showContactList(null);
			}
			
		});
		
		//It is this event where the actual saving of the information in the CSV is saved in the database
		//Some of the features in this event include: 
		//1. Progress update every n periodic second 
		//2. Information dialog showing saved records and if errors csv export option with records
		getView().getCsvSaveButton().addSelectionListener(new SelectionListener<ButtonEvent>() {
			
			@Override
			public void componentSelected(ButtonEvent ce) {
				if(getView().getCsvSaveButton().getText().equals("Save Contacts")){
					
					List<String> selectedFields = getView().getContactFieldsForCsvColumns();
					if(selectedFields != null){
						
						ChangeAwareListStore<BeanModel> selectedEntityStore = groupManagementPresenter.getSelectedEntityStore();
						List<BeanModel> addedGroupList = selectedEntityStore.getAdded();
						List<ContactGroup> groupList = convertModelListToGroupList(addedGroupList);
						
						ContactServiceAsync.saveCSVContacts(selectedFields, filePath, UserContext.getUser().getOrganization(), groupList, new MobilisrAsyncCallback<Long>() {
							@Override
							public void onFailure(Throwable arg0) {
								getView().setErrorMessage("An error occured while trying to save CSV contacts");
							}

							@Override
							public void onSuccess(final Long jobexecutionId) {
								startCsvProgressDialog(jobexecutionId); 
							}
						});
					}
				}else{
					getView().getFormPanel().submit();
				}
			}
		});
		
		getView().getExportDataAnchor().addClickHandler(new ClickHandler() {
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
		
		getView().getCsvSummaryDoneButton().addSelectionListener(new SelectionListener<ButtonEvent>() {
			
			@Override
			public void componentSelected(ButtonEvent ce) {
				getView().hideCsvSummaryPoup();
				getEventBus().showContactList(null);
			}
		});
		
		getView().getGroupAnchor().addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent arg0) {
				groupManagementPresenter.clearFormValues();
				groupManagementPresenter.getListEntityLoader().load(0, Constants.INSTANCE.pageSize());
				
				getView().getAddPopup().showAddAllAnchor(false);
				getView().getAddPopup().setAddAllMode(GenericContactManagementView.MODE_MAX_ADD, 5);				
				getView().showAddGroupPopup("Imported Contacts");
			}
		});
	}
	
	private void readSamplesFromCsvFile(FormEvent fe) {
		
		String data = fe.getResultHtml();
		String[] split = data.split(";");
		filePath = split[0];
		ContactServiceAsync.readCSVFixedRecordLength(filePath, 100, new MobilisrAsyncCallback<List<List<String>>>() {
			
			@Override
			public void onSuccess(List<List<String>> csvData) {
				totalRecords = csvData.size();
				getView().addCsvSampleDataToGrid(csvData);
			}
		});
	}

	private void startCsvProgressDialog(final Long jobexecutionId) {
		
			jobId = jobexecutionId;
			final MessageBox box = MessageBox.progress("Please wait", "Saving " + totalRecords + " Contacts", "Initializing..."); 
			box.getProgressBar().setAutoWidth(true);
			updateNumOfRecordsStored(box);
	}

	private void updateNumOfRecordsStored(final MessageBox box) {
		final Timer t = new Timer() {  
			
			@Override  
			public void run() {
				if ((numOfRecordsStored+numOfErrors) < totalRecords) {
					ContactServiceAsync.getNumOfRecordsStoredForCsvImport(filePath, jobId, new MobilisrAsyncCallback<CsvDataReport>() {
						
						@Override
						public void onSuccess(CsvDataReport csvDataReport) {
							numOfRecordsStored = csvDataReport.getNumOfRecordsStored();
							numOfErrors = csvDataReport.getNumOfErrors();
							box.getProgressBar().updateProgress(((double)numOfRecordsStored/(double)totalRecords), "Stored " + numOfRecordsStored + " contacts (Errors: " + numOfErrors + " )");  
							box.getProgressBar().setVisible(true);
						}
						
						@Override
						public void onFailure(Throwable cause) {
							cancel();
							box.close();
							super.onFailure(cause);														
						}
					});
					
				} else {
					cancel();  
					box.close();
					getView().showSummaryPopup(numOfRecordsStored, numOfErrors);
				}  
			}  
		};  
		t.scheduleRepeating(2000);
	}
		
	
	public List<ContactGroup> convertModelListToGroupList(List<BeanModel> beanModels){
		List<ContactGroup> contactGroups = new ArrayList<ContactGroup>(); 
		
		for (BeanModel beanModel : beanModels) {
			ContactGroup contactGroup = beanModel.getBean();
			contactGroups.add(contactGroup);
		}
		return contactGroups;
		
	}
	
	public void onShowImportContacts(){
		resetGlobalVars();
		getView().reset();
		getView().showCsvFileBrowser();
		getEventBus().setRegionRight(this);
	}

	private void resetGlobalVars(){
		filePath = null;
		totalRecords=0;
		numOfErrors=0;
		numOfRecordsStored=0;
		
	}

	public ContactServiceAsync getContactsServiceAsync() {
		return ContactServiceAsync;
	}
	
}