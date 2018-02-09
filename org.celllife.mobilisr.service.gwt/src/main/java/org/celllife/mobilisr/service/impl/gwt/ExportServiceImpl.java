package org.celllife.mobilisr.service.impl.gwt;

import javax.servlet.ServletException;

import org.celllife.mobilisr.domain.MobilisrEntity;
import org.celllife.mobilisr.exception.MobilisrException;
import org.celllife.mobilisr.exception.MobilisrRuntimeException;
import org.celllife.mobilisr.service.gwt.ExportService;

import com.extjs.gxt.ui.client.data.PagingLoadConfig;

public class ExportServiceImpl extends AbstractMobilisrService implements ExportService {

	private static final long serialVersionUID = 3821071199206847927L;
	private org.celllife.mobilisr.service.ExportService service;

	@Override
	public void init() throws ServletException {
		super.init();
		service = (org.celllife.mobilisr.service.ExportService) getBean("ExportService");
	}
	
	@Override
	public String exportCampaignMessages(Long campaignId)
			throws MobilisrException, MobilisrRuntimeException {
		return service.exportCampaignMessages(campaignId);
	}

	@Override
	public String exportCampaignContacts(Long campaignId)
			throws MobilisrException, MobilisrRuntimeException {
		return service.exportCampaignContacts(campaignId);
	}

	@Override
	public String exportContactImportErrors(String filePath, Long jobId)
			throws MobilisrException, MobilisrRuntimeException {
		return service.exportContactImportErrors(filePath, jobId);
	}

	@Override
	public String exportContacts(Long orgId) throws MobilisrException,
			MobilisrRuntimeException {
		return service.exportContacts(orgId);
	}

	@Override
	public String exportContactGroup(Long contactGroupId) throws MobilisrException,
			MobilisrRuntimeException {
		return service.exportContactGroup(contactGroupId);
	}

	@Override
	public String exportMessageLogs(MobilisrEntity entity, PagingLoadConfig loadConfig)
			throws MobilisrException, MobilisrRuntimeException {
		return service.exportMessageLogs(entity, loadConfig);
	}

}
