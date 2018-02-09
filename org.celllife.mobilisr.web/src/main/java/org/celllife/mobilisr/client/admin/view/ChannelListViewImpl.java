package org.celllife.mobilisr.client.admin.view;

import java.util.ArrayList;
import java.util.List;

import org.celllife.mobilisr.client.Messages;
import org.celllife.mobilisr.client.Resources;
import org.celllife.mobilisr.client.UserContext;
import org.celllife.mobilisr.client.admin.ChannelListView;
import org.celllife.mobilisr.client.template.view.EntityListTemplateImpl;
import org.celllife.mobilisr.client.view.gxt.ButtonGridCellRenderer;
import org.celllife.mobilisr.client.view.gxt.MenuAction;
import org.celllife.mobilisr.client.view.gxt.MenuActionItem;
import org.celllife.mobilisr.client.view.gxt.MyGXTButton;
import org.celllife.mobilisr.client.view.gxt.ToggleMenuActionItem;
import org.celllife.mobilisr.constants.ChannelType;
import org.celllife.mobilisr.domain.Channel;
import org.celllife.mobilisr.domain.MobilisrPermission;

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
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.Image;

public class ChannelListViewImpl extends EntityListTemplateImpl implements ChannelListView{

	private Button newInChannelButton;
	private Button newOutChannelButton;
	private MenuActionItem editMenu;
	private ToggleMenuActionItem toggleStateMenu;
	private MenuActionItem viewMessageLogsMenu;
	private MenuActionItem viewNumberMappingMenu;

	@Override
	public void createView(){
		newInChannelButton = new MyGXTButton("newInChannelButton",
				Messages.INSTANCE.channelAddNew(ChannelType.IN), Resources.INSTANCE.add(),
				IconAlign.LEFT, ButtonScale.SMALL);
		newOutChannelButton = new MyGXTButton("newOutChannelButton",
				Messages.INSTANCE.channelAddNew(ChannelType.OUT), Resources.INSTANCE.add(),
				IconAlign.LEFT, ButtonScale.SMALL);
		
		ArrayList<Button> buttons = new ArrayList<Button>();
		if (UserContext.hasPermission(MobilisrPermission.CHANNELS_IN_CREATE))
			buttons.add(newInChannelButton);
		
		if (UserContext.hasPermission(MobilisrPermission.CHANNELS_OUT_CREATE))
			buttons.add(newOutChannelButton);
		
		layoutListTemplate(Messages.INSTANCE.channelListHeader(),
				buttons.toArray(new Button[] {}), true);
	}

