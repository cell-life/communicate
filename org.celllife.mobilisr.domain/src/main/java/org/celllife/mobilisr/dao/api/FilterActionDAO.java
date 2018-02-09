package org.celllife.mobilisr.dao.api;

import java.util.List;

import org.celllife.mobilisr.domain.FilterAction;
import org.celllife.mobilisr.domain.MessageFilter;

public interface FilterActionDAO extends BaseDAO<FilterAction, Long> {

	List<FilterAction> getActionsForFilter(MessageFilter messageFilter);

}
