package org.celllife.mobilisr.service;

import org.celllife.mobilisr.domain.MobilisrEntity;
import org.celllife.mobilisr.service.exception.DataexportException;

import com.extjs.gxt.ui.client.data.PagingLoadConfig;

public interface ExportService {

	public String exportCampaignMessages(Long campaignId)
			throws DataexportException;

	public String exportCampaignContacts(Long campaignId)
			throws DataexportException;

	public String exportContactImportErrors(String filePath, Long jobId)
			throws DataexportException;

	public String exportContacts(Long orgId) throws DataexportException;

	String exportMessageLogs(MobilisrEntity entity, PagingLoadConfig loadConfig)
			throws DataexportException;

    public String exportContactGroup(Long contactGroupId) throws DataexportException;

}