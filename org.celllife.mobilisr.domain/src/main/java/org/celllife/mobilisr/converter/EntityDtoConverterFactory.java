package org.celllife.mobilisr.converter;

import java.util.List;

import org.celllife.mobilisr.api.MobilisrDto;
import org.celllife.mobilisr.constants.ApiVersion;
import org.celllife.mobilisr.domain.MobilisrEntity;

/**
 * Factory class to convert between DTO's and entities and visa versa
 * 
 * @author SimonKelly <simon@cell-life.org>
 * 
 */
public interface EntityDtoConverterFactory {

	/**
	 * Convert a single entity object to a DTO
	 * 
	 * @param entity Object extending {@link MobilisrEntity} to convert 
	 * @param dtoClass Class of DTO to convert to. Must implement {@link MobilisrDto}
	 * @param ver
	 * 
	 * @return DTO object
	 */
	public abstract <C extends MobilisrEntity, T extends MobilisrDto> T toDto(
			C entity, Class<T> dtoClass, ApiVersion ver);
	
	/**
	 * Convert a list of entity objects to a list of DTO's 
	 * 
	 * @param entityList List of entity objects extending {@link MobilisrEntity} to convert 
	 * @param dtoClass Class of DTO to convert to. Must implement {@link MobilisrDto}
	 * @param ver
	 * 
	 * @return DTO object
	 */
	public abstract <C extends MobilisrEntity, T extends MobilisrDto> List<T> toDto(
			List<C> entityList, Class<T> dtoClass, ApiVersion ver);	
	
	/**
	 * Convert a single DTO object to an entity object
	 * 
	 * @param dto DTO object implementing {@link MobilisrDto} to convert 
	 * @param entityClass Class of entity to convert to. Must extend {@link MobilisrEntity}
	 * 
	 * @return entity object
	 */
	public abstract <C extends MobilisrEntity, T extends MobilisrDto> C fromDto(
			T dto, Class<C> entityClass, ApiVersion ver);

	/**
	 * Convert a list of DTO objects to a list of entity objects
	 * 
	 * @param dtoList List of DTO objects implementing {@link MobilisrDto} to convert 
	 * @param entityClass Class of entity to convert to. Must extend {@link MobilisrEntity}
	 * 
	 * @return list of entity objects
	 */
	public abstract <C extends MobilisrEntity, T extends MobilisrDto> List<C> fromDto(
			List<T> dtoList, Class<C> entityClass, ApiVersion ver);

}