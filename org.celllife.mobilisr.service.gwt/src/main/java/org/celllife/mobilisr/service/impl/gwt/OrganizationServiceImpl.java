package org.celllife.mobilisr.service.impl.gwt;

import java.util.List;

import javax.servlet.ServletException;

import org.celllife.mobilisr.domain.Organization;
import org.celllife.mobilisr.domain.User;
import org.celllife.mobilisr.exception.MobilisrException;
import org.celllife.mobilisr.exception.MobilisrRuntimeException;
import org.celllife.mobilisr.service.gwt.OrganizationService;

import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import com.extjs.gxt.ui.client.data.PagingLoadResult;

public class OrganizationServiceImpl extends AbstractMobilisrService implements
		OrganizationService {

	private static final long serialVersionUID = 7181281435651844275L;
	private org.celllife.mobilisr.service.OrganizationService service;
	private org.celllife.mobilisr.service.UserBalanceService balanceService;
	
	@Override
	public void init() throws ServletException {
		super.init();
		service = (org.celllife.mobilisr.service.OrganizationService) getBean("crudOrganizationService");
		balanceService = (org.celllife.mobilisr.service.UserBalanceService) getBean("userBalanceService");
	}

	@Override
	public Organization saveOrUpdateOrganisation(Organization org)
			throws MobilisrException {
		return service.saveOrUpdateOrganisation(org);
	}

	@Override
	public PagingLoadResult<Organization> listAllOrganizations(
			PagingLoadConfig loadConfig, Boolean showVoided) throws MobilisrException {
		return service.listAllOrganizations(loadConfig, showVoided);
	}

	@Override
	public List<Organization> listAllOrganizations(Boolean showVoided) throws MobilisrException {
		return service.listAllOrganizations(showVoided);
	}

	@Override
	public Organization refreshOrganization(Organization org)
			throws MobilisrException {
		return service.refreshOrganization(org);
	}
	
	@Override
	public Long creditOrganizationAcount(Organization org, int amount, String reason, User user)throws MobilisrException {
		return balanceService.credit(amount, org, org.getIdentifierString(), user.getIdentifierString(), reason, user);
	}
	
	@Override
	public Long debitOrganizationAcount(Organization org, int amount,
			String reason, User user) throws MobilisrException,
			MobilisrRuntimeException {
		 return balanceService.debit(amount, org, org.getIdentifierString(), user.getIdentifierString(), reason, user);
	}
}
