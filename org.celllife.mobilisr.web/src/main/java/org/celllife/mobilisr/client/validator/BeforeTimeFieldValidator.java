package org.celllife.mobilisr.client.validator;

import org.celllife.mobilisr.client.view.gxt.MyGXTTimeField;

import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.Time;
import com.extjs.gxt.ui.client.widget.form.Validator;

public class BeforeTimeFieldValidator implements Validator {

	private final MyGXTTimeField previousTimeField;

	public BeforeTimeFieldValidator(MyGXTTimeField newTime) {
		this.previousTimeField = newTime;
	}

	@Override
	public String validate(Field<?> field, String value) {
		Time selectedTime = (Time) field.getValue();
		int selectedTime_sec = selectedTime.getHour() * 60
				+ selectedTime.getMinutes();
		int previousTime_sec = previousTimeField.getValue().getHour() * 60
				+ previousTimeField.getValue().getMinutes();
		if (selectedTime_sec <= previousTime_sec) {
			return "Time must be after previous time.";
		}
		return null;
	}
}