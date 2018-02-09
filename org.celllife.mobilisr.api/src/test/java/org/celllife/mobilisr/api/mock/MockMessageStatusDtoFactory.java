package org.celllife.mobilisr.api.mock;

import java.util.Date;

import org.celllife.mobilisr.api.rest.MessageStatusDto;
import org.celllife.mobilisr.constants.SmsStatus;

class MockMessageStatusDtoFactory extends AbstractMockPopulator<MessageStatusDto> {
	public MockMessageStatusDtoFactory() {
		super(MessageStatusDto.class);
	}

	@Override
	protected void populate(int mode, int seed, MessageStatusDto mock) {
		mock.setMsisdn("msisdn" + seed);
		mock.setStatus(SmsStatus.TX_SUCCESS);
		mock.setId(Long.valueOf(seed));
		mock.setDatetime(new Date());
		mock.setFailreason("failreason" + seed);
	}
}