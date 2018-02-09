package org.celllife.mobilisr.service.action;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.apache.commons.chain.impl.ChainBase;
import org.apache.commons.chain.impl.ContextBase;
import org.celllife.mobilisr.annotation.LogLevel;
import org.celllife.mobilisr.annotation.Loggable;
import org.celllife.mobilisr.constants.SmsStatus;
import org.celllife.mobilisr.dao.api.ChannelDAO;
import org.celllife.mobilisr.dao.api.FilterActionDAO;
import org.celllife.mobilisr.dao.api.MessageFilterDAO;
import org.celllife.mobilisr.dao.api.SmsLogDAO;
import org.celllife.mobilisr.domain.Channel;
import org.celllife.mobilisr.domain.FilterAction;
import org.celllife.mobilisr.domain.MessageFilter;
import org.celllife.mobilisr.domain.SmsLog;
import org.celllife.mobilisr.service.exception.TriggerException;
import org.celllife.mobilisr.service.filter.Action;
import org.celllife.mobilisr.service.filter.Filter;
import org.celllife.mobilisr.service.security.SecurityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.stereotype.Component;

import com.trg.search.Search;

/**
 * FilterActionRunner processes the filters and actions for incoming messages.
 *
 * @author Simon Kelly
 * @author Vikram Bindal
 *
 */
@Component("filterActionRunner")
public class FilterActionRunner {

	public static final String NAME = "filterActionRunner";

	private static Logger log = LoggerFactory.getLogger(FilterActionRunner.class);

	@Autowired
	private ChannelDAO channelDAO;

	@Autowired
	private MessageFilterDAO filterDao;

	@Autowired
	private FilterActionDAO actionDao;

	@Autowired
	private SmsLogDAO smsLogDAO;

	@Autowired
	private ApplicationContext applicationContext;

	@Loggable(LogLevel.TRACE)
	@ServiceActivator(inputChannel="messageProcessing")
	public void processMessage(SmsLog smsLog)
			throws TriggerException {
		
		// increment attempts to keep track of how many times
		// this message has been processed
		smsLog.setAttempts(smsLog.getAttempts() + 1);

		Channel channel = smsLog.getChannel();
		if (channel == null){
			channel = channelDAO.getActiveInChannelForShortCode(smsLog.getCreatedfor());
			if (channel == null) {
				log.debug("No active IN channel found for [shortCode={}]",smsLog.getCreatedfor());
				smsLog.setStatus(SmsStatus.RX_CHANNEL_FAIL);
				smsLog.setFailreason("No channel with shortcode " + smsLog.getCreatedfor());
				smsLogDAO.saveOrUpdate(smsLog);
				return;
			} else {
				smsLog.setChannel(channel);
			}
		}

		List<MessageFilter> filterList = filterDao.getActiveFilters(channel);
		
		if (filterList.isEmpty()) {
			log.warn("No active filters configured for channel [channel={}]",
					channel.getName());
			smsLog.setStatus(SmsStatus.RX_FILTER_FAIL);
			smsLog.setFailreason("No filters configured for channel " + channel.getName());
			smsLogDAO.saveOrUpdate(smsLog);
			return;
		}

		for (MessageFilter filter : filterList) {
			String filterBeanName = filter.getType();

			Filter filterBean = (Filter) applicationContext.getBean(filterBeanName);
			filterBean.init(filter);
			if (filterBean.matches(smsLog.getMessage())) {
				log.debug("Incoming message matched [filter={}]", filterBeanName);
				smsLog.setOrganization(filter.getOrganization());
				smsLog.setCreatedfor(filter.getIdentifierString());
				
				SecurityUtil util = new SecurityUtil();
				util.performSystemLogin();
				processFilter(filter, smsLog);
				util.clearSystemLogin();
				return;
			}
		}

		log.warn("No filter matches incoming message [text={}]", smsLog.getMessage());
		smsLog.setStatus(SmsStatus.RX_FILTER_FAIL);
		smsLog.setFailreason("No filter matches message text");
		smsLogDAO.saveOrUpdate(smsLog);
	}

	@SuppressWarnings("unchecked")
	@Loggable(LogLevel.TRACE)
	private void processFilter(MessageFilter filter, SmsLog smsLog) {

		Search search = new Search(FilterAction.class);
		search.addFilterEqual(FilterAction.PROP_FILTER, filter);
		List<FilterAction> actionList = actionDao.search(search);

		if (actionList.isEmpty()) {
			log.info("No actions configured for [filter={}]", filter.getName());
			smsLog.setStatus(SmsStatus.RX_SUCCESS);
		} else {

			Collection<Command> commands = getCommands(actionList);

			// TODO: each command should add a message to the context which can be used
			// as a kind of trace to see what happened.
			try {
				Context context = new ContextBase();
				context.put(Action.FILTER, filter);
				context.put(Action.SMS_LOG, smsLog);

				new ChainBase(commands).execute(context);
				smsLog.setStatus(SmsStatus.RX_SUCCESS);
			} catch (Exception e) {
				log.error("Error processing command chain for incoming message", e);
				smsLog.setStatus(SmsStatus.RX_ACTION_FAIL);
				smsLog.setFailreason("Failed during task processing: " + e.getMessage());
			}
		}

		smsLogDAO.saveOrUpdate(smsLog);
	}

	private Collection<Command> getCommands(List<FilterAction> handlers) {
		ArrayList<Command> commands = new ArrayList<Command>();

		for (FilterAction filterAction : handlers) {
			String commandBeanName = filterAction.getType();
			Action actionBean = (Action) applicationContext.getBean(commandBeanName);
			commands.add(new DelegatingCommand(actionBean, filterAction));
		}
		return commands;
	}

}
