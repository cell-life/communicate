package org.celllife.mobilisr.service.message.processors;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.celllife.mobilisr.api.messaging.SmsMt;
import org.celllife.mobilisr.api.util.AlphanumComparator;
import org.celllife.mobilisr.api.util.BeanPropertyComparator;
import org.celllife.mobilisr.constants.SmsStatus;
import org.celllife.mobilisr.dao.api.MobilisrGeneralDAO;
import org.celllife.mobilisr.domain.Channel;
import org.celllife.mobilisr.domain.NumberInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.Transformer;
import org.springframework.stereotype.Component;

import com.trg.search.Search;

/**
 * This transformer selects the appropriate channel for each message by matching
 * the message MSISDN against the configured NumberInfo objects.
 * 
 * <p>Note that the prefixes are sorted in reverse specificity (and reverse alphabetic)
 * so that the most specific prefixes match first.
 * 
 * <pre>
 * e.g.
 * prefixes = 27, 2781, 278
 * sorted prefixes = 2781, 278, 27
 * 
 * So the number '2782' is matched by 278 but '277' is matched by 27
 * </pre>
 * 
 * @author Simon Kelly
 */
@MessageEndpoint
@Component
public class ChannelSelector {
	
	public static final String SIMULATOR_CHANNEL = "out-simulator";

	private final Logger log = LoggerFactory.getLogger(getClass());

	@Autowired
	private MobilisrGeneralDAO generalDao;
	
	@Value("${simulateMessageSending}")
	private boolean simulateMessageSending;

	private Map<String, Channel> prefixMap;
	
	private final Object lock = new Object();

	@Transformer(inputChannel = "correlatedMessageChannel", outputChannel = "messagesWithChannel")
	public SmsMt selectChannel(SmsMt message) {
		
		synchronized (lock){
			initPrefixList();
			
			String msisdn = message.getMsisdn();
			for (Entry<String, Channel> entry : prefixMap.entrySet()) {
				if (msisdn.startsWith(entry.getKey())){
					Channel channel = entry.getValue();
					message.setChannelId(channel.getId());
					message.setChannelName(channel.getHandler());
					break;
				}
			}
		}
		
		if (message.getChannelId() == null){
			log.warn("Msisdn [{}] is not matched by any number prefixes", message.getMsisdn());

			message.setStatus(SmsStatus.QUEUE_FAIL);
			message.setErrorMessage("Number format is not supported by any active channels.");
			/*
			 * channelName must match a channel in outgoingMessageContext.xml
			 */
			message.setChannelName("individualMessageResponse");
			
		} else if (log.isDebugEnabled()){
			log.debug("Channel [id={}, name={}] selected for " +
					"message [msisdn={}, createdFor={}]",
					new Object[] { message.getChannelId(),
							message.getChannelName(),
							message.getCreatedFor(), message.getMsisdn() });
		}
		
		if (simulateMessageSending && (message.getStatus() == null || !message.getStatus().isFailure())){
			if (log.isTraceEnabled())
				log.trace("Message [createdFor={}] [msisdn={}] re-routed to simulation channel", 
						message.getCreatedFor(), message.getMsisdn());
			/*
			 * channelName must match a channel in outgoingMessageContext.xml
			 */
			message.setChannelName(SIMULATOR_CHANNEL);
		}
		
		return message;
	}
	
	/**
	 * Populate the prefixMap from the NumberInfo objects in the database.
	 */
	private void initPrefixList() {
		if (prefixMap == null){
			prefixMap = new LinkedHashMap<String, Channel>();
			Search s = new Search(NumberInfo.class);
			s.addFetch(NumberInfo.PROP_CHANNEL);
			s.addFilterEqual(NumberInfo.PROP_VOIDED, false);
			@SuppressWarnings("unchecked")
			List<NumberInfo> list = generalDao.search(s);
			
			BeanPropertyComparator<NumberInfo, String> prefixComparator = new BeanPropertyComparator<NumberInfo, String>(
					NumberInfo.PROP_PREFIX, new AlphanumComparator());
			Collections.sort(list, prefixComparator);
			Collections.reverse(list);
			
			for (NumberInfo info : list) {
				prefixMap.put(info.getPrefix(), info.getChannel());
			}
		}
	}
	
	public void reset(){
		synchronized (lock){
			prefixMap = null;
		}
	}

	/**
	 * If set to true the message will be routed to the simulation channel
	 * regardless of the channel selected. The SmsLog will however still 
	 * record the real channel that was selected.
	 * 
	 * @param simulateMessageSending
	 */
	public void simulateMessageSending(boolean simulateMessageSending) {
		this.simulateMessageSending = simulateMessageSending;
	}
	
	void setGeneralDao(MobilisrGeneralDAO generalDao) {
		this.generalDao = generalDao;
	}
}
