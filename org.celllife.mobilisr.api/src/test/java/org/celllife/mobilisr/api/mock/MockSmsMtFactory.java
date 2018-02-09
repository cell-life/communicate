package org.celllife.mobilisr.api.mock;

import org.celllife.mobilisr.api.messaging.SmsMt;
import org.celllife.mobilisr.constants.SmsStatus;

public class MockSmsMtFactory extends AbstractMockPopulator<SmsMt> {

	public MockSmsMtFactory() {
		super(SmsMt.class);
	}

	@Override
	protected void populate(int mode, int seed, SmsMt mock) {
		mock.setChannelId(Long.valueOf(seed));
		mock.setCreatedFor("createdFor" + seed);
		mock.setUserId(Long.valueOf(seed));
		mock.setOrganizationId(Long.valueOf(seed));
		mock.setContactId(Long.valueOf(seed));
		mock.setMessage("message" + seed);
		mock.setMsisdn(MockUtils.createMsisdn(seed));
		mock.setChannelName("mockOutChannel");
		mock.setProcessCampaignCompletion(false);

		if (mode == DtoMockFactory.MODE_POST){
			mock.setErrorMessage("error message" + seed);
			mock.setMessageTrackingNumber("messageTrackingNumber" + seed);
			mock.setSendingAttempts(seed);
			mock.setStatus(SmsStatus.WASP_SUCCESS);
		}
	}

}
