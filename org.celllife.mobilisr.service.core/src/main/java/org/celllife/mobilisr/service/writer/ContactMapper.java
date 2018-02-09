package org.celllife.mobilisr.service.writer;

import org.celllife.mobilisr.domain.Contact;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;

public class ContactMapper implements FieldSetMapper<Contact> {

	private String fieldOrder;

	@Override
	public Contact mapFieldSet(FieldSet fieldSet) {
		Contact contact = new Contact();
		String[] fields = fieldOrder.split(",");
		String[] values = fieldSet.getValues();
		for (int i = 0; i < values.length; i++) {
			
			if (fields[i].equals(Contact.PROP_MSISDN)) {
				contact.setMsisdn(values[i]);
			}

			if (fields[i].equals(Contact.PROP_FIRST_NAME)) {
				contact.setFirstName(values[i]);
			}

			if (fields[i].equals(Contact.PROP_LAST_NAME)) {
				contact.setLastName(values[i]);
			}

			if (fields[i].equals(Contact.PROP_MOBILE_NETWORK)) {
				contact.setMobileNetwork(values[i]);
			}
		}

		return contact;
	}

	public String getFieldOrder() {
		return fieldOrder;
	}

	public void setFieldOrder(String fieldOrder) {
		this.fieldOrder = fieldOrder;
	}
}
