package org.celllife.mobilisr.api.util;

import java.util.Comparator;

import junit.framework.Assert;

import org.junit.Test;

public class GenericComparatorTests {

	@Test
	public void testGenericComparator_single(){
		@SuppressWarnings("unchecked")
		Comparator<String> c = GenericComparator.createComparator(new Comparator<String>(){
			@Override
			public int compare(String o1, String o2) {
				return o1.compareTo(o2);
			}
		});
		
		int result = c.compare("a","b");
		Assert.assertTrue(result < 0);
	}
	
	@Test
	public void testGenericComparator_multiple1(){
		Comparator<TestBean> c = getMultiComparator();
		
		int result = c.compare(new TestBean("a",1), new TestBean("b",2));
		Assert.assertTrue(result < 0);
	}
	
	@Test
	public void testGenericComparator_multiple2(){
		Comparator<TestBean> c = getMultiComparator();
		
		int result = c.compare(new TestBean("a",2), new TestBean("a",1));
		Assert.assertTrue(result > 0);
	}
	
	@Test
	public void testGenericComparator_multiple3(){
		Comparator<TestBean> c = getMultiComparator();
		
		int result = c.compare(new TestBean("a",1), new TestBean("a",1));
		Assert.assertTrue(result == 0);
	}

	private Comparator<TestBean> getMultiComparator() {
		BeanPropertyComparator<TestBean,String> nameC = new BeanPropertyComparator<TestBean, String>(TestBean.NAME, new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				return o1.compareTo(o2);
			}
		});
		BeanPropertyComparator<TestBean,Integer> sizeC = new BeanPropertyComparator<TestBean, Integer>(TestBean.SIZE, new Comparator<Integer>() {
			@Override
			public int compare(Integer o1, Integer o2) {
				return o1.compareTo(o2);
			}
		});
		
		@SuppressWarnings("unchecked")
		Comparator<TestBean> c = GenericComparator.createComparator(nameC, sizeC);
		return c;
	}
}
