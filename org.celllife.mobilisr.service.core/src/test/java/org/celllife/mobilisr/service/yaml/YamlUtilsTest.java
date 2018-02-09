package org.celllife.mobilisr.service.yaml;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.celllife.pconfig.model.BooleanParameter;
import org.celllife.pconfig.model.DateParameter;
import org.celllife.pconfig.model.EntityParameter;
import org.celllife.pconfig.model.IntegerParameter;
import org.celllife.pconfig.model.LabelParameter;
import org.celllife.pconfig.model.Parameter;
import org.celllife.pconfig.model.StringParameter;
import org.junit.Assert;
import org.junit.Test;

public class YamlUtilsTest {
	
	@Test
	public void testUtil(){
		List<Parameter<?>> params = new ArrayList<Parameter<?>>();

		DateParameter p1 = new DateParameter();
		p1.setName("dateparam");
		p1.setValue(new Date());
		params.add(p1);

		IntegerParameter p2 = new IntegerParameter();
		p2.setName("intparam");
		p2.setValue(5);
		params.add(p2);

		StringParameter p3 = new StringParameter();
		p3.setName("template");
		p3.setValue("<p>hello ${number}</p><p>this is a new message ${message}</p>");
		params.add(p3);

		EntityParameter p4 = new EntityParameter();
		p4.setName("org");
		p4.setValue("123345");
		params.add(p4);

		BooleanParameter p5 = new BooleanParameter();
		p5.setName("boolparam");
		p5.setValue(true);
		params.add(p5);

		String paramString = YamlUtils.dumpParameterList(params);
		List<Parameter<?>> loadParameterList = YamlUtils.loadParameterList(paramString);
		
		for (int i = 0; i < loadParameterList.size(); i++) {
			Parameter<?> loadP = loadParameterList.get(i);
			Parameter<?> origP = params.get(i);
			
			Assert.assertEquals(origP.getName(), loadP.getName());
			Assert.assertEquals(origP.getValue(), loadP.getValue());
		}
	}
	
	@Test
	public void testLabelParam(){
		List<Parameter<?>> params = new ArrayList<Parameter<?>>();
		BooleanParameter p1 = new BooleanParameter();
		p1.setName("boolparam");
		p1.setValue(true);
		params.add(p1);
		
		LabelParameter p2 = new LabelParameter();
		p2.setValue("label value");
		params.add(p2);
		
		String paramString = YamlUtils.dumpParameterList(params);
		List<Parameter<?>> loadParams = YamlUtils.loadParameterList(paramString);
		
		Assert.assertEquals(1, loadParams.size());
		Assert.assertEquals(p1.getValue(), loadParams.get(0).getValue());
	}
	
	@Test
	public void testEntityParam(){
		List<Parameter<?>> params = new ArrayList<Parameter<?>>();
		EntityParameter origP = new EntityParameter();
		origP.setName("org");
		origP.setValue("123345");
		origP.setValueLabel("value label");
		origP.setValueType(Long.class.getName());
		params.add(origP);
		
		String paramString = YamlUtils.dumpParameterList(params);
		List<Parameter<?>> loadParams = YamlUtils.loadParameterList(paramString);
		
		EntityParameter loadP = (EntityParameter) loadParams.get(0);
		Assert.assertEquals(origP.getName(), loadP.getName());
		Assert.assertEquals(origP.getValue(), loadP.getValue());
		Assert.assertEquals(origP.getValueLabel(), loadP.getValueLabel());
		Assert.assertEquals(origP.getValueType(), loadP.getValueType());
	}

}
