package org.celllife.mobilisr.client.template.view;

import java.util.List;

import org.celllife.mobilisr.client.Constants;
import org.celllife.mobilisr.client.app.EntityList;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.form.StoreFilterField;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridView;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.toolbar.LabelToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.PagingToolBar;
import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.ui.Widget;

public abstract class EntityListTemplateImpl extends LayoutContainer implements EntityList{

	// Margin for the layout
	protected Margins m = new Margins(10);
	// Button bar for different buttons in the list view
	protected ButtonBar buttonBar = new ButtonBar();

	protected PagingToolBar toolBar = new PagingToolBar( Constants.INSTANCE.pageSize() );
	private Label msgLabel = new Label();
	protected Label titleLabel = new Label();
	protected Label subtitleLabel = new Label();
	protected ContentPanel listContentPanel = new ContentPanel();
	protected Grid<BeanModel> entityList = null;
	protected ToolBar topToolBar;

	@Override
	public void layoutListTemplate(String headerTitle, Button[] buttons, boolean addPagingToolbar) {

		setIntStyleAttribute("margin", 10);
		setLayout(new RowLayout(Orientation.VERTICAL));

		listContentPanel.setHeaderVisible(false);
		listContentPanel.setLayout(new FitLayout());
		listContentPanel.setWidth("98%");
		listContentPanel.setHeight("100%");
		listContentPanel.setBodyBorder(false);
		listContentPanel.setBorders(true);
		if (addPagingToolbar){
			listContentPanel.setBottomComponent(toolBar);
		}

		addTitleLabel(headerTitle);

		addButtonBar(buttons);

		addMessageLabel();

		add(listContentPanel, new RowData(1, 1, new Margins(20, 10, 0, 10)));
	}

	protected void addMessageLabel() {
		if (buttonBar.getItemCount() == 0) {
			// Kludge: add an empty panel to force msgLabel onto a new line in the case where
			//  button-bar is empty.
			add(new HorizontalPanel(), new RowData(1, -1, new Margins(10, 0, 0, 0)));
		}
		add(msgLabel, new RowData(1, -1, new Margins(0, 10, 0, 10)));
	}

	protected void addButtonBar(Button[] buttons) {
		if(buttons != null){
			buttonBar.setSpacing(10);
			for (Button button : buttons) {
				buttonBar.add(button);
			}
			add(buttonBar, new RowData(1, -1, m));
		}
	}

	protected void addTitleLabel(String headerTitle) {
		titleLabel.setText(headerTitle);
		titleLabel.setId("headerTitle");
		titleLabel.getElement().setId("headerTitle");
		titleLabel.setStyleName(Constants.INSTANCE.styleFont14());
		add(titleLabel, new RowData(1, -1, m));
	}

	protected void setTitleLabel(String headerTitle){
		titleLabel.setText(headerTitle);
	}

	protected void setSubtitleLabel(String subtitle){
		subtitleLabel.setText(subtitle);
		int titleIndex = indexOf(titleLabel);
		insert(subtitleLabel, titleIndex + 1, new RowData(1, -1, new Margins(0, 10, 0, 10)));
		// Kludge: add an empty panel to force subtitle onto a new line.
		insert(new HorizontalPanel(), titleIndex+1, new RowData(1, -1, new Margins(5, 0, 0, 0)));
	}

	@Override
	public void renderEntityListGrid(ListStore<BeanModel> store, StoreFilterField<BeanModel> filter, List<ColumnConfig> configs, String viewClickText, String searchText) {

		for (ColumnConfig columnConfig : configs) {
			columnConfig.setStyle("vertical-align: middle;");
		}

		ColumnModel cm = new ColumnModel( configs );

		entityList = new Grid<BeanModel>( store, cm );

		GridView gridView = createGridView();

		entityList.setLoadMask(true);
		entityList.setView(gridView);
		entityList.setBorders( true );
		entityList.setHeight("100%");
		entityList.setStripeRows( true );

		listContentPanel.add( entityList );

		topToolBar = new ToolBar();
		topToolBar.setAlignment(HorizontalAlignment.LEFT);
		topToolBar.setIntStyleAttribute("padding", 5);
		topToolBar.setSpacing(10);
		if(viewClickText != null){
			LabelToolItem clickTextLabel = new LabelToolItem(viewClickText);
			clickTextLabel.setId("clickTextLabel");
			topToolBar.add(clickTextLabel);
			topToolBar.add(new SeparatorToolItem());
		}

		if (filter != null){
			filter.bind(store);
			filter.setId("searchEntityList");
			filter.setEmptyText(searchText);
			LabelToolItem filterLabel = new LabelToolItem(searchText);
			filterLabel.setId("searchEntityListLabel");
			topToolBar.add(filterLabel);
			topToolBar.add(filter);
		}

		listContentPanel.setTopComponent(topToolBar);
	}

	protected GridView createGridView() {
		GridView gridView = new GridView();
		gridView.setAutoFill(true);
		gridView.setForceFit(true);
		gridView.setEmptyText("No records currently exist");
		return gridView;
	}

	@Override
	public void displaySuccessMsg(String msg) {
		displayMessage(msg, true);
	}
	
	@Override
	public void displaySuccessMsg(String msg, String id) {
		if( msg != null && !msg.isEmpty()){
			msgLabel.setText(msg);
			msgLabel.setStyleName("create-success");
			msgLabel.getElement().setId("create-success-"+id);
			msgLabel.getElement().setPropertyString("name", "create-success"+id);
		}else{
			clearSuccessMsg();
		}
	}

	public void displayMessage(String msg, boolean addSuccessStyle) {
		if( msg != null && !msg.isEmpty()){
			msgLabel.setText(msg);
			if (addSuccessStyle)
				msgLabel.setStyleName("create-success");
				msgLabel.getElement().setId("create-success");
		}else{
			clearSuccessMsg();
		}
	}

	public void clearSuccessMsg() {
		msgLabel.setText("");
		msgLabel.removeStyleName("create-success");
	}

	public PagingToolBar getPagingToolBar() {
		return toolBar;
	}

	public ToolBar getTopToolBar() {
		return topToolBar;
	}

	public Widget getViewWidget() {
		return this;
	}

	public Grid<BeanModel> getEntityListGrid() {
		return entityList;
	}

	public void reconfigureGrid(List<ColumnConfig> configs) {
		ListStore<BeanModel> store = getEntityListGrid().getStore();
		// disable events when calling re-configure to prevent re-loading of store
		getEntityListGrid().enableEvents(false);
		getEntityListGrid().reconfigure(store, new ColumnModel(configs));
		getEntityListGrid().enableEvents(true);
	}

	/**
	 * Default implementation that returns null. Override in classes that require it.
	 * @see org.celllife.mobilisr.client.app.EntityList#getNewEntityButton()
	 */
	@Override
	public Button getNewEntityButton() {
		return null;
	}

}
