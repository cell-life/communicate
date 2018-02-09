package org.celllife.mobilisr.service.qrtz.beans;

import java.util.List;

import org.celllife.mobilisr.api.messaging.BaseMT;
import org.celllife.mobilisr.domain.Messagable;

public class SmsBatchConfig extends BaseMT {

	private List<? extends Messagable> recipients;

	public SmsBatchConfig() {
		super();
	}
	
	/**
	 * @param batchCorrelationId
	 *            this must be the same for all batches that are part of a
	 *            single send
	 * @param createdFor
	 *            the id string of the entity sending the messages e.g.
	 *            campaign, filter action etc.
	 * @param smsMsg
	 *            the text of the message
	 * @param recipients
	 *            the list of recipients
	 * @param totalNumOfContacts
	 *            the total number of contact for this send (across all batches
	 *            with the same correlation id)
	 * @param reservedAmntRef
	 *            a transaction reference for to debit the cost against. If
	 *            missing a transaction will be created per recipient.
	 * @param channel
	 *            the channel to send the messages via
	 * @param userId
	 *            the id of the user who triggered the action
	 * @param organizationId
	 *            the id of the organisation to whom these messages belong
	 * @param processCampaignCompletion
	 *            if set to true the campaign will be marked as finished once
	 *            all messages have been sent
	 */
	public SmsBatchConfig(String batchCorrelationId, String createdFor,
			String smsMsg, List<? extends Messagable> recipients,
			int totalNumOfContacts, Long reservedAmntRef,
			Long userId, Long organizationId, boolean processCampaignCompletion) {
		super();
		this.recipients = recipients;
		setBatchCorrelationId(batchCorrelationId);
		setMessage(smsMsg);
		setTotalContacts(totalNumOfContacts);
		setTransactionRef(reservedAmntRef);
		setCreatedFor(createdFor);
		setUserId(userId);
		setOrganizationId(organizationId);
		setProcessCampaignCompletion(processCampaignCompletion);
	}
	
	public void setRecipients(List<? extends Messagable> recipients) {
		this.recipients = recipients;
	}

	public List<? extends Messagable> getRecipients() {
		return recipients;
	}
}
