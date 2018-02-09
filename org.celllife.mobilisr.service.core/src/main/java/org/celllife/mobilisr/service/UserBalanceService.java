package org.celllife.mobilisr.service;

import org.celllife.mobilisr.domain.Campaign;
import org.celllife.mobilisr.domain.Organization;
import org.celllife.mobilisr.domain.User;
import org.celllife.mobilisr.exception.DuplicateTransactionException;
import org.celllife.mobilisr.exception.InsufficientBalanceException;
import org.celllife.mobilisr.exception.TransactionNotFoundException;
import org.celllife.mobilisr.service.qrtz.BackgroundServices;
import org.celllife.mobilisr.utilbean.TransactionSummary;

/**
 * Interface for the UserBalanceService management
 * 
 * @author Vikram Bindal (e-mail: vikram@cell-life.org)
 * @author Simon Kelly (e-mail: simon@cell-life.org)
 */
public interface UserBalanceService {

	/**
	 * The following method helps log the amount for debit in the Client Transaction. It is to be
	 * noted that the balance for the organization does not get updated immediately, only when the
	 * {@link BackgroundServices} runs, only then the organisation balance gets updated.
	 * 
	 * @param amountToDebit
	 *            Amount to be debited
	 * @param reservedUsed
	 *            The amount to be recored as used in the reserved transaction
	 * @param reference
	 *            The refernce id for the reserved transaction
	 * @param createdFor
	 *            Identifier string belonging to campaign or contact for which the transaction
	 *            originated
	 * @param billingMsg
	 * @param user
	 *            user for which the client transaction must be recorded
	 * @param transMsg
	 *            Message for transaction
	 * @return reference of resulting transaction
	 * @throws TransactionNotFoundException
	 */
	public abstract Long debitOrgBalance(int amountToDebit, int reservedUsed, Long reference,
			String createdFor, String createdBy, String billingMsg, User user)
			throws TransactionNotFoundException;

	/**
	 * Update all organisation balances from the transaction log. Also check
	 * the balance against the low balance threshold and send alerts if
	 * necessary.
	 */
	public void updateOrgBalances();

	/**
	 * Reserve amount from organisation account. Any negative amount is made
	 * positive.
	 * @param organization
	 * @param amountToReserve
	 * @param createdFor
	 *            entity to reserve amount for
	 * @param createdBy
	 *            user who is performing the action
	 * @param message
	 *            transaction message
	 * 
	 * @return the ID of the created transaction
	 * @throws InsufficientBalanceException
	 */
	public Long reserveAmount(Organization organization, int amountToReserve,
			String createdFor, User createdBy, String message) throws InsufficientBalanceException;

/**
	 * Unreserve an amount from the organisations account. The amount unreserved
	 * will be whatever amount is remaining for the given reserve transaction.
	 * 
	 * @param refForReservedAmount
	 *            initial reserve transaction
	 * @param campaign
	 *            the campaign relating to this transaction
	 * @param message
	 *            the transaction message
	 * @param user
	 *            the user performing the action
	 * @throws TransactionNotFoundException
	 *             if the transaction is not found
	 */
	public void unreserve(Long refForReservedAmount, Campaign campaign,
			String message, User user) throws TransactionNotFoundException;
			
	/**
	 * Method called when Admin user credits organisation account
	 * 
	 * @param amount
	 * @param organization
	 * @param createdFor
	 * @param createdBy
	 * @param message
	 *            transaction message (must be unique)
	 * @param user
	 * @return
	 * @throws DuplicateTransactionException
	 *             if a credit transaction with the same message already exists
	 */
	public Long credit(int amount, Organization organization, String createdFor, String createdBy,
			String message, User user) throws DuplicateTransactionException;
	
	/**
	 * Method called when Admin user debits organisation account
	 * 
	 * @param amount
	 * @param organization
	 * @param createdFor
	 * @param createdBy
	 * @param message
	 *            transaction message (must be unique)
	 * @param user
	 * @return
	 * @throws DuplicateTransactionException
	 *             if a credit transaction with the same message already exists
	 */
	public Long debit(int amount, Organization organization, String createdFor,
			String createdBy, String message, User user)
			throws DuplicateTransactionException;

	public TransactionSummary getAccountSummary(Campaign campaign);

	public void verifyZeroReserved(Campaign campaign, String message, User user);

	public void verifyZeroReserved(Long reference, Campaign campaign, String string,
			User user) throws TransactionNotFoundException;

	TransactionSummary getAccountSummary(String createdFor);

	TransactionSummary getAccountSummary(Organization organization);

}