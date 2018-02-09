package org.celllife.mobilisr.api.util;

public class TestBean {
	public static final String NAME = "name";
	public static final String SIZE = "size";

	private String name;
	private int size;

	public TestBean(String name, int size) {
		super();
		this.name = name;
		this.size = size;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}
}