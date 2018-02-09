package org.celllife.mobilisr.service.yaml;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.celllife.pconfig.model.BooleanParameter;
import org.celllife.pconfig.model.DateParameter;
import org.celllife.pconfig.model.EntityParameter;
import org.celllife.pconfig.model.IntegerParameter;
import org.celllife.pconfig.model.LabelParameter;
import org.celllife.pconfig.model.Parameter;
import org.celllife.pconfig.model.StringParameter;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.JavaBeanDumper;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.Tag;

public class YamlUtils {
	
	public static List<Parameter<?>> loadParameterList(String yaml){
		if (yaml == null || yaml.isEmpty()){
			return new ArrayList<Parameter<?>>();
		}
		
		Constructor constructor = new Constructor();
		constructor.addTypeDescription(new TypeDescription(DateParameter.class, "!date"));
		constructor.addTypeDescription(new TypeDescription(IntegerParameter.class, "!integer"));
		constructor.addTypeDescription(new TypeDescription(BooleanParameter.class, "!boolean"));
		constructor.addTypeDescription(new TypeDescription(LabelParameter.class, "!label"));
		constructor.addTypeDescription(new TypeDescription(EntityParameter.class, "!entity"));
		constructor.addTypeDescription(new TypeDescription(StringParameter.class, "!string"));
		
		@SuppressWarnings("unchecked")
		List<Parameter<?>> params = (List<Parameter<?>>) new Yaml(constructor).load(yaml);
		return params;
	}
	
	public static String dumpParameterList(Parameter<?>... params){
		if (params == null || params.length == 0){
			return "";
		}
		List<Parameter<?>> asList = Arrays.asList(params);
		return dumpParameterList(asList);
	}
	
	public static String dumpParameterList(List<Parameter<?>> params){
		// remove label parameters
		CollectionUtils.filter(params, new Predicate() {
			@Override
			public boolean evaluate(Object object) {
				if (object instanceof LabelParameter){
					return false;
				}
				return true;
			}
		});
		
		CustomPropertiesRepresenter representer = new CustomPropertiesRepresenter();
		Map<Class<?>, List<String>> propMap = new HashMap<Class<?>, List<String>>();
		propMap.put(DateParameter.class, Arrays.asList("name", "value"));
		propMap.put(StringParameter.class, Arrays.asList("name", "value"));
		propMap.put(BooleanParameter.class, Arrays.asList("name", "value"));
		propMap.put(IntegerParameter.class, Arrays.asList("name", "value"));
		propMap.put(EntityParameter.class, Arrays.asList("name", "value", "valueType", "valueLabel"));
		representer.setIncludePropertyMap(propMap);

		representer.addClassTag(DateParameter.class, new Tag("!date"));
		representer.addClassTag(StringParameter.class, new Tag("!string"));
		representer.addClassTag(IntegerParameter.class, new Tag("!integer"));
		representer.addClassTag(BooleanParameter.class, new Tag("!boolean"));
		representer.addClassTag(LabelParameter.class, new Tag("!label"));
		representer.addClassTag(EntityParameter.class, new Tag("!entity"));

		String output = new JavaBeanDumper(representer, new DumperOptions())
				.dump(params);
		return output;
	}

}
