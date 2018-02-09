package org.celllife.mobilisr.service.gwt;

import java.util.List;

import org.celllife.mobilisr.domain.Organization;
import org.celllife.mobilisr.domain.User;
import org.celllife.mobilisr.exception.MobilisrException;
import org.celllife.mobilisr.exception.MobilisrRuntimeException;
import org.celllife.mobilisr.service.UserBalanceService;

import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * @see org.celllife.mobilisr.service.OrganizationService
 */
@RemoteServiceRelativePath("crudOrganization.rpc")
public interface OrganizationService extends RemoteService {

	/**
	 * @see org.celllife.mobilisr.service.OrganizationService#saveOrUpdateOrganisation(Organization)
	 */
	Organization saveOrUpdateOrganisation(Organization org) throws MobilisrException, MobilisrRuntimeException;
	
	/**
	 * @see org.celllife.mobilisr.service.OrganizationService#listAllOrganizations(PagingLoadConfig)
	 */
	PagingLoadResult<Organization> listAllOrganizations(PagingLoadConfig loadConfig, Boolean showVoided) throws MobilisrException, MobilisrRuntimeException;

	/**
	 * @see org.celllife.mobilisr.service.OrganizationService#listAllOrganizations()
	 */
	List<Organization> listAllOrganizations(Boolean showVoided) throws MobilisrException, MobilisrRuntimeException;
	
	/**
	 * @see org.celllife.mobilisr.service.OrganizationService#refreshOrganization(Organization)
	 */
	Organization refreshOrganization(Organization org) throws MobilisrException, MobilisrRuntimeException;

	/**
	 * @see UserBalanceService#credit(int, Organization, String, String, String, User)
	 */
	Long creditOrganizationAcount(Organization org, int amount, String reason, User user) throws MobilisrException, MobilisrRuntimeException;
	
	/**
	 * @see UserBalanceService#debit(int, Organization, String, String, String, User)
	 */
	Long debitOrganizationAcount(Organization org, int amount, String reason, User user) throws MobilisrException, MobilisrRuntimeException;
	
}
