package org.celllife.mobilisr.api;

import java.util.List;

import org.celllife.mobilisr.api.messaging.SmsMo;
import org.celllife.mobilisr.api.messaging.SmsMt;
import org.celllife.mobilisr.api.mock.DtoMockFactory;
import org.celllife.mobilisr.api.rest.CampaignDto;
import org.celllife.mobilisr.api.rest.ContactDto;
import org.celllife.mobilisr.api.rest.ErrorDto;
import org.celllife.mobilisr.api.rest.MessageDto;
import org.celllife.mobilisr.api.rest.MessageStatusDto;
import org.celllife.mobilisr.api.rest.PagedListDto;
import org.junit.Ignore;
import org.junit.Test;

public class DtoMarshallingTest extends MarshallingTest {

	@Test
	public void testCampaignDto() {
		testMarshalling(CampaignDto.class);
	}

	@Test
	public void testMessageDto() {
		testMarshalling(MessageDto.class);
	}

	@Test
	public void testMessageSatusDto() {
		testMarshalling(MessageStatusDto.class);
	}

	@Test
	public void testContactDto() {
		testMarshalling(ContactDto.class);
	}

	@Test
	public void testErrorDto() {
		testMarshalling(ErrorDto.class);
	}
	
	@Test
	@Ignore("Not using xml at the present")
	public void testSmsMT() {
		testMarshalling(SmsMt.class);
	}
	
	@Test
	@Ignore("Not using xml at the present")
	public void testSmsMO() {
		testMarshalling(SmsMo.class);
	}

	@Test
	public void testCreateCampaign() {
		CampaignDto camp = DtoMockFactory._().on(CampaignDto.class)
				.withMode(DtoMockFactory.MODE_POST).create();
		testMarshalling(camp);
	}

	@Test
	public void testPagedList() {
		testPagedList(CampaignDto.class);
		testPagedList(ContactDto.class);
		testPagedList(ErrorDto.class);
		testPagedList(MessageDto.class);
		testPagedList(MessageStatusDto.class);
	}
	
	private void testPagedList(Class<? extends MobilisrDto> clazz) {
		testPagedList(clazz, DtoMockFactory.MODE_GET);
		testPagedList(clazz, DtoMockFactory.MODE_POST);
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void testPagedList(Class<? extends MobilisrDto> clazz, int mode) {
		List<? extends MobilisrDto> camp = DtoMockFactory._().on(clazz).withMode(mode).create(10);
		PagedListDto pagedListDto = new PagedListDto(0, 10, 10);
		pagedListDto.setElements(camp);
		testMarshalling(pagedListDto);
	}

}