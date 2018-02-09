package org.celllife.mobilisr.api.mock;

import java.util.List;

public interface MockPopulator<T> {

	/**
	 * Create several objects
	 * 
	 * @param entityManager
	 *            the entity manager, or null if the mocked objects should not
	 *            be persisted
	 * @param count
	 *            the number of objects to create
	 * @return the created objects
	 */
	public abstract List<T> create(int count);

	/**
	 * Create a single object
	 * 
	 * @param entityManager
	 *            the entity manager, or null if the mocked object should not be
	 *            persisted
	 * @return the mocked object
	 */
	public abstract T create();

	public abstract Class<T> getDomainClass();

	/**
	 * @param mode
	 * @return
	 */
	public abstract MockPopulator<T> withMode(int mode);

	/**
	 * Resets the seed counter to 0
	 */
	public abstract void reset();

	public abstract void resetMode();

}