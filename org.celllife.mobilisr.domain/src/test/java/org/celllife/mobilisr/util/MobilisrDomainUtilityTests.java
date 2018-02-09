package org.celllife.mobilisr.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import junit.framework.Assert;

import org.celllife.mobilisr.domain.Campaign;
import org.celllife.mobilisr.domain.MobilisrEntity;
import org.junit.Before;
import org.junit.Test;

public class MobilisrDomainUtilityTests {
	
	private SimpleDateFormat dateFormat;

	@Before
	public void setup(){
		dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	}
	
	@Test
	public void testDaysDiff(){
		
		Date d1 = new Date();
		for( int i = 1 ; i < 10 ; i++){
			
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.DAY_OF_MONTH, i);
			Date d2 = cal.getTime();
			
			int daysBetween = MobilisrDomainUtility.getDaysBetween(d1, d2);
			Assert.assertEquals(i, daysBetween);
		}
	}
	
	@Test
	public void testMonthDiff(){
		for( int i = 1 ; i < 10 ; i++){
			Date date = new Date();
			Calendar cal = Calendar.getInstance();
			cal.setTime(date);
			cal.add(Calendar.MONTH, i);
			
			int diff = MobilisrDomainUtility.getMonthsBetween(date, cal.getTime());
			Assert.assertEquals(i, diff);
		}
	}
	
	@Test
	public void testCombineDateAndTime() throws ParseException{
		String datePattern = "yyyy-MM-dd";
		SimpleDateFormat dateFormat = new SimpleDateFormat(datePattern);
		String dateString = "2010-10-01";
		Date date = dateFormat.parse(dateString);
		
		String timePattern = "hh:mm:ss";
		SimpleDateFormat timeFormat = new SimpleDateFormat(timePattern);
		String timeString = "15:03:21";
		Date time = timeFormat.parse(timeString);
		
		Date combineDateAndTime = MobilisrDomainUtility.combineDateAndTime(date, time);
		
		SimpleDateFormat combinedFormat = new SimpleDateFormat(datePattern + timePattern);
		Date expectedCombinedDate = combinedFormat.parse(dateString + timeString);
		
		Assert.assertEquals(expectedCombinedDate, combineDateAndTime);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testGetIdFromIdentifier_illegal(){
		MobilisrDomainUtility.getIdFromIdentifier("invalid");
	}
	
	@Test
	public void testGetIdFromIdentifier(){
		Long id = MobilisrDomainUtility.getIdFromIdentifier("someClass:13");
		Assert.assertEquals(13, id.intValue());
	}
	
	@Test
	public void testGetClassFromIdentifier(){
		Class<? extends MobilisrEntity> clazz = MobilisrDomainUtility.getClassFromIdentifier(new Campaign().getIdentifierString(3L));
		Assert.assertEquals(Campaign.class, clazz);
	}
	
	@Test
	public void testGetEndOfDay() throws ParseException{
		Date date = MobilisrDomainUtility.getEndOfDay(dateFormat.parse("2011-01-12 00:00:00"));
		Assert.assertEquals("2011-01-12 23:59:59", dateFormat.format(date));
	}
	
	@Test
	public void testGetEndOfMonth() throws ParseException{
		Date date = MobilisrDomainUtility.getEndOfMonth(dateFormat.parse("2011-01-12 00:00:00"));
		Assert.assertEquals("2011-01-31 23:59:59", dateFormat.format(date));
	}
	
	@Test
	public void testGetEndOfMonth2() throws ParseException{
		Date date = MobilisrDomainUtility.getEndOfMonth(dateFormat.parse("2011-04-01 00:00:00"));
		Assert.assertEquals("2011-04-30 23:59:59", dateFormat.format(date));
	}

	@Test
	public void testGetEndOfYear() throws ParseException{
		Date date = MobilisrDomainUtility.getEndOfYear(dateFormat.parse("2011-01-12 00:00:00"));
		Assert.assertEquals("2011-12-31 23:59:59", dateFormat.format(date));
	}
	
	@Test
	public void testGetBeginningOfDay() throws ParseException{
		Date date = MobilisrDomainUtility.getBeginningOfDay(dateFormat.parse("2011-01-12 23:59:59"));
		Assert.assertEquals("2011-01-12 00:00:00", dateFormat.format(date));
	}
	
	@Test
	public void testGetBeginningOfMonth() throws ParseException{
		Date date = MobilisrDomainUtility.getBeginningOfMonth(dateFormat.parse("2011-01-12 23:59:59"));
		Assert.assertEquals("2011-01-01 00:00:00", dateFormat.format(date));
	}
	
	@Test
	public void testGetBeginningOfYear() throws ParseException{
		Date date = MobilisrDomainUtility.getBeginningOfYear(dateFormat.parse("2011-12-12 23:59:59"));
		Assert.assertEquals("2011-01-01 00:00:00", dateFormat.format(date));
	}
}
