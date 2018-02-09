package org.celllife.mobilisr.client.role;

import org.celllife.mobilisr.client.app.EntityList;

import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.StoreFilterField;

public interface AdminRoleListView extends EntityList {

	public Button getNewRoleButton();
	
	public void buildWidget( ListStore<BeanModel> store, StoreFilterField<BeanModel> filter );

	public Button getDeleteRoleButton();
}
