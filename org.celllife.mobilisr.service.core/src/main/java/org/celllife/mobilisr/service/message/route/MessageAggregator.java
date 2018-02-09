package org.celllife.mobilisr.service.message.route;

import java.util.List;

import org.celllife.mobilisr.api.messaging.SmsMt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.annotation.Aggregator;
import org.springframework.integration.annotation.CorrelationStrategy;
import org.springframework.integration.annotation.MessageEndpoint;

/**
 * This class aggregates individual messages into batches
 * 
 * Note: you should be able to do this purely in xml but I couldn't get it
 * working
 * 
 * <aggregator input-channel="individualMessageResponse" method="collectBatch"
 *		output-channel="batchResponse"
 *	 	correlation-strategy-expression="payload.getBatchId()"
 *		expression="new MessageBatch(payload)" /> 
 * 
 * @author Simon Kelly
 */
@MessageEndpoint
public class MessageAggregator {
	
	private static final Logger log = LoggerFactory.getLogger(MessageAggregator.class);
	
	@Aggregator(inputChannel = "individualMessageResponse", outputChannel = "batchResponse")
	public MessageBatch collectBatch(List<SmsMt> response) {
		MessageBatch batch = new MessageBatch(response);
		log.debug("Aggregated individual messages into batch: {}", batch);
		return batch;
	}

	@CorrelationStrategy
	public Object correlateByBatch(SmsMt message) {
		if (log.isTraceEnabled()){
			log.trace("Correlating message: [batchId={}]", message.getBatchId());
		}

		return message.getBatchId();
	}
}
