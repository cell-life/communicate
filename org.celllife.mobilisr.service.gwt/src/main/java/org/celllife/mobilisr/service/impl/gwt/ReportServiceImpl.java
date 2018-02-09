package org.celllife.mobilisr.service.impl.gwt;

import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;

import org.celllife.mobilisr.constants.DatePeriod;
import org.celllife.mobilisr.domain.Organization;
import org.celllife.mobilisr.exception.MobilisrException;
import org.celllife.mobilisr.service.gwt.ReportService;
import org.celllife.mobilisr.service.gwt.SmsReportData;
import org.celllife.pconfig.model.FilledPconfig;
import org.celllife.pconfig.model.Pconfig;
import org.celllife.pconfig.model.ScheduledPconfig;

public class ReportServiceImpl extends AbstractMobilisrService implements ReportService {

	private static final long serialVersionUID = 6966896268834750307L;
	private org.celllife.mobilisr.service.ReportService service;

	@Override
	public void init() throws ServletException {
		super.init();
		service = (org.celllife.mobilisr.service.ReportService) getBean("reportService");
	}
	
	@Override
	public List<SmsReportData> getSmsReportData(Organization org,
			Date from, Date to, DatePeriod period) throws MobilisrException {
		return service.getSmsReportData(org, from, to, period);
	}
	
	@Override
	public List<Pconfig> getReports() {
		return service.getReports();
	}

	@Override
	public String generateReport(Pconfig report) throws MobilisrException{
		return service.generateReport(report);
	}
	
	@Override
	public List<FilledPconfig> getGeneratedReports(String name, boolean showAll) {
		return service.getGeneratedReports(name, showAll);
	}
	
	@Override
	public void deleteGeneratedReport(String reportId){
		service.deleteGeneratedReport(reportId);
	}
	
	@Override
	public void refreshCache(){
		service.refreshCache();
	}
	
	@Override
	public String addScheduledReport(ScheduledPconfig report) {
		return service.addScheduledReport(report);
	}
	
	@Override
	public List<ScheduledPconfig> getScheduledReports(String reportId, boolean showAll) {
		return service.getScheduledReports(reportId, showAll);
	}
	
	@Override
	public void deleteScheduledReport(String reportId){
		service.deleteScheduledReport(reportId);
	}
}
