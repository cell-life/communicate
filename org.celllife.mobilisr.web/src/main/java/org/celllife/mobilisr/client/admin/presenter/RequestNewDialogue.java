package org.celllife.mobilisr.client.admin.presenter;

import java.util.Collection;

import org.celllife.mobilisr.client.Messages;
import org.celllife.mobilisr.client.UserContext;
import org.celllife.mobilisr.client.model.ViewModel;
import org.celllife.mobilisr.client.view.gxt.DirtyCheckFormButtonBinding;
import org.celllife.mobilisr.client.view.gxt.MessageBoxWithIds;
import org.celllife.mobilisr.client.view.gxt.MyGXTButton;
import org.celllife.mobilisr.client.view.gxt.MyGXTFormPanel;
import org.celllife.mobilisr.client.view.gxt.MyGXTTextArea;
import org.celllife.mobilisr.client.view.gxt.MyGXTTextField;
import org.celllife.mobilisr.domain.User;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.binding.FieldBinding;
import com.extjs.gxt.ui.client.binding.FormBinding;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.FormButtonBinding;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;

public class RequestNewDialogue extends Window {

	private RowLayout rowLayout;
	private MyGXTButton submitButton;
	private MyGXTButton cancelButton;
	private MyGXTTextField txtFirstname;
	private MyGXTTextField txtLastname;
	private MyGXTTextField txtUsername;
	private MyGXTTextField txtEmailAddress;
	private MyGXTTextField txtOrganisation;
	private MyGXTTextField txtMSISDN;
	private MyGXTTextArea txtEmailBody;

	private MyGXTFormPanel formPanel;
	private FormLayout formLayout = new FormLayout();

	private FormBinding formBinding;

	private FormData formData;
	private User user = null;

	private FormButtonBinding binding;
	private ButtonBar buttonBar;
	private String formType;
	
	private Listener<BaseEvent> listener;

	public RequestNewDialogue(String type) {
		formType = type;
		rowLayout = new RowLayout(Orientation.VERTICAL);
		submitButton = new MyGXTButton("doneBtn", Messages.INSTANCE.done());
		cancelButton = new MyGXTButton("cancelBtn", Messages.INSTANCE.cancel());

		txtFirstname = new MyGXTTextField(Messages.INSTANCE.userFirstName(),
				User.PROP_FIRST_NAME, false, "e.g. Thandi");
		txtLastname = new MyGXTTextField(Messages.INSTANCE.userLastName(),
				User.PROP_LAST_NAME, false, "e.g. Mandela");
		txtUsername = new MyGXTTextField(Messages.INSTANCE.userUsername(),
				User.PROP_USERNAME, false, "Enter username");
		txtEmailAddress = new MyGXTTextField(
				Messages.INSTANCE.userEmailAddress(), User.PROP_EMAIL, false,
				"e.g. thandi.mandela@freeemail.com");
		txtOrganisation = new MyGXTTextField(
				Messages.INSTANCE.userOrganisation(), User.PROP_ORGANIZATION,
				false, "e.g. Microsoft");
		txtMSISDN = new MyGXTTextField(Messages.INSTANCE.userMobileNumber(),
				User.PROP_MSISDN, false, "e.g. 0721231234");
		txtEmailBody = new MyGXTTextArea(formType + " Description", formType
				+ "Description", false,
				"Please give us a general description of the filter you need.");

		formPanel = new MyGXTFormPanel("Enter a description for the "
				+ formType, true);
		formLayout = new FormLayout();
		formData = new FormData("-20");
		user = null;

		init();

		add(initFormAndDoBinding());
		add(createFormButtons());

		setVisible(true);
	}

	private void init() {

		formBinding = null;
		binding = null;
		user = UserContext.getUser();
	}

	private MyGXTFormPanel initFormAndDoBinding() {

		formLayout.setLabelSeparator("");

		setHeading("Request a New " + formType);
		setBlinkModal(true);
		setClosable(false);
		setModal(true);
		setPlain(true);
		setLayout(rowLayout);
		setWidth(377);

		createForm(formPanel);

		doBindings();

		formPanel.setWidth(375);
		formPanel.setLayout(formLayout);
		formPanel.setPadding(30);
		formPanel.setBorders(false);

		return formPanel;

	}

	private void createForm(LayoutContainer container) {
		configureFormFields();
		container.add(txtUsername, formData);
		container.add(txtFirstname, formData);
		container.add(txtLastname, formData);
		container.add(txtMSISDN, formData);
		container.add(txtEmailAddress, formData);
		container.add(txtOrganisation, formData);
		txtEmailBody.setHeight(150); // set the form data so that the entire message shows.
		container.add(txtEmailBody, formData);
	}

	private void configureFormFields() {

		txtUsername.disable();
		txtOrganisation.disable();
		txtFirstname.disable();
		txtLastname.disable();
		txtEmailAddress.disable();
		txtMSISDN.disable();

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

		buttonBar = new ButtonBar();

		submitButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				submitForm();
			}
		});

		cancelButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				resetForm();
			}
		});

		buttonBar.setAlignment(HorizontalAlignment.CENTER);
		buttonBar.setSpacing(10);
		buttonBar.add(submitButton);
		buttonBar.add(cancelButton);
		return buttonBar;
	}

	private void submitForm() {

		try {
			listener.handleEvent(new BaseEvent(this));
		}
		catch (Exception e) {
			MessageBoxWithIds.alert("Submit Form Exception", e.toString(), null);
		}
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

	private void resetForm() {
		formBinding = null;
		binding = null;
		hide();
	}

	public String getRequestText(){
		return txtEmailBody.getValue();
	}

	public void setListener(Listener<BaseEvent> listener2) {
		this.listener = listener2;
	}
	
	public User getUser(){
		return user;
	}

}
