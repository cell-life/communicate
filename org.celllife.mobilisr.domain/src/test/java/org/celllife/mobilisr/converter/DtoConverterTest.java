package org.celllife.mobilisr.converter;

import java.util.ArrayList;
import java.util.List;

import org.celllife.mobilisr.api.mock.DtoMockFactory;
import org.celllife.mobilisr.api.rest.CampaignDto;
import org.celllife.mobilisr.constants.ApiVersion;
import org.celllife.mobilisr.constants.CampaignStatus;
import org.celllife.mobilisr.constants.CampaignType;
import org.celllife.mobilisr.domain.Campaign;
import org.junit.Assert;
import org.junit.Test;


public class DtoConverterTest {
	
	@Test
	public void testToDto(){
		Campaign campaign = getCampaign(1);
		CampaignDto dto = DtoConverterFactory.getInstance().toDto(campaign, CampaignDto.class, ApiVersion.getLatest());
		Assert.assertEquals(campaign.getName(), dto.getName());
		Assert.assertEquals(new Integer(campaign.getCost()), dto.getCost());
	}

	@Test
	public void testToDto_null(){
		Campaign campaign = null;
		CampaignDto dto = DtoConverterFactory.getInstance().toDto(campaign, CampaignDto.class, ApiVersion.getLatest());
		Assert.assertNull(dto);
	}
	
	@Test
	public void testToDtoList(){
		List<Campaign> camps = new ArrayList<Campaign>();
		camps.add(getCampaign(1));
		camps.add(getCampaign(2));
		
		List<CampaignDto> dtos = DtoConverterFactory.getInstance().toDto(camps, CampaignDto.class, ApiVersion.getLatest());
		
		Assert.assertEquals(camps.size(), dtos.size());
		
		for (int i = 0; i < camps.size(); i++) {
			Assert.assertEquals(camps.get(i).getName(), dtos.get(i).getName());
			Assert.assertEquals(new Integer(camps.get(i).getCost()), dtos.get(i).getCost());
		}
	}
	
	@Test
	public void testToDtoList_empty(){
		List<Campaign> camps = new ArrayList<Campaign>();
		List<CampaignDto> dtos = DtoConverterFactory.getInstance().toDto(camps, CampaignDto.class, ApiVersion.getLatest());
		Assert.assertEquals(0, dtos.size());
	}
	
	@Test
	public void testFromDto(){
		CampaignDto dto = DtoMockFactory._().on(CampaignDto.class).withMode(DtoMockFactory.MODE_POST).create();
		Campaign campaign = DtoConverterFactory.getInstance().fromDto(dto, Campaign.class, ApiVersion.getLatest());
		Assert.assertEquals(campaign.getName(), dto.getName());
	}

	@Test
	public void testFromDtoList(){
		List<CampaignDto> dtos = DtoMockFactory._().on(CampaignDto.class).withMode(DtoMockFactory.MODE_POST).create(10);
		
		List<Campaign> campaigns = DtoConverterFactory.getInstance().fromDto(dtos, Campaign.class, ApiVersion.getLatest());
		
		Assert.assertEquals(dtos.size(), campaigns.size());
		
		for (int i = 0; i < dtos.size(); i++) {
			Assert.assertEquals(dtos.get(i).getName(), campaigns.get(i).getName());
		}
	}

	private Campaign getCampaign(int seed) {
		Campaign campaign = new Campaign();
		campaign.setCost(5*seed);
		campaign.setName("testname" + seed);
		campaign.setStatus(CampaignStatus.INACTIVE);
		campaign.setType(CampaignType.DAILY);
		return campaign;
	}
}
