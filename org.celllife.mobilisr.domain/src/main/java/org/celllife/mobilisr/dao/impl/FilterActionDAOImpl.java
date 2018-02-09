package org.celllife.mobilisr.dao.impl;

import java.util.List;

import org.celllife.mobilisr.annotation.LogLevel;
import org.celllife.mobilisr.annotation.Loggable;
import org.celllife.mobilisr.dao.api.FilterActionDAO;
import org.celllife.mobilisr.domain.FilterAction;
import org.celllife.mobilisr.domain.MessageFilter;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.trg.search.Search;

@Repository("filterActionDAO")
public class FilterActionDAOImpl extends BaseDAOImpl<FilterAction, Long>
		implements FilterActionDAO{


	@Override
	@Transactional(readOnly=true)
	@Loggable(LogLevel.TRACE)
	public List<FilterAction> getActionsForFilter(MessageFilter filter) {
		Search s = new Search();		
		s.addFilterEqual(FilterAction.PROP_FILTER, filter);

		return search(s);
	}
}
