package org.celllife.mobilisr.client.admin;


import org.celllife.mobilisr.client.app.EntityList;
import org.celllife.mobilisr.client.view.gxt.MenuActionItem;

import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.form.StoreFilterField;

public interface NumberInfoListView extends EntityList{

	public void buildWidget( ListStore<BeanModel> store, StoreFilterField<BeanModel> filter );

	MenuActionItem getEditMenu();

	MenuActionItem getVoidMenu();
}
