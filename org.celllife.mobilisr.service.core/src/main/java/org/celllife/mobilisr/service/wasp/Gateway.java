package org.celllife.mobilisr.service.wasp;

import java.io.IOException;

import org.celllife.mobilisr.service.exception.ChannelProcessingException;
import org.jsmpp.InvalidResponseException;
import org.jsmpp.PDUException;
import org.jsmpp.extra.NegativeResponseException;
import org.jsmpp.extra.ResponseTimeoutException;

public interface Gateway {

	/**
	 * @param destinationAddr
	 *            the address to send the message to
	 * @param shortMessage
	 *            the message text
	 * @param sourceAddress
	 *            the address to send the message from
	 * @param serviceType
	 *            max 5 characters
	 * @return
	 * @throws PDUException
	 * @throws ResponseTimeoutException
	 * @throws InvalidResponseException
	 * @throws NegativeResponseException
	 * @throws IOException
	 */
	public String submitShortMessage(String destinationAddr,
			byte[] shortMessage, String sourceAddress, String serviceType)
			throws ChannelProcessingException;

	public void shutdown();
	
	public boolean isClosed();
}
