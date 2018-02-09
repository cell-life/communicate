package org.celllife.mobilisr.service.impl;

import java.util.List;

import org.celllife.mobilisr.annotation.LogLevel;
import org.celllife.mobilisr.annotation.Loggable;
import org.celllife.mobilisr.dao.api.OrganizationDAO;
import org.celllife.mobilisr.dao.api.TransactionDAO;
import org.celllife.mobilisr.domain.Campaign;
import org.celllife.mobilisr.domain.Organization;
import org.celllife.mobilisr.domain.Transaction;
import org.celllife.mobilisr.domain.User;
import org.celllife.mobilisr.exception.DuplicateTransactionException;
import org.celllife.mobilisr.exception.InsufficientBalanceException;
import org.celllife.mobilisr.exception.TransactionNotFoundException;
import org.celllife.mobilisr.service.MailService;
import org.celllife.mobilisr.service.UserBalanceService;
import org.celllife.mobilisr.utilbean.TransactionSummary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;

/**
 * Default implementation for the UserBalanceService interface
 * @author Vikram Bindal (e-mail: vikram@cell-life.org)
 * @author Simon Kelly (e-mail: simon@cell-life.org)
 */
@Service("userBalanceService")
public class UserBalanceServiceImpl implements UserBalanceService {

	private static Logger log = LoggerFactory.getLogger(UserBalanceServiceImpl.class);

	@Autowired
	private OrganizationDAO orgDAO;

	@Autowired
	private TransactionDAO transactionDao;

	@Autowired
	private MailService mailService;

	@Loggable(LogLevel.TRACE)
	@Override
	public Long debitOrgBalance(int amountToDebit, int reservedUsed,
			Long reference, String createdFor,
			String createdBy, String transMsg, User user) throws TransactionNotFoundException{

		log.debug("Debiting organization account: [refId={}] [credits={}] [reserved={}]",
				new Object[]{reference, amountToDebit, reservedUsed});
		try {
			Transaction t = transactionDao.debitOrgAccount(reference, amountToDebit, reservedUsed,
					createdFor, createdBy, transMsg, user);
			return t.getId();
		} catch (TransactionNotFoundException e) {
			mailService.sendSystemAlert("Transaction not found for reference " + reference
					+ ". Unable to debit account: amount=" + amountToDebit + " reserved="
					+ reservedUsed + "createdFor=" + createdFor);
			throw e;
		}
	}
	
	@Loggable(LogLevel.TRACE)
	@Override
	@Secured({"PERM_ORGANISATIONS_CREDIT_BALANCE"})
	public Long credit(int amount, Organization organization, String createdFor,
			String createdBy, String message, User user) throws DuplicateTransactionException {
		amount = Math.abs(amount);
		return creditDebit(amount, organization, createdFor, createdBy, message, user);
	}
	
	@Loggable(LogLevel.TRACE)
	@Override
	@Secured({"PERM_ORGANISATIONS_CREDIT_BALANCE"})
	public Long debit(int amount, Organization organization, String createdFor,
			String createdBy, String message, User user) throws DuplicateTransactionException {
		amount = -Math.abs(amount);
		return creditDebit(amount, organization, createdFor, createdBy, message, user);
	}

	private Long creditDebit(int amount, Organization organization, String createdFor,
			String createdBy, String message, User user) throws DuplicateTransactionException {
		if (amount == 0){
			return null;
		}

		String transactionMessage = (amount > 0 ? "Credit: " : "Debit: " ) + organization.getName() + ": " + message;
		// NB: Since we check for uniqueness of transactionMessage, changing the format of this
		//   message may have unintended consequences. Consider changes carefully.
		List<Transaction> transactions = transactionDao.getCreditTransactionsWithMessage(transactionMessage);
		if (!transactions.isEmpty()){
			throw new DuplicateTransactionException("A transaction with the message '"
					+ message + "' already exists");
		}

		log.debug("Credit/debit organisation account [userId={}] [orgId={}] [amount={}]",
				new Object[] {user == null ? null : user.getId(), organization.getId(), amount});
		Long reference = transactionDao.createTransaction(
				amount, organization, createdFor, createdBy, transactionMessage, user);

		if (user != null) { // User is null for system refunds; Don't send email for that case.
			mailService.sendCreditNotification(organization, user, amount, message);
		}

		return reference;
	}

	@Override
	public Long reserveAmount(Organization organization, int amountToReserve,
			String createdFor, User user, String message) throws InsufficientBalanceException {
		String createdById = "SYSTEM";
		if (user != null){
			createdById = user.getIdentifierString();
		}
		return reserveAmount(user, amountToReserve, organization,
				createdFor, createdById, message);
	}

	private Long reserveAmount(User user, int amountToReserve, Organization organization,
								String createdFor, String createdBy,
								String message) throws InsufficientBalanceException {
		if (amountToReserve < 0){
			log.warn("'amountToReserve' is negative. Making it positive.");
			amountToReserve = Math.abs(amountToReserve);
		} else if (amountToReserve == 0){
			return null;
		}

		Long reference = saveReservedAmount(user, amountToReserve, organization,
											createdFor, createdBy, message);
		return reference;
	}

