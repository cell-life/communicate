package org.celllife.mobilisr.service.qrtz;

/**
 * Enum to hold the names of the background jobs. These names must match
 * the names in mobilisr-serviceContext.xml
 *
 * @author Simon Kelly
 */
public enum BackgroundJobs {

	PROCESS_CAMPAIGN_CONTACTS_CURRENT_DAY_JOB("processCampContactProgress"),
	PROCESS_CAMPAIGN_FINISH_JOB("processCampFinish"),
	UPDATE_ORGANIZATION_BALANCES_JOB("updateOrganizationBalances"),
	PROCESS_INCOMING_QUEUE("processIncomingQueue"),
	PROCESS_MAIL_QUEUE("processMailQueue");

	private String jobName;


	private BackgroundJobs(String jobName) {
		this.jobName = jobName;
	}

	public String getJobName() {
		return jobName;
	}
}
