package org.celllife.mobilisr.service.impl;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import liquibase.csv.opencsv.CSVWriter;

import org.apache.commons.lang.StringUtils;
import org.celllife.mobilisr.constants.CampaignType;
import org.celllife.mobilisr.domain.*;
import org.celllife.mobilisr.service.CampaignService;
import org.celllife.mobilisr.service.ContactsService;
import org.celllife.mobilisr.service.ExportService;
import org.celllife.mobilisr.service.MessageLogService;
import org.celllife.mobilisr.service.OrganizationService;
import org.celllife.mobilisr.service.exception.DataexportException;
import org.celllife.mobilisr.service.utility.ServiceUtil.FilterType;
import org.celllife.mobilisr.util.CommunicateHome;
import org.celllife.mobilisr.utilbean.ContactExportSummary;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.extjs.gxt.ui.client.data.BaseFilterPagingLoadConfig;
import com.extjs.gxt.ui.client.data.BasePagingLoadConfig;
import com.extjs.gxt.ui.client.data.FilterConfig;
import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import com.extjs.gxt.ui.client.data.PagingLoadResult;

@Service("ExportService")
public class ExportServiceImpl implements ExportService {

	protected static final int PAGE_SIZE = 200;

	private SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");

	@Autowired
	private CampaignService campaignService;

	@Autowired
	private ContactsService contactService;

	@Autowired
	private OrganizationService organizationService;

	@Autowired
	private MessageLogService logService;

	@Override
	public String exportCampaignMessages(Long campaignId) throws DataexportException {
		final Campaign campaign = campaignService.getCampaign(campaignId);
		if (campaign == null) {
			throw new DataexportException("Campaign with id " + campaignId + " not found.");
		}

		String safeName = campaign.getName().replaceAll("\\W+", "_");
		String filename = "Campaign-" + safeName + "-messages.csv";

		EntityExporter<CampaignMessage> exporter = new EntityExporter<CampaignMessage>() {
			SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");

			public String[] transform(CampaignMessage message) {
				return new String[] { message.getMessage(),
						String.valueOf(message.getMsgDay()),
						sdf.format(message.getMsgTime()) };
			}
		};
		exporter.setColumnHeaders( new String[] { "Message text", "Message day",
		"Message time" });
		exporter.setEntities(campaignService.findCampMessageByCampaign(campaign));
		writeExportData(filename, exporter);
		return filename;
	}

	@Override
	public String exportCampaignContacts(Long campaignId) throws DataexportException {
		final Campaign campaign = campaignService.getCampaign(campaignId);
		if (campaign == null) {
			throw new DataexportException("Campaign with id " + campaignId + " not found.");		
		}

		String safeName = campaign.getName().replaceAll("\\W+", "_");
		String filename = MessageFormat.format(
				"{0}-recipients({1,date,yyyy-MM-dd}).csv", new Object[] {
						safeName, new Date() });

		EntityExporter<CampaignContact> exporter = new EntityExporter<CampaignContact>() {
			SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
			
			@Override
			public Collection<CampaignContact> getEntityBatch(int batch) {
				PagingLoadConfig loadConfig = new BasePagingLoadConfig(batch*PAGE_SIZE, PAGE_SIZE);
				PagingLoadResult<CampaignContact> result = contactService.listAllCampaignContactsForCampaign(campaign, loadConfig, true, true);
				return result.getData();
			}

			public String[] transform(CampaignContact campContact) {
				Contact contact = campContact.getContact();
				Campaign campaign = campContact.getCampaign();

				if (campaign.getType()==CampaignType.FIXED){
					return new String[] {contact.getFirstName(),
						contact.getLastName(),
						contact.getMsisdn()};
				}else if (campaign.getType()==CampaignType.DAILY){
					List<ContactMsgTime> contactMsgTimes = campContact.getContactMsgTimes();
					String[] times = new String[4];

					for (int i = 0; i< contactMsgTimes.size(); i++){
						times[i] = sdf.format(contactMsgTimes.get(i).getMsgTime());
					}
					return new String[] {contact.getFirstName(),
							contact.getLastName(),
							contact.getMsisdn(),
							dateFormat.format(campContact.getJoiningDate()),
							campContact.getEndDate() == null ? "" : dateFormat.format(campContact.getEndDate()),
							String.valueOf(campContact.getProgress()) + " of "
									+ String.valueOf(campaign.getDuration()),
							((times[0]==null)?"":times[0]),
							((times[1]==null)?"":times[1]),
							((times[2]==null)?"":times[2]),
							((times[3]==null)?"":times[3])
					};
				}else{ // assume FLEXI
					return new String[] {contact.getFirstName(),
							contact.getLastName(),
							contact.getMsisdn(),
							dateFormat.format(campContact.getJoiningDate()),
							campContact.getEndDate() == null ? "" : dateFormat.format(campContact.getEndDate()),
							String.valueOf(campContact.getProgress()) + " of "
									+ String.valueOf(campaign.getDuration())
							};

				}
			}
			@Override
			public void writeHeading(CSVWriter csvWriter) {

				csvWriter.writeNext(new String[]{"Organisation: " +campaign.getOrganization().getName()});
				csvWriter.writeNext(new String[]{"Campaign Name: " + campaign.getName()});
				Date date = new Date();
				csvWriter.writeNext(new String[]{"Recipients exported on " + dateFormat.format(date) + " at " + sdf.format(date)});
				if (campaign.getType()== CampaignType.FIXED){
					csvWriter.writeNext(new String[]{"Date Message Sent: " + dateFormat.format(campaign.getStartDate()) + " at " + sdf.format(campaign.getStartDate())});
				}
				csvWriter.writeNext(new String[]{""});
			}
		};
		
		String[] headers = new String[0];
		if (campaign.getType()== CampaignType.FIXED){
			 headers = new String[] { "First Name", "Last Name", "Mobile Number"};
		}else if (campaign.getType() == CampaignType.DAILY){
			 headers = new String[] { "First Name", "Last Name", "Mobile Number", "Start Date", "End Date", "Progress", "Time 1", "Time 2", "Time 3", "Time 4"};
		}else{ // FLEXI
			 headers = new String[] { "First Name", "Last Name", "Mobile Number", "Start Date", "End Date", "Progress"};
		}
		
		exporter.setColumnHeaders(headers);
		exporter.setEntities(campaign.getCampaignContacts());
		writeExportData(filename, exporter);
		
		return filename;
	}


