package org.celllife.mobilisr.client.impl;

import org.celllife.mobilisr.api.rest.MessageLogDto;
import org.celllife.mobilisr.api.validation.ValidatorFactory;
import org.celllife.mobilisr.client.MessageLogService;
import org.celllife.mobilisr.client.command.RestCommandFactory;
import org.celllife.mobilisr.client.exception.RestCommandException;

public class MessageLogServiceImpl extends BaseRestService implements MessageLogService {

    public MessageLogServiceImpl(RestCommandFactory factory, ValidatorFactory vfactory) {
        super(factory, vfactory);
    }

    @Override
    public MessageLogDto getMessageLog(Long id) throws RestCommandException {
        return (MessageLogDto) getCommandFactory().getCommandGet("messageLog/{id}", id).execute(MessageLogDto.class);
    }

}
