package org.celllife.mobilisr.service.impl;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import junit.framework.Assert;

import org.celllife.mobilisr.constants.DatePeriod;
import org.celllife.mobilisr.constants.SmsStatus;
import org.celllife.mobilisr.domain.SmsLog;
import org.celllife.mobilisr.domain.User;
import org.celllife.mobilisr.service.ReportService;
import org.celllife.mobilisr.service.gwt.SmsReportData;
import org.celllife.mobilisr.service.qrtz.QuartzService;
import org.celllife.mobilisr.service.qrtz.impl.QuartzServiceImpl;
import org.celllife.mobilisr.test.AbstractServiceTest;
import org.celllife.mobilisr.test.TestUtils;
import org.celllife.pconfig.model.BooleanParameter;
import org.celllife.pconfig.model.DateParameter;
import org.celllife.pconfig.model.EntityParameter;
import org.celllife.pconfig.model.IntegerParameter;
import org.celllife.pconfig.model.Parameter;
import org.celllife.pconfig.model.Pconfig;
import org.celllife.pconfig.model.RepeatInterval;
import org.celllife.pconfig.model.ScheduledPconfig;
import org.celllife.pconfig.model.StringParameter;
import org.celllife.reporting.ReportingException;
import org.celllife.reporting.service.impl.JasperReportServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.trg.search.Search;

public class ReportServiceImplTests extends AbstractServiceTest {
	
	private static final int FAILURE_THRESHOLD = 3;

	private static final SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
	
	@Autowired
	private ReportService reportService;
	
	@Autowired
	private QuartzService quartzService;
	
	@Autowired
	private org.celllife.reporting.service.ReportService staticReportService;
	
	private User user;
	private int[] smsLogNumbers;
	
	@Before
	public void beforeTest() throws Exception{
		Search s = new Search(User.class).addFilterEqual(User.PROP_USERNAME, "username 0");
		user = (User) getGeneralDao().searchUnique(s);
		// values should be unique to avoid possible test errors 
		smsLogNumbers = new int[] { 0,0,5,1,0,4,0,2,0,0 };
		JasperReportServiceImpl serviceImpl = TestUtils.getTargetObject(staticReportService, JasperReportServiceImpl.class);
		serviceImpl.setGeneratedReportFolder("target/reports/generated");
		serviceImpl.setScheduledReportFolder("target/reports/scheduled");
		serviceImpl.buildService();
	}
	
	@Test
	public void testGetSmsReportData(){
		DatePeriod period = DatePeriod.DAY;
		createData(period);
		
		int reportDays = 10;
		List<SmsReportData> reportData = reportService.getDailySmsReportData(null, reportDays);
		for (int i = 0; i < reportDays; i++) {
			Calendar cal = Calendar.getInstance();
			cal.add(period.getCalendarField(), -(reportDays-i-1));
			String label = sdf.format(cal.getTime());
			Assert.assertEquals(String.valueOf(i), smsLogNumbers[i], reportData.get(i).getNumberOfMessages().intValue());
			int failuresExpected = smsLogNumbers[i]-FAILURE_THRESHOLD > 0 ? smsLogNumbers[i]-FAILURE_THRESHOLD : 0;
			Assert.assertEquals(String.valueOf(i), failuresExpected, reportData.get(i).getNumberOfFailures().intValue());
			Assert.assertEquals(String.valueOf(i), label, reportData.get(i).getLabel());
		}
	}
	
	@Test
	public void testGetSmsReportData_month(){
		DatePeriod period = DatePeriod.MONTH;
		createData(period);
		
		int reportMonths = 10;
		List<SmsReportData> reportData = reportService.getMonthlySmsReportData(null, reportMonths);
		for (int i = 0; i < reportMonths; i++) {
			Calendar cal = Calendar.getInstance();
			cal.add(period.getCalendarField(), -(reportMonths-i-1));
			String label = sdf.format(cal.getTime());
			label = label.substring(label.indexOf('-')+1);
			
			Assert.assertEquals(String.valueOf(i), smsLogNumbers[i], reportData.get(i).getNumberOfMessages().intValue());
			int failuresExpected = smsLogNumbers[i]-FAILURE_THRESHOLD > 0 ? smsLogNumbers[i]-FAILURE_THRESHOLD : 0;
			Assert.assertEquals(String.valueOf(i), failuresExpected, reportData.get(i).getNumberOfFailures().intValue());
			Assert.assertEquals(String.valueOf(i), label, reportData.get(i).getLabel());
		}
	}
	
