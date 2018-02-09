package org.celllife.mobilisr.api.mock;

import org.celllife.mobilisr.api.messaging.SmsMo;

public class MockSmsMoFactory extends AbstractMockPopulator<SmsMo> {

	public MockSmsMoFactory() {
		super(SmsMo.class);
	}

	@Override
	protected void populate(int mode, int seed, SmsMo mock) {
		mock.setMessage("message" + seed);
		mock.setSourceAddr(MockUtils.createMsisdn(seed));
		mock.setMobileNetwork("mobileNetwork");
	}

}
