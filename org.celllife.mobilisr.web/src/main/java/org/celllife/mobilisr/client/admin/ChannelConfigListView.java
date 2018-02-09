package org.celllife.mobilisr.client.admin;


import org.celllife.mobilisr.client.app.EntityList;
import org.celllife.mobilisr.client.view.gxt.MenuActionItem;

import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.button.Button;

public interface ChannelConfigListView extends EntityList{

	public void buildWidget( ListStore<BeanModel> store);

	Button getNewChannelConfigButton();

	MenuActionItem getEditMenu();

	MenuActionItem getVoidMenu();

}