	@Loggable(LogLevel.TRACE)
	@Override
	public void unreserve(Long transactionId, Campaign campaign, String message, User user)
					throws TransactionNotFoundException {
		transactionDao.unreserve(transactionId, campaign.getIdentifierString(),
				user == null ? "SYSTEM" : user.getIdentifierString(), message, user);
	}

	@Loggable(LogLevel.TRACE)
	@Override
	public void verifyZeroReserved(Campaign campaign, String message, User user) {
		TransactionSummary summary = transactionDao.getSummaryTransaction(
										campaign.getIdentifierString() );
		if (summary.getReserved() > 0) {
			String newMessage = message + " (" + campaign.getName() + " [" + campaign.getId()
			+ "]) - reversing reservation of unused credits for campaign.";
			String createdBy = "SYSTEM";
			if (user != null)
				createdBy = user.getIdentifierString();
			else
				createdBy = campaign.getIdentifierString();
			Long ref = transactionDao.unreserve(summary.getReserved(), campaign.getOrganization(),
					campaign.getIdentifierString(), createdBy, newMessage, user);

			mailService.sendSystemAlert("Non-zero reserved amount for campaign [name='"
					+ campaign.getName() + "'] [id=" + campaign.getId()
					+ "] [message='" + message + "'] [newTransactionRef=" + ref + "]");
		}
	}

	@Loggable(LogLevel.TRACE)
	@Override
	public void verifyZeroReserved(Long reservedRef, Campaign campaign, String message, User user)
					throws TransactionNotFoundException {
		String userString;
		if (user != null)
			userString = user.getIdentifierString();
		else
			userString = null;
		String newMessage = message + " - reversing reservation of unused credits for Ref: "
		+ reservedRef + " (campaign: " + campaign.getName() + " [" + campaign.getId() + "])";
		// Don't need to check transaction summary for zero reserved balance, since unreserve()
		// will do that for us, so just call it.
		Long ref = transactionDao.unreserve(reservedRef, campaign.getIdentifierString(),
				userString, newMessage, user);
		// If reserved balance was zero, unreserve() will return null (transaction not created).
		if (ref != null) {
			mailService.sendSystemAlert("Non-zero reserved amount for campaign [name='"
					+ campaign.getName() + "'] [id=" + campaign.getId()
					+ "] [message='" + newMessage + "'] [newTransactionRef=" + ref + "]");
		}
	}

	@Loggable(LogLevel.TRACE)
	@Override
	public void updateOrgBalances(){
		List<Organization> allOrgs = orgDAO.findAll();
		for (Organization org : allOrgs) {
			checkBalanceAgainstThreshold(org);
		}
	}

	@Loggable(LogLevel.TRACE)
	private void checkBalanceAgainstThreshold(Organization organization) {
		int balance = organization.getBalance();
		int reserved = organization.getReserved();
		transactionDao.updateOrganizationBalances(organization);

		int balance2 = organization.getBalance();
		int reserved2 = organization.getReserved();
		if (log.isDebugEnabled()) {
			log.debug("Re-calculated org balance from transactions: "
					+ "[org={}] [newBalance={}] [newReserved={}]",
					new Object[] {organization.getId(), balance2, reserved2});
		}

		if (balance != balance2 || reserved != reserved2){
			String msg = "Organization balances out of sync [orgId="+organization.getId()+"]"
				+ " [oldBalance="+balance+"] [newBalance="+balance2+"]"
				+ " [oldReserved="+reserved+"] [newReserved="+reserved2+"]";
			mailService.sendSystemAlert(msg);
		}

		int threshold = organization.getBalanceThreshold();
		double availableBalance = organization.getAvailableBalance();

		if (availableBalance < threshold) {
			log.info("Balance below threshold, sending alerts. [org={}]", organization.getId());
			mailService.sendBalanceLowAlert(organization);
		}
	}

	private Long saveReservedAmount(User user, int amountToReserve,
			Organization organization, String createdFor, String createdBy,
			String message) throws InsufficientBalanceException {

		// TODO? how do we prevent multiple threads reserving credit at the same time for the same org?
		// should this be moved to the dao which is transactional?
		synchronized (organization.getId()) {

			orgDAO.refresh(organization);
			log.debug("Reserving amount for organization "
					+ "[id={}] [currentBalance={}] [currentReserved={}] [amountToReserve={}]",
					new Object[]{ organization.getId(), organization.getBalance(),
							organization.getReserved(), amountToReserve});
			int availableBalance = organization.getAvailableBalance();
			if (availableBalance >= amountToReserve) {
				Long reference = transactionDao.reserveAmount(amountToReserve, organization,
									createdFor, createdBy, message, user);
				// TODO: figure out what to do about this
		//			checkBalanceAgainstThreshold(organization);
				return reference;
			} else {
				throw new InsufficientBalanceException(amountToReserve, availableBalance, message);
			}
		}
	}

	@Override
	public TransactionSummary getAccountSummary(Campaign campaign) {
		return transactionDao.getSummaryTransaction(campaign.getIdentifierString());
	}

	@Override
	public TransactionSummary getAccountSummary(String createdFor) {
		return transactionDao.getSummaryTransaction(createdFor);
	}

	@Override
	public TransactionSummary getAccountSummary(Organization organization) {
		return transactionDao.getSummaryTransaction(organization);
	}
}
