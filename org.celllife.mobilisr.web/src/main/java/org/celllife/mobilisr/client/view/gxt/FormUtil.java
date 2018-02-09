package org.celllife.mobilisr.client.view.gxt;

import com.extjs.gxt.ui.client.binding.FieldBinding;
import com.extjs.gxt.ui.client.binding.FormBinding;
import com.extjs.gxt.ui.client.binding.SimpleComboBoxFieldBinding;
import com.extjs.gxt.ui.client.binding.TimeFieldBinding;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;
import com.extjs.gxt.ui.client.widget.form.TimeField;

public class FormUtil {
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static FormBinding createFormBinding(FormPanel formPanel,
			boolean autobind) {
		FormBinding formBindings = new FormBinding(formPanel, false);
		if (autobind) {
			for (Field<?> f : formPanel.getFields()) {
				if (formBindings.getBinding(f) == null) {
					String name = f.getName();
					if (name != null && name.length() > 0) {
						FieldBinding b;
						if (f instanceof TimeField) {
							b = new TimeFieldBinding((TimeField) f, name);
						} else if (f instanceof SimpleComboBox) {
							b = new SimpleComboBoxFieldBinding(
									(SimpleComboBox) f, name);
						} else if (f instanceof ComboBox) {
							b = new ComboBoxFieldBinding((ComboBox) f, name);
						} else {
							b = new FieldBinding(f, name);
						}
						b.setUpdateOriginalValue(true);
						formBindings.addFieldBinding(b);
					}
				}
			}
		}

		return formBindings;
	}
}
