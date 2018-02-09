package org.celllife.mobilisr.service.message.processors;

import javassist.tools.rmi.ObjectNotFoundException;

import org.celllife.mobilisr.api.messaging.DeliveryReceipt;
import org.celllife.mobilisr.constants.DeliveryReceiptState;
import org.celllife.mobilisr.constants.SmsStatus;
import org.celllife.mobilisr.dao.api.ContactDAO;
import org.celllife.mobilisr.dao.api.SmsLogDAO;
import org.celllife.mobilisr.domain.Contact;
import org.celllife.mobilisr.domain.Organization;
import org.celllife.mobilisr.domain.SmsLog;
import org.celllife.mobilisr.exception.DuplicateTransactionException;
import org.celllife.mobilisr.service.UserBalanceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.stereotype.Component;

/**
 * This class manages the processing of messages delivery receipts.
 * 
 * @author Simon Kelly
 */
@Component("DeliveryProcessor")
public class DeliveryProcessor {
	
	private static final Logger log = LoggerFactory.getLogger(DeliveryProcessor.class);
	
	@Autowired
	private SmsLogDAO smslogDao;

    @Autowired
    private ContactDAO contactDAO;

	@Autowired
	private UserBalanceService balanceService;
	
	@ServiceActivator(inputChannel="deliveryChannel")
	public void processDelivery(DeliveryReceipt receipt){
		DeliveryReceiptState finalStatus = receipt.getFinalStatus();
		SmsStatus smsStatus = DeliveryReceiptState.DELIVRD.equals(finalStatus) ? SmsStatus.TX_SUCCESS
				: SmsStatus.TX_FAIL;

		SmsLog smsLog;
		try {
			smsLog = smslogDao.updateSmsLog(receipt);

			if (smsLog != null && smsStatus.equals(SmsStatus.TX_FAIL)) {
                createRefundClientTransaction(receipt.getId(), receipt.getSourceAddr(), smsLog);
                updateContactValidity(smsLog);
			}
		} catch (ObjectNotFoundException e) {
			log.error("No smslog found for delivery receipt [{}]",receipt);
		}
		
	}

    private void updateContactValidity(SmsLog smsLog) {

        Organization organization = smsLog.getOrganization();
        Contact contact = smsLog.getContact();

        if (!contact.isInvalid() && (organization.getRetriesBeforeInvalid() > 0)) {
            Long count = smslogDao.countFailedMessages(organization.getRetriesBeforeInvalid(),contact.getMsisdn());
            if (count == organization.getRetriesBeforeInvalid()) {
                contact.setInvalid(true);
                contactDAO.saveOrUpdate(contact);
                log.info("Setting contact with id " + contact.getId() + " and msisdn " + contact.getMsisdn() + " as invalid, because the past " + count + " messages have failed.");
            }
            log.info("Keeping contact with id " + contact.getId() + " and msisdn " + contact.getMsisdn() + " as valid, although the past " + count + " messages have failed.");
        }

    }
	
	private void createRefundClientTransaction(String seqNum, String msisdn,
			SmsLog smsLog) {
		String createdFor = smsLog.getCreatedfor();
		Organization organization = smsLog.getOrganization();
		// cost is always 1 for Telfree since long messages are split
		int transactionCost = 1;
		try {
			balanceService.credit(transactionCost, organization, createdFor,
					smsLog.getIdentifierString(),
					"Refund for undelivered message to: " + msisdn
							+ " (reference: " + seqNum + ")", null);
		} catch (DuplicateTransactionException e) {
			log.error("Unable to credit organization account [orgId="
					+ organization.getId() + "]" + " [amount="
					+ transactionCost + "] [createdFor=" + createdFor
					+ "] [createdBy=" + smsLog.getIdentifierString() + "]", e);
		}
	}
}
