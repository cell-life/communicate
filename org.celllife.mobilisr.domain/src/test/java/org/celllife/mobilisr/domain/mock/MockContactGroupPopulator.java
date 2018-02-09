package org.celllife.mobilisr.domain.mock;

import java.util.Random;

import org.celllife.mobilisr.api.mock.AbstractMockPopulator;
import org.celllife.mobilisr.domain.ContactGroup;
import org.celllife.mobilisr.domain.Organization;

public class MockContactGroupPopulator extends AbstractMockPopulator<ContactGroup> {

	public MockContactGroupPopulator() {
		super(ContactGroup.class);
	}

	@Override
	protected void populate(int mode, int seed, ContactGroup mock) {
		if(mode == DomainMockFactory.MODE_LOAD){
			mock.setId(new Long(new Random(seed).nextInt(100)));
		}
		mock.setGroupName("group"+seed);
		mock.setOrganization(DomainMockFactory._().on(Organization.class).create());
	}

}
