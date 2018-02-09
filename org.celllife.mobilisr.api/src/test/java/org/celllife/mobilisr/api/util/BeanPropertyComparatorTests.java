package org.celllife.mobilisr.api.util;

import java.util.Comparator;

import junit.framework.Assert;

import org.junit.Test;

public class BeanPropertyComparatorTests {

	@Test
	public void testBeanPropertyComparator_string(){
		BeanPropertyComparator<TestBean,String> comparator = new BeanPropertyComparator<TestBean, String>(TestBean.NAME, new Comparator<String>() {

			@Override
			public int compare(String o1, String o2) {
				return o1.compareTo(o2);
			}
		});
		
		int result = comparator.compare(new TestBean("a",10), new TestBean("b",10));
		Assert.assertTrue(result < 0);
	}
	
	@Test
	public void testBeanPropertyComparator_int(){
		BeanPropertyComparator<TestBean,Integer> comparator = new BeanPropertyComparator<TestBean, Integer>(TestBean.SIZE, new Comparator<Integer>() {

			@Override
			public int compare(Integer o1, Integer o2) {
				return o1.compareTo(o2);
			}
		});
		
		int result = comparator.compare(new TestBean("a",1), new TestBean("b",2));
		Assert.assertTrue(result < 0);
	}
	
	@Test(expected=RuntimeException.class)
	public void testBeanPropertyComparator_missingProperty(){
		BeanPropertyComparator<TestBean,Integer> comparator = new BeanPropertyComparator<TestBean, Integer>("notThere", new Comparator<Integer>() {
			@Override
			public int compare(Integer o1, Integer o2) {
				return o1.compareTo(o2);
			}
		});
		
		comparator.compare(new TestBean("a",1), new TestBean("b",2));
		// expect RuntimeException
	}
}