	@Override
	public String exportContactImportErrors(String filePath,Long jobId) throws DataexportException {

		List<Contact> errorContactList = contactService.generateCsvErrorFile(
				filePath, jobId);

		DateTime tNow = new DateTime();
		String filename = "ContactImportErrors" + tNow.toString("yyyy-MM-dd") + ".csv";

		EntityExporter<Contact> exporter = getContactsExporter();
		exporter.setEntities(errorContactList);
		exporter.setErrorMessage("These contacts were not imported because their mobile number are invalid.");

		writeExportData(filename, exporter);
		
		return filename;
	}

	@Override
	public String exportContacts(Long orgId) throws DataexportException {

		final Organization organization = organizationService.findOrganization(orgId);

		if (organization == null) {
			throw new DataexportException("Organisation with id " + orgId + " not found.");
		}

		String safeName = organization.getName().replaceAll("\\W+", "_");
		DateTime tNow = new DateTime();
		String fileName = safeName + "-Contacts_Export_" + tNow.toString("yyyy-MM-dd") + ".csv";

		EntityExporter<ContactExportSummary> exporter = getContactsSummaryExporter();
		exporter.setBatcher(new Batcher<ContactExportSummary>(){
			@Override
			public Collection<ContactExportSummary> getBatch(int batch) {
				List<ContactExportSummary> result = contactService.listAllExportContactsForOrganization(organization, batch, PAGE_SIZE);
				return result;
			}
		});

		writeExportData(fileName, exporter);
		
		return fileName;
	}

    @Override
    public String exportContactGroup(Long contactGroupId) throws DataexportException {

        final ContactGroup contactGroup = contactService.getContactGroup(contactGroupId);

        DateTime tNow = new DateTime();
        String fileName = contactGroup.getGroupName().replaceAll("\\W+", "_") + "-Contacts_Export_" + tNow.toString("yyyy-MM-dd") + ".csv";

        EntityExporter<ContactExportSummary> exporter = getContactsSummaryExporter();
        exporter.setBatcher(new Batcher<ContactExportSummary>(){
            @Override
            public Collection<ContactExportSummary> getBatch(int batch) {
                List<ContactExportSummary> result = contactService.listAllExportContactsForGroup(contactGroup, batch, PAGE_SIZE);
                return result;
            }
        });

        writeExportData(fileName, exporter);

        return fileName;
    }

	@Override
	public String exportMessageLogs(final MobilisrEntity entity, final PagingLoadConfig loadConfig) throws DataexportException {

		String filename = entity.getClass().getSimpleName() + "-" + entity.getId() + "-messageLogs.csv";

		EntityExporter<SmsLog> exporter = new EntityExporter<SmsLog>() {
			SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy hh:mm:ss");
			@Override
			public String[] transform(SmsLog o) {
				return new String[] { sdf.format(o.getDatetime()), o.getMsisdn(),
						o.getStatus().getText(), o.getMessage() };
			}
			
			@Override
			public void writeHeading(CSVWriter csvWriter) {
				writeHeadingForLoadConfig(loadConfig, csvWriter);
			}
		};
		
		exporter.setColumnHeaders(new String[] { "Date Received", "Mobile Number",
				"Message Status", "Message" });
		exporter.setBatcher(new Batcher<SmsLog>() {
			@Override
			public Collection<SmsLog> getBatch(int batch) {
				loadConfig.setOffset(batch*PAGE_SIZE);
				loadConfig.setLimit(PAGE_SIZE);
				PagingLoadResult<SmsLog> result = logService.getMessageLogsForEntity(entity, loadConfig);
				return result.getData();
			}
		});

		writeExportData(filename, exporter);
		
		return filename;
	}

