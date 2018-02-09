package org.celllife.mobilisr.api.mock;

import java.util.Arrays;
import java.util.Date;

import org.celllife.mobilisr.api.rest.ContactDto;

class MockContactDtoFactory extends AbstractMockPopulator<ContactDto> {
	public MockContactDtoFactory() {
		super(ContactDto.class);
	}

	@Override
	protected void populate(int mode, int seed, ContactDto mock) {
		mock.setFirstName("firstName" + seed);
		mock.setLastName("lastName" + seed);
		mock.setMsisdn(MockUtils.createMsisdn(seed));
		mock.setContactMessageTimes(Arrays.asList(new Date(), new Date()));
	}
}