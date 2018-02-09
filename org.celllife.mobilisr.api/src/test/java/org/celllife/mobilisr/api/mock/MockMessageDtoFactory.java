package org.celllife.mobilisr.api.mock;

import java.util.Date;

import org.celllife.mobilisr.api.rest.MessageDto;

public class MockMessageDtoFactory extends AbstractMockPopulator<MessageDto> {

	public MockMessageDtoFactory() {
		super(MessageDto.class);
	}

	@Override
	protected void populate(int mode, int seed, MessageDto mock) {
		mock.setDate(new Date());
		mock.setTime(new Date());
		mock.setText("message text"+ seed);
	}

}
