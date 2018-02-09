package org.celllife.mobilisr.client.impl;

import java.util.List;

import org.celllife.mobilisr.api.rest.ContactDto;
import org.celllife.mobilisr.api.rest.ErrorDto;
import org.celllife.mobilisr.api.rest.PagedListDto;
import org.celllife.mobilisr.api.validation.ValidationError;
import org.celllife.mobilisr.api.validation.ValidatorFactory;
import org.celllife.mobilisr.client.command.RestCommandFactory;
import org.celllife.mobilisr.client.exception.RestCommandException;
import org.celllife.mobilisr.constants.ErrorCode;

public class BaseRestService {
	private final RestCommandFactory factory;
	private final ValidatorFactory vfactory;
	
	public BaseRestService(RestCommandFactory factory, ValidatorFactory vfactory) {
		this.factory = factory;
		this.vfactory = vfactory;
	}
	
	protected RestCommandFactory getCommandFactory(){
		return factory;
	}
	
	protected String validateMsisdn(String msisdn) throws RestCommandException {
		ValidationError error = vfactory.validateMsisdn(msisdn);
		if (error != null){
			RestCommandException exception = new RestCommandException(error.getMessage());
			PagedListDto<ErrorDto> errors = new PagedListDto<ErrorDto>();
			errors.addElement(new ErrorDto(ErrorCode.INVALID_MSISDN, error.getMessage()));
			exception.setErrors(errors);
			throw exception;
		}
		return msisdn;
	}
	
	protected void validateContacts(List<ContactDto> contacts) throws RestCommandException {
		if (contacts == null || contacts.isEmpty()){
			throw new RestCommandException("Empty contact list");
		}
		
		for (ContactDto contact : contacts) {
			contact.setMsisdn(validateMsisdn(contact.getMsisdn()));
		}
		
	}

}
