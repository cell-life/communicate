package org.celllife.mobilisr.api.mock;

import java.util.ArrayList;
import java.util.List;

import org.celllife.mobilisr.api.MobilisrDto;

/**
 * A factory for domain objects that mocks their data. 
 * 
 * @author David Green
 * @author Simon Kelly
 */
public abstract class AbstractMockPopulator<T> implements MockPopulator<T> {

	private final Class<T> domainClass;

	private int seed;
	private int mode = -1;

	protected AbstractMockPopulator(Class<T> domainClass) {
		if (domainClass.isAssignableFrom(MobilisrDto.class)) {
			throw new IllegalArgumentException(
					"Factory only produces classes implementing "
							+ MobilisrDto.class.getCanonicalName());
		}
		this.domainClass = domainClass;
	}

	@Override
	public List<T> create(int count) {
		List<T> mocks = new ArrayList<T>(count);
		for (int x = 0; x < count; ++x) {
			T t = create();
			mocks.add(t);
		}
		return mocks;
	}

	@Override
	public T create() {
		T mock;
		try {
			mock = domainClass.newInstance();
		} catch (Exception e) {
			e.printStackTrace();
			// must have a default constructor
			throw new IllegalStateException();
		}
		populate(mode, ++seed, mock);
		return mock;
	}

	@Override
	public Class<T> getDomainClass() {
		return domainClass;
	}

	/**
	 * Populate the given domain object with data
	 * 
	 * @param mode
	 *            
	 * @param seed
	 *            a seed that may be used to create data
	 * @param mock
	 *            the domain object to populate
	 */
	protected abstract void populate(int mode, int seed, T mock);

	@Override
	public MockPopulator<T> withMode(int mode) {
		this.mode = mode;
		return this;
	}
	
	@Override
	public void reset(){
		seed = 0;
		mode = -1;
	}
	
	@Override
	public void resetMode() {
		mode = -1;
	}

}
