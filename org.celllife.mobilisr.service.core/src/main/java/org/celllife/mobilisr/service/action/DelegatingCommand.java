package org.celllife.mobilisr.service.action;

import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.celllife.mobilisr.domain.PropertyConfig;
import org.celllife.mobilisr.service.filter.Action;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DelegatingCommand extends AbstractConfigurable implements Command {

	private final Logger log = LoggerFactory.getLogger(getClass());
	private final Action delegate;

	public DelegatingCommand(Action delegate, PropertyConfig config) {
		this.delegate = delegate;
		init(config);
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean execute(Context context) throws Exception {
		if (log.isTraceEnabled()){
			log.trace("Executing command for incoming message [{}]", delegate.getClass().getSimpleName());
		}
		context.putAll(getProperties());
		
		return delegate.execute(context);
	}

}
