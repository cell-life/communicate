package org.celllife.mobilisr.service.impl;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.celllife.mobilisr.annotation.LogLevel;
import org.celllife.mobilisr.annotation.Loggable;
import org.celllife.mobilisr.constants.DatePeriod;
import org.celllife.mobilisr.dao.api.SmsLogDAO;
import org.celllife.mobilisr.domain.MobilisrEntity;
import org.celllife.mobilisr.domain.MobilisrPermission;
import org.celllife.mobilisr.domain.Organization;
import org.celllife.mobilisr.domain.User;
import org.celllife.mobilisr.exception.MobilisrRuntimeException;
import org.celllife.mobilisr.service.MailService;
import org.celllife.mobilisr.service.ReportService;
import org.celllife.mobilisr.service.TemplateService;
import org.celllife.mobilisr.service.UserService;
import org.celllife.mobilisr.service.constants.Templates;
import org.celllife.mobilisr.service.exception.MobilisrSecurityException;
import org.celllife.mobilisr.service.gwt.ServiceAndUIConstants;
import org.celllife.mobilisr.service.gwt.SmsReportData;
import org.celllife.mobilisr.service.qrtz.QuartzService;
import org.celllife.mobilisr.service.utility.MapBuilder;
import org.celllife.mobilisr.service.utility.MobilisrUtility;
import org.celllife.mobilisr.util.LogUtil;
import org.celllife.mobilisr.utilbean.LogSummary;
import org.celllife.pconfig.model.EntityParameter;
import org.celllife.pconfig.model.FileType;
import org.celllife.pconfig.model.FilledPconfig;
import org.celllife.pconfig.model.Parameter;
import org.celllife.pconfig.model.Pconfig;
import org.celllife.pconfig.model.ScheduledPconfig;
import org.celllife.reporting.ReportingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;

@Service("reportService")
public class ReportServiceImpl implements ReportService {
	
	private static final double MAX_LABELS = 12d;

	private static final long serialVersionUID = -3242765737178376409L;

	private static Logger log = LoggerFactory
			.getLogger(ReportServiceImpl.class);

	@Autowired
	private TemplateService templateService;
	
	@Autowired
	private SmsLogDAO smsLogDao;
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private org.celllife.reporting.service.ReportService staticReportService;
	
	@Autowired
	private QuartzService quartzService;
	
	@Autowired
	private MailService mailService;
	
	@Loggable(LogLevel.TRACE)
	@Override
	public List<SmsReportData> getDailySmsReportData(Organization org,
			Integer reportDays) {
		Calendar cal = Calendar.getInstance();
		// include today in reporting
		cal.add(Calendar.DATE, -reportDays + 1);
		
		List<SmsReportData> reportData = getSmsReportData(org, cal.getTime(),
				new Date(), DatePeriod.DAY);
		return reportData;
	}

	@Loggable(LogLevel.TRACE)
	@Override
	public List<SmsReportData> getMonthlySmsReportData(Organization org,
			Integer reportMonths) {
		Calendar cal = Calendar.getInstance();
		// include today in reporting
		cal.add(Calendar.MONTH, -reportMonths + 1);
		
		List<SmsReportData> reportData = getSmsReportData(org, cal.getTime(),
				new Date(), DatePeriod.MONTH);
		return reportData;
	}
	
	@Loggable(LogLevel.TRACE)
	@Override
	public List<SmsReportData> getYearlySmsReportData(Organization org,
			Integer reportYears) {
		Calendar cal = Calendar.getInstance();
		// include today in reporting
		cal.add(Calendar.YEAR, -reportYears + 1);
		
		List<SmsReportData> reportData = getSmsReportData(org, cal.getTime(),
				new Date(), DatePeriod.YEAR);
		return reportData;
	}
	
