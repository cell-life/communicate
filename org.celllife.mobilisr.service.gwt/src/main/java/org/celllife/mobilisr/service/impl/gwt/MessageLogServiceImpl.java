package org.celllife.mobilisr.service.impl.gwt;

import java.util.List;

import javax.servlet.ServletException;

import org.celllife.mobilisr.constants.SmsStatus;
import org.celllife.mobilisr.domain.MobilisrEntity;
import org.celllife.mobilisr.domain.SmsLog;
import org.celllife.mobilisr.service.gwt.MessageLogService;

import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import com.extjs.gxt.ui.client.data.PagingLoadResult;

public class MessageLogServiceImpl extends AbstractMobilisrService implements MessageLogService {

	private static final long serialVersionUID = 6596699354608243301L;
	private org.celllife.mobilisr.service.MessageLogService service;

	@Override
	public void init() throws ServletException {
		super.init();
		service = (org.celllife.mobilisr.service.MessageLogService) getBean("messageLogService");
	}

	@Override
	public PagingLoadResult<SmsLog> getMessageLogsForEntity(
			MobilisrEntity entity, PagingLoadConfig loadConfig) {
		return service.getMessageLogsForEntity(entity, loadConfig);
	}

	@Override
	public PagingLoadResult<SmsLog> getMessageLogsByStatus(SmsStatus[] statuses,
			PagingLoadConfig loadConfig) {
		return service.getMessageLogsByStatus(statuses, loadConfig);
	}

	@Override
	public void reprocessMessages(List<Long> logEntryIds) {
		service.reprocessMessages(logEntryIds);
	}

	@Override
	public void voidMessages(List<Long> logEntryIds) {
		service.voidMessages(logEntryIds);
	}

}
