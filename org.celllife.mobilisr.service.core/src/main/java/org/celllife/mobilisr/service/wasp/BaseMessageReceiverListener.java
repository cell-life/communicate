package org.celllife.mobilisr.service.wasp;

import java.util.Date;

import org.celllife.mobilisr.api.messaging.DeliveryReceipt;
import org.celllife.mobilisr.api.messaging.SmsMo;
import org.celllife.mobilisr.constants.DeliveryReceiptState;
import org.jsmpp.bean.AlertNotification;
import org.jsmpp.bean.DataSm;
import org.jsmpp.bean.DeliverSm;
import org.jsmpp.bean.MessageType;
import org.jsmpp.extra.ProcessRequestException;
import org.jsmpp.session.DataSmResult;
import org.jsmpp.session.MessageReceiverListener;
import org.jsmpp.session.Session;
import org.jsmpp.util.InvalidDeliveryReceiptException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseMessageReceiverListener implements MessageReceiverListener {
	
	private static final Logger log = LoggerFactory.getLogger(BaseMessageReceiverListener.class);
	
	@Override
	public void onAcceptDeliverSm(DeliverSm deliverSm)
			throws ProcessRequestException {
		if (MessageType.SMSC_DEL_RECEIPT.containedIn(deliverSm.getEsmClass())) {
			try {
				org.jsmpp.bean.DeliveryReceipt delReceipt = deliverSm
						.getShortMessageAsDeliveryReceipt();

				DeliveryReceiptState receiptState = DeliveryReceiptState
						.valueOf(delReceipt.getFinalStatus().value());

				// convert id to HEX string (?? not sure why)
				/*String id = delReceipt.getId();
				String messageId = new BigInteger(id).toString(16).toUpperCase();*/
				
				String messageId = delReceipt.getId().toUpperCase();
				DeliveryReceipt deliveryReceipt = new DeliveryReceipt(
						messageId, delReceipt.getDoneDate(), receiptState,
						delReceipt.getError());
				
				deliveryReceipt.setSourceAddr(deliverSm.getSourceAddr());

				if (log.isTraceEnabled()) {
					log.trace("Receiving delivery receipt for message '{}' from {}"+
							" to {}: {}", new Object[]{messageId, deliverSm.getSourceAddr(),
							deliverSm.getDestAddress(), deliveryReceipt});
				}
				
				deliveryReceived(deliveryReceipt);
				
			} catch (InvalidDeliveryReceiptException e) {
				log.error("Failed getting delivery receipt", e);
			}
		} else {

            SmsMo smsMo = new SmsMo();

            try {
			smsMo = new SmsMo(deliverSm.getSourceAddr(), deliverSm.getDestAddress(),
					new String(deliverSm.getShortMessage()), new Date(), null);
            } catch (Exception e) {
                log.error("Failed to process deliver sm: " + deliverSm.toString());
                log.error(e.getCause().toString());
                log.error(e.getStackTrace().toString());
            }
			
			if (log.isTraceEnabled()) {
				log.trace("Receiving message: {}",smsMo);
			}
			
			incomingMessageReceived(smsMo);
		}
	}

	protected abstract void incomingMessageReceived(SmsMo smsMo);

	protected abstract void deliveryReceived(DeliveryReceipt deliveryReceipt);

	@Override
	public void onAcceptAlertNotification(AlertNotification alertNotification) {
	}

	@Override
	public DataSmResult onAcceptDataSm(DataSm dataSm, Session source)
			throws ProcessRequestException {
		return null;
	}

}
