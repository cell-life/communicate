package org.celllife.mobilisr.client.impl;

import org.celllife.mobilisr.api.rest.ContactDto;
import org.celllife.mobilisr.api.validation.ValidatorFactory;
import org.celllife.mobilisr.client.ContactService;
import org.celllife.mobilisr.client.command.PutCommand;
import org.celllife.mobilisr.client.command.RestCommandFactory;
import org.celllife.mobilisr.client.exception.RestCommandException;


public class ContactServiceImpl implements ContactService {

	private final RestCommandFactory factory;

	public ContactServiceImpl(RestCommandFactory factory, ValidatorFactory vfactory) {
		this.factory = factory;
	}

	@Override
	public void updateContactDetails(String originalMsisdn, ContactDto contact)
			throws RestCommandException {
		PutCommand command = factory.getPutCommand("contacts/{msidsn}", contact, originalMsisdn);
		command.execute(Void.class);
		
		/*String location = command.getHeader("Location");
		if (location == null){
			throw new RestCommandException("No location header specified in response");
		}*/
	}

}
