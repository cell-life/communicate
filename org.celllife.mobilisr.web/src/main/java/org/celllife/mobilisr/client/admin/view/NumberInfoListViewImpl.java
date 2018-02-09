package org.celllife.mobilisr.client.admin.view;

import java.util.ArrayList;
import java.util.List;

import org.celllife.mobilisr.client.Messages;
import org.celllife.mobilisr.client.Resources;
import org.celllife.mobilisr.client.admin.NumberInfoListView;
import org.celllife.mobilisr.client.template.view.EntityListTemplateImpl;
import org.celllife.mobilisr.client.view.gxt.ButtonGridCellRenderer;
import org.celllife.mobilisr.client.view.gxt.MenuAction;
import org.celllife.mobilisr.client.view.gxt.MenuActionItem;
import org.celllife.mobilisr.client.view.gxt.MyGXTButton;
import org.celllife.mobilisr.client.view.gxt.ToggleMenuActionItem;
import org.celllife.mobilisr.domain.Channel;
import org.celllife.mobilisr.domain.NumberInfo;

import com.extjs.gxt.ui.client.Style.ButtonScale;
import com.extjs.gxt.ui.client.Style.IconAlign;
import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.StoreFilterField;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.Image;

public class NumberInfoListViewImpl extends EntityListTemplateImpl implements NumberInfoListView{

	private Button newInNumberInfoButton;
	private MenuActionItem editMenu;
	private ToggleMenuActionItem voidMenu;

	@Override
	public void createView(){
		newInNumberInfoButton = new MyGXTButton("newNumberInfoButton",
				Messages.INSTANCE.numberInfoAddNew(), Resources.INSTANCE.add(),
				IconAlign.LEFT, ButtonScale.SMALL);

		ArrayList<Button> buttons = new ArrayList<Button>();
		buttons.add(newInNumberInfoButton);
		layoutListTemplate(Messages.INSTANCE.numberInfoListHeader(),
				buttons.toArray(new Button[] {}), true);
	}

	public void buildWidget( final ListStore<BeanModel> store, StoreFilterField<BeanModel> filter) {

		List<ColumnConfig> configs = new ArrayList<ColumnConfig>();
		
		ColumnConfig actions = new ColumnConfig( "channelConfigActions", "Actions", 50 );
		actions.setFixed(true);
		actions.setSortable(false);

		ButtonGridCellRenderer actionRenderer = new ButtonGridCellRenderer();

		MenuAction menuAction = new MenuAction(null, null,
				Resources.INSTANCE.cog(), "actions");
		
		editMenu = new MenuActionItem("Edit", Resources.INSTANCE.tableEdit());
		menuAction.addMenuItem(editMenu);
		
		voidMenu = new ToggleMenuActionItem(
				"Make inactive", Resources.INSTANCE.chameleonRed(),
				NumberInfo.PROP_VOIDED);
		voidMenu.setAltText("Make active");
		voidMenu.setAltImage(Resources.INSTANCE.chameleonGreen());
		menuAction.addMenuItem(voidMenu);
		
		actionRenderer.addAction(menuAction);
		actions.setRenderer(actionRenderer);
		configs.add(actions);
		
		configs.add( new ColumnConfig( NumberInfo.PROP_NAME, "Name", 100) );
		configs.add( new ColumnConfig( NumberInfo.PROP_PREFIX, "Prefix", 50 ) );
		configs.add( new ColumnConfig( NumberInfo.PROP_VALIDATOR, "Validation Regex", 50 ) );
		configs.add( new ColumnConfig( NumberInfo.PROP_CHANNEL+"."+Channel.PROP_NAME, "Channel", 50 ) );
		ColumnConfig status = new ColumnConfig( NumberInfo.PROP_VOIDED, "Status", 50 );
		status.setRenderer(new GridCellRenderer<BeanModel>() {
			@Override
			public Object render(BeanModel model, String property,
					ColumnData config, int rowIndex, int colIndex,
					ListStore<BeanModel> store, Grid<BeanModel> grid) {
				
				Boolean voided = model.get(property);
				ImageResource image;
				String text;
				if (voided){
					image = Resources.INSTANCE.chameleonRed();
					text = "Inactive";
				} else {
					image = Resources.INSTANCE.chameleonGreen();
					text = "Active";
				}
				
				LayoutContainer container = new LayoutContainer(new RowLayout(Orientation.HORIZONTAL));
				container.add(new Image(image), new RowData());
				container.add(new Label(text), new RowData(-1,-1, new Margins(0,0,0,5)));
				// hack to get it to display since the layout() method is not called
				// after it is added to the cell
				container.setHeight(32);
				return container;
			}
		});
		configs.add( status );

		renderEntityListGrid(store, filter, configs, null, "Search for a channel");
	}

	@Override
	public Button getNewEntityButton() {
		return newInNumberInfoButton;
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
