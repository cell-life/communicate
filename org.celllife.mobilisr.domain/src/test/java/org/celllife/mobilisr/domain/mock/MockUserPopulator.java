package org.celllife.mobilisr.domain.mock;

import java.util.Random;

import org.celllife.mobilisr.api.mock.AbstractMockPopulator;
import org.celllife.mobilisr.api.mock.MockUtils;
import org.celllife.mobilisr.domain.Organization;
import org.celllife.mobilisr.domain.User;

public class MockUserPopulator extends AbstractMockPopulator<User> {

	public MockUserPopulator() {
		super(User.class);
	}

	@Override
	protected void populate(int mode, int seed, User mock) {
		if(mode == DomainMockFactory.MODE_LOAD){
			mock.setId(new Long(new Random(seed).nextInt(100)));
		}
		mock.setFirstName("firstName" + seed);
		mock.setLastName("lastName" + seed);
		mock.setEmailAddress("email" + seed + "@cell-life.org");
		mock.setUserName("userName" + seed);
		mock.setPassword("password" + seed);
		mock.setSalt("salt" + seed);
		mock.setOrganization(DomainMockFactory._().on(Organization.class).withMode(mode).create());
		mock.setMsisdn(MockUtils.createMsisdn(seed));
	}

}
