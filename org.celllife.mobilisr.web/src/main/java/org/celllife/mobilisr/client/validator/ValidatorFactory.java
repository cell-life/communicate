package org.celllife.mobilisr.client.validator;

import java.util.List;

import org.celllife.mobilisr.api.validation.MsisdnRule;
import org.celllife.mobilisr.exception.MobilisrRuntimeException;

public class ValidatorFactory {

	private static List<MsisdnRule> numberInfoList;

	public static void setNumberInfoList(List<MsisdnRule> numberInfoList) {
		ValidatorFactory.numberInfoList = numberInfoList;
	}

	public static MsisdnFieldValidator getMsisdnValidator() {
		if (numberInfoList == null) {
			String msg = "Trying to get MsisdnFieldValidator but numberInfoList is null.";
			throw new MobilisrRuntimeException(msg);
		}
		return new MsisdnFieldValidator(numberInfoList);
	}

	public static MsisdnListFieldValidator getMsisdnListValidator(){
		if (numberInfoList == null) {
			String msg = "Trying to get MsisdnFieldValidator but numberInfoList is null.";
			throw new MobilisrRuntimeException(msg);
		}
		return new MsisdnListFieldValidator(numberInfoList);
	}

}
