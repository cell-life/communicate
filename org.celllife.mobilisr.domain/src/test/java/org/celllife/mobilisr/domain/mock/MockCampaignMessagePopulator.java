package org.celllife.mobilisr.domain.mock;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

import org.celllife.mobilisr.api.mock.AbstractMockPopulator;
import org.celllife.mobilisr.domain.CampaignMessage;

public class MockCampaignMessagePopulator extends AbstractMockPopulator<CampaignMessage> {

	private List<Date> times = new ArrayList<Date>();
	private int msgDay = 0;

	public MockCampaignMessagePopulator() {
		super(CampaignMessage.class);
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
			times.add(sdf.parse("07:00"));
			times.add(sdf.parse("12:00"));
			times.add(sdf.parse("19:00"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void populate(int mode, int seed, CampaignMessage mock) {
		mock.setId(new Long(new Random(seed).nextInt(100)));
		int msgSlot = ((seed-1) % 3) + 1;
		if (msgSlot == 1){
			msgDay++;
		}
		mock.setMessage("message" + seed);
		mock.setMsgDay(msgDay);
		mock.setMsgSlot(msgSlot);
		mock.setMsgTime(times.get(msgSlot-1));
		Calendar instance = Calendar.getInstance();
		instance.add(Calendar.DATE, (seed-1)/3);
		mock.setMsgDate(instance.getTime());
	}
	
	@Override
	public void reset() {
		super.reset();
		msgDay = 0;
	}

}
