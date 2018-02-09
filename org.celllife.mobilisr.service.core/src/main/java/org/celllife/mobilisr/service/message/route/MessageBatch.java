package org.celllife.mobilisr.service.message.route;

import java.util.ArrayList;
import java.util.List;

import org.celllife.mobilisr.api.messaging.BaseMT;
import org.celllife.mobilisr.api.messaging.SmsMt;
import org.celllife.mobilisr.constants.SmsStatus;

/**
 * This class represents a batch of messages.
 * 
 * @author Simon Kelly
 */
public class MessageBatch extends BaseMT {

	private List<SmsMt> success;
	private List<SmsMt> fail;
	private List<SmsMt> emptyTransaction;
	private int batchTotalCount;
	private int batchFailureCount;
	private int batchSuccessCount;
	private int emptyTransactionCount;
	private int messageLength;

	public MessageBatch(List<SmsMt> messages) {
		this.batchTotalCount = messages.size();
		if (batchTotalCount > 0) {
			SmsMt message0 = messages.get(0);
			copy(message0);
			this.messageLength = message0.getMessage().length();
		}
		splitList(messages);
		this.batchSuccessCount = success.size();
		this.batchFailureCount = fail.size();
		this.emptyTransactionCount = emptyTransaction.size();
	}

	public List<SmsMt> getSuccesses() {
		return success;
	}

	public List<SmsMt> getFailures() {
		return fail;
	}

	public int getBatchSuccessCount() {
		return batchSuccessCount;
	}

	public int getBatchFailureCount() {
		return batchFailureCount;
	}

	public int getBatchTotalCount() {
		return batchTotalCount;
	}
	
	public int getEmptyTransactionCount() {
		return emptyTransactionCount;
	}

	public int getMessageLength() {
		return messageLength;
	}
	
	private void splitList(List<SmsMt> messages) {
		success = new ArrayList<SmsMt>();
		fail = new ArrayList<SmsMt>();
		emptyTransaction = new ArrayList<SmsMt>();

		for (SmsMt sms : messages) {
			SmsStatus status = sms.getStatus();
			if (sms.getTransactionRef() == null){
				emptyTransaction.add(sms);
			}
			
			if (status == null) {
				fail.add(sms);
			} else if (status.isFailure()){
				fail.add(sms);
			} else {
				success.add(sms);
			}
		}
	}
}
