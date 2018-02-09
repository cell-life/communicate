package org.celllife.mobilisr.client.contacts.presenter;

import org.celllife.mobilisr.client.Constants;
import org.celllife.mobilisr.client.UserContext;
import org.celllife.mobilisr.client.app.presenter.MobilisrBasePresenter;
import org.celllife.mobilisr.client.contacts.ContactEventBus;
import org.celllife.mobilisr.client.contacts.GenericContactManagementView;
import org.celllife.mobilisr.client.contacts.GroupListView;
import org.celllife.mobilisr.client.contacts.handler.ContactToGroupEventHandler;
import org.celllife.mobilisr.client.contacts.handler.GenericContactManagementEventHandler;
import org.celllife.mobilisr.client.contacts.view.GroupListViewImpl;
import org.celllife.mobilisr.client.model.ViewModel;
import org.celllife.mobilisr.client.view.gxt.GridModelEvent;
import org.celllife.mobilisr.client.view.gxt.MobilisrAsyncCallback;
import org.celllife.mobilisr.client.view.gxt.grid.MyGXTPaginatedGridSearch;
import org.celllife.mobilisr.domain.Contact;
import org.celllife.mobilisr.domain.ContactGroup;
import org.celllife.mobilisr.domain.MobilisrEntity;
import org.celllife.mobilisr.domain.Organization;
import org.celllife.mobilisr.service.gwt.ContactGroupContactViewModel;
import org.celllife.mobilisr.service.gwt.ContactServiceAsync;

import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.mvp4g.client.annotation.Presenter;

@Presenter(view=GroupListViewImpl.class)
public class GroupListPresenter extends MobilisrBasePresenter<GroupListView, ContactEventBus> {

	@Inject
	private ContactServiceAsync contactsServiceAsync;

	private MyGXTPaginatedGridSearch<ContactGroup> gridSearch;
	private GenericContactManagementEventHandler<Contact> contactManagementPresenter;

	@Override
	public void bindView() {
		GenericContactManagementView<Contact> contactManagement = getView().getAddPopup();
		contactManagementPresenter = new ContactToGroupEventHandler(contactManagement);

		gridSearch = new MyGXTPaginatedGridSearch<ContactGroup>(ContactGroup.PROP_GROUP_NAME, Constants.INSTANCE.pageSize()) {

			@Override
			public void rpcListServiceCall(PagingLoadConfig pagingLoadConfig, AsyncCallback<PagingLoadResult<ContactGroup>> callback) {
				contactsServiceAsync.listAllGroupsForOrganization(UserContext.getUser().getOrganization(), pagingLoadConfig, callback);
			}
		};

		getView().getPagingToolBar().bind(gridSearch.getLoader());
		getView().buildWidget(gridSearch.getStore(), gridSearch.getFilter());

		getView().getEntityListGrid().addListener(Events.RowClick, new Listener<GridEvent<BeanModel>>() {

			@Override
			public void handleEvent(GridEvent<BeanModel> gridEvent) {
				BeanModel beanModel = gridEvent.getModel();
				ContactGroup contactGroup =  beanModel.getBean();
				ViewModel<ContactGroup> viewEntityModel = new ViewModel<ContactGroup>(contactGroup);
				getEventBus().showGroupCreate(viewEntityModel);
			}
		});

		getView().getManageContactsAction().setListener(new SelectionListener<GridModelEvent>(){

			@Override
			public void componentSelected(GridModelEvent ce) {
				final BeanModel model = ce.getModel();
				ContactGroup cg = (ContactGroup) model.getBean();
				getView().showAddContactPopup(cg.getGroupName());
				contactManagementPresenter.clearFormValues();
				ViewModel<ContactGroup> vem = new ViewModel<ContactGroup>(cg);

				contactManagementPresenter.setViewModel(vem);
				contactManagementPresenter.getListEntityLoader().load(0, Constants.INSTANCE.pageSize());
				contactManagementPresenter.getSelEntityLoader().load(0, Constants.INSTANCE.pageSize());

			}});
		getView().getViewContactsAction().setListener(new SelectionListener<GridModelEvent>(){

			@Override
			public void componentSelected(GridModelEvent ce) {
				final BeanModel model = ce.getModel();
				ContactGroup cg = (ContactGroup) model.getBean();
				ViewModel<ContactGroup> vem = new ViewModel<ContactGroup>(cg);
				getEventBus().showContactGroupList(vem);
			}});

		getView().getDeleteGroupAction().setListener(new SelectionListener<GridModelEvent>(){
			@Override
			public void componentSelected(GridModelEvent ce) {
				final BeanModel model = ce.getModel();
				ContactGroup cg = (ContactGroup) model.getBean();
				contactsServiceAsync.deleteContactGroup(cg, new MobilisrAsyncCallback<Boolean>() {

					@Override
					public void onSuccess(Boolean arg0) {
						gridSearch.getLoader().load(0, Constants.INSTANCE.pageSize());
					}
				});
			}});

		getView().getNewEntityButton().addListener(Events.OnClick, new Listener<ButtonEvent>() {

					public void handleEvent(ButtonEvent be) {
						ViewModel<ContactGroup> viewEntityModel = new ViewModel<ContactGroup>(new ContactGroup());
						getEventBus().showGroupCreate(viewEntityModel);
					}
				});



		getView().setAddContactPopupSave(new SelectionListener<ButtonEvent> (){
			@Override
			public void componentSelected(ButtonEvent ce) {
				Organization organization = UserContext.getUser().getOrganization();

				ContactToGroupEventHandler contactToGroupEventHandler = (ContactToGroupEventHandler) contactManagementPresenter;
				final ViewModel<? extends MobilisrEntity> viewEntityModel = contactManagementPresenter.getViewModel();
				ContactGroup contactGroup = (ContactGroup) viewEntityModel.getModelObject();

				ContactGroupContactViewModel contactModel = contactToGroupEventHandler.getContactModel(contactGroup);
				contactsServiceAsync.saveOrUpdateContactGroup(organization, contactModel, new MobilisrAsyncCallback<ContactGroup>() {
					@Override
					public void onSuccess(ContactGroup contactGroup) {
						getEventBus().showGroupList(new ViewModel<ContactGroup>(contactGroup));
					}
				});
			}
		});

	}

	public void onShowGroupList(ViewModel<ContactGroup> vem) {
		if (vem != null && vem.getModelObject() != null && vem.getModelObject() instanceof ContactGroup){
			ContactGroup contactGroup = (ContactGroup) vem.getModelObject();
			getView().displaySuccessMsg("ContactGroup: \'" +  contactGroup.getGroupName() + "\' saved successfully");
		}
		getEventBus().setRegionRight(this);
		gridSearch.clearGridSearchTxt();
		gridSearch.getLoader().load(0, Constants.INSTANCE.pageSize());
	}
}
