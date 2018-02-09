package org.celllife.mobilisr.client.validator;

import java.util.List;

import org.celllife.mobilisr.api.validation.MsisdnRule;

import com.extjs.gxt.ui.client.widget.form.Field;

public class MsisdnListFieldValidator extends MsisdnFieldValidator {
	
	private String separator;
	
	public MsisdnListFieldValidator(List<MsisdnRule> numberInfoList) {
		super(numberInfoList);
		separator = ",";
	}
	
	public MsisdnListFieldValidator(List<MsisdnRule> numberInfoList, String separator) {
		super(numberInfoList);
		this.separator = separator;
	}

	@Override
	public String validate(Field<?> field, String value) {
		String[] msisdnArr = value.split(separator);
		
		for (String msisdn : msisdnArr) {
			msisdn = msisdn.trim();
			String validate = super.validate(field, msisdn);
			if (validate != null){
				return "Validation failed for '" + msisdn + "': " + validate;
			}
		}
		return null;
	}
}
