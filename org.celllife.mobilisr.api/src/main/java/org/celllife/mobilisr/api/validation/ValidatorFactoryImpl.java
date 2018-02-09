package org.celllife.mobilisr.api.validation;

import org.celllife.mobilisr.constants.ErrorCode;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ValidatorFactoryImpl implements ValidatorFactory {

    private List<? extends MsisdnRule> countryRules;

    private MsisdnValidator msisdnValidator;

    private DateValidator dateValidator;

    public ValidatorFactoryImpl() {
        msisdnValidator = new MsisdnValidator(null);
        dateValidator = new DateValidator();
    }

    public ValidatorFactoryImpl(List<? extends MsisdnRule> countryRules) {
        msisdnValidator = new MsisdnValidator(countryRules);
        dateValidator = new DateValidator();
    }

    @Override
    public MsisdnValidator getMsisdnValidator() {
        return msisdnValidator;
    }

	@Override
	public ValidationError validateMsisdn(String msisdn) {
		return msisdnValidator.validate(msisdn);
    }

	public void setCountryRules(List<? extends MsisdnRule> countryRules) {
		this.countryRules = countryRules;
        msisdnValidator.setRules(countryRules);
	}

    @Override
    public DateValidator getDateValidator() {
        return dateValidator;
    }

    public void setDateValidator(DateValidator dateValidator) {
        this.dateValidator = dateValidator;
    }
}
