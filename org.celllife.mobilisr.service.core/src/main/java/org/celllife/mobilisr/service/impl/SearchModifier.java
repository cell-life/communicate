package org.celllife.mobilisr.service.impl;

import com.trg.search.Search;

/**
 * Interface used to allow callers to modify Search configurations
 */
public interface SearchModifier {
	
	public void modify(Search search);

}
