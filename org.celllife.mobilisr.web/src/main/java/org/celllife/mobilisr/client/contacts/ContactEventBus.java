package org.celllife.mobilisr.client.contacts;

import org.celllife.mobilisr.client.app.PresenterStateAware;
import org.celllife.mobilisr.client.contacts.presenter.ContactCampaignStatusPresenter;
import org.celllife.mobilisr.client.contacts.presenter.ContactCreatePresenter;
import org.celllife.mobilisr.client.contacts.presenter.ContactGroupListPresenter;
import org.celllife.mobilisr.client.contacts.presenter.ContactLeftViewPresenter;
import org.celllife.mobilisr.client.contacts.presenter.ContactListPresenter;
import org.celllife.mobilisr.client.contacts.presenter.GroupCreatePresenter;
import org.celllife.mobilisr.client.contacts.presenter.GroupListPresenter;
import org.celllife.mobilisr.client.contacts.presenter.ImportContactsPresenter;
import org.celllife.mobilisr.client.contacts.view.ContactListViewImpl;
import org.celllife.mobilisr.client.model.ViewModel;
import org.celllife.mobilisr.domain.Contact;
import org.celllife.mobilisr.domain.ContactGroup;
import org.celllife.mobilisr.domain.MobilisrEntity;

import com.mvp4g.client.annotation.Event;
import com.mvp4g.client.annotation.Events;
import com.mvp4g.client.event.EventBus;

@Events(module=ContactModule.class, startView=ContactListViewImpl.class)
public interface ContactEventBus extends EventBus {
	
	@Event(handlers = ContactRegionHandler.class, navigationEvent = true)
	public void showContactsRegion();
	
	@Event( handlers = ContactLeftViewPresenter.class)
	public void showContactsButtonPanel();
	
	@Event( handlers = ContactListPresenter.class, navigationEvent = true)
	public void showContactList(ViewModel<? extends MobilisrEntity> vem);

	@Event( handlers = ContactGroupListPresenter.class, navigationEvent = true)
	public void showContactGroupList(ViewModel<? extends MobilisrEntity> vem);
	
	@Event( handlers = ContactCreatePresenter.class, navigationEvent = true)
	public void showContactCreate(ViewModel<Contact> vem);
	
	@Event( handlers = ContactCampaignStatusPresenter.class, navigationEvent = true)
	public void showContactCampaignStatusReport(ViewModel<Contact> vem);
	
	@Event( handlers = GroupListPresenter.class, navigationEvent = true)
	public void showGroupList(ViewModel<ContactGroup> vem);

	@Event(handlers=GroupCreatePresenter.class, navigationEvent = true)
	public void showGroupCreate(ViewModel<ContactGroup> vem);

	@Event( handlers = ImportContactsPresenter.class, navigationEvent = true)
	public void showImportContacts();

	@Event( forwardToParent=true)
	public void setRegionLeft(PresenterStateAware mobilisrBasePresenter);
	
	@Event( forwardToParent=true)
	public void setRegionRight(PresenterStateAware mobilisrBasePresenter);

	@Event( forwardToParent=true)
	public void showMessageLog(ViewModel<Contact> viewModel);

}