	@Loggable(LogLevel.TRACE)
	@Override
	public List<SmsReportData> getSmsReportData(Organization org,
			Date from, Date to, DatePeriod period) {
		
		if (from == null){
			Calendar cal = Calendar.getInstance();
			switch (period){
			case MONTH:
			case YEAR:
				cal.add(period.getCalendarField(), -1);
				break;
			case DAY:
				cal.add(Calendar.MONTH, -1);
			}
			from = cal.getTime();
		}
		
		if (to == null){
			to = new Date();
		}
		
		from = MobilisrUtility.getBeginningOfPeriod(period, from);
		to = MobilisrUtility.getEndOfPeriod(period, to);

		List<LogSummary> logs = smsLogDao.getSmsLogs(org, from, to, period);
		List<SmsReportData> reportData = addMissingData(from, to, logs,
				period);

		return reportData;
	}

	/**
	 * This method fills in zero's where data is missing. e.g. if no sms's were
	 * sent during one of the periods then the database query will return no data.
	 * This method then inserts a '0' in that position.
	 * 
	 * @param from
	 *            start date of the reporting period
	 * @param to
	 *            end date of the reporting period
	 * @param logs
	 *            a list of the LogSummary objects returned from the database
	 * @param period
	 *            the group by enum (DAY, MONTH, YEAR)
	 * @return a list of SmsReportData with the blanks filled in
	 */
	private List<SmsReportData> addMissingData(Date from,
			Date to, List<LogSummary> logs, DatePeriod period) {
		List<SmsReportData> reportData = new ArrayList<SmsReportData>();
		MobilisrUtility.zeroTime(to);
		int skipped = 0;
		
		Calendar cal = Calendar.getInstance();
		cal.setTime(from);
		
		for (int i = 0; cal.getTime().before(to); i++) {

			LogSummary lpd = null;
			if (logs.size() > (i - skipped)) {
				lpd = logs.get(i - skipped);
			}

			Date date = cal.getTime();
			if (lpd == null || !lpd.getDate().equals(date)) {
				log.debug("no report data for {}", date);
				lpd = new LogSummary();
				lpd.setNumberOfFailuers(0);
				lpd.setNumberOfMessages(0);

				// NB: intentional case fall through
				switch (period) {
				case DAY:
					lpd.setDay(cal.get(Calendar.DAY_OF_MONTH));
				case MONTH:
					lpd.setMonth(cal.get(Calendar.MONTH) + 1);
				case YEAR:
					lpd.setYear(cal.get(Calendar.YEAR));
				}

				skipped++;
			}

			reportData.add(new SmsReportData(lpd.getLabel(), lpd
					.getNumberOfMessages().longValue(), lpd
					.getNumberOfFailures().longValue()));
			cal.add(period.getCalendarField(), 1);
		}
		
		if (reportData.size() > MAX_LABELS+2){
			int spacing = Double.valueOf(Math.ceil(reportData.size()/MAX_LABELS)).intValue();
			for (int i = 1; i < reportData.size()-1; i++) {
				if (i%spacing != 0) {
					reportData.get(i).clearLabel();
				}
			}
		}
		return reportData;
	}
	
	@Override
	public String generateReport(Pconfig report) {
		try {
			Organization organisation = userService.getCurrentLoggedInUser().getOrganization();
			Long orgId = organisation.getId();
			report.addProperty(ServiceAndUIConstants.PROP_REPORT_ORGANISATION_ID, orgId.toString());
			report.addProperty(ServiceAndUIConstants.PROP_REPORT_ORGANISATION_NAME, organisation.getName());
			
			// auto-fill parameters
			List<? extends Parameter<?>> parameters = report.getParameters();
			for (Parameter<?> parameter : parameters) {
				if (parameter instanceof EntityParameter){
					EntityParameter eparam = (EntityParameter) parameter;
					if (eparam.isAutofill()){
						String entityClass = eparam.getEntityClass();
						if (entityClass.equals(Organization.class.getName())){
							Organization organization = userService.getCurrentLoggedInUser().getOrganization();
							autoFillParameter(eparam, organization);
						} else if (entityClass.equals(User.class.getName())){
							User user = userService.getCurrentLoggedInUser();
							autoFillParameter(eparam, user);
						}
					}
				}
			}
			
			return staticReportService.generateReport(report);
		} catch (ReportingException e) {
			throw new MobilisrRuntimeException(e);
		}
	}
	
