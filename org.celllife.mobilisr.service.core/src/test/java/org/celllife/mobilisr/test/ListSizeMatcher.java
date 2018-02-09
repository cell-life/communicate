package org.celllife.mobilisr.test;

import java.util.List;

import org.mockito.ArgumentMatcher;

public class ListSizeMatcher<T> extends ArgumentMatcher<List<T>> {
	
	private int size;

	public ListSizeMatcher(int size) {
		this.size = size;
	}
	
	@Override
	public boolean matches(Object argument) {
		return ((List<?>) argument).size() == size; 
	}
}