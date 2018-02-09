package org.celllife.mobilisr.api.mock;

import java.util.HashMap;
import java.util.Map;

/**
 * @author David Green
 * @author Simon Kelly
 */
public abstract class BaseMockFactory {
	
	private Map<Class<?>, MockPopulator<?>> factories = new HashMap<Class<?>, MockPopulator<?>>();
	
	public BaseMockFactory(){
		registerFactories();
	}

	@SuppressWarnings("unchecked")
	public <T> MockPopulator<T> on(Class<T> domainClass) {
		MockPopulator<?> factory = factories.get(domainClass);
		if (factory == null) {
			throw new IllegalStateException(
					"Did you forget to register a mock factory for "
							+ domainClass.getClass().getName() + "?");
		}
		return (MockPopulator<T>) factory;
	}
	
	protected void register(MockPopulator<?> mockFactory) {
		factories.put(mockFactory.getDomainClass(), mockFactory);
	}
	
	protected void resetMode() {
		for (MockPopulator<?> populator : factories.values()) {
			populator.resetMode();
		}
		
	}
	
	protected abstract void registerFactories();
}
