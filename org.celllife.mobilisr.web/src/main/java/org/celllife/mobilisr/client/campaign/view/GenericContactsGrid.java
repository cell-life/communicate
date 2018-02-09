package org.celllife.mobilisr.client.campaign.view;

import java.util.List;

import org.celllife.mobilisr.client.Constants;
import org.celllife.mobilisr.client.view.gxt.RemoteStoreFilterField;
import org.celllife.mobilisr.client.view.gxt.grid.MyGXTPaginatedGridSearch;
import org.celllife.mobilisr.domain.Contact;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridView;
import com.extjs.gxt.ui.client.widget.grid.GridViewConfig;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.toolbar.PagingToolBar;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;

public class GenericContactsGrid extends ContentPanel {
	
	private ToolBar topToolBar;
	private PagingToolBar pagingToolBar;
	private final MyGXTPaginatedGridSearch<?> gridSearch;
	private Grid<BeanModel> entityGridList;

	public GenericContactsGrid(List<ColumnConfig> configs, MyGXTPaginatedGridSearch<?> gridSearch) {
		setBorders(true);
		setLayout(new FitLayout());
		
		this.gridSearch = gridSearch;
		
		createGrid(configs);
		createTopToolbar();

		RemoteStoreFilterField<BeanModel> filter = gridSearch.getFilter();
		filter.setEmptyText("Search contacts");
		//LabelToolItem filterLabel = new LabelToolItem("Search contacts");
		//filterLabel.setId("search" + id + "ContactsLabel");
		//topToolBar.add(filterLabel);
		topToolBar.add(filter);

		setTopComponent(topToolBar);
		add(entityGridList);

		pagingToolBar = new PagingToolBar(Constants.INSTANCE.pageSize());
		pagingToolBar.bind(gridSearch.getLoader());
		setBottomComponent(pagingToolBar);
	}

	private void createTopToolbar() {
		topToolBar = new ToolBar();
		topToolBar.setAlignment(HorizontalAlignment.LEFT);
		topToolBar.setIntStyleAttribute("padding", 5);
		topToolBar.setSpacing(10);
	}

	private void createGrid(List<ColumnConfig> configs) {
		GridView gridView = createGridView();
		
		ColumnModel cm = new ColumnModel(configs);
		
		entityGridList = new Grid<BeanModel>(getGridSearch().getStore(), cm);
		entityGridList.setLoadMask(true);
		entityGridList.setView(gridView);
		entityGridList.setBorders( false );
		entityGridList.setHeight("60%");
	}
	
	private GridView createGridView() {
		GridView gridView = new GridView();
		gridView.setAutoFill(true);
		gridView.setForceFit(true);
		gridView.setEmptyText("No records currently exist");
		
		gridView.setViewConfig(new GridViewConfig(){
			@Override
			public String getRowStyle(ModelData model, int rowIndex,
					ListStore<ModelData> ds) {
				if (model != null) {
					Boolean invalid = model.get(Contact.PROP_INVALID);
					if (invalid != null && invalid) {
						return "row-invalid";
					}
				}
				return "";
			}
		});
		
		return gridView;
	}
	
	public ToolBar getTopToolBar() {
		return topToolBar;
	}
	
	public PagingToolBar getPagingToolBar() {
		return pagingToolBar;
	}
	
	public MyGXTPaginatedGridSearch<?> getGridSearch() {
		return gridSearch;
	}
	
	public void reconfigureGrid(List<ColumnConfig> configs){
		ColumnModel cm = new ColumnModel(configs);
		entityGridList.enableEvents(false);
		entityGridList.reconfigure(entityGridList.getStore(), cm);
		entityGridList.enableEvents(true);
	}
}
