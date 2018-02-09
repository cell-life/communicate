package org.celllife.mobilisr.client.role.view;

import java.util.ArrayList;
import java.util.List;

import org.celllife.mobilisr.client.Messages;
import org.celllife.mobilisr.client.Resources;
import org.celllife.mobilisr.client.role.AdminRoleListView;
import org.celllife.mobilisr.client.template.view.EntityListTemplateImpl;
import org.celllife.mobilisr.client.view.gxt.MyGXTButton;
import org.celllife.mobilisr.client.view.gxt.grid.EntityIDColumnConfig;
import org.celllife.mobilisr.domain.Role;

import com.extjs.gxt.ui.client.Style.ButtonScale;
import com.extjs.gxt.ui.client.Style.IconAlign;
import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.StoreFilterField;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;

public class AdminRoleListViewImpl extends EntityListTemplateImpl implements AdminRoleListView{

	private Button newRoleButton;
	private Button deleteRoleButton;
	
	public AdminRoleListViewImpl(){
		newRoleButton = new MyGXTButton("newRoleButton",
				Messages.INSTANCE.roleAddNew(), Resources.INSTANCE.add(),
				IconAlign.LEFT, ButtonScale.SMALL);
		deleteRoleButton = new MyGXTButton("deleteRoleButton",
				Messages.INSTANCE.roleDeleteSelected(), Resources.INSTANCE.delete(),
				IconAlign.LEFT, ButtonScale.SMALL);
		
		deleteRoleButton.disable();
		
		Button[] buttons = new Button[]{newRoleButton, deleteRoleButton};
		
		layoutListTemplate(Messages.INSTANCE.roleListHeader(), buttons, false);
	}

	@Override
	public void createView(){}
	
	public void buildWidget( ListStore<BeanModel> store, StoreFilterField<BeanModel> filter ) {
		
		List<ColumnConfig> configs = new ArrayList<ColumnConfig>();

		configs.add( new EntityIDColumnConfig( Role.PROP_NAME, "Role Name", 150, "role" ) );
		
		renderEntityListGrid(store, filter, configs, null, "Search for Role");
		
	}
	
	public Button getNewRoleButton() {
		return newRoleButton;
	}

	@Override
	public Button getNewEntityButton() {
		return newRoleButton;
	}
	
	public Button getDeleteRoleButton() {
		return deleteRoleButton;
	}
}
