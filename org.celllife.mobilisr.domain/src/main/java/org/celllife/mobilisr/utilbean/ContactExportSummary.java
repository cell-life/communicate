package org.celllife.mobilisr.utilbean;

import java.io.Serializable;

/**
 * Contact Summary object used for the export. Contains basic Contact information 
 * and Campaigns to which the Contact belongs (if any)
 */
public class ContactExportSummary implements Serializable {

	private static final long serialVersionUID = -1074751860020566980L;

	private String firstName;
	private String lastName;
	private String msisdn;
	private String campaigns;

	public ContactExportSummary() {
		// default
	}
	
	/**
	 * Create a ContactExportSummary object
	 * 
	 * @param firstName String first name of the Contact
	 * @param lastName String last name of the Contact
	 * @param msisdn String msisdn (cellphone number) of the Contact
	 */
	public ContactExportSummary(String firstName, String lastName, String msisdn) {
		this.firstName = firstName;
		this.lastName = lastName;
		this.msisdn = msisdn;
	}
	
	/**
	 * Create a ContactExportSummary with campaigns
	 * 
	 * @param firstName String first name of the Contact
	 * @param lastName String last name of the Contact
	 * @param msisdn String msisdn (cellphone number) of the Contact
	 * @param campaigns String comma separated list of campaign names to which this Contact belongs
	 */
	public ContactExportSummary(String firstName, String lastName, String msisdn, String campaigns) {
		this(firstName, lastName, msisdn);
		this.campaigns = campaigns;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getMsisdn() {
		return msisdn;
	}

	public void setMsisdn(String msisdn) {
		this.msisdn = msisdn;
	}

	/**
	 * @return String comma separated list of Campaign names (can be null or empty)
	 */
	public String getCampaigns() {
		return campaigns;
	}

	/**
	 * @param campaigns String comma separated list of Campaign names (can be null or empty)
	 */
	public void setCampaigns(String campaigns) {
		this.campaigns = campaigns;
	}
}