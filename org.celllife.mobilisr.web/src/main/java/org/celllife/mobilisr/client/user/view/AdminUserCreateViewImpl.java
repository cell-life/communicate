package org.celllife.mobilisr.client.user.view;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.celllife.mobilisr.client.Constants;
import org.celllife.mobilisr.client.Messages;
import org.celllife.mobilisr.client.Resources;
import org.celllife.mobilisr.client.UserContext;
import org.celllife.mobilisr.client.model.ViewModel;
import org.celllife.mobilisr.client.template.view.EntityCreateTemplateImpl;
import org.celllife.mobilisr.client.user.AdminUserCreateView;
import org.celllife.mobilisr.client.validator.PasswordValidator;
import org.celllife.mobilisr.client.validator.ValidatorFactory;
import org.celllife.mobilisr.client.view.gxt.ButtonGridCellRenderer;
import org.celllife.mobilisr.client.view.gxt.GridModelEvent;
import org.celllife.mobilisr.client.view.gxt.MenuAction;
import org.celllife.mobilisr.client.view.gxt.MenuActionItem;
import org.celllife.mobilisr.client.view.gxt.ModelUtil;
import org.celllife.mobilisr.client.view.gxt.MyGXTButton;
import org.celllife.mobilisr.client.view.gxt.MyGXTComboBox;
import org.celllife.mobilisr.client.view.gxt.MyGXTFormPanel;
import org.celllife.mobilisr.client.view.gxt.MyGXTTextField;
import org.celllife.mobilisr.client.view.gxt.ToggleMenuActionItem;
import org.celllife.mobilisr.domain.ApiKey;
import org.celllife.mobilisr.domain.MobilisrPermission;
import org.celllife.mobilisr.domain.Organization;
import org.celllife.mobilisr.domain.Role;
import org.celllife.mobilisr.domain.User;

import com.extjs.gxt.ui.client.Style.ButtonScale;
import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.IconAlign;
import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.Style.SortDir;
import com.extjs.gxt.ui.client.data.BaseListLoadResult;
import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.data.BeanModelFactory;
import com.extjs.gxt.ui.client.data.BeanModelLookup;
import com.extjs.gxt.ui.client.data.LoadEvent;
import com.extjs.gxt.ui.client.data.ModelComparer;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.LoadListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.event.TabPanelEvent;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.StoreSorter;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.AdapterField;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.DualListField;
import com.extjs.gxt.ui.client.widget.form.FormButtonBinding;
import com.extjs.gxt.ui.client.widget.form.ListField;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.grid.GridView;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.extjs.gxt.ui.client.widget.layout.HBoxLayout;
import com.extjs.gxt.ui.client.widget.layout.HBoxLayoutData;
import com.extjs.gxt.ui.client.widget.layout.MarginData;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;

public class AdminUserCreateViewImpl extends EntityCreateTemplateImpl<User> implements AdminUserCreateView {
	
	/**
	 * This listener is used to allow pre-selection of the roles that
	 * get added to the roleList.
	 * 
	 * @author simon@cell-life.org
	 */
	private class CustomLoadListener extends LoadListener{
		@Override
		public void loaderLoad(LoadEvent le) {
			super.loaderLoad(le);
			BaseListLoadResult<BeanModel> baseListLoadResult = le.getData();
			List<BeanModel> roleList = baseListLoadResult.getData();
			setRoles(roleList);
		}
	}

	private MyGXTTextField userFirstName;
	private MyGXTTextField userLastName;
	private MyGXTTextField userName;
	private MyGXTTextField password;
	private TextField<String> newPwdField;
	private TextField<String> confirmNewPwdField;
	private MyGXTTextField email;
	private MyGXTTextField msisdn;

	private Button resetPassword;
	private Button pwdSaveButton;
	private Button pwdCancelButton;
	private MyGXTButton createKeyButton;

	private ComboBox<BeanModel> orgComboBox;

	private TabItem userDetailTab;
	private TabItem userRoleTab;
	private TabItem apiKeyTab;
	
	private final Window resetPasswordDialog = new Window();

	private final DualListField<BeanModel> roleListField = new DualListField<BeanModel>();
	private ListField<BeanModel> availableRolesList = roleListField.getFromList();
	private ListField<BeanModel> selectedRoleList = roleListField.getToList();
	private BeanModelFactory roleBeanModelFactory = BeanModelLookup.get().getFactory(Role.class);
	private ListStore<BeanModel> rolesStore = new ListStore<BeanModel>();
	private ListStore<BeanModel> selectedRolesStore = new ListStore<BeanModel>();
	private ListStore<BeanModel> apiKeyStore;
	protected List<ApiKey> removedKeys;

