package org.celllife.mobilisr.client.user.view;

import java.util.Collection;

import org.celllife.mobilisr.client.Constants;
import org.celllife.mobilisr.client.Messages;
import org.celllife.mobilisr.client.UserContext;
import org.celllife.mobilisr.client.model.ViewModel;
import org.celllife.mobilisr.client.validator.PasswordValidator;
import org.celllife.mobilisr.client.validator.ValidatorFactory;
import org.celllife.mobilisr.client.view.gxt.DirtyCheckFormButtonBinding;
import org.celllife.mobilisr.client.view.gxt.MessageBoxWithIds;
import org.celllife.mobilisr.client.view.gxt.MobilisrAsyncCallback;
import org.celllife.mobilisr.client.view.gxt.ModelUtil;
import org.celllife.mobilisr.client.view.gxt.MyGXTButton;
import org.celllife.mobilisr.client.view.gxt.MyGXTFormPanel;
import org.celllife.mobilisr.client.view.gxt.MyGXTTextField;
import org.celllife.mobilisr.domain.ApiKey;
import org.celllife.mobilisr.domain.Role;
import org.celllife.mobilisr.domain.User;
import org.celllife.mobilisr.service.gwt.UserServiceAsync;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.binding.FieldBinding;
import com.extjs.gxt.ui.client.binding.FormBinding;
import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.FieldSet;
import com.extjs.gxt.ui.client.widget.form.FormButtonBinding;
import com.extjs.gxt.ui.client.widget.form.ListField;
import com.extjs.gxt.ui.client.widget.form.Validator;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;

/**
 * 
 * @author Munaf Sheikh (munaf@cell-life.org)
 */
public class UserProfileViewImpl extends Window{

	private RowLayout rowLayout = new RowLayout(Orientation.VERTICAL);
	private MyGXTButton submitButton;
	private MyGXTButton cancelButton;
	
	private MyGXTTextField txtFirstname;
	private MyGXTTextField txtLastname;
	private MyGXTTextField txtUsername;
	private MyGXTTextField txtPassword;
	private MyGXTTextField txtEmailAddress;
	private MyGXTTextField txtOrganisation;
	private MyGXTTextField txtMSISDN;
	private MyGXTTextField txtNewPassword;
	private MyGXTTextField txtConfirmNewPwd;
	
	private MyGXTFormPanel formPanel = new MyGXTFormPanel("Edit your profile and click save", true);
	private FormLayout formLayout = new FormLayout();

	private String allOkay;

	private FormBinding formBinding;

	private User user = null;
	
	private FieldSet fieldSet;

	private FormLayout fieldSetlayout;
	private boolean changePassword = false;
	private ListField<BeanModel> roleList;
	private ListField<BeanModel> apiKeyList;
	private FormButtonBinding binding;
	private final UserServiceAsync userServiceAsync;

	public UserProfileViewImpl(UserServiceAsync userServiceAsync) {
		this.userServiceAsync = userServiceAsync;
		
		init();
		
		add(initFormAndDoBinding());
		add(createFormButtons());

		setVisible(true);
	}

	private void init(){
		formBinding = null;
		binding = null;
		user = UserContext.getUser();
	}
	
	private MyGXTFormPanel initFormAndDoBinding() {

		formLayout.setLabelSeparator("");

		setId("profileEdit");
		setHeading("My Profile");
		getHeader().setId("profileEditHeader");
		setBlinkModal(true);
		setClosable(false);
		setModal(true);
		setPlain(true);
		setLayout(rowLayout);
		setWidth(377);
		
		createMyProfileForm(formPanel);
		
		doBindings();

		formPanel.setWidth(375);
		formPanel.setLayout(formLayout);
		formPanel.setPadding(30);
		formPanel.setBorders(false);
		formPanel.setId("profileEditPanel");
		formPanel.getHeader().setId("profileEditPanelHeader");

		return formPanel;

	}