	private void autoFillParameter(EntityParameter param,
			MobilisrEntity entity) {
		String valueProperty = param.getValueProperty();
		String displayProperty = param.getDisplayProperty();
		try {
			String value = BeanUtils.getProperty(entity, valueProperty);
			param.setValue(value.toString());
			
			String valueLabel = BeanUtils.getProperty(entity, displayProperty);
			param.setValueLabel(valueLabel.toString());
		} catch (Exception e) {
			log.error(LogUtil.getMarker_notifyAdmin(),
					"Error auto-filling report property. [report="+param.getName()+"] ", e);
		}
	}

	@Override
	public List<FilledPconfig> getGeneratedReports(String id, boolean showAll) {
		List<FilledPconfig> reports = staticReportService.getGeneratedReports(id);
	
		List<FilledPconfig> list;
		if (showAll){
			list = new ArrayList<FilledPconfig>(reports);
		} else {
			final String orgId = userService.getCurrentLoggedInUser()
				.getOrganization().getId().toString();
			
			@SuppressWarnings("unchecked")
			Collection<FilledPconfig> selection = CollectionUtils.select(reports, new Predicate() {
				@Override
				public boolean evaluate(Object object) {
							String property = ((FilledPconfig) object)
									.getProperty(ServiceAndUIConstants.PROP_REPORT_ORGANISATION_ID);
					if (property != null && orgId.equals(property)){
						return true;
					}
					
					return false;
				}
			});
			
			list = new ArrayList<FilledPconfig>(selection);
		}
		
		return list;
	}
	
	@Secured({"PERM_REPORT_SCHEDULES_VIEW", "PERM_REPORT_SCHEDULES_ADMIN_VIEW"})
	@Override
	public List<ScheduledPconfig> getScheduledReports(String reportId, boolean showAll) {
		
		List<ScheduledPconfig> reports = staticReportService.getScheduledReports(reportId);		
		List<ScheduledPconfig> list;
		
		// filter reports to exclude reports not belonging to current user's organisation
		final String orgId = userService.getCurrentLoggedInUser().getOrganization().getId().toString();
		
		if (showAll && userService.getCurrentLoggedInUser().hasPermission(MobilisrPermission.REPORT_SCHEDULES_ADMIN_VIEW)){
			list = new ArrayList<ScheduledPconfig>(reports);
		} else {			
			@SuppressWarnings("unchecked")
			Collection<ScheduledPconfig> selection = CollectionUtils.select(reports, new Predicate() {
				@Override
				public boolean evaluate(Object object) {
							String property = ((ScheduledPconfig) object)
									.getProperty(ServiceAndUIConstants.PROP_REPORT_ORGANISATION_ID);
					if (property != null && orgId.equals(property)){
						return true;
					}
					
					return false;
				}
			});
			list = new ArrayList<ScheduledPconfig>(selection);
		}
		return list;
	}
	
	@Override
	@Secured({"PERM_REPORTS_VIEW","PERM_REPORTS_ADMIN_VIEW"})
	public List<Pconfig> getReports() {
		List<Pconfig> reports = staticReportService.getReports();
		final User user = userService.getCurrentLoggedInUser();
		
		@SuppressWarnings("unchecked")
		Collection<Pconfig> selection = CollectionUtils.select(reports, new Predicate() {
			@Override
			public boolean evaluate(Object object) {
				String property = ((Pconfig) object)
						.getProperty(ServiceAndUIConstants.PROP_REPORT_PERMISSIONS);
				if (property == null || property.isEmpty() || user == null){
					return true;
				}
				
				String[] permissions = property.split(",");
				for (String permission : permissions) {
					MobilisrPermission perm = MobilisrPermission.safeValueOf(permission);
					if (perm != null && user.hasPermission(perm)){
						return true;
					}
				}
				
				return false;
			}
		});
		
		List<Pconfig> list = new ArrayList<Pconfig>(selection);
		return list;
	}
	
