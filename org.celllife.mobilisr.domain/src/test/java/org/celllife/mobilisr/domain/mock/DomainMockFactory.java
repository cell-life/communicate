package org.celllife.mobilisr.domain.mock;

import org.celllife.mobilisr.api.mock.BaseMockFactory;

public class DomainMockFactory extends BaseMockFactory {

	public static final int MODE_LOAD = 0;

	private static DomainMockFactory instance;

	public static DomainMockFactory _() {
		if (instance == null) {
			instance = new DomainMockFactory();
		}
		
		instance.resetMode();
		return instance;
	}

	private DomainMockFactory() {
		registerFactories();
	}

	@Override
	protected void registerFactories() {
		register(new MockContactPopulator());
		register(new MockOrganizationPopulator());
		register(new MockUserPopulator());
		register(new MockCampaignPopulator());
		register(new MockCampaignContactPopulator());
		register(new MockCampaignMessagePopulator());
		register(new MockRolePopulator());
		register(new MockChannelPopulator());
		register(new MockContactGroupPopulator());
		register(new MockSmsLogPopulator());
	}
}
