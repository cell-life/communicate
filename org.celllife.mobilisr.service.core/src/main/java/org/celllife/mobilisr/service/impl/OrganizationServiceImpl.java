package org.celllife.mobilisr.service.impl;

import java.util.List;

import org.celllife.mobilisr.annotation.LogLevel;
import org.celllife.mobilisr.annotation.Loggable;
import org.celllife.mobilisr.dao.api.OrganizationDAO;
import org.celllife.mobilisr.domain.Organization;
import org.celllife.mobilisr.service.OrganizationService;
import org.celllife.mobilisr.service.exception.UniquePropertyException;
import org.celllife.mobilisr.service.utility.ServiceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;

import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.trg.search.Search;
import com.trg.search.SearchResult;

@Service("crudOrganizationService")
public class OrganizationServiceImpl implements OrganizationService {

	private static final long serialVersionUID = 8910982835245986905L;

	private static Logger log = LoggerFactory.getLogger(OrganizationServiceImpl.class);

	@Autowired
	private OrganizationDAO organizationDAO;
	
	@Loggable(LogLevel.TRACE)
	@Override
	public Organization findOrganization(long organizationId) {
		return organizationDAO.find(organizationId);
	}
	
	@Loggable(LogLevel.TRACE)
	@Override
	public List<Organization> listAllOrganizations(Boolean voided) {
		Search s = new Search();
		s.addSortAsc(Organization.PROP_NAME);
		if (voided!=null){
			s.addFilterEqual(Organization.PROP_VOIDED, voided);
		}
		return organizationDAO.search(s);
	}
	
	@Loggable(LogLevel.TRACE)
	@Override
	public PagingLoadResult<Organization> listAllOrganizations(PagingLoadConfig loadConfig, Boolean voided) {

		Search search = ServiceUtil.getSearchFromLoadConfig(Organization.class,	loadConfig, Organization.PROP_NAME);
		if (voided!=null){
			search.addFilterEqual(Organization.PROP_VOIDED, voided);
		}
		SearchResult<Organization> searchResult = organizationDAO.searchAndCount(search);
		
		return ServiceUtil.getPagingLoadResult(loadConfig, searchResult);
	}

	@Loggable(LogLevel.TRACE)
	@Override
	@Secured({"PERM_ORGANISATIONS_MANAGE"})
	public Organization saveOrUpdateOrganisation(Organization org) throws UniquePropertyException {
		log.debug("Saving organization [id={}]", org.getId());

		try {
			organizationDAO.saveOrUpdate(org);
		} catch (Exception e) {
			throw new UniquePropertyException("Organization with name \'"
					+ org.getName() + "\' already exist");
		}
		
		return org;
	}
	
	@Loggable(LogLevel.TRACE)
	@Override
	public Organization refreshOrganization(Organization org){
		organizationDAO.refresh(org);
		return org;
	}
}
