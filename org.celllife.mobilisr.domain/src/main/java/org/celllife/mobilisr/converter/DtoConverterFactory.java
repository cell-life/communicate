package org.celllife.mobilisr.converter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.celllife.mobilisr.api.MobilisrDto;
import org.celllife.mobilisr.constants.ApiVersion;
import org.celllife.mobilisr.domain.MobilisrEntity;

public class DtoConverterFactory implements EntityDtoConverterFactory {

	private static DtoConverterFactory instance;
	private Map<Class<?>, EntityDtoConverter<?,?>> entityDtoConverters = new HashMap<Class<?>, EntityDtoConverter<?,?>>();
	
	public static EntityDtoConverterFactory getInstance(){
		if (instance == null){
			instance = new DtoConverterFactory();
		}
		return instance;
	}
	
	private DtoConverterFactory(){
		register(new CampaignDtoConverter());
		register(new ContactDtoConverter());
		register(new CampaignMessageDtoConverter());
		register(new SmsMtConverter());
		register(new MessageStatusConverter());
	}
	
	private void register(EntityDtoConverter<?,?> converter) {
		Class<?> dtoType = converter.getDtoType();
		if (dtoType.isAssignableFrom(MobilisrDto.class)) {
			throw new IllegalArgumentException("Factory only converts to dto classes implementing " + MobilisrDto.class.getCanonicalName());
		}
		entityDtoConverters.put(dtoType, converter);
		
		Class<?> entityType = converter.getEntityType();
		if (entityType.isAssignableFrom(MobilisrEntity.class)) {
			throw new IllegalArgumentException("Factory only converts to enity classes extending " + MobilisrEntity.class.getCanonicalName());
		}
		entityDtoConverters.put(entityType, converter);
	}
	
	@Override
	public <C extends MobilisrEntity, T extends MobilisrDto> T toDto(C entity, Class<T> dtoClass, ApiVersion ver){
		return (T) (entity == null ? null : getEntityDtoConverter(entity, dtoClass).toDto(entity, ver));
	}
	
	@Override
	public <C extends MobilisrEntity, T extends MobilisrDto> List<T> toDto(List<C> entityList, Class<T> dtoClass, ApiVersion ver){
		List<T> listDto = new ArrayList<T>(entityList.size());
		if (!entityList.isEmpty()){
			EntityDtoConverter<C, T> converter = getEntityDtoConverter(entityList.get(0), dtoClass);
			for (C entity : entityList) {
				listDto.add(converter.toDto(entity, ver));
			}
		}
		return listDto;
	}
	
	@Override
	public <C extends MobilisrEntity, T extends MobilisrDto> C fromDto(T dto, Class<C> entityClass, ApiVersion ver){
		return (C) getDtoEntityConverter(dto, entityClass).fromDto(dto, ver);
	}

	@Override
	public <C extends MobilisrEntity, T extends MobilisrDto> List<C> fromDto(List<T> dtoList, Class<C> entityClass, ApiVersion ver){
		EntityDtoConverter<C, T> converter = getDtoEntityConverter(dtoList.get(0), entityClass);
		List<C> listDto = new ArrayList<C>(dtoList.size());
		for (T dto : dtoList) {
			listDto.add(converter.fromDto(dto, ver));
		}
		return listDto;
	}
	
	private <C extends MobilisrEntity, T extends MobilisrDto> EntityDtoConverter<C,T> getEntityDtoConverter(C entity, Class<T> dtoClass) {
		return getConverter(entity.getClass());
	}
	
	private <C extends MobilisrEntity, T extends MobilisrDto> EntityDtoConverter<C,T> getDtoEntityConverter(T dto, Class<C> entityClass) {
		return getConverter(dto.getClass());
	}

	@SuppressWarnings("unchecked")
	private <C extends MobilisrEntity, T extends MobilisrDto> EntityDtoConverter<C,T> getConverter(Class<?> fromClass) {
		EntityDtoConverter<C, T> converter = (EntityDtoConverter<C, T>) entityDtoConverters.get(fromClass);
		return converter;
	}
	
}
