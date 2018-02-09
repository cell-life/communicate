package org.celllife.mobilisr.client.validator;

import java.util.Arrays;
import java.util.List;

import org.celllife.mobilisr.api.validation.MsisdnRule;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class MsisdnFieldValidatorTests {

	private static List<MsisdnRule> infoList;

	@BeforeClass
	public static void setup(){
		infoList = getNumberInfoList();
	}
	
	@Test
	public void testMsisdnValidator() {
		String validateSA = new MsisdnFieldValidator(infoList).validate(null, "27722547859");
		Assert.assertNull(validateSA);
		
		String validateZW = new MsisdnFieldValidator(infoList).validate(null, "263772253154");
		Assert.assertNull(validateZW);
		
		String validateNG = new MsisdnFieldValidator(infoList).validate(null, "2348045679856");
		Assert.assertNull(validateNG);
	}
	
	@Test
	public void testMsisdnValidator_unknownCountryCode() {
		String validate = new MsisdnFieldValidator(infoList).validate(null, "26722547859");
		Assert.assertTrue(validate.startsWith("Unknown number prefix"));
	}
	
	@Test
	public void testMsisdnValidator_incorrectFormat() {
		String validate = new MsisdnFieldValidator(infoList).validate(null, "2772254785");
		Assert.assertEquals("Phone Number format not valid for SA",validate);
	}
	
	@Test
	public void testMsisdnListValidator_single(){
		String validate = new MsisdnFieldValidator(infoList).validate(null, "2348045679856");
		Assert.assertNull(validate);
	}
	
	@Test
	public void testMsisdnListValidator_list(){
		String validate = new MsisdnListFieldValidator(infoList,",").validate(null, "2348045679856,263772253154,27722547859");
		Assert.assertNull(validate);
	}
	
	@Test
	public void testMsisdnListValidator_list_incorrect(){
		String msisdn = "26377225315";
		String validate = new MsisdnListFieldValidator(infoList,",").validate(null, "2348045679856,"+msisdn +",27722547859");
		Assert.assertTrue(validate.startsWith("Validation failed for '" + msisdn));
	}

	private static List<MsisdnRule> getNumberInfoList() {
		return Arrays.asList(new MsisdnRule("ZW", "263", "^263[1-9][0-9]{8}$"),
				new MsisdnRule("SA", "27", "^27[1-9][0-9]{8}$"),
				new MsisdnRule("NG", "234", "^234[1-9][0-9]{9}$"));
	}
}
