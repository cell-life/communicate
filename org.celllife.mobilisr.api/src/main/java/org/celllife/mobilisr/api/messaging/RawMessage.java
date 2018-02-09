package org.celllife.mobilisr.api.messaging;

import java.util.Map;

public class RawMessage {

	private final String body;
	private final Map<String, String[]> parameterMap;

	public RawMessage(String body, Map<String, String[]> parameterMap) {
		this.body = body;
		this.parameterMap = parameterMap;
	}
	
	public String getBody() {
		return body;
	}
	
	public Map<String, String[]> getParameterMap() {
		return parameterMap;
	}
}
