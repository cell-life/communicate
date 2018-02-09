package org.celllife.mobilisr.client.filter;

import org.celllife.mobilisr.client.app.EntityCreate;
import org.celllife.mobilisr.client.reporting.EntityStoreProvider;
import org.celllife.mobilisr.client.view.gxt.MyGXTButton;
import org.celllife.mobilisr.service.gwt.MessageFilterViewModel;
import org.celllife.pconfig.model.Pconfig;

import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.form.ComboBox;

public interface FilterCreateView extends EntityCreate<MessageFilterViewModel> {
	
	void setOrganizationStore(ListStore<BeanModel> store);

	void setFilterStore(ListStore<BeanModel> store);

	void setChannelStore(ListStore<BeanModel> store);

	void createActionsGrid();

	void addAction(Pconfig actionItem);

	MyGXTButton getAddActionButton();

	void setEntityStoreProvider(EntityStoreProvider entityStoreProvider);

	ComboBox<BeanModel> getOrganizationCombo();

}
