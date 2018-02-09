package org.celllife.mobilisr.client.admin.view;

import java.util.ArrayList;
import java.util.List;

import org.celllife.mobilisr.client.Messages;
import org.celllife.mobilisr.client.Resources;
import org.celllife.mobilisr.client.admin.ChannelConfigListView;
import org.celllife.mobilisr.client.template.view.EntityListTemplateImpl;
import org.celllife.mobilisr.client.view.gxt.ButtonGridCellRenderer;
import org.celllife.mobilisr.client.view.gxt.MenuAction;
import org.celllife.mobilisr.client.view.gxt.MenuActionItem;
import org.celllife.mobilisr.client.view.gxt.MyGXTButton;
import org.celllife.mobilisr.domain.ChannelConfig;

import com.extjs.gxt.ui.client.Style.ButtonScale;
import com.extjs.gxt.ui.client.Style.IconAlign;
import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;

public class ChannelConfigListViewImpl extends EntityListTemplateImpl implements ChannelConfigListView{

	private Button newChannelConfigButton;
	private MenuActionItem editMenu;
	private MenuActionItem voidMenu;

	@Override
	public void createView(){
		newChannelConfigButton = new MyGXTButton("newChannelConfigButton",
				Messages.INSTANCE.channelConfigAddNew(), Resources.INSTANCE.add(),
				IconAlign.LEFT, ButtonScale.SMALL);
		
		Button[] buttons = new Button[]{newChannelConfigButton};
		layoutListTemplate(Messages.INSTANCE.channelConfigListHeader(), buttons, false);
	}

	public void buildWidget( final ListStore<BeanModel> store) {

		List<ColumnConfig> configs = new ArrayList<ColumnConfig>();

		ColumnConfig actions = new ColumnConfig( "channelConfigActions", "Actions", 50 );
		actions.setFixed(true);
		actions.setSortable(false);

		ButtonGridCellRenderer actionRenderer = new ButtonGridCellRenderer();

		MenuAction menuAction = new MenuAction(null, null,
				Resources.INSTANCE.cog(), "actions");
		
		editMenu = new MenuActionItem("Edit", Resources.INSTANCE.tableEdit());
		menuAction.addMenuItem(editMenu);
		
		voidMenu = new MenuActionItem("Void", Resources.INSTANCE.delete());
		menuAction.addMenuItem(voidMenu);
		
		actionRenderer.addAction(menuAction);
		actions.setRenderer(actionRenderer);
		configs.add(actions);
		
		configs.add( new ColumnConfig( ChannelConfig.PROP_NAME, "Name", 100) );
		configs.add( new ColumnConfig( ChannelConfig.PROP_HANDLER, "Handler", 50 ) );
		
		renderEntityListGrid(store, null, configs, null, "Search for a config");
	}

	@Override
	public Button getNewChannelConfigButton() {
		return newChannelConfigButton;
	}
	
	@Override
	public MenuActionItem getEditMenu() {
		return editMenu;
	}
	
	@Override
	public MenuActionItem getVoidMenu() {
		return voidMenu;
	}
	
}
