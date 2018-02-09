package org.celllife.mobilisr.service;

import org.celllife.mobilisr.domain.MobilisrEntity;
import org.celllife.mobilisr.domain.Organization;
import org.celllife.mobilisr.service.impl.SearchModifier;

import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import com.extjs.gxt.ui.client.data.PagingLoadResult;

public interface BaseService {

	/**
	 * @param organization
	 *            restrict the returned list to those belonging to the specified
	 *            organisation
	 * @param entityClassName
	 *            the fully qualified Java class name for entities to search for
	 * @param loadConfig
	 *            the load configuration for paging and filtering
	 * @return
	 */
	PagingLoadResult<MobilisrEntity> getEntityList(Organization organization,
			String entityClassName, PagingLoadConfig loadConfig);

	/**
	 * @param <T>
	 * @param organization
	 *            restrict the returned list to those belonging to the specified
	 *            organisation
	 * @param entityClass
	 *            the class for entities to search for
	 * @param loadConfig
	 *            the load configuration for paging and filtering
	 * @param includeVoided include voided objects
	 * @return
	 */
	<T extends MobilisrEntity> PagingLoadResult<T> getEntityList(
			Organization organization, Class<T> entityClass,
			PagingLoadConfig loadConfig, boolean includeVoided);

	/**
	 * @param <T>
	 * @param organization
	 *            restrict the returned list to those belonging to the specified
	 *            organisation
	 * @param entityClass
	 *            the class for entities to search for
	 * @param loadConfig
	 *            the load configuration for paging and filtering
	 * @param includeVoided include voided objects
	 * @param modifier a SearchModifier or null
	 * @return
	 */
	<T extends MobilisrEntity> PagingLoadResult<T> getEntityList(Organization organization,
			Class<T> entityClass, PagingLoadConfig loadConfig,
			boolean includeVoided, SearchModifier modifier);

}
