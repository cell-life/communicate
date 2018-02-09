package org.celllife.mobilisr.api.validation;

import java.util.List;

public interface ValidatorFactory {

	public MsisdnValidator getMsisdnValidator();

	public ValidationError validateMsisdn(String msisdn);

    public DateValidator getDateValidator();

}