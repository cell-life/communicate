package org.celllife.mobilisr.test;

import java.util.List;

import org.mockito.ArgumentMatcher;

import com.trg.search.Filter;
import com.trg.search.Search;

public class SearchWithFilterMatcher extends ArgumentMatcher<Search> {
	

	private final String filterProp;
	private final Object filterValue;

	public SearchWithFilterMatcher(String filterProp, Object filterValue) {
		this.filterProp = filterProp;
		this.filterValue = filterValue;
	}
	
	@Override
	public boolean matches(Object argument) {
		List<Filter> filters = ((Search) argument).getFilters();
		for (Filter filter : filters) {
			if (filter.getProperty().equals(filterProp) 
					&& filter.getValue().equals(filterValue)){
				return true;
			}
		}
		return false;
	}
}