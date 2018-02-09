package org.celllife.mobilisr.client.contacts.presenter;

import org.celllife.mobilisr.client.app.presenter.MobilisrBasePresenter;
import org.celllife.mobilisr.client.contacts.ContactEventBus;
import org.celllife.mobilisr.client.contacts.ContactsLeftView;
import org.celllife.mobilisr.client.contacts.view.ContactsLeftViewImpl;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.mvp4g.client.annotation.Presenter;

@Presenter(view = ContactsLeftViewImpl.class)
public class ContactLeftViewPresenter extends MobilisrBasePresenter<ContactsLeftView, ContactEventBus> {

	@Override
	public void bindView() {
		
		getView().getMyContactButton().addSelectionListener(new SelectionListener<ButtonEvent>() {
			
			@Override
			public void componentSelected(ButtonEvent ce) {
				getEventBus().showContactList(null);
			}
		});
		
		getView().getMyGroupButton().addSelectionListener(new SelectionListener<ButtonEvent>() {
			
			@Override
			public void componentSelected(ButtonEvent ce) {
				getEventBus().showGroupList(null);
			}
		});
		
		getView().getImportContactButton().addSelectionListener(new SelectionListener<ButtonEvent>() {
			
			@Override
			public void componentSelected(ButtonEvent ce) {
				getEventBus().showImportContacts();
			}
		});
	}
	
	public void onShowContactsButtonPanel() {

		getEventBus().setRegionLeft(this);

	}
}
