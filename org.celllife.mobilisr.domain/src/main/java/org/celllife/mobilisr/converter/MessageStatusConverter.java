package org.celllife.mobilisr.converter;

import org.celllife.mobilisr.api.rest.MessageStatusDto;
import org.celllife.mobilisr.constants.ApiVersion;
import org.celllife.mobilisr.domain.SmsLog;

public class MessageStatusConverter implements EntityDtoConverter<SmsLog, MessageStatusDto> {

	@Override
	public Class<MessageStatusDto> getDtoType() {
		return MessageStatusDto.class;
	}

	@Override
	public Class<SmsLog> getEntityType() {
		return SmsLog.class;
	}
	
	@Override
	public MessageStatusDto toDto(SmsLog smslog, ApiVersion ver) {
		MessageStatusDto smsLog = new MessageStatusDto();
		smsLog.setId(smslog.getId());
		smsLog.setDatetime(smslog.getDatetime());
		smsLog.setMsisdn(smslog.getMsisdn());
		smsLog.setStatus(smslog.getStatus());
		smsLog.setFailreason(smslog.getFailreason());
		smsLog.setMessage(smslog.getMessage());
		return smsLog;
	}
	
	@Override
	public SmsLog fromDto(MessageStatusDto dto, ApiVersion ver) {
		throw new UnsupportedOperationException();
	}

}
