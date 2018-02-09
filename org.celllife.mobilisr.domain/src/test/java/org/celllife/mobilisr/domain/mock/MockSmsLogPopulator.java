package org.celllife.mobilisr.domain.mock;

import java.util.Date;
import java.util.Random;

import org.celllife.mobilisr.api.mock.AbstractMockPopulator;
import org.celllife.mobilisr.api.mock.MockUtils;
import org.celllife.mobilisr.constants.SmsStatus;
import org.celllife.mobilisr.domain.SmsLog;


public class MockSmsLogPopulator extends AbstractMockPopulator<SmsLog> {

	public MockSmsLogPopulator() {
		super(SmsLog.class);
	}

	@Override
	protected void populate(int mode, int seed, SmsLog mock) {
		if(mode == DomainMockFactory.MODE_LOAD){
			mock.setId(new Long(new Random(seed).nextInt(100)));
		}
		mock.setMsisdn(MockUtils.createMsisdn(seed));
		mock.setDir(SmsLog.SMS_DIR_IN);
		mock.setStatus(SmsStatus.RX_FILTER_FAIL);
		mock.setCreatedfor("mock");
		mock.setDatetime(new Date());
		mock.setMessage("mock message");
		mock.setVoided(false);
	}

}
