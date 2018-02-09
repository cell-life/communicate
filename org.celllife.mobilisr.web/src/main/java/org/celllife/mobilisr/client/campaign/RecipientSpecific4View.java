package org.celllife.mobilisr.client.campaign;


import org.celllife.mobilisr.client.app.EntityList;
import org.celllife.mobilisr.client.view.gxt.RemoteStoreFilterField;
import org.celllife.mobilisr.domain.Campaign;

import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.store.ListStore;

public interface RecipientSpecific4View extends EntityList {
	
	void reloadMessages(Campaign campaign);
	Campaign getCampaign();
	void buildWidget(ListStore<BeanModel> store, RemoteStoreFilterField<BeanModel> filter);
}
