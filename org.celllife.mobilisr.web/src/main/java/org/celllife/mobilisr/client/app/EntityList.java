package org.celllife.mobilisr.client.app;

import java.util.List;


import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.StoreFilterField;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.toolbar.PagingToolBar;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;

public interface EntityList extends BasicView {

	public void layoutListTemplate(String headerTitle, Button[] buttons, boolean addPagingToolbar);
	
	public void renderEntityListGrid(ListStore<BeanModel> store, StoreFilterField<BeanModel> filter, List<ColumnConfig> configs, String viewClickText, String searchText);
	
	public Button getNewEntityButton();
	
	public Grid<BeanModel> getEntityListGrid();

	public PagingToolBar getPagingToolBar();
	
	public void displaySuccessMsg(String msg);

	public void clearSuccessMsg();

	public ToolBar getTopToolBar();

	void displaySuccessMsg(String msg, String id);
}
