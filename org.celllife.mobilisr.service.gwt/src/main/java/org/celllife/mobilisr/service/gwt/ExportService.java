package org.celllife.mobilisr.service.gwt;

import org.celllife.mobilisr.domain.MobilisrEntity;
import org.celllife.mobilisr.exception.MobilisrException;
import org.celllife.mobilisr.exception.MobilisrRuntimeException;

import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("exportService.rpc")
public interface ExportService extends RemoteService {

	/**
	 * @see org.celllife.mobilisr.service.ExportService#exportCampaignMessages(Long)
	 */
	public String exportCampaignMessages(Long campaignId)
			throws MobilisrException, MobilisrRuntimeException;

	/**
	 * @see org.celllife.mobilisr.service.ExportService#exportCampaignContacts(Long)
	 */
	public String exportCampaignContacts(Long campaignId)
			throws MobilisrException, MobilisrRuntimeException;

	/**
	 * @see org.celllife.mobilisr.service.ExportService#exportContactImportErrors(String, Long)
	 */
	public String exportContactImportErrors(String filePath, Long jobId)
			throws MobilisrException, MobilisrRuntimeException;

	/**
	 * @see org.celllife.mobilisr.service.ExportService#exportContacts(Long)
	 */
	public String exportContacts(Long orgId) throws MobilisrException,
			MobilisrRuntimeException;

	/**
	 * @see org.celllife.mobilisr.service.ExportService#exportMessageLogs(MobilisrEntity, PagingLoadConfig)
	 */
	String exportMessageLogs(MobilisrEntity entity, PagingLoadConfig loadConfig)
		throws MobilisrException, MobilisrRuntimeException;

	/**
	 * @see org.celllife.mobilisr.service.ExportService#exportContactGroups(Long)
	 */
	public String exportContactGroup(Long contactGroupId) throws MobilisrException, MobilisrRuntimeException;
}