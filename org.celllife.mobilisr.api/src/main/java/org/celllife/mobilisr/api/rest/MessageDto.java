package org.celllife.mobilisr.api.rest;

import java.util.Date;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.celllife.mobilisr.api.MobilisrDto;
import org.celllife.mobilisr.constants.CampaignType;

@XmlRootElement(name = "message")
@XmlType(name="Message", propOrder={"date","time","msgDay","text"})
public class MessageDto implements MobilisrDto {

	private static final long serialVersionUID = -2770905973599305167L;
	
	private String text;
	private Date date;
	private Date time;
	private Integer msgDay;

	
	public MessageDto() {
		
	}
	
	/**
	 * @return the date at which the message was sent or is scheduled to be sent
	 */
	public Date getDate() {
		return date;
	}

	public Integer getMsgDay() {
		return msgDay;
	}

	public String getText() {
		return text;
	}

	public Date getTime() {
		return time;
	}

	/**
	 * @param date
	 *            to send the message
	 */
	@XmlElement(name = "date", required = false)
	@XmlJavaTypeAdapter(type = Date.class, value = DateAdapter.class)
	public void setDate(Date date) {
		this.date = date;
	}

	/**
	 * Used only for {@link CampaignType#DAILY} or
	 * {@link CampaignType#FLEXI} campaigns. Represents the number of days
	 * after being enrolled on a campaign that the contact will get this
	 * message.
	 * 
	 * @param msgDay
	 */
	@XmlElement(name = "msgDay", required = false)
	public void setMsgDay(Integer msgDay) {
		this.msgDay = msgDay;
	}

	@XmlElement(name = "text", required = true)
	public void setText(String text) {
		this.text = text;
	}

	/**
	 * @param time
	 *            to send the message
	 */
	@XmlElement(name = "time", required = false)
	@XmlJavaTypeAdapter(type = Date.class, value = TimeAdapter.class)
	public void setTime(Date time) {
		this.time = time;
	}

	@Override
	public String toString() {
		String toString = "";
		try {
			toString = "MessageDto [text=" + text + ", date="
					+ new DateAdapter().marshal(date) + ", time="
					+ new TimeAdapter().marshal(time) + ", msgDay=" + msgDay
					+ "]";
		} catch (Exception e) {
			e.printStackTrace();
		}

		return toString;
	}
}
