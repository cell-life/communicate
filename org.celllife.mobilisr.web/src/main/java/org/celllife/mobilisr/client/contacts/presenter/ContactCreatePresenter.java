package org.celllife.mobilisr.client.contacts.presenter;

import org.celllife.mobilisr.client.Constants;
import org.celllife.mobilisr.client.UserContext;
import org.celllife.mobilisr.client.app.presenter.DirtyPresenter;
import org.celllife.mobilisr.client.contacts.ContactCreateView;
import org.celllife.mobilisr.client.contacts.ContactEventBus;
import org.celllife.mobilisr.client.contacts.GenericContactManagementView;
import org.celllife.mobilisr.client.contacts.handler.GenericContactManagementEventHandler;
import org.celllife.mobilisr.client.contacts.handler.GroupToContactEventHandler;
import org.celllife.mobilisr.client.contacts.view.ContactCreateViewImpl;
import org.celllife.mobilisr.client.model.ViewModel;
import org.celllife.mobilisr.client.view.gxt.MobilisrAsyncCallback;
import org.celllife.mobilisr.domain.Contact;
import org.celllife.mobilisr.domain.ContactGroup;
import org.celllife.mobilisr.domain.Organization;
import org.celllife.mobilisr.service.gwt.ContactContactGroupViewModel;
import org.celllife.mobilisr.service.gwt.ContactServiceAsync;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.inject.Inject;
import com.mvp4g.client.annotation.Presenter;

@Presenter(view=ContactCreateViewImpl.class)
public class ContactCreatePresenter extends DirtyPresenter<ContactCreateView,ContactEventBus> {

	@Inject
	private ContactServiceAsync contactsServiceAsync;
	private boolean popupConfigured = false;
	private GenericContactManagementEventHandler<ContactGroup> groupManagementPresenter;
	
	@Override
	public void bindView() {		
		GenericContactManagementView<ContactGroup> groupManagement = getView().getAddPopup();
		groupManagementPresenter = new GroupToContactEventHandler(groupManagement);
		
		getView().getAnchor().addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent clientEvent) {
				showGroupPopup();
			}
		});
		
		getView().getFormCancelButton().addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				getEventBus().showContactList(null);
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
		final ViewModel<Contact> viewEntityModel = (ViewModel<Contact>) getView().getFormObject();
		Contact contact = (Contact) viewEntityModel.getModelObject();
		Organization organization = UserContext.getUser().getOrganization();
		
		GroupToContactEventHandler groupToContactEventHandler = (GroupToContactEventHandler) groupManagementPresenter;
		ContactContactGroupViewModel contactModel = groupToContactEventHandler.getContactModel(contact);
		contactsServiceAsync.saveOrUpdateContact(organization, contactModel, new MobilisrAsyncCallback<Contact>() {
			@Override
			protected void handleExpectedException(Throwable error) {
				viewEntityModel.setViewMessage(error.getMessage());
				onShowContactCreate(viewEntityModel);
			}
			
			@Override
			public void onSuccess(Contact contact) {
				getView().setDirty(false);
				getEventBus().showContactList(new ViewModel<Contact>(contact));
			}
		});
	}
	
	private void showGroupPopup() {
		if(!getView().getMsisdn().isEmpty()){
			if(!popupConfigured){
				popupConfigured = true;
				groupManagementPresenter.getListEntityLoader().load(0, Constants.INSTANCE.pageSize());
				ViewModel<Contact> viewEntityModel = getView().getFormObject();
				if(viewEntityModel.isModeUpdate()){
					groupManagementPresenter.getSelEntityLoader().load(0, Constants.INSTANCE.pageSize());
				}
			}
			getView().showAddGroupPopup();
		}
	}
	
	public void onShowContactCreate(ViewModel<Contact> viewEntityModel){
		getEventBus().setNavigationConfirmation(this);
		popupConfigured = false;
		groupManagementPresenter.clearFormValues();	
		groupManagementPresenter.setViewModel(viewEntityModel);
		getView().setFormObject(viewEntityModel);
		getEventBus().setRegionRight(this);
	}

	public ContactServiceAsync getContactsServiceAsync() {
		return contactsServiceAsync;
	}	
	
}
