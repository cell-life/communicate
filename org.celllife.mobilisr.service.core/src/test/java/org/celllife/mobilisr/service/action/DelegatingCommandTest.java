package org.celllife.mobilisr.service.action;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.chain.Context;
import org.apache.commons.chain.impl.ContextBase;
import org.celllife.mobilisr.domain.FilterAction;
import org.celllife.mobilisr.service.filter.Action;
import org.celllife.mobilisr.service.yaml.YamlUtils;
import org.celllife.pconfig.model.DateParameter;
import org.celllife.pconfig.model.IntegerParameter;
import org.celllife.pconfig.model.Parameter;
import org.celllife.pconfig.model.Pconfig;
import org.junit.Assert;
import org.junit.Test;

public class DelegatingCommandTest {
	
	@Test
	public void testCommand() throws Exception{
		List<Parameter<?>> params = new ArrayList<Parameter<?>>();

		final String dateName = "dateparam";
		final Date dateValue = new Date();
		
		DateParameter dateParameter = new DateParameter();
		dateParameter.setName(dateName);
		dateParameter.setValue(dateValue);
		params.add(dateParameter);

		final String intName = "intparam";
		final Integer intValue = 5;
		IntegerParameter p2 = new IntegerParameter();
		p2.setLabel("Count");
		p2.setName(intName);
		p2.setValue(intValue);
		params.add(p2);
		
		FilterAction config = new FilterAction();
		config.setProps(YamlUtils.dumpParameterList(params));
		
		Action a = new Action() {
			
			@Override
			public boolean execute(Context context) throws Exception {
				Date date = (Date) context.get(dateName);
				Assert.assertEquals(dateValue, date);
				
				Integer inte = (Integer) context.get(intName);
				Assert.assertEquals(intValue, inte);
				
				return false;
			}
			
			@Override
			public Pconfig getConfigDescriptor() {
				return null;
			}
		};
		
		DelegatingCommand command = new DelegatingCommand(a, config);
		command.execute(new ContextBase());
	}

}
