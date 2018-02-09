package org.celllife.mobilisr.test;

import java.util.List;

import org.mockito.ArgumentMatcher;
import org.mockito.Mockito;

import com.trg.search.Search;

public class MobilisrMockito {
	
	/**
     * Matches any list with the size given
     *  
     * @return empty List.
     */
    public static <T> List<T> listOfSize(Class<T> clazz, int size) {
    	return Mockito.argThat(new ListSizeMatcher<T>(size));
    }  
    
	/**
     * Matches any list with the size given
     *  
     * @return empty List.
     */
    public static <T> T isInList(final List<T> list) {
    	return Mockito.argThat(new ArgumentMatcher<T>() {
			@Override
			public boolean matches(Object argument) {
				return list.contains(argument);
			}
		});
    } 
    
    /**
     * Matches any Search with the given filter criteria
     *  
     * @return empty List.
     */
    public static Search searchWithFilter(String filterProp, Object filterValue) {
    	return Mockito.argThat(new SearchWithFilterMatcher(filterProp, filterValue));
    }  
    

}