	@Override
	public void createView(){
		super.createView();
		
		Map<Button, Boolean> formButtons = new LinkedHashMap<Button, Boolean>();
		formButtons.put(submitButton, true);
		formButtons.put(cancelButton, false);
		
		layoutCreateTemplateUsingTabs(Messages.INSTANCE.userCreateHeader());
		
		apiKeyStore = new ListStore<BeanModel>();
		removedKeys = new ArrayList<ApiKey>();

		orgComboBox = new MyGXTComboBox<BeanModel>("Select an Organisation",
				Organization.PROP_NAME, true);
		orgComboBox.setName(User.PROP_ORGANIZATION);
		 
		resetPassword = new MyGXTButton("Set Password");
		pwdSaveButton = new MyGXTButton(Messages.INSTANCE.done());
		pwdCancelButton = new MyGXTButton(Messages.INSTANCE.cancel());
		
		userFirstName = new MyGXTTextField(Messages.INSTANCE.compulsory() + 
				Messages.INSTANCE.userFirstName(),
				User.PROP_FIRST_NAME, false, "e.g. Thandi");
		userLastName = new MyGXTTextField(Messages.INSTANCE.compulsory() + 
				Messages.INSTANCE.userLastName(),
				User.PROP_LAST_NAME, false, "e.g. Mandela");
		userName = new MyGXTTextField(Messages.INSTANCE.compulsory() +
				Messages.INSTANCE.userUsername(),
				User.PROP_USERNAME, false, "Enter username");
		password = new MyGXTTextField(Messages.INSTANCE.compulsory()+ 
				Messages.INSTANCE.userPassword(),
				User.PROP_PASSWORD, false, "Use \'Set Password\' to set");
		newPwdField = new TextField<String>();
		confirmNewPwdField = new TextField<String>();
		email = new MyGXTTextField(Messages.INSTANCE.compulsory() +
				Messages.INSTANCE.userEmailAddress(),
				User.PROP_EMAIL, false, "e.g. thandi.mandela@freeemail.com");
		msisdn = new MyGXTTextField(Messages.INSTANCE.compulsory() + 
				Messages.INSTANCE.userMobileNumber(),
				User.PROP_MSISDN, false, "e.g. 27721231234");
		
		createKeyButton = new MyGXTButton("createKeyButton",
				Messages.INSTANCE.userAddNewApiKey(), Resources.INSTANCE.add(),
				IconAlign.LEFT, ButtonScale.SMALL);

		tabPanel.add(createUserDetailTab());
		tabPanel.add(createUserRoleTab());
		if (UserContext.hasPermission(MobilisrPermission.MANAGE_API_KEYS)){
			tabPanel.add(createApiKeyTab());
		}

		LayoutContainer c1 = new LayoutContainer();
		c1.setAutoWidth(true);

		FormLayout formlayout = new FormLayout();
		formlayout.setLabelSeparator("");
		c1.setLayout(formlayout);

		newPwdField.setFieldLabel(Messages.INSTANCE.compulsory() + 
				Messages.INSTANCE.userNewPassword());
		newPwdField.setPassword(true);
		newPwdField.setAllowBlank(false);
		newPwdField.setEmptyText("Enter the new password");
		newPwdField.setMinLength(6);
		newPwdField.setMaxLength(20);
		confirmNewPwdField.setFieldLabel(Messages.INSTANCE.compulsory() + 
				Messages.INSTANCE.userConfirmPassword());
		confirmNewPwdField.setPassword(true);
		confirmNewPwdField.setAllowBlank(false);
		confirmNewPwdField.setEmptyText("Re-enter the above password");
		PasswordValidator pwdValidator = new PasswordValidator(newPwdField);
		confirmNewPwdField.setValidator(pwdValidator);

		c1.add(newPwdField);
		c1.add(confirmNewPwdField);
		
		pwdCancelButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				closeSetPwdDialog();
			}
		});
		
		pwdSaveButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				setPwdValue();
			}
		});
		
		resetPasswordDialog.setWidth(380);
		resetPasswordDialog.setPlain(true);
		resetPasswordDialog.setModal(true);
		resetPasswordDialog.setBlinkModal(true);
		resetPasswordDialog.setHeading("Set Password");
		
		MyGXTFormPanel pwdResetFormPanel = new MyGXTFormPanel(
				"NOTE: Fields marked with * are required", true);
		pwdResetFormPanel.setAutoWidth(true);
		pwdResetFormPanel.setButtonAlign(HorizontalAlignment.CENTER);
		pwdResetFormPanel.add(c1);
		pwdResetFormPanel.addButton(pwdSaveButton);
		pwdResetFormPanel.addButton(pwdCancelButton);
		FormButtonBinding pwdbinding = new FormButtonBinding(pwdResetFormPanel);
		pwdbinding.addButton(pwdSaveButton);
		resetPasswordDialog.add(pwdResetFormPanel);

		rolesStore.setModelComparer(new ModelComparer<BeanModel>() {
			@Override
			public boolean equals(BeanModel m1, BeanModel m2) {
				Role role1 = m1.getBean();
				Role role2 = m2.getBean();
				if (role1.getName().equals(role2.getName())) {
					return true;
				} else {
					return false;
				}
			}
		});

		addAndConfigFormButtons(formButtons, true);
		createFormBinding(formPanel, true);
	}

	private TabItem createUserDetailTab() {
		userDetailTab = new TabItem("Details");
		userDetailTab.setAutoHeight(true);
		userDetailTab.setAutoWidth(true);
		userDetailTab.setStyleAttribute("margin-left", "35%");
		userDetailTab.setStyleAttribute("margin-top", "2%");

		FormData formData = new FormData();
		formData.setMargins(new Margins(2));
		
		FormLayout formLayout = new FormLayout();
		formLayout.setLabelSeparator("");
		userDetailTab.setLayout(formLayout);
		userDetailTab.setScrollMode(Scroll.AUTO);

		orgComboBox.setFieldLabel(Messages.INSTANCE.compulsory() + 
				Messages.INSTANCE.userOrganisation());
		orgComboBox.setAllowBlank(false);

		userName.setMinLength(6);
		userName.setMaxLength(20);
		
		password.setPassword(true);
		password.setMinLength(6);
		password.setMaxLength(20);
		password.setAllowBlank(false);
		password.setEnabled(false);

		resetPassword.addListener(Events.Select, new Listener<ButtonEvent>(){
			@Override
			public void handleEvent(ButtonEvent be) {
				displaySetPwdDialog();
			}
		});

		email.setRegex(Constants.INSTANCE.emailRegex(),Messages.INSTANCE.validationEmail());
		msisdn.setValidator(ValidatorFactory.getMsisdnValidator());
		
		userDetailTab.add(userFirstName, formData);
		userDetailTab.add(userLastName, formData);
		userDetailTab.add(userName, formData);
		userDetailTab.add(password, formData);
		userDetailTab.add(new AdapterField(resetPassword), formData);
		userDetailTab.add(msisdn, formData);
		userDetailTab.add(email, formData);
		userDetailTab.add(orgComboBox, formData);
		userDetailTab.layout();
		
		return userDetailTab;
	}

	private TabItem createUserRoleTab() {
		userRoleTab = new TabItem("Roles");
		userRoleTab.setAutoWidth(true);
		userRoleTab.setAutoHeight(true);
		userRoleTab.setStyleAttribute("margin-left", "30%");
		userRoleTab.setStyleAttribute("margin-top", "5%");
		userRoleTab.addListener(Events.Select, new Listener<TabPanelEvent>() {
			@Override
			public void handleEvent(TabPanelEvent be) {
				displayUserRoleTabContent();
			}
		});

		return userRoleTab;
	}
	
	private TabItem createApiKeyTab() {
		apiKeyTab = new TabItem("API Keys");
		apiKeyTab.setAutoWidth(true);
		apiKeyTab.setAutoHeight(true);
		apiKeyTab.addListener(Events.Select, new Listener<TabPanelEvent>() {
			@Override
			public void handleEvent(TabPanelEvent be) {
				displayApiKeyTabContent();
			}
		});

		return apiKeyTab;
	}

	protected void displayApiKeyTabContent() {
		Boolean created = apiKeyTab.getData("created");
		if (created != null) {
			return;
		}
		
		apiKeyTab.setData("created", true);
		apiKeyTab.setLayout(new RowLayout(Orientation.VERTICAL));
		
		apiKeyTab.add(createKeyButton, new MarginData(10));
		
		ContentPanel container = new ContentPanel();
		container.setHeaderVisible(false);
		container.setLayout(new FitLayout());
		container.setSize("100%", "100%");
		container.setBodyBorder(false);
		container.setBorders(true);

		GridView gridView = new GridView();
		gridView.setAutoFill(true);
		gridView.setForceFit(true);
		gridView.setEmptyText("No records currently exist");

		ColumnModel cm = new ColumnModel(getColumnConfigs() );

		Grid<BeanModel> apikeyGrid = new Grid<BeanModel>(apiKeyStore, cm);
		apikeyGrid.setLoadMask(true);
		apikeyGrid.setView(gridView);
		apikeyGrid.setBorders( true );
		apikeyGrid.setHeight("100%");
		apikeyGrid.setStripeRows( true );
		apikeyGrid.disableTextSelection(false);

		container.add(apikeyGrid);
		apiKeyTab.add(container, new RowData(0.5, 1, new Margins(0, 10, 10, 10)));
		apiKeyTab.layout();
	}

	private List<ColumnConfig> getColumnConfigs() {
		List<ColumnConfig> configs = new ArrayList<ColumnConfig>();
		
		ColumnConfig actions = new ColumnConfig( "apiKeyActions", "Actions", 50 );
		actions.setFixed(true);
		actions.setSortable(false);

		ButtonGridCellRenderer actionRenderer = new ButtonGridCellRenderer();

		MenuAction menuAction = new MenuAction(null, null,
				Resources.INSTANCE.cog(), "actions");
		
		ToggleMenuActionItem voidMenu = new ToggleMenuActionItem("Deactivate", Resources.INSTANCE.chameleonRed(), ApiKey.PROP_VOIDED);
		voidMenu.setAltText("Activate");
		voidMenu.setAltImage(Resources.INSTANCE.chameleonGreen());
		voidMenu.addListener(Events.Select, new Listener<GridModelEvent>() {
			@Override
			public void handleEvent(GridModelEvent be) {
				BeanModel model = be.getModel();
				ApiKey key = model.getBean();
				key.setVoided(!key.isVoided());
				apiKeyStore.update(model);
				setDirty(true);
			}
		});
		menuAction.addMenuItem(voidMenu);
		
		MenuActionItem deleteMenu = new MenuActionItem("Delete", Resources.INSTANCE.delete());
		deleteMenu.addListener(Events.Select, new Listener<GridModelEvent>() {
			@Override
			public void handleEvent(GridModelEvent be) {
				BeanModel model = be.getModel();
				apiKeyStore.remove(model);
				ApiKey key = model.getBean();
				key.setDeleted(true);
				removedKeys.add(key);
				setDirty(true);
			}
		});
		menuAction.addMenuItem(deleteMenu);
		
		actionRenderer.addAction(menuAction);
		actions.setRenderer(actionRenderer);
		configs.add(actions);
		
		ColumnConfig labelConfig = new ColumnConfig(ApiKey.PROP_KEY, "Key", 50);
		configs.add(labelConfig);
		
		ColumnConfig type = new ColumnConfig( ApiKey.PROP_VOIDED, "Status", 50 );
		type.setRenderer(new GridCellRenderer<BeanModel>() {
			@Override
			public Object render(BeanModel model, String property,
					ColumnData config, int rowIndex, int colIndex,
					ListStore<BeanModel> store, Grid<BeanModel> grid) {
				
				Boolean voided = model.get(property);
				ImageResource image = voided ? Resources.INSTANCE.chameleonRed() :
					Resources.INSTANCE.chameleonGreen();
				
				return new Image(image);
			}
		});
		configs.add( type );

		return configs;
	}

	private void displayUserRoleTabContent() {
		Boolean created = userRoleTab.getData("created");
		if (created != null) {
			return;
		}
		
		userRoleTab.setData("created", true);
		
		final FormData formData = new FormData("-200");
		userRoleTab.setLayout(new FormLayout());

		roleListField.setFieldLabel(Messages.INSTANCE.userAssignRoles());
		roleListField.setAutoWidth(true);

		availableRolesList.setDisplayField("name");
		rolesStore.setStoreSorter(new StoreSorter<BeanModel>());
		rolesStore.setSortDir(SortDir.ASC);
		availableRolesList.setStore(rolesStore);

		selectedRoleList.setDisplayField("name");
		selectedRolesStore.setStoreSorter(new StoreSorter<BeanModel>());
		selectedRolesStore.setSortDir(SortDir.ASC);
		selectedRoleList.setStore(selectedRolesStore);
		
		HBoxLayout vBoxLayout = new HBoxLayout();
		LayoutContainer main = new LayoutContainer();  
		main.setLayout(vBoxLayout);
		main.setWidth("100%");
		//main.setBorders(true);
		LayoutContainer left = new LayoutContainer();
		left.setLayout(new FitLayout());
		left.setAutoWidth(true);
		LayoutContainer right = new LayoutContainer();
		right.setLayout(new FitLayout());
		right.setAutoWidth(true);
		left.add(new Label("Available Roles"));
		right.add(new Label("Assigned Roles"));
		
		HBoxLayoutData flex = new HBoxLayoutData(5, 10, 5, 120);
		flex.setFlex(1);
		main.add(left, flex);  
		main.add(right, new HBoxLayoutData(5, 10, 5, 5));
		
		userRoleTab.add(main, formData);
		userRoleTab.add(roleListField, formData);
		userRoleTab.layout();
	}

	private void displaySetPwdDialog() {
		newPwdField.clear();
		confirmNewPwdField.clear();
		resetPasswordDialog.show();
	}

	public ViewModel<User> getFormObject() {
		ViewModel<User> viewEntityModel = super.getFormObject();
		User user = (User) viewEntityModel.getModelObject();
		user.setPassword(password.getValue());
		user.setRoles(getSelectedRoles());
		
		// FIXME: deleting of API keys does not work
		List<ApiKey> keys = ModelUtil.convertBeanListToEntityList(apiKeyStore.getModels());
		keys.addAll(removedKeys);
		user.setApiKeys(keys);

		viewEntityModel.setModelObject(user);

		return viewEntityModel;
	}

	public void setFormObject(ViewModel<User> viewEntityModel) {
		super.setFormObject(viewEntityModel);
		User user = getModel();
		tabPanel.setSelection(userDetailTab);

		boolean modeUpdate = viewEntityModel.isModeUpdate();
		addToUserRoles(user.getRoles());
		
		removedKeys.clear();
		apiKeyStore.removeAll();
		apiKeyStore.add(ModelUtil.convertEntityListToBeanList(user.getApiKeys()));

		if (modeUpdate) {
			getHeaderLabel().setText("User: " + user.getFirstName() + " " + user.getLastName());
		} else {
			getHeaderLabel().setText(Messages.INSTANCE.userCreateHeader());
		}

	}

	private void closeSetPwdDialog() {
		newPwdField.clear();
		confirmNewPwdField.clear();
		resetPasswordDialog.hide();
	}

	private void setPwdValue() {
		password.setValue(newPwdField.getValue());
		setDirty();
		closeSetPwdDialog();
	}

	@Override
	public void setOrganizationStore(ListStore<BeanModel> store) {
		orgComboBox.setStore(store);
	}
	
	private void setRoles(List<BeanModel> listOfRoles) {
		rolesStore.removeAll();
		rolesStore.add(listOfRoles);
		User user = getModel();
		List<Role> userRolesList = user.getRoles();
		final List<BeanModel> toBeanModelList = roleBeanModelFactory.createModel(userRolesList);
		for(BeanModel toBeanModel: toBeanModelList)
		{ 
			if( rolesStore.contains(toBeanModel)){
				 BeanModel beanModel = rolesStore.findModel("name",((Role)toBeanModel.getBean()).getName());
				 rolesStore.remove(beanModel); 
			 } 
		}
	}

	@Override
	public void setRolesStore(ListStore<BeanModel> listOfRolesStore) {
		rolesStore = listOfRolesStore;
		rolesStore.getLoader().addLoadListener(new CustomLoadListener());
		ModelComparer<BeanModel> modelComparer = new ModelComparer<BeanModel>() {
			@Override
			public boolean equals(BeanModel m1, BeanModel m2) {
				Role role1 = m1.getBean();
				Role role2 = m2.getBean();
				if (role1.getId().equals(role2.getId())) {
					return true;
				} else {
					return false;
				}
			}
		};
		rolesStore.setModelComparer(modelComparer);
		availableRolesList.setStore(rolesStore);
	}

	private void addToUserRoles(List<Role> userRoles) {
		selectedRolesStore.removeAll();
		final List<BeanModel> toBeanModelList = roleBeanModelFactory.createModel(userRoles);
		selectedRolesStore.add(toBeanModelList);
	}

	@Override
	public List<Role> getSelectedRoles() {
		List<BeanModel> permissionBeanModels = selectedRolesStore.getModels();
		List<Role> roles = new ArrayList<Role>();
		for (BeanModel beanModel : permissionBeanModels) {
			Role roleBean = beanModel.getBean();
			roles.add(roleBean);
		}
		return roles;
	}
	
	@Override
	public Button getCreateKeyButton() {
		return createKeyButton;
	}
	
	@Override
	public void addApiKey(ApiKey apiKey) {
		setDirty(true);
		apiKeyStore.add(ModelUtil.convertEntityToBeanModel(apiKey));
	}
}
