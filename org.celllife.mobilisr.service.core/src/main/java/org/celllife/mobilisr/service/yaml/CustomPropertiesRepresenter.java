package org.celllife.mobilisr.service.yaml;
import java.beans.IntrospectionException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.yaml.snakeyaml.introspector.Property;


class CustomPropertiesRepresenter extends SkipNullRepresenter {

	private Map<Class<?>, List<String>> propMap;
	private boolean include;

	public void setIncludePropertyMap(Map<Class<?>, List<String>> properties) {
		this.propMap = properties;
		this.include = true;
	}

	public void setExcludePropertyMap(Map<Class<?>, List<String>> properties) {
		this.propMap = properties;
		this.include = false;
	}

	@Override
	protected Set<Property> getProperties(Class<? extends Object> type)
			throws IntrospectionException {
		Set<Property> set = super.getProperties(type);
		if (propMap.containsKey(type)) {
			Set<Property> filtered = new TreeSet<Property>();
			List<String> props = propMap.get(type);
			for (Property prop : set) {
				String name = prop.getName();
				boolean inPropList = props.contains(name);
				if (include){
					if (inPropList)
						filtered.add(prop);
				} else {
					if (!inPropList)
						filtered.add(prop);
				}
			}
			return filtered;
		} else {
			return set;
		}
	}
}