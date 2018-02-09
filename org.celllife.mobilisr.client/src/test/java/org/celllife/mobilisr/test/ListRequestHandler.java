package org.celllife.mobilisr.test;

import java.util.ArrayList;
import java.util.List;

import org.celllife.mobilisr.api.MobilisrDto;
import org.celllife.mobilisr.api.mock.DtoMockFactory;
import org.celllife.mobilisr.api.rest.PagedListDto;

public class ListRequestHandler extends RestRequestHandler {

	private int returnSize = 10;

	public ListRequestHandler(String expectedUrl, Class<? extends MobilisrDto> returnClass) {
		super(expectedUrl, returnClass);
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	protected MobilisrDto getReturnObject() {
		List<? extends MobilisrDto> list = new ArrayList<MobilisrDto>();
		if (returnSize > 0){
			list = DtoMockFactory._().on(getReturnClass()).create(returnSize);
		}
		PagedListDto listDto = new PagedListDto<MobilisrDto>(0, 100, returnSize);
		listDto.setElements(list);
		return listDto;
	}

	public void setReturnSize(int returnSize) {
		this.returnSize = returnSize;
	}

}