	@Test
	public void testGetYearlySmsReportData(){
		DatePeriod period = DatePeriod.YEAR;
		createData(period);
		
		int reportMonths = 10;
		List<SmsReportData> reportData = reportService.getYearlySmsReportData(null, reportMonths);
		for (int i = 0; i < reportMonths; i++) {
			Calendar cal = Calendar.getInstance();
			cal.add(period.getCalendarField(), -(reportMonths-i-1));
			String label = sdf.format(cal.getTime());
			label = label.substring(label.lastIndexOf('-')+1);
			
			Assert.assertEquals(String.valueOf(i), smsLogNumbers[i], reportData.get(i).getNumberOfMessages().intValue());
			int failuresExpected = smsLogNumbers[i]-FAILURE_THRESHOLD > 0 ? smsLogNumbers[i]-FAILURE_THRESHOLD : 0;
			Assert.assertEquals(String.valueOf(i), failuresExpected, reportData.get(i).getNumberOfFailures().intValue());
			Assert.assertEquals(String.valueOf(i), label, reportData.get(i).getLabel());
		}
	}
	
	private void createData(DatePeriod period){
		for (int i = 0; i < smsLogNumbers.length; i++) {
			Calendar cal = Calendar.getInstance();
			cal.add(period.getCalendarField(), -(smsLogNumbers.length-i-1));
			for (int j = 0; j < smsLogNumbers[i]; j++) {
				SmsStatus status = j >= FAILURE_THRESHOLD ? SmsStatus.WASP_FAIL : SmsStatus.WASP_SUCCESS;
				SmsLog l = new SmsLog("2775654123" + i+j, "", "msg " + i+j, "", status, "", "" , cal.getTime(), null,user.getOrganization());
				getGeneralDao().save(l);
			} 
		}
	}
	
	@Test
	public void testAddScheduledReport() {		
		ScheduledPconfig report = new ScheduledPconfig();
		report.setRepeatInterval(RepeatInterval.Daily);
		report.setStartDate(new Date());
		report.setPconfig(new Pconfig());
		
		reportService.addScheduledReport(report);
		Assert.assertNotNull(report.getId());
		
		ScheduledPconfig scheduledReport = staticReportService.getScheduledReport(report.getId());
		Assert.assertNotNull(scheduledReport);
		
		List<String> triggers = quartzService.getTriggers(QuartzServiceImpl.SCHEDULED_REPORT_GROUP);
		Assert.assertEquals(1, triggers.size());
		Assert.assertTrue(triggers.get(0).contains(report.getId()));
	}
	
	@Test
	public void testGenerateScheduledReport() throws ReportingException{
		
		List<Pconfig> reports = reportService.getReports();
		Pconfig reportPconfig = reports.get(0);
		fillParameters(reportPconfig);
		
		ScheduledPconfig report = new ScheduledPconfig(reportPconfig);
		report.setRepeatInterval(RepeatInterval.Daily);
		report.setStartDate(new Date());
		report.setScheduledFor("");
		
		staticReportService.saveScheduledReportConfig(report);
		
		String pdfID = reportService.generateScheduledReport(report.getId());
		File reportfile = staticReportService.getGeneratedReportFile(pdfID);
		Assert.assertTrue(reportfile.exists());
	}
	
	/**
	 * Sets test values for the report paramters
	 */
	private void fillParameters(Pconfig report) {
		List<? extends Parameter<?>> parameters = report.getParameters();
		for (Parameter<?> param : parameters) {
			if (param instanceof StringParameter){
				((StringParameter) param).setValue("test string");
			} else if (param instanceof IntegerParameter){
				((IntegerParameter) param).setValue(13);
			} else if (param instanceof DateParameter){
				((DateParameter) param).setValue(new Date());
			} else if (param instanceof BooleanParameter){
				((BooleanParameter) param).setValue(true);
			} else if (param instanceof EntityParameter){
				EntityParameter eparam = (EntityParameter) param;
				String type = eparam.getValueType();
				if (Integer.class.getSimpleName().equals(type)){
					eparam.setValue("21");
				} else if (Long.class.getSimpleName().equals(type)){
					eparam.setValue("42");
				}if (Double.class.getSimpleName().equals(type)){
					eparam.setValue("9.4");
				}if (Boolean.class.getSimpleName().equals(type)){
					eparam.setValue("false");
				}
			}
		}
	}
	
}
