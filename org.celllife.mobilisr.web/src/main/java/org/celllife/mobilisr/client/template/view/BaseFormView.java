package org.celllife.mobilisr.client.template.view;

import java.util.Collection;
import java.util.Map;
import java.util.logging.Logger;

import org.celllife.mobilisr.client.Constants;
import org.celllife.mobilisr.client.Messages;
import org.celllife.mobilisr.client.app.EntityCreate;
import org.celllife.mobilisr.client.model.ViewModel;
import org.celllife.mobilisr.client.view.gxt.DirtyCheckFormButtonBinding;
import org.celllife.mobilisr.client.view.gxt.FormUtil;
import org.celllife.mobilisr.client.view.gxt.MyGXTButton;
import org.celllife.mobilisr.client.view.gxt.MyGXTFormPanel;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.binding.FieldBinding;
import com.extjs.gxt.ui.client.binding.FormBinding;
import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.BooleanPropertyEditor;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.FormButtonBinding;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.HiddenField;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.user.client.ui.Widget;

public abstract class BaseFormView<T> extends LayoutContainer implements EntityCreate<T> {

	private static final Logger log = Logger.getLogger(BaseFormView.class.getName());
	
	private FormBinding formBinding;
	
	private Listener<FieldEvent> dirtyListener;
	
	protected Label msgLabel;
	protected Label titleLabel;
	protected MyGXTButton cancelButton;
	protected MyGXTButton submitButton;
	protected MyGXTFormPanel formPanel;

	/**
	 * This field is used to track the dirty state of the form. We need
	 * to use an actual field since the forms dirty state is taken solely
	 * from its fields.
	 * 
	 * @see FormPanel#isDirty()
	 */
	private HiddenField<Boolean> dirtyField;

	@Override
	public void createView() {
		setIntStyleAttribute("margin", 10);	
		setScrollMode(Scroll.AUTOY);		
		setLayout(new RowLayout(Orientation.VERTICAL));
		
		msgLabel = new Label("");
		titleLabel = new Label("");
		dirtyField = new HiddenField<Boolean>();
		dirtyField.setPropertyEditor(new BooleanPropertyEditor());
		cancelButton = new MyGXTButton("cancelButton", Messages.INSTANCE.cancel());
		submitButton = new MyGXTButton("submitButton", Messages.INSTANCE.save());
		formPanel = new MyGXTFormPanel("NOTE: Fields marked with * are required", true);
	}
	
	protected void configureFormPanel() {
		formPanel.setPadding(5);
		formPanel.setFrame(true);
		formPanel.setHeaderVisible(true);
		formPanel.setBorders(false);
		formPanel.setIntStyleAttribute("margin", 10);
		formPanel.setButtonAlign(HorizontalAlignment.CENTER);
		formPanel.setLayout(new RowLayout(Orientation.VERTICAL));
		formPanel.setAutoWidth(true);
		formPanel.setScrollMode(Scroll.AUTOY);
		formPanel.add(dirtyField);
	}
	
	protected void addTitleLabel(String titleLabelText){
		titleLabel.setText(titleLabelText);
		titleLabel.setId("titleLabelText");
		titleLabel.getElement().setId("titleLabelText");
		titleLabel.setStyleName(Constants.INSTANCE.styleFont14());
		add(titleLabel, new RowData(1,-1, new Margins(10)));

	}

	/**
	 * Method to add buttons to the field with form button bindings.
	 * 
	 * @param formButtons
	 *            a map of Buttons with corresponding boolean values. If the
	 *            boolean value for a button is true the button will be included
	 *            in the FormButtonBinding. Otherwise it will not.
	 * @param disableIfNotDirty
	 *            if set to true the buttons that are included in the form
	 *            button binding will only be enabled if the form is dirty
	 */
	protected void addAndConfigFormButtons(Map<Button, Boolean> formButtons, boolean disableIfNotDirty) {
		FormButtonBinding binding = new DirtyCheckFormButtonBinding(formPanel, disableIfNotDirty);
		for(Map.Entry<Button, Boolean> formButton: formButtons.entrySet()){
			if(formButton.getValue()){
				binding.addButton(formButton.getKey());
			}
			formPanel.addButton(formButton.getKey());
		}
	}
	