	private EntityExporter<Contact> getContactsExporter() {

		EntityExporter<Contact> exporter = new EntityExporter<Contact>() {
			public String[] transform(Contact contact) {
				return new String[] { contact.getLastName(),
						contact.getFirstName(), contact.getMsisdn() };
			}
		};

		exporter.setColumnHeaders( new String[] { "Last Name", "First Name",
				"Mobile Number" });
		return exporter;
	}
	
	private EntityExporter<ContactExportSummary> getContactsSummaryExporter() {

		EntityExporter<ContactExportSummary> exporter = new EntityExporter<ContactExportSummary>() {
			public String[] transform(ContactExportSummary contact) {
				return new String[] { contact.getLastName(),
						contact.getFirstName(), contact.getMsisdn(), contact.getCampaigns() };
			}
		};
		
		exporter.setColumnHeaders(new String[] { "Last Name", "First Name",
				"Mobile Number", "Campaigns" });

		return exporter;
	}

	/**
	 * Write the export data to the response
	 * 
	 * @param filename
	 *            the name of the file to export the data to
	 * @param exporter
	 *            an implementation of EntityExporter that converts each object
	 *            in exportObjects into a String array
	 * @throws IOException
	 * @throws DataexportException
	 */
	private <T> void writeExportData(String filename,
			EntityExporter<T> exporter) throws DataexportException {
		FileWriter writer = null;
		try {

			File downloadFolder = CommunicateHome.getDownloadsFolder();
			String path = downloadFolder.getAbsolutePath() + File.separator + filename;
			
			writer = new FileWriter(path);
			CSVWriter csvWriter = new CSVWriter(writer);
			exporter.writeHeading(csvWriter);
			csvWriter.writeNext(exporter.getColumnHeaders());
			String errorMessage = exporter.getErrorMessage();
			if ( (errorMessage != null) && !(errorMessage.isEmpty()) ) {
				csvWriter.writeNext(new String[]{errorMessage});
			}

			int batch = 0;
			Collection<T> entityBatch = null;
			do {
				entityBatch = exporter.getEntityBatch(batch);
				if (entityBatch != null && !entityBatch.isEmpty()){
					for (T object : entityBatch) {
						String[] values = exporter.transform(object);
						csvWriter.writeNext(values);
					}
				}
				batch++;
			} while (entityBatch != null && !entityBatch.isEmpty());

			csvWriter.flush();
			csvWriter.close();
			writer.close();
		} catch (IOException e) {
			throw new DataexportException("Error while exporting", e);
		} finally {
			if (writer != null){
				try { writer.close(); } catch (IOException ignore) {}
			}
		}
	}
	
	private void writeHeadingForLoadConfig(final PagingLoadConfig loadConfig,
			CSVWriter csvWriter) {
		if (loadConfig instanceof BaseFilterPagingLoadConfig){
			List<FilterConfig> filterConfigs = ((BaseFilterPagingLoadConfig)loadConfig).getFilterConfigs();
			for (FilterConfig filterConfig : filterConfigs) {
				String[] row = getRowForFilterConfig(filterConfig);
				if (row != null){
					csvWriter.writeNext(row);
				}
			}
			
			if (filterConfigs.size() > 0)
				csvWriter.writeNext(new String[]{""});
		}
	}

	private String[] getRowForFilterConfig(FilterConfig filterConfig) {
		String typeString = filterConfig.getType();
		String field = filterConfig.getField();
		String comparison = filterConfig.getComparison();
		Object value = filterConfig.getValue();
		
		if (value == null || field == null || field.isEmpty()) {
			return null;
		}

		FilterType type = FilterType.valueByType(typeString);
		
		switch (type){
		case STRING:
		case BOOLEAN:
			return new String[] {field, String.valueOf(value)};
		case NUMBERIC:
			String comparisonOp = comparison == null ? "eq" : comparison;
			return new String[] { field + " (" + comparisonOp + ")",
					String.valueOf(value) };
		case DATE:
			String comparisonOp1 = comparison == null ? "on" : comparison;
			return new String[] { field + " (" + comparisonOp1 + ")",
					dateFormat.format((Date) value) };
		case LIST:
			@SuppressWarnings("unchecked")
			List<String> list = (List<String>) value;
			if (list.isEmpty()){
				return null;
			}
			
			String join = StringUtils.join(list, ", ");
			return new String[] {field, join};
		}
		
		return null;
	}

	void setCampaignService(CampaignService campaignService) {
		this.campaignService = campaignService;
	}

	void setContactService(ContactsService contactService) {
		this.contactService = contactService;
	}
}
