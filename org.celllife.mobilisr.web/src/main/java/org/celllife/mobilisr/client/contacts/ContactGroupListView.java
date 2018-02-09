package org.celllife.mobilisr.client.contacts;

import org.celllife.mobilisr.client.app.EntityList;
import org.celllife.mobilisr.client.view.gxt.Action;

import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.StoreFilterField;

public interface ContactGroupListView extends EntityList {

	public void buildWidget(ListStore<BeanModel> store, StoreFilterField<BeanModel> filter);

	public Button getExportContactsButton();

	void setTitle(String title);
	
	void setTitleLabel(String headerTitle);

	Action getCampaignStatusAction();

	Action getMessageLogsAction();
	
}
