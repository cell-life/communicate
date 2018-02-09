package org.celllife.mobilisr.service.gwt;

import java.util.Collection;
import java.util.List;

import org.celllife.mobilisr.domain.Channel;
import org.celllife.mobilisr.domain.MessageFilter;
import org.celllife.mobilisr.domain.Organization;
import org.celllife.mobilisr.exception.MobilisrException;
import org.celllife.mobilisr.exception.MobilisrRuntimeException;
import org.celllife.pconfig.model.Pconfig;

import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * @see org.celllife.mobilisr.service.MessageFilterService
 */
@RemoteServiceRelativePath("messageFilterService.rpc")
public interface MessageFilterService extends RemoteService {

	/**
	 * @see org.celllife.mobilisr.service.MessageFilterService#listMessageFilters(Organization, Boolean, PagingLoadConfig)
	 */
	public PagingLoadResult<MessageFilter> listMessageFilters(Organization organization, Boolean showVoided, PagingLoadConfig config) throws MobilisrException, MobilisrRuntimeException;

	/**
	 * @see org.celllife.mobilisr.service.MessageFilterService#listAllActions()
	 */
	public Collection<Pconfig> listAllActions() throws MobilisrException, MobilisrRuntimeException;

	/**
	 * @see org.celllife.mobilisr.service.MessageFilterService#getActionsForFilter(Long)
	 */
	List<Pconfig> getActionsForFilter(Long filterId) throws MobilisrException, MobilisrRuntimeException;

	/**
	 * @see org.celllife.mobilisr.service.MessageFilterService#getMessageFilterViewModel(Long)
	 */
	MessageFilterViewModel getMessageFilterViewModel(Long filterId) throws MobilisrException, MobilisrRuntimeException;

	/**
	 * @see org.celllife.mobilisr.service.MessageFilterService#saveMessageFilter(MessageFilter)
	 */
	void saveMessageFilter(MessageFilter mf) throws MobilisrException, MobilisrRuntimeException;

	/**
	 * @throws Exception 
	 * @see org.celllife.mobilisr.service.MessageFilterService#saveMessageFilter(MessageFilterViewModel)
	 */
	void saveMessageFilter(MessageFilterViewModel model) throws MobilisrException, MobilisrRuntimeException, Exception;

	/**
	 * @see org.celllife.mobilisr.service.MessageFilterService#listAllChannels()
	 */
	List<Channel> listAllChannels() throws MobilisrException, MobilisrRuntimeException;

	/**
	 * @see org.celllife.mobilisr.service.MessageFilterService#listAllFilterTypes()
	 */
	Collection<Pconfig> listAllFilterTypes() throws MobilisrException, MobilisrRuntimeException;
}
