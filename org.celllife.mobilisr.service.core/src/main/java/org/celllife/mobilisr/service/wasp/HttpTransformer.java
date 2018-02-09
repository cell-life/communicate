package org.celllife.mobilisr.service.wasp;

import java.util.Map;

import org.celllife.mobilisr.service.exception.ChannelProcessingException;

public abstract class HttpTransformer extends BaseChannelHandler {

	protected String getParameter(Map<String, String[]> map, String key)
			throws ChannelProcessingException {
		String[] valArr = map.get(key);
		if (valArr != null && valArr.length > 0) {
			return valArr[0];
		}
		throw new ChannelProcessingException("Null parameter: " + key);
	}

}