package org.celllife.mobilisr.service.qrtz.beans;

import org.celllife.mobilisr.service.ReportService;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.quartz.QuartzJobBean;

public class ScheduledReportsJobRunner extends QuartzJobBean{
	
	public static final String PROP_REPORT_ID = "reportId";

	private String reportId;

	private ApplicationContext applicationContext;
	
	@Override
	protected void executeInternal(JobExecutionContext jobExecutionContext)throws JobExecutionException {
		ReportService service = applicationContext.getBean(ReportService.class);
		service.generateScheduledReport(reportId);
	}
	
	public void setReportId(String reportId) {
		this.reportId = reportId;
	}
	
	public void setApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}
}
