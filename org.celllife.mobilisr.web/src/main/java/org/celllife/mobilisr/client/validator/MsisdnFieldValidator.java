package org.celllife.mobilisr.client.validator;

import java.util.List;

import org.celllife.mobilisr.api.validation.MsisdnRule;
import org.celllife.mobilisr.api.validation.MsisdnValidator;
import org.celllife.mobilisr.api.validation.ValidationError;

import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.Validator;

public class MsisdnFieldValidator extends MsisdnValidator implements Validator {
	
	public MsisdnFieldValidator(List<MsisdnRule> numberInfoList) {
		super(numberInfoList);
	}

	@Override
	public String validate(Field<?> field, String value) {
		ValidationError validate = super.validate(value);
		if (validate == null){
			return null;
		}
		
		return validate.getMessage();
	}
}
