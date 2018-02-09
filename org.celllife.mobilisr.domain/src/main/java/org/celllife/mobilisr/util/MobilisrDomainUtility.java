package org.celllife.mobilisr.util;

import java.util.Calendar;
import java.util.Date;

import org.celllife.mobilisr.constants.DatePeriod;
import org.celllife.mobilisr.domain.MobilisrEntity;
import org.celllife.mobilisr.exception.MobilisrRuntimeException;
import org.gwttime.time.DateMidnight;
import org.gwttime.time.DateTime;
import org.gwttime.time.Days;
import org.gwttime.time.Months;

public class MobilisrDomainUtility {
	
	public static int getDaysBetween(Date dateOne, Date dateTwo) {
		assert (dateOne != null && dateTwo != null) : "Null date argument exception.";
		Days days = Days.daysBetween(new DateMidnight(dateOne), new DateMidnight(dateTwo));
		int dayDiff = days.getDays();
		return dayDiff;
	}
	
	public static Date zeroTime(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);	
		return zeroTime(calendar).getTime();
	}

	public static Calendar zeroTime(Calendar calendar) {
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar;
	}
	
	public static final int getMonthsBetween(Date d1, Date d2) {
		Months d = Months.monthsBetween( new DateTime(d1), new DateTime(d2));
		int monthsDiff = d.getMonths();
		return monthsDiff;
	}
	
	/**
	 * Give the input date this method returns a new date on the same day at
	 * 00:00:00
	 * 
	 * @param theDate
	 * @return
	 */
	public static Date getBeginningOfDay(Date theDate) {
		return zeroTime(theDate);
	}
	
	/**
	 * Give the input date this method returns a new date on the same day at
	 * 23:59:59
	 * 
	 * @param theDate
	 * @return
	 */
	public static Date getEndOfDay(Date theDate) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(theDate);
		maxTime(cal);
		return cal.getTime();
	}

	private static Calendar maxTime(Calendar cal) {
		cal.set(Calendar.HOUR_OF_DAY, 23);
		cal.set(Calendar.MINUTE, 59);
		cal.set(Calendar.SECOND, 59);
		cal.set(Calendar.MILLISECOND, 59);
		return cal;
	}

	/**
	 * Combines the date portion of one Date with the time portion of another Date
	 * @param date
	 * @param time
	 * @return
	 */
	public static Date combineDateAndTime(Date date, Date time){
		Calendar calDate = Calendar.getInstance();
		calDate.setTime(date);
		
		Calendar calTime = Calendar.getInstance();
		calTime.setTime(time);
		
		calDate.set(Calendar.HOUR_OF_DAY, calTime.get(Calendar.HOUR_OF_DAY));
		calDate.set(Calendar.MINUTE, calTime.get(Calendar.MINUTE));
		calDate.set(Calendar.SECOND, calTime.get(Calendar.SECOND));
		calDate.set(Calendar.MILLISECOND, calTime.get(Calendar.MILLISECOND));
		
		return calDate.getTime();
	}
	
	public static Date getBeginningOfYear(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.set(Calendar.DAY_OF_MONTH, 1);
		calendar.set(Calendar.MONTH, 0);
		zeroTime(calendar);
		return calendar.getTime();
	}
	
	public static Long getIdFromIdentifier(String identifier) {
		int split = identifier.indexOf(":");
		if (split < 0){
			throw new IllegalArgumentException("Invalid identifier [" + identifier + "]");
		}
		
		String idString = identifier.substring(split+1);
		return Long.valueOf(idString);
	}
	
	@SuppressWarnings("unchecked")
	public static Class<? extends MobilisrEntity> getClassFromIdentifier(String identifier) {
		int split = identifier.indexOf(":");
		if (split < 0){
			throw new IllegalArgumentException("Invalid identifier [" + identifier + "]");
		}
		
		String classString = identifier.substring(0,split);
		Class<? extends MobilisrEntity> entityClass = null;
		try {
			entityClass = (Class<? extends MobilisrEntity>) Class.forName(classString);
		} catch (Exception e) {
			throw new MobilisrRuntimeException("Unknown entity: " + classString);
		}
		return entityClass;
	}
	
	public static Date getBeginningOfMonth(Date date){
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.set(Calendar.DAY_OF_MONTH, 1);
		zeroTime(calendar);
		return calendar.getTime();
	}
	
	public static Date getEndOfMonth(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
		maxTime(calendar);
		return calendar.getTime();
	}

	public static Date getEndOfYear(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.set(Calendar.MONTH, 11);
		calendar.set(Calendar.DAY_OF_MONTH, 31);
		maxTime(calendar);
		return calendar.getTime();
	}
	
	public static Date getBeginningOfPeriod(DatePeriod period, Date date){
		switch(period){
		case DAY:
			return MobilisrDomainUtility.getBeginningOfDay(date);
		case MONTH:
			return MobilisrDomainUtility.getBeginningOfMonth(date);
		case YEAR:
			return MobilisrDomainUtility.getBeginningOfYear(date);
		default:
			return date;
		}
	}
	
	public static Date getEndOfPeriod(DatePeriod period, Date date){
		switch(period){
		case DAY:
			return MobilisrDomainUtility.getEndOfDay(date);
		case MONTH:
			return MobilisrDomainUtility.getEndOfMonth(date);
		case YEAR:
			return MobilisrDomainUtility.getEndOfYear(date);
		default:
			return date;
		}
	}
}
