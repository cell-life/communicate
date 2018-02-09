package org.celllife.mobilisr.client.contacts.presenter;

import org.celllife.mobilisr.client.Constants;
import org.celllife.mobilisr.client.UserContext;
import org.celllife.mobilisr.client.app.presenter.DirtyPresenter;
import org.celllife.mobilisr.client.contacts.ContactEventBus;
import org.celllife.mobilisr.client.contacts.GenericContactManagementView;
import org.celllife.mobilisr.client.contacts.GroupCreateView;
import org.celllife.mobilisr.client.contacts.handler.ContactToGroupEventHandler;
import org.celllife.mobilisr.client.contacts.handler.GenericContactManagementEventHandler;
import org.celllife.mobilisr.client.contacts.view.GroupCreateViewImpl;
import org.celllife.mobilisr.client.model.ViewModel;
import org.celllife.mobilisr.client.view.gxt.MobilisrAsyncCallback;
import org.celllife.mobilisr.domain.Contact;
import org.celllife.mobilisr.domain.ContactGroup;
import org.celllife.mobilisr.domain.Organization;
import org.celllife.mobilisr.service.exception.UniquePropertyException;
import org.celllife.mobilisr.service.gwt.ContactGroupContactViewModel;
import org.celllife.mobilisr.service.gwt.ContactServiceAsync;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.inject.Inject;
import com.mvp4g.client.annotation.Presenter;

@Presenter(view=GroupCreateViewImpl.class)
public class GroupCreatePresenter extends DirtyPresenter<GroupCreateView,ContactEventBus> {

	@Inject
	private ContactServiceAsync contactsServiceAsync;

	private boolean isClicked = false;
	private GenericContactManagementEventHandler<Contact> contactManagementPresenter;

	@Override
	public void bindView() {
		GenericContactManagementView<Contact> contactManagement = getView().getAddPopup();
		contactManagementPresenter = new ContactToGroupEventHandler(contactManagement);

		getView().getAnchor().addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent clientEvent) {
				if(getView().getGroupName() != null && !getView().getGroupName().isEmpty()){
					if(!isClicked){
						isClicked = true;
						contactManagementPresenter.getListEntityLoader().load(0, Constants.INSTANCE.pageSize());
						ViewModel<ContactGroup> viewEntityModel = getView().getFormObject();
						if(viewEntityModel.isModeUpdate()){
							contactManagementPresenter.getSelEntityLoader().load(0, Constants.INSTANCE.pageSize());
						}
					}
					getView().showAddContactPopup(getView().getGroupName());
				}
			}
		});

		getView().getFormCancelButton().addSelectionListener(new SelectionListener<ButtonEvent>() {

			@Override
			public void componentSelected(ButtonEvent ce) {
				getEventBus().showGroupList(null);
			}
		});

		getView().getFormSubmitButton().addSelectionListener(new SelectionListener<ButtonEvent>() {

			@Override
			public void componentSelected(ButtonEvent ce) {
				submitForm();
			}
		});
	}

	private void submitForm() {
		final ViewModel<ContactGroup> viewEntityModel = getView().getFormObject();
		//Obtain the ContactGroup
		final ContactGroup contactGroup = viewEntityModel.getModelObject();
		//Get the Organisation for which the group must be added
		Organization organization = UserContext.getUser().getOrganization();

		ContactToGroupEventHandler contactToGroupEventHandler = (ContactToGroupEventHandler) contactManagementPresenter;
		ContactGroupContactViewModel contactModel = contactToGroupEventHandler.getContactModel(contactGroup);
		//It is this object that we'll be passing over to the server with essential details for save/update
		contactsServiceAsync.saveOrUpdateContactGroup(organization, contactModel, new MobilisrAsyncCallback<ContactGroup>() {
			@Override
			public void onSuccess(ContactGroup contactGroup) {
				getView().setDirty(false);
				getEventBus().showGroupList(new ViewModel<ContactGroup>(contactGroup));
			}

			@Override
			public void onFailure(Throwable cause) {
				if (cause instanceof UniquePropertyException) {
					viewEntityModel.setViewMessage(cause.getMessage());
					getView().setErrorMessage(viewEntityModel.getViewMessage());
				} else {
					super.onFailure(cause);
				}
			}
		});
	}

	public void onShowGroupCreate(ViewModel<ContactGroup> viewEntityModel){
		getEventBus().setNavigationConfirmation(this);
		isClicked = false;
		contactManagementPresenter.clearFormValues();
		contactManagementPresenter.setViewModel(viewEntityModel);
		getView().setFormObject(viewEntityModel);
		getEventBus().setRegionRight(this);
	}

	public ContactServiceAsync getContactsServiceAsync() {
		return contactsServiceAsync;
	}
}
