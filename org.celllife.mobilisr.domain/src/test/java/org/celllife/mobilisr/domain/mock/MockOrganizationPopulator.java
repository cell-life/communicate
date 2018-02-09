package org.celllife.mobilisr.domain.mock;

import java.util.Random;

import org.celllife.mobilisr.api.mock.AbstractMockPopulator;
import org.celllife.mobilisr.domain.Organization;

public class MockOrganizationPopulator extends AbstractMockPopulator<Organization> {

	public MockOrganizationPopulator() {
		super(Organization.class);
	}

	@Override
	protected void populate(int mode, int seed, Organization mock) {
		if(mode == DomainMockFactory.MODE_LOAD){
			mock.setId(new Long(new Random(seed).nextInt(100)));
		}
		mock.setBalance(100*seed);
		mock.setName("name" + seed);
		mock.setContactEmail("orgcontact" + seed + "@example.com");
	}

}
