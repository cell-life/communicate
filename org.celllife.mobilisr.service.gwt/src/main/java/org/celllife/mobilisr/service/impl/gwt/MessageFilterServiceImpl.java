package org.celllife.mobilisr.service.impl.gwt;

import java.util.Collection;
import java.util.List;

import javax.servlet.ServletException;

import org.celllife.mobilisr.domain.Channel;
import org.celllife.mobilisr.domain.MessageFilter;
import org.celllife.mobilisr.domain.Organization;
import org.celllife.mobilisr.exception.MobilisrException;
import org.celllife.mobilisr.exception.MobilisrRuntimeException;
import org.celllife.mobilisr.service.gwt.MessageFilterService;
import org.celllife.mobilisr.service.gwt.MessageFilterViewModel;
import org.celllife.pconfig.model.Pconfig;

import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import com.extjs.gxt.ui.client.data.PagingLoadResult;

public class MessageFilterServiceImpl extends AbstractMobilisrService implements MessageFilterService {

	private static final long serialVersionUID = -6167326731238228315L;

	private org.celllife.mobilisr.service.MessageFilterService service;

	@Override
	public void init() throws ServletException {
		super.init();
		service = (org.celllife.mobilisr.service.MessageFilterService) getBean("messageFilterService");
	}

	@Override
	public PagingLoadResult<MessageFilter> listMessageFilters(
			Organization organization, Boolean showVoided,
			PagingLoadConfig config) throws MobilisrException, MobilisrRuntimeException {
		return service.listMessageFilters(organization, showVoided, config);
	}

	@Override
	public Collection<Pconfig> listAllActions() throws MobilisrException, MobilisrRuntimeException {
		return service.listAllActions();
	}

	@Override
	public Collection<Pconfig> listAllFilterTypes() throws MobilisrException, MobilisrRuntimeException {
		return service.listAllFilterTypes();
	}

	@Override
	public List<Pconfig> getActionsForFilter(Long filterId) throws MobilisrException, MobilisrRuntimeException {
		return service.getActionsForFilter(filterId);
	}

	@Override
	public MessageFilterViewModel getMessageFilterViewModel(Long filterId) throws MobilisrException, MobilisrRuntimeException {
		return service.getMessageFilterViewModel(filterId);
	}

	@Override
	public void saveMessageFilter(MessageFilter mf) throws MobilisrException, MobilisrRuntimeException {
		service.saveMessageFilter(mf);
	}

	@Override
	public void saveMessageFilter(MessageFilterViewModel model) throws Exception {
		service.saveMessageFilter(model);
	}

	@Override
	public List<Channel> listAllChannels() throws MobilisrException, MobilisrRuntimeException {
		return service.listAllChannels();
	}
}