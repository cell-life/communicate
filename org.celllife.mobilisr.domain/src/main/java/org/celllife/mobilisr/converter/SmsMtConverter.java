package org.celllife.mobilisr.converter;

import java.util.Date;

import org.celllife.mobilisr.api.messaging.SmsMt;
import org.celllife.mobilisr.constants.ApiVersion;
import org.celllife.mobilisr.domain.SmsLog;

public class SmsMtConverter implements EntityDtoConverter<SmsLog, SmsMt> {

	@Override
	public Class<SmsMt> getDtoType() {
		return SmsMt.class;
	}

	@Override
	public Class<SmsLog> getEntityType() {
		return SmsLog.class;
	}
	
	@Override
	public SmsMt toDto(SmsLog contact, ApiVersion ver) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public SmsLog fromDto(SmsMt dto, ApiVersion ver) {
		SmsLog smsLog = new SmsLog();
		smsLog.setDatetime(new Date());
		smsLog.setMsisdn(dto.getMsisdn());
		smsLog.setMobileNetwork(dto.getMobileNetwork());
		smsLog.setMessage(dto.getMessage());
		smsLog.setCreatedfor(dto.getCreatedFor());
		smsLog.setDir(SmsLog.SMS_DIR_OUT);
		smsLog.setStatus(dto.getStatus());
		smsLog.setTrackingnumber(dto.getMessageTrackingNumber());
		smsLog.setFailreason(dto.getErrorMessage());
		Integer sendingAttempts  = dto.getSendingAttempts();
		smsLog.setAttempts(sendingAttempts == null ? 0 : sendingAttempts);
		return smsLog;
	}

}
