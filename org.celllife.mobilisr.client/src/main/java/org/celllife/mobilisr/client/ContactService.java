package org.celllife.mobilisr.client;

import org.celllife.mobilisr.api.rest.ContactDto;
import org.celllife.mobilisr.client.exception.RestCommandException;

public interface ContactService {

	/**
	 * Update a contacts details on Mobilisr.
	 * 
	 * @param originalMsisdn
	 * @param contact with new MSISDN and other details
	 * 
	 * @throws RestCommandException
	 */
	public void updateContactDetails(String originalMsisdn, ContactDto contact) throws RestCommandException;

}
