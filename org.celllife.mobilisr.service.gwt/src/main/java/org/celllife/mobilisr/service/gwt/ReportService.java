package org.celllife.mobilisr.service.gwt;

import java.util.Date;
import java.util.List;

import org.celllife.mobilisr.constants.DatePeriod;
import org.celllife.mobilisr.domain.Organization;
import org.celllife.mobilisr.exception.MobilisrException;
import org.celllife.pconfig.model.FilledPconfig;
import org.celllife.pconfig.model.Pconfig;
import org.celllife.pconfig.model.ScheduledPconfig;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * @see org.celllife.mobilisr.service.ReportService
 */
@RemoteServiceRelativePath("reporting.rpc")
public interface ReportService extends RemoteService {

	/**
	 * @see org.celllife.mobilisr.service.ReportService#getSmsReportData(Organization, Date, Date, DatePeriod)
	 */
	List<SmsReportData> getSmsReportData(Organization org, Date from, Date to, DatePeriod period) throws MobilisrException;
	
	/**
	 * @see org.celllife.mobilisr.service.ReportService#getReports()
	 */
	List<Pconfig> getReports() throws MobilisrException;

	/**
	 * @see org.celllife.mobilisr.service.ReportService#generateReport(Pconfig)
	 */
	String generateReport(Pconfig report) throws MobilisrException;

	/**
	 * @param showAll
	 * @see org.celllife.mobilisr.service.ReportService#getGeneratedReports(String, boolean)
	 */
	List<FilledPconfig> getGeneratedReports(String name, boolean showAll);

	/**
	 * @see org.celllife.mobilisr.service.ReportService#deleteGeneratedReport(String)
	 */
	void deleteGeneratedReport(String reportId);

	/**
	 * @see org.celllife.mobilisr.service.ReportService#refreshCache()
	 */
	void refreshCache();

	/**
	 * @return 
	 * @see org.celllife.mobilisr.service.ReportService#addScheduledReport(ScheduledPconfig, String)
	 */
	String addScheduledReport(ScheduledPconfig report);
	
	/**
	 * @return 
	 * @see org.celllife.mobilisr.service.ReportService#getScheduledReports(String, boolean)
	 */
	List<ScheduledPconfig> getScheduledReports(String reportId, boolean showAll);
	
	/**
	 * @see org.celllife.mobilisr.service.ReportService#deleteScheduledReport(String)
	 */
	void deleteScheduledReport(String reportId);
}
