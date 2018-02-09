package org.celllife.mobilisr.api.rest;

import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.celllife.mobilisr.api.MobilisrDto;


@XmlRootElement(name="contact")
@XmlType(name="Contact", propOrder = {"msisdn","firstName","lastName","contactMessageTimes","startDate"})
public class ContactDto implements MobilisrDto {

	private static final long serialVersionUID = 4703576454515958088L;
	
	/**
	 * MSISDN formatted in international format
	 * e.g. 27784561236
	 */
	private String msisdn;
	private String firstName;
	private String lastName;
    private String startDate;
	
	/**
	 * Only used when adding contact to a relative campaign and needing to specify custom
	 * message times for the contact. List size should match the number of messages slots
	 * in the relative campaign
	 */
	private List<Date> contactMessageTimes;

	public String getMsisdn() {
		return msisdn;
	}

	@XmlElement(name="msisdn", required=true)
	public void setMsisdn(String msisdn) {
		this.msisdn = msisdn;
	}

	public String getFirstName() {
		return firstName;
	}

	@XmlElement(name="firstName", required=false)
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	@XmlElement(name="lastName", required=false)
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	
	@XmlJavaTypeAdapter(type = Date.class, value = TimeAdapter.class)
	@XmlElementWrapper(name="contactMessageTimes")
	@XmlElement(name="msgTime", required=false)
	public void setContactMessageTimes(List<Date> contactMessageTimes) {
		this.contactMessageTimes = contactMessageTimes;
	}
	
	public List<Date> getContactMessageTimes() {
		return contactMessageTimes;
	}

	@Override
	public String toString() {
		return "ContactDto [msisdn=" + msisdn
				+ ", firstName=" + firstName + ", lastName=" + lastName + "]";
	}


    public String getStartDate() {
        return startDate;
    }

    @XmlElement(name="startDate", required=false)
    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }
}
