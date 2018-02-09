package org.celllife.mobilisr.constants;

/**
 * This enum represents the status codes of messages.
 * 
 * Note: variaous reports rely on the '_' in the status to determine if a
 * message has failed i.e. if status ends in FAIL
 * 
 * @author Simon Kelly
 */
public enum SmsStatus {

	/**
	 * Sent to queue
	 */
	QUEUED_SUCCESS("Sent to queue"),
	
	/**
	 * Currently being processed
	 */
	QUEUED_PROCESSING("Currently being processed"),
	
	/**
	 * Failed to queue message
	 */
	QUEUE_FAIL("Failed to queue message"),
	
	/**
	 * Message sent to WASP
	 */
	WASP_SUCCESS("Message sent to WASP"),
	
	/**
	 * Failed to send message to WASP
	 */
	WASP_FAIL("Failed to send message to WASP"),
	
	/**
	 * Message delivered
	 */
	TX_SUCCESS("Message delivered"),
	
	/**
	 * Delivery failed
	 */
	TX_FAIL("Delivery failed"),
	
	/**
	 * Message received and processed without error
	 */
	RX_SUCCESS("Message received and processed without error"),
	
	/**
	 * Error while processing message actions
	 */
	RX_ACTION_FAIL("Error while processing message actions"),
	
	/**
	 * Message does not match any filters
	 */
	RX_FILTER_FAIL("Message does not match any filters"),
	
	/**
	 * Message received on unknown channel
	 */
	RX_CHANNEL_FAIL("Message received on unknown channel");
	
	private final String text;
	
	private SmsStatus(String text) {
		this.text = text;
	}
	
	public String getText() {
		return text;
	}
	
	public boolean isFailure(){
		return this.equals(QUEUE_FAIL)
			|| this.equals(RX_CHANNEL_FAIL)
			|| this.equals(RX_FILTER_FAIL)
			|| this.equals(RX_ACTION_FAIL)
			|| this.equals(TX_FAIL)
			|| this.equals(WASP_FAIL);
	}
}
