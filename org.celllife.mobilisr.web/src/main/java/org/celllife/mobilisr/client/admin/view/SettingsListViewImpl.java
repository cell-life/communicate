package org.celllife.mobilisr.client.admin.view;

import java.util.ArrayList;
import java.util.List;

import org.celllife.mobilisr.client.Messages;
import org.celllife.mobilisr.client.template.view.EntityListTemplateImpl;
import org.celllife.mobilisr.client.view.gxt.grid.EntityIDColumnConfig;
import org.celllife.mobilisr.service.gwt.SettingViewModel;

import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;

public class SettingsListViewImpl extends EntityListTemplateImpl implements SettingsListView{

	@Override
	public void createView(){
		layoutListTemplate(Messages.INSTANCE.settingListHeader(), null, false);
	}
	
	public void buildWidget( ListStore<BeanModel> store) {
		List<ColumnConfig> configs = new ArrayList<ColumnConfig>();

		configs.add( new EntityIDColumnConfig( SettingViewModel.PROP_NAME, "Name", 100, "setting") );
		configs.add( new ColumnConfig( SettingViewModel.PROP_VALUE, "Value", 150 ) );
		
		renderEntityListGrid(store, null, configs, null, null);
		
	}
	
	@Override
	public Button getNewEntityButton() {
		return null;
	}
}
