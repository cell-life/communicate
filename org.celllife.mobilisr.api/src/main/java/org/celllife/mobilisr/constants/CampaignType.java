package org.celllife.mobilisr.constants;

import java.util.HashMap;
import java.util.Map;

public enum CampaignType {
	
	// NOTE: changes to these constants affect the REST API. Make sure you append the old version to the list of previous versions.

	/**
	 * Fixed campaigns are those where each message has a specific date that it gets sent on.
	 */
	FIXED, 
	
	/**
	 * Daily campaigns have X messages per day for the duration of the campaign. Each contact
	 * on the campaign starts at day 0 and each day will get that days messages until they
	 * reach the end of the campaign.
	 * 
	 * Having the messages at fixed times every day allows the times to be customised for each
	 * contact on the campaign.
	 * 
	 * Daily and Flexi campaigns can also be referred to as a 'relative' campaign since the messages are
	 * sent relative to the contacts progress.
	 */
	DAILY(new Version(ApiVersion.v1,"RELATIVE")), 
	
	/**
	 * Flexi campaigns are similar to Daily campaigns in that the messages that are sent out
	 * depend on the contacts progress through the campaign. The difference is that the messages
	 * can be on any day and any time i.e. there aren't a fixed number of messages per day.
	 * 
	 * Flexi campaigns do not allow custom message times for the contacts.
	 * 
	 * Daily and Flexi campaigns can also be referred to as a 'relative' campaign since the messages are
	 * sent relative to the contacts progress.
	 */
	FLEXI(new Version(ApiVersion.v1,"GENERIC"));
	
	private Version[] previousVersions;
	private static Map<String, CampaignType> apiVersionMap;
	
	private CampaignType(Version... previousVersions) {
		this.previousVersions = previousVersions;
	}
	
	private static void initApiVersionMap() {
		if (apiVersionMap == null){
			apiVersionMap = new HashMap<String, CampaignType>();
			for (CampaignType type : values()) {
				if (type.previousVersions == null)
					continue;
				
				for (Version oldType : type.previousVersions) {
					apiVersionMap.put(oldType.getValue(), type);
				}
			}
		}
	}
	
	public String apiName(ApiVersion version){
		for (Version v : previousVersions) {
			if (v.getVersion().equals(version)){
				return v.getValue();
			}
		}
		return name();
	}

	/**
	 * This method is used to convert CampaignType values received via the REST
	 * API. It allows the use of old type names.
	 * 
	 * @param value
	 * @return CampaignType
	 * @throws IllegalArgumentException if value does not match any type
	 */
	public static CampaignType apiValueOf(String value) {
		CampaignType type = null;
		try {
			type = CampaignType.valueOf(value);
		} catch (Exception ignore) {
			initApiVersionMap();
			type = apiVersionMap.get(value.toUpperCase());
			if (type == null){
				throw new IllegalArgumentException("Unknown CampaignType: " + value);
			}
		}
		return type;
	}
	
	/**
	 * Same as {@link #valueOf(String)} but will return null instead
	 * of throw IllegalArgumentException if value is not valid.
	 * 
	 * @param value
	 * @return {@link CampaignStatus} or null
	 */
	public static CampaignType safeValueOf(String value) {
		CampaignType type = null;
		try {
			type = CampaignType.valueOf(value);
		} catch (Exception ignore) {
			// ignore exception
		}
		return type;
	}

}
