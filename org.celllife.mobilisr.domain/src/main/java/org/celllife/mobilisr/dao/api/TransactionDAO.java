package org.celllife.mobilisr.dao.api;

import java.util.List;

import org.celllife.mobilisr.domain.Organization;
import org.celllife.mobilisr.domain.Transaction;
import org.celllife.mobilisr.domain.User;
import org.celllife.mobilisr.exception.TransactionNotFoundException;
import org.celllife.mobilisr.utilbean.TransactionSummary;

/**
 * @author Simon Kelly <simon@cell-life.org>
 */
public interface TransactionDAO extends BaseDAO<Transaction, Long> {

	/**
	 * Create transaction to reserve amount from orgnaization's account.
	 * 
	 * @param amount the amount to reserve (+ve integer)
	 * @param organization the organisation to reserve credit for
	 * @param createdFor e.g. campaign identifier
	 * @param createdBy e.g. campaignContact identifier
	 * @param message message to describe reason transaction
	 * @param user user who is performing the action (or null if system action)
	 * @return the ID of the created transaction
	 */
	public Long reserveAmount(int amount, Organization organization, String createdFor, String createdBy, String message, User user);

	/**
	 * Un-reserve an amount from the organisation's account.
	 * 
	 * @param reserveRef the ID of the inital reserve transaction
	 * @param createdFor e.g. campaign identifier
	 * @param createdBy e.g. campaignContact identifier
	 * @param message message to describe reason transaction
	 * @param user user who is performing the action (or null if system action)
	 * @return the ID of the new transaction
	 * @throws TransactionNotFoundException if no transaction with ID = reserveRef is found
	 */
	public Long unreserve(Long reserveRef, String createdFor, String createdBy, String message, User user) throws TransactionNotFoundException;
	
	/**
	 * Un-reserve an amount without a reference to the parent transaction. This should only
	 * be used in situations when the parent transaction is not known.
	 * 
	 * @param amount the amount to un-reserve
	 * @param organization the organisation to un-reserve credit for
	 * @param createdFor e.g. campaign identifier
	 * @param createdBy e.g. campaignContact identifier
	 * @param message message to describe reason transaction
	 * @param user user who is performing the action (or null if system action)
	 * @return the ID of the new transaction
	 */
	public Long unreserve(int amount, Organization organization, String createdFor, String createdBy, String message, User user);

	/**
	 * Debit the organisation account with the amount spent and amount to un-reserve.
	 * These amount may differ in certain situations e.g. Campaign sends
	 * 10 messages and 3 fail. Debit 7 but update reserved by 10.
	 * 
	 * @param reserveRef ID of the reserved transaction
	 * @param amount the amount to debit
	 * @param reserved the amount to un-reserve
	 * @param createdFor e.g. campaign identifier
	 * @param createdBy e.g. campaignContact identifier
	 * @param message message to describe reason transaction
	 * @param user user who is performing the action (or null if system action)
	 * @return the created transaction
	 * @throws TransactionNotFoundException if no transaction with ID = reserveRef is found
	 */
	public Transaction debitOrgAccount(Long reserveRef, int amount, int reserved, 
			String createdFor, String createdBy, String message, User user) throws TransactionNotFoundException;

	/**
	 * Credit the organisation's account.
	 * 
	 * @param amount the amount to credit
	 * @param organization the organisation to credit
	 * @param createdFor e.g. campaign identifier
	 * @param createdBy e.g. campaignContact identifier
	 * @param message message to describe reason transaction
	 * @param user user who is performing the action
	 * @return the ID of the created transaction
	 * @throws IllegalArgumentException if user is null
	 */
	public Long createTransaction(int amount, Organization organization, String createdFor, String createdBy, String message, User user);

	/**
	 * Get a summary of all the transactions for the organisation.
	 * 
	 * @param org
	 * @return TransactionSummary containing the sum of the debits and reserves on the
	 * 			organisation's account as well as a count of the number of transactions
	 * 			used in the calculation.
	 */
	public TransactionSummary getSummaryTransaction(Organization org);

	/**
	 * Get a summary of all the transactions whose ID's are supplied. This summary
	 * will include transactions with the specified ID and all child transactions.
	 * 
	 * @param reserveRefs the ID's of the parent transactions to include in the summary
	 * @return TransactionSummary containing the sum of the debits and reserves of the 
	 * 			transactions as well as a count of the number of transactions
	 * 			used in the calculation.
	 */
	public TransactionSummary getSummaryTransaction(List<Long> reserveRefs);

	/**
	 * Get a summary of the transactions whose ID is supplied and all 
	 * child transactions.
	 * 
	 * @param reserveRef ID of the parent transaction
	 * @return TransactionSummary containing the sum of the debits and reserves of the 
	 * 			transactions as well as a count of the number of transactions
	 * 			used in the calculation.
	 * @throws TransactionNotFoundException
	 */
	public TransactionSummary getSummaryTransaction(Long reserveRef) throws TransactionNotFoundException;

	/**
	 * Get a summary of the transactions whose createdFor field is supplied. This
	 * is usually used to get a summary for a campagin by supplying the
	 * campaign identifier as the createFor parameter.
	 * @param createdFor
	 * @return TransactionSummary for all transactions whose createdFor field 
	 * 			is equal to the parameter supplied.
	 */
	public TransactionSummary getSummaryTransaction(String createdFor);

	/**
	 * Search for credit transactions with messages matching the provided
	 * message.
	 * 
	 * @param message
	 * @return 
	 */
	public List<Transaction> getCreditTransactionsWithMessage(String message);

	/**
	 * Update an organization's balance (to be done very carefully)
	 * @param org
	 */
	void updateOrganizationBalances(Organization org);
}