	private void createMyProfileForm(LayoutContainer container) {

		submitButton = new MyGXTButton("doneBtn",
				Messages.INSTANCE.save());
		cancelButton = new MyGXTButton("cancelBtn",
				Messages.INSTANCE.cancel());
		
		txtFirstname = new MyGXTTextField(Messages.INSTANCE.compulsory() + 
				Messages.INSTANCE.userFirstName(), User.PROP_FIRST_NAME, false,
				"e.g. Thandi");
		txtLastname = new MyGXTTextField(Messages.INSTANCE.compulsory() +
				Messages.INSTANCE.userLastName(), User.PROP_LAST_NAME, false,
				"e.g. Mandela");
		txtUsername = new MyGXTTextField(Messages.INSTANCE.compulsory() + 
				Messages.INSTANCE.userUsername(), User.PROP_USERNAME, false,
				"Enter username");
		txtPassword = new MyGXTTextField(Messages.INSTANCE.compulsory() +
				Messages.INSTANCE.userPassword(), "oldUserPwd", false,
				"Use \'Set Password\' to set");
		txtEmailAddress = new MyGXTTextField(Messages.INSTANCE.compulsory() +
				Messages.INSTANCE.userEmailAddress(), User.PROP_EMAIL, false,
				"e.g. thandi.mandela@freeemail.com");
		txtOrganisation = new MyGXTTextField(
				Messages.INSTANCE.userOrganisation(), User.PROP_ORGANIZATION, false,
				"e.g. Microsoft");
		txtMSISDN = new MyGXTTextField(Messages.INSTANCE.compulsory() + 
				Messages.INSTANCE.userMobileNumber(), User.PROP_MSISDN, false, "e.g. 0721231234");
		txtNewPassword = new MyGXTTextField(Messages.INSTANCE.compulsory() + 
				Messages.INSTANCE.userNewPassword(), "NEW_PASSWORD", false, "eg: Password");
		txtConfirmNewPwd = new MyGXTTextField(Messages.INSTANCE.compulsory() +
				Messages.INSTANCE.userConfirmPassword(), "CONFIRM_PASSWORD", false,
				"eg: Password");
		
		configureUserProfileFields();
		createUserRoleList();
		createApiKeyList();
		txtUsername.setMinLength(6);
		txtUsername.setMaxLength(20);
		container.add(txtUsername, new FormData("-20"));
		createPwdFieldSet(container);
		container.add(txtFirstname, new FormData("-20"));
		container.add(txtLastname, new FormData("-20"));
		container.add(txtMSISDN, new FormData("-20"));
		container.add(txtEmailAddress, new FormData("-20"));
		container.add(txtOrganisation, new FormData("-20"));
		container.add(roleList, new FormData("-20"));
		container.add(apiKeyList, new FormData("-20"));
	}
	
	public void createPwdFieldSet(LayoutContainer formElementContainer) {

		txtNewPassword.setMinLength(6);
		txtNewPassword.setMaxLength(20);
		txtNewPassword.disable();
		txtNewPassword.setPassword(true);
		
		txtConfirmNewPwd.disable();
		txtConfirmNewPwd.setPassword(true);
		
		fieldSet = new FieldSet();
		fieldSet.setId("changePass");
		fieldSet.setHeading("Change my password");
		fieldSet.setCollapsible(true);
		fieldSet.setCheckboxToggle(true);
		fieldSet.collapse();

		fieldSetlayout = new FormLayout();
		fieldSetlayout.setLabelWidth(100);
		fieldSet.setLayout(fieldSetlayout);

		fieldSet.add(txtPassword, new FormData("-20"));
		fieldSet.add(txtNewPassword, new FormData("-20"));
		fieldSet.add(txtConfirmNewPwd, new FormData("-20"));

		PasswordValidator pwdValidator = new PasswordValidator(txtNewPassword);
		txtConfirmNewPwd.setValidator(pwdValidator);
		
		fieldSet.addListener(Events.Collapse, new Listener<BaseEvent>(){
			@Override
			public void handleEvent(BaseEvent be) {
				changePassword = false;
				txtPassword.disable();
			}
		});
		fieldSet.addListener(Events.Expand, new Listener<BaseEvent>(){
			@Override
			public void handleEvent(BaseEvent be) {
				changePassword = true;
				txtPassword.enable();
				txtPassword.setValue("");
				txtConfirmNewPwd.setValue("");
				txtNewPassword.setValue("");
			}
		});

		formElementContainer.add(fieldSet);
	}
	
	private void createUserRoleList() {
		ListStore<BeanModel> store = new ListStore<BeanModel>();  
		store.add(ModelUtil.convertEntityListToBeanList(user.getRoles()));
		roleList = new ListField<BeanModel>();
		roleList.setId("roleList");
		roleList.setDisplayField(Role.PROP_NAME);
		roleList.setFieldLabel("Roles");
		roleList.setHeight(75);
		roleList.setStore(store);
	}
	
	private void createApiKeyList() {
		ListStore<BeanModel> store = new ListStore<BeanModel>();  
		store.add(ModelUtil.convertEntityListToBeanList(user.getActiveApiKeys()));
		apiKeyList = new ListField<BeanModel>();
		apiKeyList.setId("apiKeyList");
		apiKeyList.setDisplayField(ApiKey.PROP_KEY);
		apiKeyList.setFieldLabel("API Keys");
		apiKeyList.setHeight(75);
		apiKeyList.setStore(store);
	}
	
