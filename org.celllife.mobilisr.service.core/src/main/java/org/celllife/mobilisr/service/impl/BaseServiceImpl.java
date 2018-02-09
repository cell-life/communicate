package org.celllife.mobilisr.service.impl;

import org.celllife.mobilisr.dao.api.MobilisrGeneralDAO;
import org.celllife.mobilisr.domain.HasOrganization;
import org.celllife.mobilisr.domain.MobilisrEntity;
import org.celllife.mobilisr.domain.Organization;
import org.celllife.mobilisr.domain.Voidable;
import org.celllife.mobilisr.exception.MobilisrRuntimeException;
import org.celllife.mobilisr.service.BaseService;
import org.celllife.mobilisr.service.utility.ServiceUtil;
import org.springframework.beans.factory.annotation.Autowired;

import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.trg.search.Search;
import com.trg.search.SearchResult;

public class BaseServiceImpl implements BaseService {

	@Autowired
	private MobilisrGeneralDAO generalDao;
	
	@Override
	@SuppressWarnings("unchecked")
	public PagingLoadResult<MobilisrEntity> getEntityList(
			Organization organization, String entityName,
			PagingLoadConfig loadConfig) {
		Class<MobilisrEntity> entityClass = null;
		try {
			entityClass = (Class<MobilisrEntity>) Class.forName(entityName);
		} catch (Exception e) {
			throw new MobilisrRuntimeException("Unknown entity: " + entityName);
		}
		
		return getEntityList(organization, entityClass, loadConfig, false);
	}
	
	@Override
	public <T extends MobilisrEntity> PagingLoadResult<T> getEntityList(
			Organization organization, Class<T> entityClass,
			PagingLoadConfig loadConfig, boolean includeVoided) {
		return getEntityList(organization, entityClass, loadConfig, includeVoided, null);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public <T extends MobilisrEntity> PagingLoadResult<T> getEntityList(
			Organization organization, Class<T> entityClass,
			PagingLoadConfig loadConfig, boolean includeVoided, SearchModifier modifier) {
		
		Search search = ServiceUtil.getSearchFromLoadConfig(entityClass, loadConfig, null);

		if (organization != null && HasOrganization.class.isAssignableFrom(entityClass)){
			search.addFilterEqual(HasOrganization.PROP_ORGANIZATION, organization);
		}
		
		if (!includeVoided && Voidable.class.isAssignableFrom(entityClass)){
			search.addFilterEqual(Voidable.PROP_VOIDED, false);
		}
		
		if (modifier != null){
			modifier.modify(search);
		}
		
		SearchResult<T> searchResult = generalDao.searchAndCount(search);
		return ServiceUtil.getPagingLoadResult(loadConfig, searchResult);
	}
	
	public MobilisrGeneralDAO getGeneralDao() {
		return generalDao;
	}
	
	
	public void setGeneralDao(MobilisrGeneralDAO dao){
		generalDao = dao;
		
	}
}
