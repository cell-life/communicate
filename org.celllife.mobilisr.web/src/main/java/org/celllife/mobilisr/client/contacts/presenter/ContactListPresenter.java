package org.celllife.mobilisr.client.contacts.presenter;

import org.celllife.mobilisr.client.Constants;
import org.celllife.mobilisr.client.Messages;
import org.celllife.mobilisr.client.URLUtil;
import org.celllife.mobilisr.client.UserContext;
import org.celllife.mobilisr.client.app.presenter.MobilisrBasePresenter;
import org.celllife.mobilisr.client.contacts.ContactEventBus;
import org.celllife.mobilisr.client.contacts.ContactListView;
import org.celllife.mobilisr.client.contacts.GenericContactManagementView;
import org.celllife.mobilisr.client.contacts.handler.GenericContactManagementEventHandler;
import org.celllife.mobilisr.client.contacts.handler.GroupToContactEventHandler;
import org.celllife.mobilisr.client.contacts.view.ContactListViewImpl;
import org.celllife.mobilisr.client.model.ViewModel;
import org.celllife.mobilisr.client.view.gxt.GridModelEvent;
import org.celllife.mobilisr.client.view.gxt.MobilisrAsyncCallback;
import org.celllife.mobilisr.client.view.gxt.grid.MyGXTPaginatedGridSearch;
import org.celllife.mobilisr.domain.*;
import org.celllife.mobilisr.service.gwt.ContactContactGroupViewModel;
import org.celllife.mobilisr.service.gwt.ContactServiceAsync;
import org.celllife.mobilisr.service.gwt.ExportServiceAsync;

import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.mvp4g.client.annotation.Presenter;

@Presenter(view=ContactListViewImpl.class)
public class ContactListPresenter extends MobilisrBasePresenter<ContactListView, ContactEventBus> {

	@Inject 
	private ExportServiceAsync exportService;
	
	@Inject
	private ContactServiceAsync contactService;

	private MyGXTPaginatedGridSearch<Contact> gridSearch;
	
	private GenericContactManagementEventHandler<ContactGroup> groupManagementPresenter;

	private Contact contact;
	
	@Override
	public void bindView() {
		GenericContactManagementView<ContactGroup> groupManagement = getView().getGroupPopup();
		groupManagementPresenter = new GroupToContactEventHandler(groupManagement);

        gridSearch = new MyGXTPaginatedGridSearch<Contact>(
                new StringBuilder()
                        .append(Contact.PROP_MSISDN)
                        .append(",")
                        .append(Contact.PROP_FIRST_NAME)
                        .append(",")
                        .append(Contact.PROP_LAST_NAME)
                        .toString(),
                Constants.INSTANCE.pageSize()) {

            @Override
            public void rpcListServiceCall(PagingLoadConfig pagingLoadConfig,
                                           AsyncCallback<PagingLoadResult<Contact>> callback) {
                contactService.listAllContactsForOrganization(UserContext.getUser().getOrganization(), pagingLoadConfig, callback);
            }
        };

		getView().getPagingToolBar().bind(gridSearch.getLoader());
		getView().buildWidget(gridSearch.getStore(), gridSearch.getFilter());

		getView().getEntityListGrid().addListener(Events.RowClick, new Listener<GridEvent<BeanModel>>() {
			@Override
			public void handleEvent(GridEvent<BeanModel> gridEvent) {
				BeanModel bm  = gridEvent.getModel();
				Contact contact = bm.getBean();
				ViewModel<Contact> vem = new ViewModel<Contact>(contact);
				getEventBus().showContactCreate(vem);
			}
		});

		getView().getNewEntityButton().addListener(Events.OnClick, new Listener<ButtonEvent>() {
			public void handleEvent(ButtonEvent be) {
				ViewModel<Contact> viewEntityModel = new ViewModel<Contact>(new Contact());
				getEventBus().showContactCreate(viewEntityModel);
			}
		});
		
		getView().getExportContactsButton().addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				final MessageBox wait = MessageBox.wait("Exporting", "Exporting contacts", null);
				exportService.exportContacts(UserContext.getUser().getOrganization().getId(), new MobilisrAsyncCallback<String>() {
					@Override
					public void onFailure(Throwable error) {
						super.onFailure(error);
						wait.close();
					}
					
					@Override
					public void onSuccess(String result) {
						wait.close();
						URLUtil.getTextFile(result);
					}
				});
			}
		});
		getView().getGroupPopup().getDoneButton().addSelectionListener(new SelectionListener<ButtonEvent>(){
			@Override
			public void componentSelected(ButtonEvent ce) {
				submitForm(contact);
			}
		});
		
		getView().getManageGroupsAction().setListener(new SelectionListener<GridModelEvent>() {
			@Override
			public void componentSelected(GridModelEvent ce) {
				Contact contact = ce.getModel().getBean();
				loadManageGroups(contact);
			}
		});
		
		getView().getCampaignStatusAction().setListener(new SelectionListener<GridModelEvent>() {
			@Override
			public void componentSelected(GridModelEvent ce) {
				Contact contact = ce.getModel().getBean();
				getEventBus().showContactCampaignStatusReport(new ViewModel<Contact>(contact));
			}
		});
		
		getView().getMessageLogsAction().setListener(new SelectionListener<GridModelEvent>() {
			@Override
			public void componentSelected(GridModelEvent ce) {
				Contact contact = ce.getModel().getBean();
				getEventBus().showMessageLog(new ViewModel<Contact>(contact));
			}
		});
	}
	
	private void submitForm(Contact contact) {
		
		Organization organization = UserContext.getUser().getOrganization();
		
		GroupToContactEventHandler groupToContactEventHandler = (GroupToContactEventHandler) groupManagementPresenter;
		ContactContactGroupViewModel contactModel = groupToContactEventHandler.getContactModel(contact);
		contactService.saveOrUpdateContact(organization, contactModel, new MobilisrAsyncCallback<Contact>() {
			@Override
			public void onSuccess(Contact contact) {
				getEventBus().showContactList(new ViewModel<Contact>(contact));
			}
		});
	}

	public void onShowContactList(ViewModel<? extends MobilisrEntity> vem) {
		getView().clearSuccessMsg();
		
		if (vem != null && vem.getModelObject() != null){
	
			MobilisrEntity entityObject = vem.getModelObject();
	
			if (entityObject instanceof Contact){
				Contact contact = (Contact) entityObject;
				getView().displaySuccessMsg("Contact: \'" +  contact.getMsisdn() + "\' saved successfully");
			}
		}
		
		getEventBus().setRegionRight(this);
		gridSearch.clearGridSearchTxt();
		gridSearch.getLoader().load(0, Constants.INSTANCE.pageSize());
	}
	
	private void loadManageGroups(Contact contact){
		this.contact = contact;
		groupManagementPresenter.clearFormValues();	
		groupManagementPresenter.setViewModel(new ViewModel<Contact>(contact));
		
		groupManagementPresenter.getListEntityLoader().load(0, Constants.INSTANCE.pageSize());
		groupManagementPresenter.getSelEntityLoader().load(0, Constants.INSTANCE.pageSize());
		getView().showGroupPopup(contact.getMsisdn());
		
	}
}