	public void buildWidget( final ListStore<BeanModel> store, StoreFilterField<BeanModel> filter) {

		List<ColumnConfig> configs = new ArrayList<ColumnConfig>();
		
		ColumnConfig actions = new ColumnConfig( "channelActions", "Actions", 50 );
		actions.setFixed(true);
		actions.setSortable(false);

		ButtonGridCellRenderer actionRenderer = new ButtonGridCellRenderer();

		MenuAction menuAction = new MenuAction(null, null,
				Resources.INSTANCE.cog(), "actions");
		
		editMenu = new MenuActionItem("Edit", Resources.INSTANCE.tableEdit()){
			private boolean canEditIn;
			private boolean canEditOut;
			private boolean init = false;
			
			public MenuItem getMenuItem(BeanModel model) {
				initPermissions();
				Channel channel = model.getBean();
				switch (channel.getType()){
				case IN:
					if (canEditIn){
						return super.getMenuItem(model);
					}
					break;
				case OUT:
					if (canEditOut){
						return super.getMenuItem(model);
					}
					break;
				}
				return null;
			}

			private void initPermissions() {
				if (!init){
					canEditIn = UserContext.hasPermission(MobilisrPermission.CHANNELS_IN_EDIT);
					canEditOut = UserContext.hasPermission(MobilisrPermission.CHANNELS_OUT_EDIT);
					init  = true;
				}
			};
		};
		menuAction.addMenuItem(editMenu);
		
		viewMessageLogsMenu = new MenuActionItem("View message logs", Resources.INSTANCE.messageLogs());
		menuAction.addMenuItem(viewMessageLogsMenu);
		
		viewNumberMappingMenu = new MenuActionItem("View number routing", Resources.INSTANCE.routing());
		menuAction.addMenuItem(viewNumberMappingMenu);
		
		toggleStateMenu = new ToggleMenuActionItem(
				"Deactivate", Resources.INSTANCE.stop(),
				Channel.PROP_VOIDED){
			private boolean init;
			private boolean canStartStopIn;

			public MenuItem getMenuItem(BeanModel model) {
				initPermissions();
				Channel channel = model.getBean();
				if (channel.getType() == ChannelType.IN && canStartStopIn){
					// only allow start / stop of IN channels from here
					return super.getMenuItem(model);
				}
				return null;
			}
			
			private void initPermissions() {
				if (!init){
					canStartStopIn = UserContext.hasPermission(MobilisrPermission.CHANNELS_IN_START_STOP);
					init  = true;
				}
			}
		};
		toggleStateMenu.setAltText("Activate");
		toggleStateMenu.setAltImage(Resources.INSTANCE.start());
		menuAction.addMenuItem(toggleStateMenu);
		
		actionRenderer.addAction(menuAction);
		actions.setRenderer(actionRenderer);
		configs.add(actions);

		configs.add( new ColumnConfig( Channel.PROP_NAME, "Name", 100) );
		configs.add( new ColumnConfig( Channel.PROP_SHORT_CODE, "Channel number", 50 ) );
		ColumnConfig type = new ColumnConfig( Channel.PROP_TYPE, "Type", 50 );
		type.setRenderer(new GridCellRenderer<BeanModel>() {
			@Override
			public Object render(BeanModel model, String property,
					ColumnData config, int rowIndex, int colIndex,
					ListStore<BeanModel> store, Grid<BeanModel> grid) {
				
				ChannelType type = model.get(property);
				ImageResource image;
				switch (type) {
				case IN:
					image = Resources.INSTANCE.incoming();
					break;
				case OUT:
					image = Resources.INSTANCE.outgoing();
					break;
				default:
					image = Resources.INSTANCE.scheduleError();
					break;
				}
				
				LayoutContainer container = new LayoutContainer(new RowLayout(Orientation.HORIZONTAL));
				container.add(new Image(image), new RowData());
				container.add(new Label(type.name()), new RowData(-1,-1, new Margins(0,0,0,5)));
				// hack to get it to display since the layout() method is not called
				// after it is added to the cell
				container.setHeight(32);
				return container;
			}
		});
		configs.add( type );
		
		ColumnConfig status = new ColumnConfig( Channel.PROP_VOIDED, "Status", 50 );
		status.setRenderer(new GridCellRenderer<BeanModel>() {
			@Override
			public Object render(BeanModel model, String property,
					ColumnData config, int rowIndex, int colIndex,
					ListStore<BeanModel> store, Grid<BeanModel> grid) {
				
				Boolean voided = model.get(property);
				ImageResource image;
				String text;
				if (voided){
					image = Resources.INSTANCE.stop();
					text = "Inactive";
				} else {
					image = Resources.INSTANCE.start();
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
		
		ColumnConfig activated = new ColumnConfig( Channel.PROP_DATE_ACTIVATED, "Date activated", 50 );
		activated.setDateTimeFormat(DateTimeFormat.getFormat("dd-MM-yyyy"));
		configs.add( activated );
		
		ColumnConfig deactivated = new ColumnConfig( Channel.PROP_DATE_DEACTIVATED, "Date deactivated", 50 );
		deactivated.setDateTimeFormat(DateTimeFormat.getFormat("dd-MM-yyyy"));
		configs.add( deactivated );
		
		configs.add( new ColumnConfig( Channel.PROP_HANDLER, "Handler", 50 ) );
		
		renderEntityListGrid(store, filter, configs, null, "Search for a channel");
	}

	@Override
	public MenuActionItem getViewMessageLogsMenu() {
		return viewMessageLogsMenu;
	}
	
	@Override
	public MenuActionItem getViewNumberMappingMenu() {
		return viewNumberMappingMenu;
	}

	@Override
	public MenuActionItem getToggleStateMenu() {
		return toggleStateMenu;
	}
	
	@Override
	public MenuActionItem getEditMenu() {
		return editMenu;
	}

	@Override
	public Button getNewInChannelButton() {
		return newInChannelButton;
	}
	
	@Override
	public Button getNewOutChannelButton() {
		return newOutChannelButton;
	}
}
