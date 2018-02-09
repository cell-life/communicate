package org.celllife.mobilisr.service;

import java.util.Date;
import java.util.List;

import org.celllife.mobilisr.constants.DatePeriod;
import org.celllife.mobilisr.domain.Organization;
import org.celllife.mobilisr.service.gwt.SmsReportData;
import org.celllife.pconfig.model.FilledPconfig;
import org.celllife.pconfig.model.Pconfig;
import org.celllife.pconfig.model.ScheduledPconfig;

import com.google.gwt.user.client.rpc.RemoteService;

public interface ReportService extends RemoteService {

	/**
	 * @param org
	 *            organisation to report on or null for all organisations
	 * @param reportDays
	 *            the number of days to report on including the current day
	 * @return a list of SmsReportData, one of each day
	 */
	List<SmsReportData> getDailySmsReportData(Organization org,
			Integer reportDays);

	/**
	 * @param org
	 *            organisation to report on or null for all organisations
	 * @param reportMonths
	 *            the number of months to report on including the current month
	 * @return a list of SmsReportData, one of each month
	 */
	List<SmsReportData> getMonthlySmsReportData(Organization org,
			Integer reportMonths);

	/**
	 * @param org
	 *            organisation to report on or null for all organisations
	 * @param reportYears
	 *            the number of years to report on including the current year
	 * @return a list of SmsReportData, one of each year
	 */
	List<SmsReportData> getYearlySmsReportData(Organization org,
			Integer reportYears);

	/**
	 * @param org
	 *            organisation to report on or null for all organisations
	 * @param from
	 *            start date. This date is rounded down to the beginning of the
	 *            period. If it is null is rounded to the beginning of the
	 *            current period.
	 * @param to
	 *            end date. This date is rounded up to the beginning of the
	 *            period. If it is null is rounded to the end of the current
	 *            period.
	 * @param period
	 *            the type of period to report on (DAY, MONTH, YEAR)
	 * @return a list of SmsReportData, one of each period in the time.
	 */
	List<SmsReportData> getSmsReportData(Organization org, Date from, Date to,
			DatePeriod period);

	List<FilledPconfig> getGeneratedReports(String id, boolean showAll);

	String generateReport(Pconfig report);

	List<Pconfig> getReports();

	void deleteGeneratedReport(String reportId);

	void refreshCache();

	/**
	 * Generates a scheduled report. Returns the ID of the generated PDF report;
	 * 
	 * @param reportID Report ID of desired report.
	 * @return 
	 */
	public String generateScheduledReport(String reportID);
	
	/**
	 * Adds a new scheduled report to the existing list of scheduled reports.
	 * 
	 * @param report New Report
	 * @return 
	 */
	String addScheduledReport(ScheduledPconfig report);

	/**
	 * Get a list of {@link ScheduledPconfig} for the given reportId. If
	 * filterByOrg is true the list will be filtered to contain only 
	 * elements owned by the currently logged in user's organisation.
	 * 
	 * @param reportId
	 * @param filterByOrg
	 * @return
	 */
	List<ScheduledPconfig> getScheduledReports(String reportId,
			boolean filterByOrg);

	void deleteScheduledReport(String reportId);

}