	public void setErrorMessage(String errorMsg) {
		if (errorMsg == null || errorMsg.length() == 0){
			msgLabel.setText("");
			msgLabel.removeStyleName("create-error");
		} else {
			msgLabel.setText("Error : " + errorMsg);
			msgLabel.setStyleName("create-error");
		}
	}
	
	public MyGXTFormPanel getFormPanel() {
		return formPanel;
	}
	
	public Label getHeaderLabel(){
		return titleLabel;
	}
	
	public Label getMessageLabel() {
		return msgLabel;
	}
	
	public MyGXTButton getFormCancelButton() {
		return cancelButton;
	}

	public MyGXTButton getFormSubmitButton() {
		return submitButton;
	}
	
	public Widget getViewWidget() {
		return this;
	}

	@Override
	public void setDirty(boolean dirty) {
		log.finer("Set dirty: " + dirty);
		if (dirty){
			this.dirtyField.setValue(dirty);
		} else {
			this.dirtyField.reset();
		}
	}
	
	@Override
	public void setDirty(){
		setDirty(true);
	}

	@Override
	public boolean isDirty() {
		Boolean value = dirtyField.getValue();
		return (value != null && value == true);
	}
	
	protected void addDirtyListenerToBindings(FormBinding binding) {
		Collection<FieldBinding> bindings = binding.getBindings();
		for (FieldBinding fieldBinding : bindings) {
			addDirtyListenerToField(fieldBinding.getField());
		}
	}
	
	protected void addDirtyListenerToBindings(FieldBinding fieldBinding) {
		addDirtyListenerToField(fieldBinding.getField());
	}
	
	protected void addDirtyListenerToField(final Field<?> field) {
		if (dirtyListener == null){
			dirtyListener = new Listener<FieldEvent>() {
				@Override
				public void handleEvent(FieldEvent be) {
					log.finer("Field value change: " + be.getField().getName());
					setDirty(true);
				}
			};
		}
		field.addListener(Events.Change, dirtyListener);
	}
	
	public void createFormBinding(FormPanel panel, boolean autobind) {
		this.formBinding = FormUtil.createFormBinding(panel, autobind);
		if (autobind){
			addDirtyListenerToBindings(formBinding);
		}
	}

	private void removeDirtyListerFromBinding(FieldBinding fieldBinding) {
		fieldBinding.getField().removeListener(Events.Change, dirtyListener);
	}

	public FormBinding getFormBinding() {
		return formBinding;
	}
	
	protected void addFieldBinding(FieldBinding fieldBinding) {
		if (formBinding != null) {
			formBinding.addFieldBinding(fieldBinding);
			addDirtyListenerToBindings(fieldBinding);
		}
	}
	
	protected void removeFieldBinding(FieldBinding fieldBinding){
		if (formBinding != null) {
			removeDirtyListerFromBinding(fieldBinding);
			formBinding.removeFieldBinding(fieldBinding);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public T getModel() {
		BeanModel model = super.getModel();
		if (model != null) {
			return (T) model.getBean();
		}
		return null;
	}

	protected void setModel(BeanModel model) {
		super.setModel(model);
	}
	
	@Override
	@Deprecated
	protected void setModel(ModelData model) {
	}
	
	@Override
	public void setFormObject(final ViewModel<T> viewModel) {
		BeanModel model = viewModel.getModelData();
		setModel(model);
		setDirty(viewModel.isDirty());
		setErrorMessage(viewModel.getViewMessage());
		
		if (viewModel.isModeCreate()) {
			submitButton.setText(Messages.INSTANCE.save());
		} else {
			submitButton.setText(Messages.INSTANCE.update());
		} 
		
		if (formBinding != null) {
			formBinding.bind(model);

			// clear the invalid state of the fields so that the user
			// isn't presented with a lot of errors particularly when
			// creating a new object
			Scheduler.get().scheduleDeferred(new ScheduledCommand() {
				@Override
				public void execute() {
					Collection<FieldBinding> bindings = getFormBinding().getBindings();
					for (FieldBinding fieldBinding : bindings) {
						fieldBinding.getField().clearInvalid();
					}
				}
			});
		}
	}
	
	@Override
	public ViewModel<T> getFormObject() {
		ViewModel<T> viewModel = new ViewModel<T>();
		viewModel.setDirty(isDirty());

		T entity = getModel();
		viewModel.setModelObject(entity);

		return viewModel;
	}
}
