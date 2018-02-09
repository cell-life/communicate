package org.celllife.mobilisr.api.util;

import java.util.Comparator;

import org.apache.commons.beanutils.PropertyUtils;

/**
 * This class compares beans based on a property of the bean.
 * 
 * @param <T> The type of the bean
 * @param <V> The type of the bean property
 */
public class BeanPropertyComparator<T, V> implements Comparator<T> {
	private String property;
	private Comparator<V> comparator;

	public BeanPropertyComparator(String property, Comparator<V> comparator) {
		this.property = property;
		this.comparator = comparator;
	}

	public int compare(T bean1, T bean2) {
		try {
			@SuppressWarnings("unchecked")
			V value1 = (V) PropertyUtils.getProperty(bean1, property);
			
			@SuppressWarnings("unchecked")
			V value2 = (V) PropertyUtils.getProperty(bean2, property);
			return comparator.compare(value1, value2);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}