package org.celllife.mobilisr.service.message.route;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.celllife.mobilisr.api.messaging.SmsMt;
import org.celllife.mobilisr.domain.Contact;
import org.celllife.mobilisr.domain.Messagable;
import org.celllife.mobilisr.service.qrtz.beans.SmsBatchConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.Splitter;

/**
 * This class takes a batch configuration and splits it into individual messages.
 * 
 * @author Simon Kelly
 */
@MessageEndpoint
public class MessageSplitter {

	private static final Logger log = LoggerFactory.getLogger(MessageSplitter.class);
	
	@Splitter(inputChannel="bulkMessageChannel", outputChannel="correlatedMessageChannel")
	public List<SmsMt> split(SmsBatchConfig batch){
		batch.setBatchId(UUID.randomUUID().toString());
		
		List<? extends Messagable> recipients = batch.getRecipients();
		final List<SmsMt> smsList = new ArrayList<SmsMt>(recipients.size());
		
		log.debug("splitting message data for [{}] and Batch contacts size [{}]",
				batch.getCreatedFor(), recipients.size());	

		for (final Messagable recipient : recipients) {
			SmsMt smsMt = new SmsMt();
			smsMt.copy(batch);
			smsMt.setMsisdn(recipient.getMsisdn());
			Contact contact = recipient.getContact();
			if (contact != null)
				smsMt.setContactId(contact.getId());

			smsList.add(smsMt);
		}
		return smsList;
	}
}
