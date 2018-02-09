package org.celllife.mobilisr.client.contacts.view;

import org.celllife.mobilisr.client.app.EntityList;
import org.celllife.mobilisr.client.contacts.presenter.ContactCampaignStatusPresenter;
import org.celllife.mobilisr.client.model.ViewModel;
import org.celllife.mobilisr.client.view.gxt.RemoteStoreFilterField;
import org.celllife.mobilisr.domain.Contact;

import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;

public interface ContactCampaignStatusView extends EntityList {

	ColumnConfig buildWidget(ListStore<BeanModel> store, RemoteStoreFilterField<BeanModel> filter, ContactCampaignStatusPresenter presenter);

	void setFormObject(ViewModel<Contact> viewEntityModel);

}
