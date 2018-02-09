package org.celllife.mobilisr.domain.mock;

import java.util.Random;

import org.celllife.mobilisr.api.mock.AbstractMockPopulator;
import org.celllife.mobilisr.constants.ChannelType;
import org.celllife.mobilisr.domain.Channel;

public class MockChannelPopulator extends AbstractMockPopulator<Channel> {

	public MockChannelPopulator() {
		super(Channel.class);
	}

	@Override
	protected void populate(int mode, int seed, Channel mock) {
		if(mode == DomainMockFactory.MODE_LOAD){
			mock.setId(new Long(new Random(seed).nextInt(100)));
		}
		mock.setName("mock channel " + seed);
		mock.setHandler("mockOutChannel");
		mock.setShortCode("shortcode"+seed);
		mock.setType(ChannelType.OUT);
	}

}
