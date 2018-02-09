package org.celllife.mobilisr.client.validator;

import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.form.Validator;

public class PasswordValidator implements Validator {
	
	private final TextField<String> newPwdText;

	public PasswordValidator(TextField<String> newPwdText) {
		this.newPwdText = newPwdText;
	}

	@Override
	public String validate(Field<?> field, String value) {
		String newPwd = newPwdText.getValue();
		String allOkay = null;
		if(newPwd != null){
			if(!newPwd.equals(value)){
				allOkay = "Passwords do not match";
			}
		}
		return allOkay;
	}

}
