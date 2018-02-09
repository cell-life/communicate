package org.celllife.mobilisr.converter;

import org.celllife.mobilisr.api.rest.ContactDto;
import org.celllife.mobilisr.constants.ApiVersion;
import org.celllife.mobilisr.domain.Contact;

public class ContactDtoConverter implements EntityDtoConverter<Contact, ContactDto> {

	@Override
	public Class<ContactDto> getDtoType() {
		return ContactDto.class;
	}

	@Override
	public Class<Contact> getEntityType() {
		return Contact.class;
	}
	
	@Override
	public ContactDto toDto(Contact contact, ApiVersion ver) {
		ContactDto dto = new ContactDto();
		dto.setMsisdn(contact.getMsisdn());
		dto.setFirstName(contact.getFirstName());
		dto.setLastName(contact.getLastName());
		return dto;
	}
	
	@Override
	public Contact fromDto(ContactDto dto, ApiVersion ver) {
		Contact contact = new Contact();
		contact.setFirstName(dto.getFirstName());
		contact.setLastName(dto.getLastName());
		contact.setMsisdn(dto.getMsisdn());
		return contact;
	}

}
