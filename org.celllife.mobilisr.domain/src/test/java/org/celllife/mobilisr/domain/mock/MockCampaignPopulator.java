package org.celllife.mobilisr.domain.mock;

import java.util.Date;
import java.util.Random;

import org.celllife.mobilisr.api.mock.AbstractMockPopulator;
import org.celllife.mobilisr.constants.CampaignStatus;
import org.celllife.mobilisr.constants.CampaignType;
import org.celllife.mobilisr.domain.Campaign;
import org.celllife.mobilisr.domain.Organization;

public class MockCampaignPopulator extends AbstractMockPopulator<Campaign> {

	public MockCampaignPopulator() {
		super(Campaign.class);
	}

	@Override
	protected void populate(int mode, int seed, Campaign mock) {
		if(mode == DomainMockFactory.MODE_LOAD){
			mock.setId(new Long(new Random(seed).nextInt(100)));
		}
		mock.setName("name" + seed);
		mock.setCost(100*seed);
		mock.setOrganization(DomainMockFactory._().on(Organization.class).withMode(mode).create());
		mock.setStatus(CampaignStatus.INACTIVE);
		mock.setType(CampaignType.FIXED);
		mock.setDuration(seed);
		mock.setCost(seed);
		mock.setWelcomeMsg("Welcome " + seed);
		mock.setTimesPerDay(seed);
		mock.setStartDate(new Date());
	}

}
