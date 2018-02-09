package org.celllife.mobilisr.service.utility;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.celllife.mobilisr.domain.Campaign;
import org.celllife.mobilisr.domain.CampaignMessage;
import org.celllife.mobilisr.util.MobilisrDomainUtility;
import org.celllife.pconfig.model.ScheduledPconfig;
import org.gwttime.time.DateTime;

public class MobilisrUtility extends MobilisrDomainUtility {
	
	public enum Day {
		MON, TUE, WED, THU, FRI, SAT, SUN;
	}

	private static final String DAILY_CRONEXPR = "{0} {1} {2} ? * *";

	public static String obtainMobileNetwork(int mobileNetwork) {
		String network = null;
		switch (mobileNetwork) {
		case 1:
			network = "Vodacom";
			break;
		case 2:
			network = "MTN";
			break;
		case 3:
			network = "Cell C";
			break;
		default:
			network = "Unknown";
			break;
		}

		return network;
	}

	public static int calculateMessageCost(String message,
			int totalNumOfContacts) {
		int totalNumOfSmsMsg = calculateNumberOfMessages(message.length());
		int tempReserveAmnt = totalNumOfContacts * totalNumOfSmsMsg;

		return tempReserveAmnt;
	}

	public static int countTotalNumberOfMessages(
			List<CampaignMessage> campaignMessages, Campaign campaign) {
		int totalNumOfMsgs = 0;
		for (CampaignMessage campaignMessage : campaignMessages) {
			int numOfMsg = calculateNumberOfMessages(campaignMessage
					.getMessage().length());
			totalNumOfMsgs += numOfMsg;
		}

		if (campaign.getWelcomeMsg() != null) {
			totalNumOfMsgs += calculateNumberOfMessages(campaign
					.getWelcomeMsg().length());
		}
		return totalNumOfMsgs;
	}

	public static int calculateNumberOfMessages(int msgLength) {
		if (msgLength == 0) {
			return 0;
		}

		int totalNumOfSmsMsg = (int) (msgLength <= Campaign.MAX_SMS_LENGTH ? 1
				: Math.ceil(msgLength / Campaign.SMS_LENGTH_FOR_MULTI_PART));
		return totalNumOfSmsMsg;
	}

	public static int calculateNumberOfMessages(String sms) {
		int length = sms != null ? sms.length() : 0;
		if (length == 0) {
			return 0;
		}
		return calculateNumberOfMessages(length);
	}

	public static void recalculateCostAndDuration(Campaign campaign,
			List<CampaignMessage> messages) {
		if (campaign != null) {
			 int totalCost = calculateNumberOfMessages(campaign.getWelcomeMsg());
			
			int duration = 0; // duration
			if (messages != null) {
				for (CampaignMessage campaignMessage : messages) {
					int numMessages = calculateNumberOfMessages(campaignMessage
							.getMsgLength().intValue());
					totalCost += numMessages;
					if (campaignMessage.getMsgDay() > duration)
						duration = campaignMessage.getMsgDay();
				}
			}
			campaign.setDuration(duration);
			campaign.setCost(totalCost);
			// 1 message = 1 credit
			campaign.setMessageCount(totalCost);
		}
	}

	public static String cronExprForDailyOccurence(Date msgDateTime) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(msgDateTime);

		String cronExpr = MessageFormat.format(DAILY_CRONEXPR, new Object[] {
				calendar.get(Calendar.SECOND), calendar.get(Calendar.MINUTE),
				calendar.get(Calendar.HOUR_OF_DAY) });

		return cronExpr;
	}
	
	/**
	 * Generate cron expressions for scheduled reports. By default reports are run
	 * at 23:30 every day. 
	 * 
	 * Running reports at the end of the day allows reports like montly reports to 
	 * be run for the full month by assuming the start and end dates run 
	 * from the first of the month to the end of the month.
	 * 
	 * @param report
	 * @return cron expression as a string
	 */
	public static String getCronExpression(ScheduledPconfig report) {
		String cronExpr = "";
		int intervalCount = report.getIntervalCount();
		
		switch (report.getRepeatInterval()) {
		case Daily:
			String days = "*";
			if (intervalCount > 1)
				days += "/" + intervalCount;
			
			cronExpr = "0 30 23 " + days + " * ?"; 
			break;
		case Weekly:
			String dayOfWeek = "1"; // SUNDAY
			if (intervalCount > 1)
				dayOfWeek += "/" + intervalCount;
			
			cronExpr = "0 30 23 ? * " + dayOfWeek;
			break;
		case Monthly:
			int dayOfMonth = new DateTime(report.getStartDate()).getDayOfMonth();
			
			// check if dayOfMonth is the last day of the month
			Calendar instance = Calendar.getInstance();
			instance.setTime(report.getStartDate());
			int maxDayOfMonth = instance.getActualMaximum(Calendar.MONTH);
			String dayOfMonthVal = Integer.toString(dayOfMonth);
			if (dayOfMonth == maxDayOfMonth)
				dayOfMonthVal = "L";
			
			String month = "*";
			if (intervalCount > 1)
				month += "/" + intervalCount;
			
			cronExpr = "0 30 23 " + dayOfMonthVal + " " + month + " ?";
			break;
		default:
			cronExpr = "0 30 23 * * ?";
		}
		return cronExpr;
	}	

	public static String findValueForRegExp(String body, String regExp) {
		List<String> list = findValuesForRegExp(body, regExp);
		if (list.isEmpty()){
			return null;
		}
		return list.get(0);
	}

	public static List<String> findValuesForRegExp(String body, String regExp) {
		List<String> data = new ArrayList<String>();
		try {
			boolean found = false;
			Matcher matcher = Pattern.compile(regExp).matcher(body);
			do {
				found = matcher.find();
				if (found) {
					data.add(matcher.group(1));
				}
			} while (found);
		} catch (Exception e) {
		}

		return data;
	}

	public static String getHostname() {
		try {
			InetAddress address = InetAddress.getLocalHost();
			String hostname = address.getHostName();
			return hostname;
		} catch (UnknownHostException e) {
			return "unknown host";
		}
	}
}
