package org.celllife.mobilisr.client;

import org.celllife.mobilisr.api.rest.MessageLogDto;
import org.celllife.mobilisr.client.exception.RestCommandException;

public interface MessageLogService {

    MessageLogDto getMessageLog(Long id) throws RestCommandException;

}
