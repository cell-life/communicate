package org.celllife.mobilisr.constants;

public enum ApiVersion {
	
	v1, v2;

	public static ApiVersion getLatest() {
		return v2;
	}

}
