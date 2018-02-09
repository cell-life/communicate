package org.celllife.mobilisr.constants;

public enum DatePeriod {
	YEAR(1 /*Calendar.YEAR*/),
	MONTH(2 /*Calendar.MONTH*/),
	DAY(5 /*Calendar.DAY_OF_MONTH*/);
	
	private int calendarField;
	
	private DatePeriod(int calendarField) {
		this.calendarField = calendarField;
	}
	
	public int getCalendarField() {
		return calendarField;
	}
	
}