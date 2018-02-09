package org.celllife.mobilisr.api.validation;

import java.util.Arrays;
import java.util.List;

import org.celllife.mobilisr.api.validation.ValidationError;
import org.celllife.mobilisr.constants.ErrorCode;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class MsisdnValidatorTests {

	private static List<MsisdnRule> infoList;

	@BeforeClass
	public static void setup(){
		infoList = getNumberInfoList();
	}
	
	@Test
	public void testMsisdnValidator() {
		ValidationError validateSA = new MsisdnValidator(infoList).validate("27722547859");
		Assert.assertNull(validateSA);
		
		ValidationError validateZW = new MsisdnValidator(infoList).validate("263772253154");
		Assert.assertNull(validateZW);
		
		ValidationError validateNG = new MsisdnValidator(infoList).validate("2348045679856");
		Assert.assertNull(validateNG);
	}
	
	@Test
	public void testMsisdnValidator_unknownPrefix() {
		ValidationError validate = new MsisdnValidator(infoList).validate("26722547859");
		Assert.assertEquals(ErrorCode.INVALID_MSISDN, validate.getCode());
	}
	
	@Test
	public void testMsisdnValidator_incorrectFormat() {
		ValidationError validate = new MsisdnValidator(infoList).validate("2772254785");
		Assert.assertEquals(ErrorCode.INVALID_MSISDN, validate.getCode());
		Assert.assertEquals("Phone Number format not valid for SA",validate.getMessage());
	}
	
	private static List<MsisdnRule> getNumberInfoList() {
		return Arrays.asList((MsisdnRule)new MsisdnRule("ZW", "263", "^263[1-9][0-9]{8}$"),
				(MsisdnRule)new MsisdnRule("SA", "27", "^27[1-9][0-9]{8}$"),
				(MsisdnRule)new MsisdnRule("NG", "234", "^234[1-9][0-9]{9}$"));
	}
}
