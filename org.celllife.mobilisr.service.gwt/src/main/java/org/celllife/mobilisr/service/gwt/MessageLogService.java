package org.celllife.mobilisr.service.gwt;

import java.util.List;

import org.celllife.mobilisr.constants.SmsStatus;
import org.celllife.mobilisr.domain.MobilisrEntity;
import org.celllife.mobilisr.domain.SmsLog;
import org.celllife.mobilisr.exception.MobilisrException;
import org.celllife.mobilisr.exception.MobilisrRuntimeException;

import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * @see org.celllife.mobilisr.service.MessageLogService
 */
@RemoteServiceRelativePath("logService.rpc")
public interface MessageLogService extends RemoteService {

	/**
	 * @see org.celllife.mobilisr.service.MessageLogService#getMessageLogsForEntity(MobilisrEntity, PagingLoadConfig)
	 */
	PagingLoadResult<SmsLog> getMessageLogsForEntity(MobilisrEntity entity,
			PagingLoadConfig loadConfig) throws MobilisrException, MobilisrRuntimeException;

	/**
	 * @see org.celllife.mobilisr.service.MessageLogService#getMessageLogsByStatus(SmsStatus[], PagingLoadConfig)
	 */
	PagingLoadResult<SmsLog> getMessageLogsByStatus(SmsStatus[] statuses,
			PagingLoadConfig loadConfig) throws MobilisrException, MobilisrRuntimeException;

	/**
	 * @see org.celllife.mobilisr.service.MessageLogService#reprocessMessages(List)
	 */
	void reprocessMessages(List<Long> logEntryIds);

	/**
	 * @see org.celllife.mobilisr.service.MessageLogService#voidMessages(List)
	 */
	void voidMessages(List<Long> logEntryIds);

}
