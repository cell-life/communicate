package org.celllife.mobilisr.constants;

import java.util.HashMap;
import java.util.Map;

public enum CampaignStatus {
	
	// NOTE: changes to these constants affect the REST API. Make sure you append the old version to the list of previous versions.

	INACTIVE,
	/**
	 * only applies to fixed campaigns 
	 */
	SCHEDULED,
	/**
	 * only applies to relative and generic campaigns
	 */
	ACTIVE,
	/**
	 * only applies to fixed campaigns
	 */
	RUNNING,
	/**
	 * only applies to relative and generic campaigns
	 */
	STOPPING,
	FINISHED,
	SCHEDULE_ERROR;
	
	private Version[] previousVersions;
	private static Map<String, CampaignStatus> apiVersionMap;
	
	private CampaignStatus(Version... previousVersions) {
		this.previousVersions = previousVersions;
	}
	
	private static void initApiVersionMap() {
		if (apiVersionMap == null){
			apiVersionMap = new HashMap<String, CampaignStatus>();
			for (CampaignStatus status : values()) {
				if (status.previousVersions == null)
					continue;
				
				for (Version oldStatus : status.previousVersions) {
					apiVersionMap.put(oldStatus.getValue(), status);
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
	 * This method is used to convert CampaignStatus values received via the REST
	 * API. It allows the use of old status names.
	 * 
	 * @param value
	 * @return CampaignStatus
	 * @throws IllegalArgumentException if value does not match any type
	 */
	public static CampaignStatus apiValueOf(String value) {
		CampaignStatus status = null;
		try {
			status = CampaignStatus.valueOf(value);
		} catch (Exception ignore) {
			initApiVersionMap();
			status = apiVersionMap.get(value.toUpperCase());
			if (status == null){
				throw new IllegalArgumentException("Unknown CampaignStatus: " + value);
			}
		}
		return status;
	}

	/**
	 * Same as {@link #valueOf(String)} but will return null instead
	 * of throw IllegalArgumentException if value is not valid.
	 * 
	 * @param value
	 * @return {@link CampaignStatus} or null
	 */
	public static CampaignStatus safeValueOf(String value) {
		CampaignStatus status = null;
		try {
			status = CampaignStatus.valueOf(value);
		} catch (Exception ignore) {
			// ignore exception
		}
		return status;
	}
	
	public boolean isActiveState(){
		return SCHEDULED.equals(this) || RUNNING.equals(this)
		|| ACTIVE.equals(this) || STOPPING.equals(this);
	}
}
