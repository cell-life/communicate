package org.celllife.mobilisr.api.mock;

import java.util.Date;
import java.util.Random;

import org.celllife.mobilisr.api.rest.CampaignDto;
import org.celllife.mobilisr.api.rest.ContactDto;
import org.celllife.mobilisr.api.rest.MessageDto;
import org.celllife.mobilisr.constants.CampaignStatus;
import org.celllife.mobilisr.constants.CampaignType;

public class MockCampaignDtoFactory extends AbstractMockPopulator<CampaignDto> {

	private static final int RAND_LIMIT = 100;

	public MockCampaignDtoFactory() {
		super(CampaignDto.class);
	}

	@Override
	protected void populate(int mode, int seed, CampaignDto mock) {
		if (mode == DtoMockFactory.MODE_GET) {
			Random random = new Random(seed);
			mock.setId(new Long(random.nextInt(RAND_LIMIT)));
			
			CampaignStatus[] status = CampaignStatus.values();
			mock.setStatus(status[seed % status.length].name());
			
			CampaignType[] types = CampaignType.values();
			mock.setType(types[seed % (types.length)].name());
			
			switch (CampaignType.valueOf(mock.getType())) {
			case FIXED:
				mock.setCost(random.nextInt(RAND_LIMIT));
				break;
			case DAILY:
				mock.setDuration(random.nextInt(RAND_LIMIT));
				mock.setTimesPerDay(random.nextInt(RAND_LIMIT));
				mock.setCost(mock.getDuration() * mock.getTimesPerDay());
				break;
			case FLEXI:
				mock.setDuration(random.nextInt(RAND_LIMIT));
				mock.setCost(random.nextInt(RAND_LIMIT));
				break;
			default:
				throw new IllegalStateException("unknown campaign type");
			}
		} else if (mode == DtoMockFactory.MODE_POST) {
			mock.setType(CampaignType.FIXED.name());
			mock.addMessage(DtoMockFactory._().on(MessageDto.class).create());
			mock.setContacts(DtoMockFactory._().on(ContactDto.class)
					.withMode(DtoMockFactory.MODE_POST).create(10));
		}

		mock.setName("name " + seed);
		mock.setDescription("description " + seed);
		mock.setStartDate(new Date());
	}

}
