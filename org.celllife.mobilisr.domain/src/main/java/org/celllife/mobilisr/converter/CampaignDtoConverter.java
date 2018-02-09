package org.celllife.mobilisr.converter;

import org.celllife.mobilisr.api.rest.CampaignDto;
import org.celllife.mobilisr.constants.ApiVersion;
import org.celllife.mobilisr.constants.CampaignStatus;
import org.celllife.mobilisr.constants.CampaignType;
import org.celllife.mobilisr.domain.Campaign;

public class CampaignDtoConverter implements
		EntityDtoConverter<Campaign, CampaignDto> {

	@Override
	public CampaignDto toDto(Campaign campaign, ApiVersion ver) {
		CampaignDto dto = new CampaignDto();
		dto.setId(campaign.getId());
		dto.setName(campaign.getName());
		dto.setStatus(campaign.getStatus().apiName(ver));
		dto.setStartDate(campaign.getStartDate());
		dto.setCost(campaign.getCost());
		dto.setDescription(campaign.getDescription());
		if (campaign.getTimesPerDay() > 0) {
			dto.setTimesPerDay(campaign.getTimesPerDay());
		}
		if (campaign.getDuration() > 0){
			dto.setDuration(campaign.getDuration());
		}
		dto.setType(campaign.getType().apiName(ver));
		return dto;
	}

	@Override
	public Campaign fromDto(CampaignDto dto, ApiVersion ver) {
		Campaign campaign = new Campaign();
		campaign.setId(dto.getId());
		campaign.setName(dto.getName());
		
		String status = dto.getStatus();
		if (status != null && !status.isEmpty()) {
			campaign.setStatus(CampaignStatus.apiValueOf(status));
		} else {
			campaign.setStatus(CampaignStatus.INACTIVE);
		}
		
		campaign.setStartDate(dto.getStartDate());
		Integer cost = dto.getCost();
		if (cost != null) {
			campaign.setCost(cost);
		}
		campaign.setDescription(dto.getDescription());
		Integer timesPerDay = dto.getTimesPerDay();
		if (timesPerDay != null) {
			campaign.setTimesPerDay(timesPerDay);
		}
		Integer duration = dto.getDuration();
		if (duration != null) {
			campaign.setDuration(duration);
		}
		String type = dto.getType();
		if (type != null && !type.isEmpty()) {
			campaign.setType(CampaignType.apiValueOf(type));
		} else {
			campaign.setType(CampaignType.FIXED);
		}
		return campaign;
	}

	@Override
	public Class<CampaignDto> getDtoType() {
		return CampaignDto.class;
	}

	@Override
	public Class<Campaign> getEntityType() {
		return Campaign.class;
	}

}
