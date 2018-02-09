package org.celllife.mobilisr.dao.impl;

import com.trg.search.Search;
import javassist.tools.rmi.ObjectNotFoundException;
import org.celllife.mobilisr.api.messaging.DeliveryReceipt;
import org.celllife.mobilisr.api.messaging.SmsMt;
import org.celllife.mobilisr.constants.DatePeriod;
import org.celllife.mobilisr.constants.DeliveryReceiptState;
import org.celllife.mobilisr.constants.SmsStatus;
import org.celllife.mobilisr.dao.api.SmsLogDAO;
import org.celllife.mobilisr.domain.Organization;
import org.celllife.mobilisr.domain.SmsLog;
import org.celllife.mobilisr.test.AbstractDBTest;
import org.celllife.mobilisr.util.MobilisrDomainUtility;
import org.celllife.mobilisr.utilbean.LogSummary;
import org.gwttime.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class SmsLogDAOImplTests extends AbstractDBTest {

	@Autowired
	private SmsLogDAO smsLogDao;

	private Organization org1;
	private Organization org2;
	private int[] smsLogsOrg1;
	private int[] smsLogsOrg2;

	@Before
	public void setup() throws Exception{
		Search s = new Search(Organization.class);
		s.addFilterEqual(Organization.PROP_NAME, "test org 0");
		org1 = (Organization) getGeneralDao().searchUnique(s);
		s = new Search(Organization.class);
		s.addFilterEqual(Organization.PROP_NAME, "test org 5");
		org2 = (Organization) getGeneralDao().searchUnique(s);

		smsLogsOrg1 = new int[] { 5,1,3,4,2 };
		smsLogsOrg2 = new int[] { 3,1,2,5,4 };
	}

	@Test
	public void testGetSmsLogsPerMonth_notNullOrganization1(){
		testGetSmsLogs(DatePeriod.MONTH, org1);
	}

	@Test
	public void testGetSmsLogsPerMonth_notNullOrganization2(){
		testGetSmsLogs(DatePeriod.MONTH, org2);
	}

	@Test
	public void testGetSmsLogsPerYear_nullOrganization(){
		testGetSmsLogs(DatePeriod.YEAR, null);
	}

	@Test
	public void testGetSmsLogsPerYear_notNullOrganization1(){
		testGetSmsLogs(DatePeriod.YEAR, org1);
	}

	@Test
	public void testGetSmsLogsPerYear_notNullOrganization2(){
		testGetSmsLogs(DatePeriod.YEAR, org2);
	}


	@Test
	public void testGetSmsLogsPerDay_nullOrganization(){
		testGetSmsLogs(DatePeriod.DAY, null);
	}

	@Test
	public void testGetSmsLogsPerDay_notNullOrganization1(){
		testGetSmsLogs(DatePeriod.DAY, org1);
	}

	@Test
	public void testGetSmsLogsPerDay_notNullOrganization2(){
		testGetSmsLogs(DatePeriod.DAY, org2);
	}

	@Test
	public void testGetSmsLogsPerMonth_nullOrganization(){
		testGetSmsLogs(DatePeriod.MONTH, null);
	}

	@Test
	public void testUpdateSmsLog(){
		SmsLog log = new SmsLog("27756541234", "", "msg 1", "", SmsStatus.QUEUED_SUCCESS, "", "" , new Date(), null,org1);
		smsLogDao.save(log);

		SmsMt mt = new SmsMt();
		mt.setMessageLogId(log.getId());
		mt.setStatus(SmsStatus.WASP_FAIL);
		mt.setErrorMessage("unknown error");
		mt.setMessageTrackingNumber("msgTrackingNum");
		mt.setSendingAttempts(3);
		smsLogDao.updateSmsLog(mt);

		SmsLog find = smsLogDao.find(log.getId());
		Assert.assertEquals(mt.getStatus(), find.getStatus());
		Assert.assertEquals(mt.getErrorMessage(), find.getFailreason());
		Assert.assertEquals(mt.getMessageTrackingNumber(), find.getTrackingnumber());
		Assert.assertEquals(mt.getSendingAttempts().intValue(), find.getAttempts());
	}

	@Test
	public void testUpdateSmsLog_deliverySuccess() throws ObjectNotFoundException{
		String seqNum = "123456";
		String msisdn = "27756541234";
		SmsLog log = new SmsLog(msisdn, "", "msg 1", "",
				SmsStatus.WASP_SUCCESS, seqNum, null, new Date(), null, org1);
		smsLogDao.save(log);

		DeliveryReceiptState status = DeliveryReceiptState.DELIVRD;
		DeliveryReceipt receipt = new DeliveryReceipt(seqNum , new Date(), status, "1");
		receipt.setSourceAddr(msisdn);
		smsLogDao.updateSmsLog(receipt);

		SmsLog find = smsLogDao.find(log.getId());
		Assert.assertEquals(SmsStatus.TX_SUCCESS, find.getStatus());
		Assert.assertEquals(DeliveryReceiptState.DELIVRD.name(), find.getWaspStatus());
		Assert.assertNull(find.getFailreason());
	}

	@Test
	public void testUpdateSmsLog_deliveryFail() throws ObjectNotFoundException{
		String seqNum = "123456";
		String msisdn = "27756541234";
		SmsLog log = new SmsLog(msisdn, "", "msg 1", "",
				SmsStatus.WASP_SUCCESS, seqNum, null, new Date(), null, org1);
		smsLogDao.save(log);

		DeliveryReceiptState status = DeliveryReceiptState.REJECTD;
		DeliveryReceipt receipt = new DeliveryReceipt(seqNum , new Date(), status, "1");
		receipt.setSourceAddr(msisdn);
		smsLogDao.updateSmsLog(receipt);

		SmsLog find = smsLogDao.find(log.getId());
		Assert.assertEquals(SmsStatus.TX_FAIL, find.getStatus());
		Assert.assertEquals(DeliveryReceiptState.REJECTD.name(), find.getWaspStatus());
		Assert.assertEquals(DeliveryReceiptState.REJECTD.getMessage(), find.getFailreason());
	}

	@Test
	public void testUpdateSmsLog_deliveryMulti() throws ObjectNotFoundException{
		String seqNum = "123456";
		String msisdn = "27756541234";
		SmsLog log = new SmsLog(msisdn, "", "msg 1", "",
				SmsStatus.WASP_SUCCESS, seqNum, null, new Date(), null, org1);
		log.setWaspStatus(DeliveryReceiptState.EXPIRED.name());
		log.setFailreason(DeliveryReceiptState.EXPIRED.getMessage());
		smsLogDao.save(log);

		DeliveryReceiptState status = DeliveryReceiptState.DELIVRD;
		DeliveryReceipt receipt = new DeliveryReceipt(seqNum , new Date(), status, "1");
		receipt.setSourceAddr(msisdn);
		smsLogDao.updateSmsLog(receipt);

		SmsLog find = smsLogDao.find(log.getId());
		Assert.assertEquals(SmsStatus.TX_SUCCESS, find.getStatus());
		Assert.assertEquals(DeliveryReceiptState.EXPIRED.name() + ","
				+ DeliveryReceiptState.DELIVRD.name(), find.getWaspStatus());
		Assert.assertEquals(DeliveryReceiptState.EXPIRED.getMessage(), find.getFailreason());
	}

	@Test
	public void testUpdateSmsLog_deliveryMultiFail() throws ObjectNotFoundException{
		String seqNum = "123456";
		String msisdn = "27756541234";
		SmsLog log = new SmsLog(msisdn, "", "msg 1", "",
				SmsStatus.WASP_SUCCESS, seqNum, null, new Date(), null, org1);
		log.setWaspStatus(DeliveryReceiptState.EXPIRED.name());
		log.setFailreason(DeliveryReceiptState.EXPIRED.getMessage());
		smsLogDao.save(log);

		DeliveryReceiptState status = DeliveryReceiptState.REJECTD;
		DeliveryReceipt receipt = new DeliveryReceipt(seqNum , new Date(), status, "1");
		receipt.setSourceAddr(msisdn);
		smsLogDao.updateSmsLog(receipt);

		SmsLog find = smsLogDao.find(log.getId());
		Assert.assertEquals(SmsStatus.TX_FAIL, find.getStatus());
		Assert.assertEquals(DeliveryReceiptState.EXPIRED.name() + ","
				+ DeliveryReceiptState.REJECTD.name(), find.getWaspStatus());
		Assert.assertEquals(DeliveryReceiptState.EXPIRED.getMessage() + ","
				+ DeliveryReceiptState.REJECTD.getMessage(), find.getFailreason());
	}

	@Test
	public void updateSmsLogStatus() throws ObjectNotFoundException{

		List<Long> logIds = new ArrayList<Long>();
		for (int i = 0; i < 10; i++) {
			SmsLog log = new SmsLog("27756541234", "", "msg 1", "",
					SmsStatus.WASP_SUCCESS, "", null, new Date(), null, org1);
			smsLogDao.save(log);
			logIds.add(log.getId());
		}

		smsLogDao.updateSmsLogStatus(SmsStatus.WASP_FAIL, logIds);

		List<SmsLog> find = smsLogDao.findAll();
		for (SmsLog smsLog : find) {
			Assert.assertEquals(SmsStatus.WASP_FAIL, smsLog.getStatus());
		}
	}

	/**
	 * Generic test method for testing the getSmsLogs method.
	 *
	 * @param groupBy which groupBy method should be used
	 * @param org which organisation is being used (null for all organisations)
	 *
	 */
	public void testGetSmsLogs(DatePeriod groupBy, Organization org){
		createTestData(groupBy);
		Calendar cal = Calendar.getInstance();
		cal.add(groupBy.getCalendarField(), -smsLogsOrg1.length+1);
		Date from = MobilisrDomainUtility.getBeginningOfPeriod(groupBy, cal.getTime());
		List<LogSummary> smsLogsPerDay = smsLogDao.getSmsLogs(org, from, new Date(), groupBy);

		for (int i = 0; i < smsLogsOrg1.length; i++) {
			LogSummary lpd = smsLogsPerDay.get(i);
			int expectedTotal = 0;
			if (org == null){
				expectedTotal = smsLogsOrg1[i]+smsLogsOrg2[i];
			} else if (org.equals(org1)){
				expectedTotal = smsLogsOrg1[i];
			} else if (org.equals(org2)){
				expectedTotal = smsLogsOrg2[i];
			}
			Assert.assertEquals("index="+i, expectedTotal, lpd.getNumberOfMessages().intValue());

			int expectedFailures = 0;
			if (org == null || org.equals(org2)){
				expectedFailures = smsLogsOrg2[i];
			}
			Assert.assertEquals("index="+i, expectedFailures, lpd.getNumberOfFailures().intValue());
			if (groupBy.getCalendarField() >= Calendar.DAY_OF_MONTH){
				Assert.assertEquals("index="+i, cal.get(Calendar.DAY_OF_MONTH), lpd.getDay());
			}

			if (groupBy.getCalendarField() >= Calendar.MONTH){
				Assert.assertEquals("index="+i, cal.get(Calendar.MONTH)+1, lpd.getMonth());
			}

			if (groupBy.getCalendarField() >= Calendar.YEAR){
				Assert.assertEquals("index="+i, cal.get(Calendar.YEAR), lpd.getYear());
			}
			cal.add(groupBy.getCalendarField(), 1);
		}
	}


	/**
	 * Create smsLog entries in the database for org1 and org2
	 * based on the smsLogsOrg1 and smsLogsOrg2 arrays.
	 *
	 * If smsLogsOrg1[i] = 5 then 5 smsLogs will be created for org1
	 * on date = today - (smsLogsOrg1.length+i+1) units
	 * where units = groupBy (DAYS, MONTHS, YEARS)
	 *
	 * Note: all smslogs for org1 have status = WASP_SENT
	 * all smslogs for org2 have status = WASP_FAIL
	 *
	 * @param groupBy
	 */
	public void createTestData(DatePeriod groupBy){
		Calendar cal = Calendar.getInstance();
		cal.add(groupBy.getCalendarField(), -smsLogsOrg1.length+1);
		for (int i = 0; i < smsLogsOrg1.length; i++) {
			Date dateTime = cal.getTime();
			for (int j = 0; j < smsLogsOrg1[i]; j++) {
				SmsLog l = new SmsLog("2775654123" + i+j, "", "msg " + i+j, "", SmsStatus.WASP_SUCCESS, "", "" , dateTime, null, org1);
				getGeneralDao().save(l);
			}
			for (int j = 0; j < smsLogsOrg2[i]; j++) {
				SmsLog l = new SmsLog("2775654123" + i+j, "", "msg " + i+j, "", SmsStatus.WASP_FAIL, "", "" , dateTime, null, org2);
				getGeneralDao().save(l);
			}
			cal.add(groupBy.getCalendarField(), 1);
		}
	}

	@Test
	public void testUpdateUndelivered(){

		Date thisDate = new DateTime().minusDays(3).toDate();

		SmsLog log = new SmsLog("27756541234", "", "msg 1", "", SmsStatus.QUEUED_SUCCESS, "", "" , thisDate , null,org1);
		smsLogDao.save(log);

		SmsMt mt = new SmsMt();
		mt.setMessageLogId(log.getId());
		mt.setStatus(SmsStatus.WASP_SUCCESS);
		mt.setErrorMessage("unknown error");
		mt.setMessageTrackingNumber("msgTrackingNum");
		mt.setSendingAttempts(3);
		smsLogDao.updateSmsLog(mt);

		smsLogDao.updateUndeliveredMessages(7);

		SmsLog find = smsLogDao.find(log.getId());
		Assert.assertEquals(SmsStatus.WASP_SUCCESS, find.getStatus());
	}

	@Test
	public void testUpdateUndelivered_Failed(){

		Date thisDate = new DateTime().minusDays(8).toDate();

		SmsLog log = new SmsLog("27756541234", "", "msg 1", "", SmsStatus.QUEUED_SUCCESS, "", "" , thisDate , null,org1);
		smsLogDao.save(log);

		SmsMt mt = new SmsMt();
		mt.setMessageLogId(log.getId());
		mt.setStatus(SmsStatus.WASP_SUCCESS);
		mt.setErrorMessage("unknown error");
		mt.setMessageTrackingNumber("msgTrackingNum");
		mt.setSendingAttempts(3);
		smsLogDao.updateSmsLog(mt);

		smsLogDao.updateUndeliveredMessages(7);

		SmsLog find = smsLogDao.find(log.getId());
		Assert.assertEquals(SmsStatus.TX_FAIL, find.getStatus());
	}

    @Test
    public void testCountFailedMessages() {

        SmsLog log = new SmsLog("27724194158", "", "msg 1", "", SmsStatus.TX_FAIL, "", "" , getDaysBefore(3) , null, org1);
        smsLogDao.save(log);

        log = new SmsLog("27724194158", "", "msg 1", "", SmsStatus.TX_FAIL, "", "" , getDaysBefore(4) , null, org1);
        smsLogDao.save(log);

        log = new SmsLog("27724194158", "", "msg 1", "", SmsStatus.TX_FAIL, "", "" , getDaysBefore(5) , null, org1);
        smsLogDao.save(log);

        log = new SmsLog("27724194158", "", "msg 1", "", SmsStatus.TX_FAIL, "", "" , getDaysBefore(6) , null, org1);
        smsLogDao.save(log);

        log = new SmsLog("27724194158", "", "msg 1", "", SmsStatus.TX_SUCCESS, "", "" , getDaysBefore(7), null, org1);
        smsLogDao.save(log);

        Long count = smsLogDao.countFailedMessages(5, "27724194158");
        Assert.assertEquals(4, count.longValue());

        count = smsLogDao.countFailedMessages(3, "27724194158");
        Assert.assertEquals(3, count.longValue());

    }

    private Date getDaysBefore(Integer days) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH,days * -1);

        return calendar.getTime();
    }


}
