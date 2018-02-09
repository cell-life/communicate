package org.celllife.mobilisr.api.mock;

import java.text.MessageFormat;

import org.celllife.mobilisr.api.rest.ErrorDto;
import org.celllife.mobilisr.constants.ErrorCode;

public class MockErrorDtoFactory extends AbstractMockPopulator<ErrorDto> {

	public MockErrorDtoFactory() {
		super(ErrorDto.class);
	}

	@Override
	protected void populate(int mode, int seed, ErrorDto mock) {
		mock.setErrorCode(ErrorCode.INVALID_MSISDN);
		mock.setMessage(MessageFormat.format("msisdn {0} is invalid",
				MockUtils.createMsisdn(seed)));
	}

}
