package org.celllife.mobilisr.domain.mock;

import java.util.Random;

import org.celllife.mobilisr.api.mock.AbstractMockPopulator;
import org.celllife.mobilisr.api.mock.MockUtils;
import org.celllife.mobilisr.domain.Contact;
import org.celllife.mobilisr.domain.Organization;

public class MockContactPopulator extends AbstractMockPopulator<Contact> {

	public MockContactPopulator() {
		super(Contact.class);
	}

	@Override
	protected void populate(int mode, int seed, Contact mock) {
		if(mode == DomainMockFactory.MODE_LOAD){
			mock.setId(new Long(new Random(seed).nextInt(100)));
		}
		mock.setFirstName("firstName" + seed);
		mock.setLastName("lastName" + seed);
		mock.setMsisdn(MockUtils.createMsisdn(seed));
		mock.setOrganization(DomainMockFactory._().on(Organization.class).create());
	}

}
