package org.celllife.mobilisr.constants;

public class Version {
	private ApiVersion version;
	private String value;

	public Version() {
	}
	
	public Version(ApiVersion version, String value) {
		this.version = version;
		this.value = value;
	}
	
	public String getValue() {
		return value;
	}
	
	public ApiVersion getVersion() {
		return version;
	}
}