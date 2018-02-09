package org.celllife.mobilisr.service;

import java.util.List;

import org.celllife.mobilisr.domain.Organization;
import org.celllife.mobilisr.service.exception.UniquePropertyException;

import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.google.gwt.user.client.rpc.RemoteService;

/**
 * Interface for CRUD operations on the Organisation
 * @author Vikram Bindal (e-mail: vikram@cell-life.org)
 */
public interface OrganizationService extends RemoteService {

	/**
	 * Method that is responsible for saving the organisation into the system.
	 * @param org			Organisation that must be saved
	 * @return				persisted Organisation 
	 * @throws UniquePropertyException if organization name already exists
	 */
	Organization saveOrUpdateOrganisation(Organization org) throws UniquePropertyException;
	
	/**
	 * Method responsible for obtaining the list of all the organisations
	 * @param loadConfig	@see PagingLoadConfig for details on various attributes
	 * @return				paginated list of all the organisations
	 */
	PagingLoadResult<Organization> listAllOrganizations(PagingLoadConfig loadConfig,Boolean voided);
	
	/**
	 * Method responsible for obtaining the list of all the organisations
	 * @return		List of all the organisations
	 */
	List<Organization> listAllOrganizations(Boolean voided);
	
	Organization refreshOrganization(Organization org);
	
	Organization findOrganization(long organizationId);
}
