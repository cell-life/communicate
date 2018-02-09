package org.celllife.mobilisr.rest;

import junit.framework.Assert;

import org.celllife.mobilisr.api.rest.CampaignDto;
import org.celllife.mobilisr.api.rest.ContactDto;
import org.celllife.mobilisr.api.rest.ErrorDto;
import org.celllife.mobilisr.api.rest.PagedListDto;
import org.celllife.mobilisr.constants.ErrorCode;
import org.junit.Test;

public class RestUtilTest {
	
	@Test
	public void testCheckPageListSizeAndType_EmptyList(){
		PagedListDto<ErrorDto> errors = RestUtility.checkPageListSizeAndType(new PagedListDto<ContactDto>(), CampaignDto.class);
		Assert.assertEquals(1, errors.size());
		Assert.assertEquals(ErrorCode.EMPTY_LIST, errors.getElements().get(0).getErrorCode());
	}
	
	@Test
	public void testCheckPageListSizeAndType_IncorrectType(){
		PagedListDto<ContactDto> list = new PagedListDto<ContactDto>();
		list.addElement(new ContactDto());
		PagedListDto<ErrorDto> errors = RestUtility.checkPageListSizeAndType(list, CampaignDto.class);
		Assert.assertEquals(1, errors.size());
		Assert.assertEquals(ErrorCode.UNSUPPORTED_DATA, errors.getElements().get(0).getErrorCode());
	}

}
