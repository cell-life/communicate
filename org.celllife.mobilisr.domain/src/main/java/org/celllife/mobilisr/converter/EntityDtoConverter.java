package org.celllife.mobilisr.converter;

import org.celllife.mobilisr.api.MobilisrDto;
import org.celllife.mobilisr.constants.ApiVersion;
import org.celllife.mobilisr.domain.MobilisrEntity;

/**
 * Converts a DTO of type T to an entity of type C and visa versa.
 * 
 * @author Simon Kelly <simon@cell-life.org>
 *
 * @param <C> entity type
 * @param <T> DTO type
 */
public interface EntityDtoConverter<C extends MobilisrEntity, T extends MobilisrDto> {

	/**
	 * @return the type of DTO that this converter supports
	 */
	public Class<T> getDtoType();
	
	/**
	 * @return the type of entity that this converter supports
	 */
	public Class<C> getEntityType();

	/**
	 * @param entity The entity to convert
	 * @return a DTO
	 */
	public T toDto(C entity, ApiVersion mode);
	
	/**
	 * @param dto The DTO to convert
	 * @param ver 
	 * @return an entity
	 */
	public C fromDto(T dto, ApiVersion ver);
}
