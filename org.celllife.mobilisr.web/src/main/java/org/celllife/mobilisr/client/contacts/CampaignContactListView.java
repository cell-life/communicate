package org.celllife.mobilisr.client.contacts;

import org.celllife.mobilisr.client.app.EntityList;
import org.celllife.mobilisr.client.model.ViewModel;
import org.celllife.mobilisr.domain.Campaign;

import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.StoreFilterField;

public interface CampaignContactListView extends EntityList {

	public void buildWidget(ListStore<BeanModel> store, StoreFilterField<BeanModel> filter);

	public Button getExportButton();

	public void setFormObject(ViewModel<Campaign> vem);

}
