package org.celllife.mobilisr.api.validation;

public class MsisdnRule {

	private String name;
	private String prefix;
	private String validator;
	
	public MsisdnRule(String name, String prefix, String validator) {
		super();
		this.name = name;
		this.prefix = prefix;
		this.validator = validator;
	}

	public String getName() {
		return name;
	}

	public String getPrefix() {
		return prefix;
	}

	public String getValidator() {
		return validator;
	}
}