	@Override
	public void deleteGeneratedReport(String reportId) {
		staticReportService.deleteGeneratedReport(reportId);
	}
	
	@Secured({"PERM_REPORT_SCHEDULES_DELETE", "PERM_REPORT_SCHEDULES_ADMIN_DELETE"})
	@Override
	public void deleteScheduledReport(String reportId){
		
		final String orgId = userService.getCurrentLoggedInUser().getOrganization().getId().toString();		
		String property = (staticReportService.getScheduledReport(reportId)).getProperty(ServiceAndUIConstants.PROP_REPORT_ORGANISATION_ID);
		
		if ((property != null && orgId.equals(property)) || 
		userService.getCurrentLoggedInUser().hasPermission(MobilisrPermission.REPORT_SCHEDULES_ADMIN_DELETE)){
			quartzService.deleteTriggerForScheduledReport(reportId);
			staticReportService.deleteScheduledReport(reportId);			
		}
		else
		{
			throw new MobilisrSecurityException("You don't have the permission to delete this report.");
		}
		
	}
	
	@Override
	public void refreshCache() {
		staticReportService.refreshCache();
	}
	
	/*package private*/ void setUserService(UserService userService) {
		this.userService = userService;
	}
	
	/*package private*/ void setStaticReportService(
			org.celllife.reporting.service.ReportService staticReportService) {
		this.staticReportService = staticReportService;
	}
	
	@Override
	public String generateScheduledReport(String reportID) {

		String pdfId = "";
		
		try {
			ScheduledPconfig scheduledReport = staticReportService.getScheduledReport(reportID);
			pdfId = staticReportService.generateReport(scheduledReport.getPconfig());
			String filePath = staticReportService.getPath(pdfId, FileType.PDF);

			Map<String, Object> map = MapBuilder.stringObject().put("generatedDate", new Date())
				.put("reportName", scheduledReport.getPconfig().getLabel())
				.put("reportID", scheduledReport.getId())
				.getMap();
			String message = templateService.generateContent(map,Templates.REPORT_MESSAGE);

			File attachment = new File(filePath);
			SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy"); 
			String reportName = "Scheduled_" + scheduledReport.getPconfig().getLabel() + "_" + formatter.format(new Date());
			mailService.enqueueMail(scheduledReport.getScheduledFor(), 
					"COMMUNICATE " + scheduledReport.getRepeatInterval() + " " + scheduledReport.getPconfig().getLabel(), 
					message, 
					attachment,
					reportName.replace(" ", "_")+".pdf"); 
		}
		catch (ReportingException e) {
			throw new MobilisrRuntimeException(e);
		}
		return pdfId;
	}
	
	@Secured({"PERM_REPORT_SCHEDULES_CREATE", "REPORT_SCHEDULES_ADMIN_EDIT"})
	@Override
	public String addScheduledReport(ScheduledPconfig report){	
			
		String id;
		String cronExpr = MobilisrUtility.getCronExpression(report);
		
		Organization organisation = userService.getCurrentLoggedInUser().getOrganization();
		Long orgId = organisation.getId();
		report.getPconfig().addProperty(ServiceAndUIConstants.PROP_REPORT_ORGANISATION_ID, orgId.toString());
		report.getPconfig().addProperty(ServiceAndUIConstants.PROP_REPORT_ORGANISATION_NAME, organisation.getName());
		
		try {
			id = staticReportService.saveScheduledReportConfig(report);
			report.setId(id);		
			quartzService.createTriggerForScheduledReport(report, cronExpr);
		} catch (ReportingException e) {
			throw new MobilisrRuntimeException(e);
		}
		
		return id;
	}

}
