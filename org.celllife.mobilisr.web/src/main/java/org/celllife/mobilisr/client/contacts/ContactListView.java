package org.celllife.mobilisr.client.contacts;

import org.celllife.mobilisr.client.app.EntityList;
import org.celllife.mobilisr.client.view.gxt.Action;
import org.celllife.mobilisr.domain.ContactGroup;

import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.StoreFilterField;

public interface ContactListView extends EntityList {

	public void buildWidget(ListStore<BeanModel> store, StoreFilterField<BeanModel> filter);

	public Button getExportContactsButton();

	GenericContactManagementView<ContactGroup> getGroupPopup();

	void showGroupPopup(String msisdn);

	void setTitle(String title);

	Action getCampaignStatusAction();

	Action getManageGroupsAction();

	Action getMessageLogsAction();
	
}
