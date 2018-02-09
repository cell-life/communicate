package org.celllife.mobilisr.service.message.route;

import java.util.List;

import org.celllife.mobilisr.api.messaging.BaseMT;

/**
 * This class collects data on a group of batches.
 * 
 * @author Simon Kelly
 */
public class BatchStats extends BaseMT {

	private int numBatches;
	private int totalCount = 0;
	private int totalSuccess = 0;
	private int totalFail = 0;
	private int totalEmptyTransaction = 0;
	private int messageLength;

	public BatchStats() {
	}
	
	public BatchStats(List<MessageBatch> batches) {
		int size = batches.size();
		this.numBatches = size;
		if (size > 0) {
			MessageBatch batch0 = batches.get(0);
			copy(batch0);
			this.messageLength = batch0.getMessageLength();
			for (MessageBatch batch : batches) {
				addBatch(batch);
			}
		}
	}

	public int getNumBatches() {
		return numBatches;
	}

	public int getTotalCount() {
		return totalCount;
	}
	
	public int getTotalFail() {
		return totalFail;
	}
	
	public int getTotalEmptyTransaction() {
		return totalEmptyTransaction;
	}
	
	public int getTotalSuccess() {
		return totalSuccess;
	}

	public void addBatch(MessageBatch batch) {
		this.totalCount += batch.getBatchTotalCount();
		this.totalSuccess += batch.getBatchSuccessCount();
		this.totalFail += batch.getBatchFailureCount();
		this.totalEmptyTransaction += batch.getEmptyTransactionCount();
	}

	
	public int getMessageLength() {
		return messageLength;
	}
	
	public  void setNumBatches(int numBatches) {
		this.numBatches = numBatches;
	}

	public void setTotalCount(int totalCount) {
		this.totalCount = totalCount;
	}

	public void setTotalSuccess(int totalSuccess) {
		this.totalSuccess = totalSuccess;
	}

	public void setTotalFail(int totalFail) {
		this.totalFail = totalFail;
	}

	public void setMessageLength(int messageLength) {
		this.messageLength = messageLength;
	}
}
