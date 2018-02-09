package org.celllife.mobilisr.client.role.view;


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.celllife.mobilisr.client.Messages;
import org.celllife.mobilisr.client.model.ViewModel;
import org.celllife.mobilisr.client.role.AdminRoleCreateView;
import org.celllife.mobilisr.client.template.view.BaseFormView;
import org.celllife.mobilisr.client.view.gxt.MyGXTTextField;
import org.celllife.mobilisr.domain.MobilisrPermission;
import org.celllife.mobilisr.domain.Role;

import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.data.BeanModelFactory;
import com.extjs.gxt.ui.client.data.BeanModelLookup;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.event.SelectionEvent;
import com.extjs.gxt.ui.client.store.GroupingStore;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.grid.CheckBoxSelectionModel;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.grid.GroupingView;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.extjs.gxt.ui.client.widget.layout.MarginData;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;

public class AdminRoleCreateViewImpl extends BaseFormView<Role> implements AdminRoleCreateView{

	private TextField<String> roleName;
	private CheckBoxSelectionModel<BeanModel> sm;
	
	public AdminRoleCreateViewImpl(){
		createView();
	}

	@Override
	public void createView() {
		super.createView();
		
		roleName = new MyGXTTextField(Messages.INSTANCE.compulsory() + Messages.INSTANCE.roleName(),
				Role.PROP_NAME, false, "Enter role name");
		sm = new CheckBoxSelectionModel<BeanModel>();
		
		sm.addListener(Events.BeforeSelect, new Listener<SelectionEvent<BeanModel>>() {
			@Override
			public void handleEvent(SelectionEvent<BeanModel> be) {
				final BeanModel model = be.getModel();
				Scheduler.get().scheduleDeferred(new ScheduledCommand() {
					@Override
					public void execute() {
						MobilisrPermission p = model.getBean();
						Collection<MobilisrPermission> implied = p.getImpliedPermissions(true);
						List<BeanModel> models = convertPermissionListToBeanModelList(implied);
						sm.select(models, true);
					}
				});
			}
		});
		sm.addSelectionChangedListener(new SelectionChangedListener<BeanModel>() {
			@Override
			public void selectionChanged(SelectionChangedEvent<BeanModel> se) {
				setDirty(true);
			}
		});
		
		addTitleLabel("");
		
		configureFormPanel();
		
		add(getFormPanel(), new RowData(1, 1));

		LayoutContainer formContainer = new LayoutContainer();
		FormLayout layout = new FormLayout();
		layout.setLabelSeparator("");
		layout.setLabelWidth(140);
		formContainer.setLayout(layout);
		formContainer.add(roleName, new FormData());
		
		getFormPanel().add(formContainer, new RowData(1,-1));
		
		createPermissionsGrid();

		Map<Button, Boolean> formButtons = new LinkedHashMap<Button, Boolean>();
		formButtons.put(submitButton, true);
		formButtons.put(cancelButton, false);
		
		addAndConfigFormButtons(formButtons, true);
		createFormBinding(getFormPanel(), true);
	}
	
	public void createPermissionsGrid() {
		Label actionGridLabel = new Label("Permissions");
		getFormPanel().add(actionGridLabel, new MarginData(5, 10, 5, 10));

		ContentPanel container = new ContentPanel();
		container.setHeaderVisible(false);
		container.setLayout(new FitLayout());
		container.setSize("100%", "100%");
		container.setBodyBorder(false);
		container.setBorders(true);
		
		GroupingStore<BeanModel> permissionStore = new GroupingStore<BeanModel>();  
		permissionStore.add(initFromPermissionData());  
		permissionStore.groupBy(MobilisrPermission.PROP_GROUP);  

		GroupingView gridView = new GroupingView();
		gridView.setShowGroupedColumn(false);
		gridView.setAutoFill(true);
		gridView.setForceFit(true);
		gridView.setEmptyText("No records currently exist");

		ColumnModel cm = new ColumnModel(getColumnConfigs());

		Grid<BeanModel> actionsGrid = new Grid<BeanModel>(permissionStore, cm);
		actionsGrid.setLoadMask(true);
		actionsGrid.setView(gridView);
		actionsGrid.setBorders( true );
		actionsGrid.setHeight("100%");
		actionsGrid.setStripeRows( true );
		
		actionsGrid.setSelectionModel(sm);
		actionsGrid.addPlugin(sm);

		container.add(actionsGrid);
		getFormPanel().add(container, new RowData(1, 1, new Margins(0, 10, 0, 10)));
	}
	
