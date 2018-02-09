package org.celllife.mobilisr.api.rest;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.celllife.mobilisr.api.MobilisrDto;
import org.celllife.mobilisr.constants.CampaignStatus;
import org.celllife.mobilisr.constants.CampaignType;

@XmlRootElement(name = "campaign")
@XmlType(name="Campaign", propOrder = { "id", "name", "description", "type", "status",
		"startDate", "cost", "timesPerDay", "duration", "messages", "contacts" })
public class CampaignDto implements MobilisrDto {

	private static final long serialVersionUID = -3155954468302275444L;
	
	private Long id;
	private String name;
	private String description;
	/**
	 * Represented as String to support backward comparability
	 * @see CampaignStatus
	 */
	private String status;
	private Date startDate;
	private Integer cost;
	private Integer timesPerDay;
	private Integer duration;
	/**
	 * Represented as String to support backward comparability
	 * @see CampaignType
	 */
	private String type;
	private List<MessageDto> messages;
	private List<ContactDto> contacts;

	public CampaignDto() {
	}

	public String getName() {
		return name;
	}

	@XmlElement(name = "name", required = true)
	public void setName(String name) {
		this.name = name;
	}

	public String getStatus() {
		return status;
	}

	@XmlElement(name = "status", required = false)
	public void setStatus(String status) {
		this.status = status;
	}

	/**
	 * @return the date on which the campaign was started or is scheduled to
	 *         start
	 */
	public Date getStartDate() {
		return startDate;
	}

	@XmlElement(name = "startDate", required = false)
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	/**
	 * The cost of the campaign. For DAILY and FLEXI campaigns this is the
	 * cost per contact. For FIXED campaigns this is the overall cost of the
	 * campaign.
	 * 
	 * @return the cost of the campaign in units
	 */
	public Integer getCost() {
		return cost;
	}

	@XmlElement(name = "cost", required = false)
	public void setCost(Integer cost) {
		this.cost = cost;
	}

	public String getDescription() {
		return description;
	}

	@XmlElement(name = "description", required = false)
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * This represents the number of messages send per day by this campaign.
	 * TimesPerDay is only applicable to DAILY campaigns
	 * 
	 * @return number of messages each contact will receive per day for the
	 *         duration of the campaign or null for FIXED and FLEXI campaigns
	 */
	public Integer getTimesPerDay() {
		return timesPerDay;
	}

	@XmlElement(name = "timesPerDay", required = false)
	public void setTimesPerDay(Integer timesPerDay) {
		this.timesPerDay = timesPerDay;
	}

	/**
	 * Duration represents the number of days this campaign runs for per
	 * contact. Duration is only applicable to DAILY and FLEXI campaigns.
	 * 
	 * @return duration of the campaign per contact in days or null for FIXED
	 *         campaigns
	 */
	public Integer getDuration() {
		return duration;
	}

	@XmlElement(name = "duration", required = false)
	public void setDuration(Integer duration) {
		this.duration = duration;
	}

	@Override
	public String toString() {
		return "CampaignDto [id=" + id + ", name=" + name + ", description="
				+ description + ", status=" + status + ", startDate="
				+ startDate + ", cost=" + cost + ", timesPerDay=" + timesPerDay
				+ ", duration=" + duration + ", type=" + type + ", messages="
				+ messages + ", contacts=" + contacts + "]";
	}

	@XmlElement(name = "type", required = true)
	public void setType(String type) {
		this.type = type;
	}

	public String getType() {
		return type;
	}

	@XmlElement(name = "id", required = false)
	public void setId(Long id) {
		this.id = id;
	}

	public Long getId() {
		return id;
	}

	@XmlElementWrapper(name="messages")
	@XmlElement(name = "message")
	public void setMessages(List<MessageDto> messages) {
		this.messages = messages;
	}

	public List<MessageDto> getMessages() {
		return messages;
	}

	@XmlElementWrapper(name="contacts")
	@XmlElement(name = "contact")
	public void setContacts(List<ContactDto> contacts) {
		this.contacts = contacts;
	}

	public List<ContactDto> getContacts() {
		return contacts;
	}

	/**
	 * Convenience method for adding a message.
	 *  
	 * @param message
	 */
	public void addMessage(MessageDto message) {
		if (messages == null){
			messages = new ArrayList<MessageDto>();
		}
		messages.add(message);
	}

}
