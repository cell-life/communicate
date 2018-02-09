package org.celllife.mobilisr.domain.mock;

import java.util.Random;

import org.celllife.mobilisr.api.mock.AbstractMockPopulator;
import org.celllife.mobilisr.domain.MobilisrPermission;
import org.celllife.mobilisr.domain.Role;

public class MockRolePopulator extends AbstractMockPopulator<Role> {

	public MockRolePopulator() {
		super(Role.class);
	}

	@Override
	protected void populate(int mode, int seed, Role mock) {
		if(mode == DomainMockFactory.MODE_LOAD){
			mock.setId(new Long(new Random(seed).nextInt(100)));
		}
		mock.setName("role " + seed);
		mock.setPermissions(MobilisrPermission.VIEW_ADMIN_CONSOLE.name());
	}

}
