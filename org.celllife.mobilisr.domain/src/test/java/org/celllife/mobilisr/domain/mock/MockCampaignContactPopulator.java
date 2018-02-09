package org.celllife.mobilisr.domain.mock;

import java.util.Random;

import org.celllife.mobilisr.api.mock.AbstractMockPopulator;
import org.celllife.mobilisr.api.mock.MockUtils;
import org.celllife.mobilisr.domain.CampaignContact;
import org.celllife.mobilisr.domain.Contact;

public class MockCampaignContactPopulator extends AbstractMockPopulator<CampaignContact> {

	public MockCampaignContactPopulator() {
		super(CampaignContact.class);
	}

	@Override
	protected void populate(int mode, int seed, CampaignContact mock) {
		if(mode == DomainMockFactory.MODE_LOAD){
			mock.setId(new Long(new Random(seed).nextInt(100)));
		}
		mock.setMsisdn(MockUtils.createMsisdn(seed));
		mock.setContact(DomainMockFactory._().on(Contact.class).create());
	}

}
