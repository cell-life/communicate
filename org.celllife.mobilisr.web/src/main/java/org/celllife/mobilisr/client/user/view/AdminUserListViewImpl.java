package org.celllife.mobilisr.client.user.view;

import java.util.ArrayList;
import java.util.List;

import org.celllife.mobilisr.client.Messages;
import org.celllife.mobilisr.client.Resources;
import org.celllife.mobilisr.client.UserContext;
import org.celllife.mobilisr.client.model.ViewModel;
import org.celllife.mobilisr.client.template.view.EntityListTemplateImpl;
import org.celllife.mobilisr.client.user.AdminUserListView;
import org.celllife.mobilisr.client.view.gxt.Action;
import org.celllife.mobilisr.client.view.gxt.ButtonGridCellRenderer;
import org.celllife.mobilisr.client.view.gxt.MyGXTButton;
import org.celllife.mobilisr.client.view.gxt.MyGXTComboBox;
import org.celllife.mobilisr.client.view.gxt.MyGXTToggleButton;
import org.celllife.mobilisr.client.view.gxt.ToggleAction;
import org.celllife.mobilisr.client.view.gxt.grid.EntityIDColumnConfig;
import org.celllife.mobilisr.domain.MobilisrPermission;
import org.celllife.mobilisr.domain.Organization;
import org.celllife.mobilisr.domain.User;

import com.extjs.gxt.ui.client.Style.ButtonScale;
import com.extjs.gxt.ui.client.Style.IconAlign;
import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.StoreFilterField;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.grid.GridView;
import com.extjs.gxt.ui.client.widget.grid.GroupingView;
import com.extjs.gxt.ui.client.widget.toolbar.FillToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;

public class AdminUserListViewImpl extends EntityListTemplateImpl implements AdminUserListView{

	private Button newUserButton;
	private ToggleAction toggleVoidAction;
	private MyGXTToggleButton showVoided;
	private ComboBox<BeanModel> filterOrgCombo;
	private MyGXTButton clearOrgFilterButton;

	@Override
	public void createView(){
		newUserButton = new MyGXTButton("newUserButton",
				Messages.INSTANCE.userAddNew(), Resources.INSTANCE.add(),
				IconAlign.LEFT, ButtonScale.SMALL);
		
		showVoided = new MyGXTToggleButton("showAll", null,
				Resources.INSTANCE.trash(), IconAlign.LEFT, ButtonScale.SMALL);
		showVoided.setToolTip("Show deleted Campaigns");
		showVoided.setToggledTooltip("Show Campaigns");
		
		filterOrgCombo = new MyGXTComboBox<BeanModel>("Filter by Organisation",
				Organization.PROP_NAME, true);
		
		clearOrgFilterButton = new MyGXTButton("clearOrgFilter", null,
				Resources.INSTANCE.clearTrigger(), IconAlign.LEFT,
				ButtonScale.SMALL);
		clearOrgFilterButton.setToolTip("Clear organisation filter");
		clearOrgFilterButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				filterOrgCombo.clear();
			}
		});

		Button[] buttons = new Button[]{newUserButton};

		layoutListTemplate(Messages.INSTANCE.userListHeader(), buttons, true);
	}

	@Override
	public void buildWidget( final ListStore<BeanModel> store, StoreFilterField<BeanModel> filter) {

		List<ColumnConfig> configs = new ArrayList<ColumnConfig>();
		EntityIDColumnConfig name = new EntityIDColumnConfig(User.PROP_FULL_NAME, "Name", 89, "user");
		name.setSortable(false);
		configs.add( name);
		configs.add( new ColumnConfig( User.PROP_USERNAME, "Username",89));
		configs.add( new ColumnConfig( User.PROP_ORGANIZATION+"."+Organization.PROP_NAME, "Organisation", 89));
		ColumnConfig e2 = new ColumnConfig( User.PROP_LAST_LOGIN_DATE, "Last Login",89);
		e2.setRenderer(new GridCellRenderer<BeanModel>() {

			@Override
			public Object render(BeanModel model, String property,
					ColumnData config, int rowIndex, int colIndex,
					ListStore<BeanModel> store, Grid<BeanModel> grid) {

				User u = model.getBean();
				String dd = "";
				DateTimeFormat dtf = DateTimeFormat.getFormat(PredefinedFormat.DATE_MEDIUM);
				if (u.getLastLoginDate()!=null){
					dd = dtf.format(u.getLastLoginDate());
				}else{
					dd = "Never Logged In";
				}

				return dd;

			}
		});
		configs.add( e2);

		ColumnConfig actions = new ColumnConfig("Actions", "Actions",89);
		ButtonGridCellRenderer actionRenderer = new ButtonGridCellRenderer();
		actions.setRenderer(actionRenderer);
		actions.setSortable(false);
		configs.add(actions);

		if (UserContext.hasPermission(MobilisrPermission.MANAGE_USERS)){
			toggleVoidAction = new ToggleAction(null, "Delete user",
					Resources.INSTANCE.delete(), "void", "voided") {
				@Override
				public Button render(BeanModel model) {
					Button b = super.render(model);
					User user = model.getBean();
					boolean orgVoided = user.getOrganization().isVoided();

					if (orgVoided){
						b.setIcon(createImage(Resources.INSTANCE.addGrey()));
						b.disable();
						b.setToolTip("User belongs to a deleted organisation");
					}
					return b;
				}
			};
			toggleVoidAction.setAltImage(Resources.INSTANCE.add());
			toggleVoidAction.setAltTooltip("Reactivate user");
			toggleVoidAction.setAltIdPrefix("unvoid");
			actionRenderer.addAction(toggleVoidAction);
		}

		renderEntityListGrid(store, filter, configs, "Click on a user name to see details and make changes", "Search for User");
		getTopToolBar().add(new FillToolItem());
		getTopToolBar().add(filterOrgCombo);
		getTopToolBar().add(clearOrgFilterButton);
		getTopToolBar().add(new SeparatorToolItem());
		getTopToolBar().add(showVoided);
	}
	
	@Override
	protected GridView createGridView() {
		GroupingView gridView = new GroupingView();
		gridView.setShowGroupedColumn(true);
		gridView.setAutoFill(true);
		gridView.setForceFit(true);
		gridView.setEmptyText("No records currently exist");
		return gridView;
	}
	
	@Override
	public void setFormObject(ViewModel<User> vm){
		// remove the right margin on the org filter combo
		filterOrgCombo.el().setStyleAttribute("marginRight", "0px");
	}
	
	@Override
	public Button getNewEntityButton() {
		return newUserButton;
	}

	@Override
	public Action getToggleVoidAction() {
		return toggleVoidAction;
	}
	
	@Override
	public Button getShowVoidedButton() {
		return showVoided;
	}
	
	@Override
	public ComboBox<BeanModel> getFilterOrgCombo() {
		return filterOrgCombo;
	}

	@Override
	public void setOrganizationStore(ListStore<BeanModel> store) {
		filterOrgCombo.setStore(store);
	}
}
