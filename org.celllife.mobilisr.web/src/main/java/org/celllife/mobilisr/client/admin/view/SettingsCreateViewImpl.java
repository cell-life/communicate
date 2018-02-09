package org.celllife.mobilisr.client.admin.view;

import java.util.LinkedHashMap;
import java.util.Map;

import org.celllife.mobilisr.client.Constants;
import org.celllife.mobilisr.client.Messages;
import org.celllife.mobilisr.client.model.ViewModel;
import org.celllife.mobilisr.client.reporting.PconfigParamterFieldFactory;
import org.celllife.mobilisr.client.template.view.EntityCreateTemplateImpl;
import org.celllife.mobilisr.client.view.gxt.MyGXTLabelField;
import org.celllife.mobilisr.domain.Setting;
import org.celllife.mobilisr.service.gwt.SettingViewModel;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.LabelField;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;

public class SettingsCreateViewImpl extends EntityCreateTemplateImpl<SettingViewModel> implements
		SettingsCreateView {

	private LabelField settingName;
	private Field<?> settingField;
	private SettingViewModel setting;

	@Override
	public void createView() {
		super.createView();

		settingName = new MyGXTLabelField("", "Name:");
		settingName.setName(Setting.PROP_NAME);
		
		Map<Button, Boolean> formButtons = new LinkedHashMap<Button, Boolean>();
		formButtons.put(submitButton, true);
		formButtons.put(cancelButton, false);

		layoutCreateTemplate(Messages.INSTANCE.settingCreateHeader(""));
		
		cancelButton.setText("Clear");

		displayForm();
		
		addAndConfigFormButtons(formButtons, true);
		createFormBinding(formPanel, true);
	}
	
	@Override
	public void layoutCreateTemplate(String titleLabelText) {

		setIntStyleAttribute("margin", 10);
		setLayout(new RowLayout(Orientation.VERTICAL));
		
		titleLabel.setText(titleLabelText);
		titleLabel.setStyleName(Constants.INSTANCE.styleFont14());
				
		formPanel.setPadding(5);
		formPanel.setBorders(true);
		formPanel.setButtonAlign(HorizontalAlignment.CENTER);
		formPanel.setScrollMode(Scroll.AUTOY);
		
		FormLayout formLayout = new FormLayout();
		formLayout.setLabelSeparator("");
		formElementContainer.setLayout(formLayout);
		
		formPanel.add(formElementContainer);
		
		add(titleLabel, new RowData(1,-1, new Margins(10)));
		add(msgLabel, new RowData(1,-1, new Margins(10)));
		add(formPanel, new RowData(1,1,new Margins(10)));
	}
	

	public void displayForm() {
		final FormData formData = new FormData("100");
		formElementContainer.add(settingName, formData);
	}

	@Override
	public void clearFormValues() {
		getHeaderLabel().setText(Messages.INSTANCE.settingCreateHeader(""));
		setErrorMessage(null);
		settingName.clear();
		clearValue();
		submitButton.setText(Messages.INSTANCE.save());
	}

	private void clearValue() {
		if (settingField != null){
			settingField.clear();
			formElementContainer.remove(settingField);
			settingField = null;
		}
	}

	@Override
	public void setFormObject(ViewModel<SettingViewModel> viewEntityModel) {
		setting = viewEntityModel.getModelObject();
		titleLabel.setText(Messages.INSTANCE.settingCreateHeader(setting.getName()));
		submitButton.setText(Messages.INSTANCE.update());
		
		createSettingWidget();
		super.setFormObject(viewEntityModel);
	}
	
	private void createSettingWidget() {
		clearValue();
		
		settingField = PconfigParamterFieldFactory.getField(setting.getConfig(), null, false);
		
		formElementContainer.add(settingField, new FormData("80%"));
		formElementContainer.layout(true);
	}

}
