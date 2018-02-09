package org.celllife.mobilisr.service.message.route;

import java.util.List;

import org.celllife.mobilisr.util.LogUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.annotation.Aggregator;
import org.springframework.integration.annotation.CorrelationStrategy;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ReleaseStrategy;

/**
 * This class collects the batches together and creates a BatchStats object
 * once all the batches have been received.
 * 
 * @author Simon Kelly
 */
@MessageEndpoint
public class BatchAggregator {
	
	private static final Logger log = LoggerFactory.getLogger(BatchAggregator.class);

	@Aggregator(inputChannel="batchResponse", outputChannel="batchStats")
	public BatchStats collectStats(List<MessageBatch> batches) {
		BatchStats stats = new BatchStats(batches);
		log.debug("Aggregated batches into stats: {}", stats);
		return stats;
	}

	@CorrelationStrategy
	public String correlateByBatch(MessageBatch batch) {
		if (log.isTraceEnabled()){
			log.trace("Correlating batch: [id={}]", batch.getBatchId());
		}
		
		return batch.getBatchCorrelationId();
	}

	/**
	 * Tells the aggregator when all batches have been received
	 * 
	 * @param batches
	 * @return true if all batches have been received
	 */
	@ReleaseStrategy
	public boolean releaseChecker(List<MessageBatch> batches) {
		int totalCount = batches.get(0).getTotalContacts();
		int batchTotal = 0;
		for (MessageBatch batch : batches) {
			batchTotal += batch.getBatchTotalCount();
		}
		if (batchTotal > totalCount){
			log.error(LogUtil.getMarker_notifyAdmin(),
					"Batch aggregator aggregated batches with more " +
					"recipients than expected. [expected={}] [actual={}]",
					totalCount, batchTotal);
		}
		return batchTotal >= totalCount;
	}

}
