package org.celllife.mobilisr.service;

import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import org.celllife.mobilisr.constants.SmsStatus;
import org.celllife.mobilisr.domain.MobilisrEntity;
import org.celllife.mobilisr.domain.SmsLog;

import java.util.List;

public interface MessageLogService {

	PagingLoadResult<SmsLog> getMessageLogsForEntity(MobilisrEntity entity, PagingLoadConfig loadConfig);

	PagingLoadResult<SmsLog> getMessageLogsByStatus(SmsStatus[] statuses,
			PagingLoadConfig loadConfig);

	void reprocessMessages(List<Long> logEntryIds);

	void voidMessages(List<Long> logEntryIds);

    /**
     * Gets a message log by id.
     * @param id The database id of the log.
     * @return The message log in question.
     */
    SmsLog getMessageLog(Long id);

}
