package org.celllife.mobilisr.client;

import java.util.List;

import org.celllife.mobilisr.constants.CampaignType;
import org.celllife.mobilisr.domain.Campaign;
import org.celllife.mobilisr.domain.CampaignMessage;

public class MobilisrClientUtil {

	public static void recalculateCostAndDuration(Campaign campaign) {
		if (campaign != null) {
			int totalCost = 0;

			if (campaign.getWelcomeMsg() != null) {
				totalCost += calculateNumberSMSs(campaign.getWelcomeMsg());
			}
			int maxMessageDay = 0; // duration
			List<CampaignMessage> campaignMessages = campaign
					.getCampaignMessages();
			if (campaignMessages != null) {
				for (CampaignMessage campaignMessage : campaignMessages) {
					int numMessages = calculateNumberSMSs(campaignMessage
							.getMessage());
					totalCost += numMessages;
					if (campaignMessage.getMsgDay() > maxMessageDay)
						maxMessageDay = campaignMessage.getMsgDay();
				}
			}
			if (campaign.getType() == CampaignType.FLEXI) {
				campaign.setDuration(maxMessageDay);
			}
			campaign.setCost(totalCost);
		}
	}

	public static int calculateNumberSMSs(String message) {
		int length = message != null ? message.length() : 0;
		return calcualteNumberSMSs(length);
	}

	public static int calcualteNumberSMSs(int length) {
		int max_length = 160;
		if (length > 160)
			max_length = 153;
		int numMesgs = length == 0 ? 0 : length / (max_length + 1) + 1;
		return numMesgs;
	}
}