	private List<ColumnConfig> getColumnConfigs() {
		List<ColumnConfig> configs = new ArrayList<ColumnConfig>();
		ColumnConfig checkColumn = sm.getColumn();
		checkColumn.setRenderer(new GridCellRenderer<BeanModel>() {
			public String render(BeanModel model, String property,
					ColumnData config, int rowIndex, int colIndex,
					ListStore<BeanModel> store, Grid<BeanModel> grid) {
				config.cellAttr = "rowspan='2'";
				MobilisrPermission p = model.getBean();
				return "<div class='x-grid3-row-checker' id='" + p.name()
						+ "'>&#160;</div>";
			}
		});
		configs.add(checkColumn);
		configs.add(new ColumnConfig(MobilisrPermission.PROP_DISPLAY_NAME,
				"Permission Name", 50));
		configs.add(new ColumnConfig(MobilisrPermission.PROP_GROUP,
				"Group", 50));
		return configs;
	}
	
	public void showSelectedPermissions(List<MobilisrPermission> permissionsList){
		Set<MobilisrPermission> impliedPermissions = new HashSet<MobilisrPermission>();
		for (MobilisrPermission perm : permissionsList) {
			impliedPermissions.addAll(perm.getImpliedPermissions(true));
		}
		List<BeanModel> modelList = convertPermissionListToBeanModelList(impliedPermissions);
		// disable firing events here to prevent listeners from executing
		sm.setFiresEvents(false);
		sm.setSelection(modelList);
		sm.setFiresEvents(true);
	}
	
	@Override
	public ViewModel<Role> getFormObject() {
		ViewModel<Role> vem = super.getFormObject();
		List<BeanModel> items = sm.getSelectedItems();
		final List<MobilisrPermission> selectedPermissions = convertSelectedPermissions(items);
		final List<MobilisrPermission> permissions = compactPermissionList(selectedPermissions);
		
		Role role = (Role) vem.getModelObject();
		role.setPermissionsList(permissions);
		
		ViewModel<Role> viewEntityModel = new ViewModel<Role>();
		viewEntityModel.setModelObject(role);
		return viewEntityModel;
	}

	/**
	 * This method removes permissions that are implied by other permissions in the list.
	 * 
	 * @param permissions
	 * @return
	 */
	private List<MobilisrPermission> compactPermissionList(List<MobilisrPermission> permissions) {
		Set<MobilisrPermission> toRemove = new HashSet<MobilisrPermission>();
		for (MobilisrPermission perm : permissions) {
			toRemove.addAll(perm.getImpliedPermissions(false));
		}
		
		permissions.removeAll(toRemove);
		return permissions;
	}

	private List<MobilisrPermission> convertSelectedPermissions(List<BeanModel> items) {
		final List<MobilisrPermission> selectedPermissions = new ArrayList<MobilisrPermission>();
		for (BeanModel item : items) {
			MobilisrPermission permission = item.getBean();
			selectedPermissions.add(permission);
		}
		return selectedPermissions;
	}

	@Override
	public void setFormObject(ViewModel<Role> viewEntityModel) {
		super.setFormObject(viewEntityModel);
		Role role = (Role) viewEntityModel.getModelObject();
		
		String name  = role.getName();
		titleLabel.setText("Role: " + name == null ? "" : name);
		showSelectedPermissions(role.getPermissionsList());
	}

	public List<BeanModel> initFromPermissionData(){
		
		MobilisrPermission[] availablePermissions = MobilisrPermission.getAssignablePermissions();
		List<MobilisrPermission> listOfPermissions = new ArrayList<MobilisrPermission>();
		for(MobilisrPermission permission: availablePermissions){
			listOfPermissions.add(permission);
		}
		
		List<BeanModel> beanModelList = convertPermissionListToBeanModelList(listOfPermissions);
		return beanModelList;
	}
	
	private List<BeanModel> convertPermissionListToBeanModelList(
			Collection<MobilisrPermission> mobilisrPermissions) {
		BeanModelFactory beanModelFactory = BeanModelLookup.get().getFactory(
				MobilisrPermission.class);
		List<BeanModel> beanModelList = beanModelFactory.createModel(mobilisrPermissions);
		return beanModelList;
	}
}
