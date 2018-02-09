package org.celllife.mobilisr.utilbean;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

public class LogSummary implements Serializable {
	
	private static final long serialVersionUID = -7869045972745601642L;

	private static final String DATE_SEPARATOR = "-";
	
	/**
	 * Integer representing the year.
	 */
	private int year;
	
	/**
	 * Integer representing the month of the year, starting at January = 1.
	 */
	private int month;
	
	/**
	 * Integer representing the day of the month, starting at 1.
	 */
	private int day;
	
	private Number numberOfMessages;
	private Number numberOfFailuers;
	
	public LogSummary() {
	}

	public LogSummary(int year, int month, int day, Number numberOfMessages, Number numberOfFailures) {
		this.year = year;
		this.month = month;
		this.day = day;
		this.numberOfMessages = numberOfMessages;
		this.numberOfFailuers = numberOfFailures;
	}

	public int getDay() {
		return day;
	}

	public String getLabel(){
		String label = "";
		if (day > 0){
			label += normalize(day) + DATE_SEPARATOR;
		}

		if (month > 0){
			label += normalize(month) + DATE_SEPARATOR;
		}
		
		label += String.valueOf(year);
		return label;
	}
	
	private String normalize(int number){
		return number < 10 ? "0"+number : String.valueOf(number); 
	}

	public int getMonth() {
		return month;
	}

	public Number getNumberOfMessages() {
		return numberOfMessages;
	}

	public int getYear() {
		return year;
	}
	
	public Date getDate(){
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, year);
		cal.set(Calendar.MONTH, month > 0 ? month - 1 : 0);
		cal.set(Calendar.DAY_OF_MONTH, day > 0 ? day : 1);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime();
	}

	public void setDay(Number day) {
		this.day = day.intValue();
	}
	
	public void setMonth(Number month) {
		this.month = month.intValue();
	}
	
	public void setNumberOfMessages(Number numberOfMessages) {
		this.numberOfMessages = numberOfMessages;
	}

	public void setYear(Number year) {
		this.year = year.intValue();
	}

	@Override
	public String toString() {
		return getLabel() + ":" + numberOfMessages;
	}

	public void setNumberOfFailuers(Number numberOfFailuers) {
		this.numberOfFailuers = numberOfFailuers;
	}

	public Number getNumberOfFailures() {
		return numberOfFailuers;
	}
}
