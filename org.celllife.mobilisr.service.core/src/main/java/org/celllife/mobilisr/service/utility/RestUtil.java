package org.celllife.mobilisr.service.utility;

import java.util.List;

import org.celllife.mobilisr.api.MobilisrDto;
import org.celllife.mobilisr.api.rest.PagedListDto;
import org.celllife.mobilisr.constants.ApiVersion;
import org.celllife.mobilisr.converter.DtoConverterFactory;
import org.celllife.mobilisr.converter.EntityDtoConverterFactory;
import org.celllife.mobilisr.domain.MobilisrEntity;

import com.trg.search.Search;
import com.trg.search.SearchResult;

public class RestUtil {
	
	public static <T extends MobilisrDto> PagedListDto<T> getPagedList(Class<T> clazz, Search search,
			SearchResult<? extends MobilisrEntity> searchResult, ApiVersion ver) {
		int offset = search.getFirstResult();
		int limit = search.getMaxResults();
		int total = searchResult.getTotalCount();
		PagedListDto<T> listDto = new PagedListDto<T>(offset == -1 ? 0 : offset,
				limit == -1 ? searchResult.getTotalCount() : limit,
				total);
		
		EntityDtoConverterFactory factory = DtoConverterFactory.getInstance();
		List<T> dtos = factory.toDto(searchResult.getResult(), clazz, ver);
		listDto.setElements(dtos);
		return listDto;
	}

}
