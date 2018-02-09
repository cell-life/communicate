package org.celllife.mobilisr.service.action;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.celllife.mobilisr.domain.PropertyConfig;
import org.celllife.mobilisr.service.exception.TriggerRuntimeException;
import org.celllife.mobilisr.service.filter.Configurable;
import org.celllife.mobilisr.service.yaml.YamlUtils;
import org.celllife.pconfig.model.EntityParameter;
import org.celllife.pconfig.model.Parameter;
import org.celllife.pconfig.util.PconfigUtils;

/**
 * Abstract class implementing {@link Configurable} interface. Loads properties
 * from String or class implementing {@link PropertyConfig}
 * 
 * @author Simon Kelly
 */
public abstract class AbstractConfigurable implements Configurable {

	private Map<String, Object> props = new HashMap<String, Object>();

	/* (non-Javadoc)
	 * @see org.celllife.mobilisr.service.Configurable#init(org.celllife.mobilisr.domain.client.PropertyConfig)
	 */
	public void init(PropertyConfig config) {
		String properties = config.getProperties();
		init(properties);
	}

	/* (non-Javadoc)
	 * @see org.celllife.mobilisr.service.Configurable#init(java.lang.String)
	 */
	public void init(String properties) {
		if (properties == null || properties.isEmpty()){
			return;
		}
		
		try {
			List<Parameter<?>> params = YamlUtils.loadParameterList(properties);
			if (params == null) {return;}
			
			for (Parameter<?> param : params) {
				Object value = param.getValue();
				if (param instanceof EntityParameter){
					EntityParameter eparam = (EntityParameter) param;
					value = PconfigUtils.convertValue(eparam.getValue(), eparam.getValueType());
				}
				props.put(param.getName(), value);
			}
		} catch (Exception e) {
			throw new TriggerRuntimeException("Unable to load properties",e);
		}
	}

	public Object getProperty(String key) {
		return props.get(key);
	}
	
	public void setProperty(String key, Object value){
		props.put(key, value);
	}
	
	public Map<String, Object> getProperties() {
		return props;
	}
}
