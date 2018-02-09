package org.celllife.mobilisr.client.org;

import org.celllife.mobilisr.client.app.EntityList;
import org.celllife.mobilisr.client.view.gxt.Action;

import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.StoreFilterField;

public interface AdminOrgListView extends EntityList{

	public void buildWidget( ListStore<BeanModel> store, StoreFilterField<BeanModel> filter );
	Action getToggleStateAction();
	Action getCreditAccountAction();
	Button getSendNotificationButton();
	Button getShowVoidedButton();
}