	private void configureUserProfileFields(){
		
		txtUsername.setId("username");
		txtOrganisation.setEnabled(false);
		txtOrganisation.setId("organization");
		txtFirstname.setErrorMsg("This field is compulsory");
		txtFirstname.setId("firstName");
		txtLastname.setErrorMsg("This field is compulsory");
		txtLastname.setId("lastname");
		txtPassword.setPassword(true);
		txtPassword.setErrorMsg( "Changing your password requires a your current valid password to be entered here");
		txtPassword.setId("password");
		txtPassword.addListener(Events.Focus, new Listener<BaseEvent>() {

			@Override
			public void handleEvent(BaseEvent be) {
				validatePasswords();
			}

		});
		txtNewPassword.setId("newPassword");
		txtConfirmNewPwd.setId("confirmNewPwd");

		txtEmailAddress.setRegex(Constants.INSTANCE.emailRegex(),
				Messages.INSTANCE.validationEmail());
		txtEmailAddress.setId("emailAddress");
		txtMSISDN.setValidator(ValidatorFactory.getMsisdnValidator());
		txtMSISDN.setId("MSISDN");
		
	}
	
	protected void validatePasswords() {

		txtPassword.setValidator(new Validator() {

			@Override
			public String validate(Field<?> field, String value) {
				userServiceAsync.validatePassword(user, value, new MobilisrAsyncCallback<Boolean>() {
							@Override
							// When password is same we return null (as that indicates sucess to gwt)
							public void onSuccess(Boolean isPwdMatch) {
								if (isPwdMatch != null && isPwdMatch) {
									allOkay = null;
									setNewPwdFieldsActive(true);
								} else {
									allOkay = "Password incorrect.";
									setNewPwdFieldsActive(false);
								}
							}
						});

				return allOkay;
			}
		});
	}

	private void doBindings() {
		
		ViewModel<User> viewEntityModel = new ViewModel<User>(user);
		setModel(viewEntityModel.getModelData());
		binding = new DirtyCheckFormButtonBinding(formPanel, true);
		binding.addButton(submitButton);
		
		formBinding = new FormBinding(formPanel, true);
		formBinding.bind(viewEntityModel.getModelData());
		formBinding.setUpdateOriginalValue(true);
		addDirtyListenerToBindings(formBinding);
	}

	private ButtonBar createFormButtons() {

		ButtonBar buttonBar = new ButtonBar();

		submitButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				submitForm();
			}
		});

		cancelButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				resetForm(null);
			}
		});

		buttonBar.setAlignment(HorizontalAlignment.CENTER);
		buttonBar.setSpacing(10);
		buttonBar.add(submitButton);
		buttonBar.add(cancelButton);
		return buttonBar;
	}

	private void submitForm() {
		if (changePassword){
			user.setPassword(txtConfirmNewPwd.getValue());
		}

		userServiceAsync.saveCurrentUser(user, new MobilisrAsyncCallback<User>() {
			@Override
			protected void handleExpectedException(Throwable error) {
				MessageBoxWithIds.alert("Username already exists", error.getMessage(), null);
			}
			
			@Override
			public void onSuccess(User user) {
				resetForm(user);
			}
		});
	}
	
	protected void addDirtyListenerToBindings(FormBinding binding) {
		Collection<FieldBinding> bindings = binding.getBindings();
		for (FieldBinding fieldBinding : bindings) {
			addDirtyListenerToField(fieldBinding.getField());
		}
	}

	protected void addDirtyListenerToField(final Field<?> field) {
		field.addListener(Events.Change, new Listener<BaseEvent>() {
			@Override
			public void handleEvent(BaseEvent be) {
				submitButton.setEnabled(true);
			}
		});
	}

	private void setNewPwdFieldsActive(boolean b) {
		txtConfirmNewPwd.setEnabled(b);
		txtNewPassword.setEnabled(b);
	}

	/**
	 * 
	 */
	private void resetForm(User user) {
		formBinding = null;
		binding = null;
		if (user != null){
			UserContext.setUser(user);
		} else {
			// refresh the user from server to overwrite any changes made by the user
			refreshUser();
		}
		hide();
	}

	private void refreshUser() {
		userServiceAsync.getUser(UserContext.getUser().getId(), new MobilisrAsyncCallback<User>() {
			@Override
			public void onSuccess(User u) {
				UserContext.setUser(u);
			}
		});	
	}

}
