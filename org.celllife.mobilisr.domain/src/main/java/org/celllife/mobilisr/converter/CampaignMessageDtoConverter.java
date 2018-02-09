package org.celllife.mobilisr.converter;

import org.celllife.mobilisr.api.rest.MessageDto;
import org.celllife.mobilisr.constants.ApiVersion;
import org.celllife.mobilisr.domain.CampaignMessage;

public class CampaignMessageDtoConverter implements EntityDtoConverter<CampaignMessage, MessageDto> {

	@Override
	public Class<MessageDto> getDtoType() {
		return MessageDto.class;
	}

	@Override
	public Class<CampaignMessage> getEntityType() {
		return CampaignMessage.class;
	}
	
	@Override
	public MessageDto toDto(CampaignMessage contact, ApiVersion ver) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public CampaignMessage fromDto(MessageDto dto, ApiVersion ver) {
		// currently only for fixed campaign messages
		CampaignMessage message = new CampaignMessage();
		message.setMessage(dto.getText());
		message.setMsgDate(dto.getDate());
		message.setMsgTime(dto.getTime());
		message.setMsgSlot(0);
		message.setMsgDay(0);
		return message;
	}

}
