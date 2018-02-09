package org.celllife.mobilisr.service.impl.gwt;

import java.util.List;

import javax.servlet.ServletException;

import org.celllife.mobilisr.domain.MobilisrEntity;
import org.celllife.mobilisr.domain.NumberInfo;
import org.celllife.mobilisr.domain.Organization;
import org.celllife.mobilisr.domain.User;
import org.celllife.mobilisr.exception.MobilisrException;
import org.celllife.mobilisr.exception.MobilisrRuntimeException;
import org.celllife.mobilisr.service.gwt.AdminService;
import org.celllife.mobilisr.service.gwt.OrganisationNotificationViewModel;

import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import com.extjs.gxt.ui.client.data.PagingLoadResult;

public class AdminServiceImpl extends AbstractMobilisrService implements AdminService {

	private static final long serialVersionUID = 1783347235337931539L;

	private org.celllife.mobilisr.service.AdminService service;

	@Override
	public void init() throws ServletException {
		super.init();
		service = (org.celllife.mobilisr.service.AdminService) getBean("adminService");
	}

	@Override
	public void sendMailNow() throws MobilisrException,	MobilisrRuntimeException {
		service.sendMailNow();
	}

	@Override
	public int getMailQueueCount() throws MobilisrException, MobilisrRuntimeException {
		return service.getMailQueueCount();
	}

	@Override
	public int getLostMessagesCount() throws MobilisrException, MobilisrRuntimeException {
		return service.getLostMessagesCount();
	}

	@Override
	public PagingLoadResult<MobilisrEntity> getEntityList(Organization organization,
			String entityName, PagingLoadConfig loadConfig) throws MobilisrException, MobilisrRuntimeException {
		return service.getEntityList(organization, entityName, loadConfig);
	}

	@Override
	public void sendNewUserRequest(User user, String requestType,
			String requestText) throws MobilisrException,
			MobilisrRuntimeException {
		service.sendNewUserRequest(user, requestType, requestText);
	}
	
	@Override
	public List<NumberInfo> getNumberInfoList(){
		return service.getNumberInfoList();
	}
	
	@Override
	public void sendNewOrganizationNotification(
			OrganisationNotificationViewModel model) throws MobilisrException,
			MobilisrRuntimeException {
		service.sendNewOrganizationNotification(model);
	}
}
