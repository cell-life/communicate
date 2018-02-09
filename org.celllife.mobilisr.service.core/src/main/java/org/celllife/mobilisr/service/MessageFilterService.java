package org.celllife.mobilisr.service;

import java.util.Collection;
import java.util.List;

import org.celllife.mobilisr.domain.Channel;
import org.celllife.mobilisr.domain.MessageFilter;
import org.celllife.mobilisr.domain.Organization;
import org.celllife.mobilisr.service.exception.UniquePropertyException;
import org.celllife.mobilisr.service.gwt.MessageFilterViewModel;
import org.celllife.pconfig.model.Pconfig;

import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import com.extjs.gxt.ui.client.data.PagingLoadResult;

public interface MessageFilterService {

	public PagingLoadResult<MessageFilter> listMessageFilters(Organization organization, Boolean showVoided, PagingLoadConfig config);

	public Collection<Pconfig> listAllActions();

	List<Pconfig> getActionsForFilter(Long filterId);

	MessageFilterViewModel getMessageFilterViewModel(Long filterId);

	void saveMessageFilter(MessageFilter messageFilter) throws UniquePropertyException;

	void saveMessageFilter(MessageFilterViewModel model) throws UniquePropertyException;

	List<Channel> listAllChannels();

	Collection<Pconfig> listAllFilterTypes();

}
