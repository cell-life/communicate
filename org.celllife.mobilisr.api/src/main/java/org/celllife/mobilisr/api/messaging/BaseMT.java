package org.celllife.mobilisr.api.messaging;

import java.util.UUID;

public class BaseMT {

	private String message;
	private String createdFor;
	private Long channelId;
	private String channelName;
	private String batchId;
	private String batchCorrelationId;
	private boolean processCampaignCompletion = false;
	private Long userId;
	private Long transactionRef;
	private Long organizationId;
	private int totalContacts = 1;

	public BaseMT() {
		super();
		batchCorrelationId = UUID.randomUUID().toString();
		batchId = UUID.randomUUID().toString();
	}
	
	public void copy(BaseMT mt) {
		setMessage(mt.getMessage());
		setCreatedFor(mt.getCreatedFor());
		setChannelId(mt.getChannelId());
		setChannelName(mt.getChannelName());
		setBatchId(mt.getBatchId());
		setProcessCampaignCompletion(mt.isProcessCampaignCompletion());
		setUserId(mt.getUserId());
		setTransactionRef(mt.getTransactionRef());
		setOrganizationId(mt.getOrganizationId());
		setTotalContacts(mt.getTotalContacts());
		setBatchCorrelationId(mt.getBatchCorrelationId());
	}

	public String getMessage() {
		return message;
	}

	/**
	 * @param message
	 *            the message to be sent
	 */
	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * @param createdFor
	 *            the identifier string of the entity that this message was
	 *            created for
	 */
	public void setCreatedFor(String createdFor) {
		this.createdFor = createdFor;
	}

	public String getCreatedFor() {
		return createdFor;
	}

	/**
	 * If set to true and this message was created for a campaign, the campaign
	 * state will be changed to FINISHED once all the messages have been sent.
	 * 
	 * @param processCampaignCompletion
	 */
	public void setProcessCampaignCompletion(boolean processCampaignCompletion) {
		this.processCampaignCompletion = processCampaignCompletion;
	}

	public boolean isProcessCampaignCompletion() {
		return processCampaignCompletion;
	}

	/**
	 * @param channelId
	 *            the database id of the channel being used
	 */
	public void setChannelId(Long channelId) {
		this.channelId = channelId;
	}

	public Long getChannelId() {
		return channelId;
	}

	/**
	 * This is used to correlate all the messages in a batch.
	 * 
	 * @param batchId
	 *            the id of this batch.
	 */
	public void setBatchId(String batchId) {
		this.batchId = batchId;
	}

	public String getBatchId() {
		return batchId;
	}

	/**
	 * @param userId
	 *            the ID of the user responsible for sending these messages
	 */
	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public Long getUserId() {
		return userId;
	}

	/**
	 * This can be null in which case the API will attempt to reserve the
	 * required amount for the message.
	 * 
	 * If it is not null it is assumed that the message can be sent. A debit
	 * transaction will be created with the message cost once all batches have
	 * been processed.
	 * 
	 * @param transactionRef
	 *            a transaction ID for the reserve transaction
	 */
	public void setTransactionRef(Long transactionRef) {
		this.transactionRef = transactionRef;
	}

	public Long getTransactionRef() {
		return transactionRef;
	}

	/**
	 * @param organizationId
	 *            the ID of the organisation to which this message belongs
	 */
	public void setOrganizationId(Long organizationId) {
		this.organizationId = organizationId;
	}

	public Long getOrganizationId() {
		return organizationId;
	}

	/**
	 * @param totalContacts
	 *            the total number of contacts across all the batches
	 */
	public void setTotalContacts(int totalContacts) {
		this.totalContacts = totalContacts;
	}

	public int getTotalContacts() {
		return totalContacts;
	}

	/**
	 * The channel name is used for routing the message to the correct channel
	 * handler.
	 * 
	 * Must match an actual channel name in outgoingMessageContext.xml
	 * 
	 * @param channelName
	 */
	public void setChannelName(String channelName) {
		this.channelName = channelName;
	}

	public String getChannelName() {
		return channelName;
	}

	/**
	 * This is used to correlate multiple batches together.
	 * 
	 * @param batchCorrelationId
	 *            a unique ID
	 */
	public void setBatchCorrelationId(String batchCorrelationId) {
		this.batchCorrelationId = batchCorrelationId;
	}

	public String getBatchCorrelationId() {
		return batchCorrelationId;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("BaseMT [createdFor=").append(createdFor)
				.append(", channelName=").append(channelName)
				.append(", processCampaignCompletion=")
				.append(processCampaignCompletion).append(", userId=")
				.append(userId).append(", transactionRef=")
				.append(transactionRef).append(", organizationId=")
				.append(organizationId).append(", totalContacts=")
				.append(totalContacts).append("]");
		return builder.toString();
	}
	
	
}