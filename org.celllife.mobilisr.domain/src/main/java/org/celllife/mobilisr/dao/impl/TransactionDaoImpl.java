package org.celllife.mobilisr.dao.impl;

import java.util.Arrays;
import java.util.List;

import org.celllife.mobilisr.annotation.LogLevel;
import org.celllife.mobilisr.annotation.Loggable;
import org.celllife.mobilisr.dao.api.MobilisrGeneralDAO;
import org.celllife.mobilisr.dao.api.TransactionDAO;
import org.celllife.mobilisr.domain.Organization;
import org.celllife.mobilisr.domain.Transaction;
import org.celllife.mobilisr.domain.User;
import org.celllife.mobilisr.exception.TransactionNotFoundException;
import org.celllife.mobilisr.utilbean.TransactionSummary;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.transform.Transformers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.trg.search.Search;

@Repository("transactionDao")
public class TransactionDaoImpl extends
		BaseDAOImpl<Transaction, Long> implements TransactionDAO {
	
	@Autowired
	private MobilisrGeneralDAO generalDAO;
	
	@Loggable(LogLevel.TRACE)
	@Override
	@Transactional
	public Long reserveAmount(int amount, Organization organization, 
			String createdFor, String createdBy, String message, User user) {
		
		amount = Math.abs(amount);
		Transaction t = new Transaction(0, amount, createdFor, createdBy, message, user, organization);
		save(t);
		updateOrganizationBalances(organization,0, amount);
		return t.getId();
	}

	@Loggable(LogLevel.TRACE)
	@Override
	@Transactional
	public Transaction debitOrgAccount(Long reserveRef, int amount, int reserved,
			String createdFor, String createdBy, String message, User user) throws TransactionNotFoundException {
		if (reserveRef == null){
			throw new TransactionNotFoundException("Null transaction ID");
		}
		
		Transaction parent = find(reserveRef);
		if (parent == null){
			throw new TransactionNotFoundException("Transaction not found: " + reserveRef);
		}
		
		amount = -Math.abs(amount);
		reserved = -Math.abs(reserved);
		Transaction t = new Transaction(amount, reserved, createdFor, createdBy, message, user, parent.getOrganization());
		t.setParent(parent);
		save(t);
		
		updateOrganizationBalances(parent.getOrganization(), amount, reserved);
		return t;
	}
	
	@Loggable(LogLevel.TRACE)
	@Override
	@Transactional
	@Secured({"PERM_ORGANISATIONS_CREDIT_BALANCE"})
	public Long createTransaction(int amount, Organization organization, String createdFor,
			String createdBy, String message, User user) {
		if (organization == null){
			throw new IllegalArgumentException("Organization can not be null");
		}
		
		Transaction t = new Transaction(amount, 0, createdFor, createdBy, message, user, organization);
		save(t);
		updateOrganizationBalances(organization, amount, 0);
		return t.getId();
	}
	
	@Loggable(LogLevel.TRACE)
	@Override
	@Transactional(readOnly=true)
	public TransactionSummary getSummaryTransaction(Organization org) {
		Criteria criteria = getCriteriaForTransactionSummary(); 
		
		criteria.createCriteria(Transaction.PROP_ORGANIZATION).add(Restrictions.eq(Organization.PROP_ID, org.getId()));
		TransactionSummary result = (TransactionSummary) criteria.uniqueResult();
		result.makeNullsZero();
		return result;
	}

	@Loggable(LogLevel.TRACE)
	@Override
	@Transactional(readOnly=true)
	public TransactionSummary getSummaryTransaction(String createdFor) {
		Criteria criteria = getCriteriaForTransactionSummary();
		
		criteria.add(Restrictions.eq(Transaction.PROP_CREATED_FOR, createdFor));
		
		TransactionSummary result = (TransactionSummary) criteria.uniqueResult();
		result.makeNullsZero();
		return result;
	}
	
	@Loggable(LogLevel.TRACE)
	@Override
	@Transactional
	public Long unreserve(Long reserveRef, String createdFor, String createdBy,
			String message, User user) throws TransactionNotFoundException {
		if (reserveRef == null){
			throw new IllegalArgumentException("Null transaction id");
		}
		
		Transaction parent = find(reserveRef);
		if (parent == null){
			throw new TransactionNotFoundException("Transaction not found: " + reserveRef);
		}
		
		
		TransactionSummary summary = getSummaryTransaction(reserveRef);
		if (summary.getReserved() > 0){
			Transaction unreserve = new Transaction(0, -summary.getReserved(), createdFor, createdBy, message, user, parent.getOrganization());
			unreserve.setParent(parent);
			save(unreserve);
			updateOrganizationBalances(parent.getOrganization(), 0, -summary.getReserved());
			return unreserve.getId();
		}
		return null;
	}
	
	@Loggable(LogLevel.TRACE)
	@Override
	@Transactional
	public Long unreserve(int amount, Organization organization, String createdFor,
			String createdBy, String message, User user) {

		amount = -Math.abs(amount);
		Transaction unreserve = new Transaction(0, amount, createdFor, createdBy, message, user, organization);
		save(unreserve);
		updateOrganizationBalances(organization, 0, amount);
		return unreserve.getId();
	}
	
	@Loggable(LogLevel.TRACE)
	@Override
	@Transactional(readOnly=true)
	public TransactionSummary getSummaryTransaction(List<Long> refs){
		Criteria criteria = getCriteriaForTransactionSummary();
		
		if (refs != null){
			criteria.add(Restrictions.or(Restrictions.in(Transaction.PROP_ID, refs), 
					Restrictions.in(Transaction.PROP_PARENT+".id", refs)));
		}
		
		TransactionSummary result = (TransactionSummary) criteria.uniqueResult();
		result.makeNullsZero();
		return result;
	}
	
	@Loggable(LogLevel.TRACE)
	@Override
	@Transactional(readOnly=true)
	public TransactionSummary getSummaryTransaction(Long ref) throws TransactionNotFoundException{
		Transaction parent = find(ref);
		if (parent == null){
			throw new TransactionNotFoundException("Transaction not found: " + ref);
		}
		return getSummaryTransaction(Arrays.asList(ref));
	}
	
	@Loggable(LogLevel.TRACE)
	@Override
	@Transactional(readOnly=true)
	public List<Transaction> getCreditTransactionsWithMessage(String message) {
		Search s = new Search();
		s.addFilterEqual(Transaction.PROP_MSG, message);
		s.addFilterGreaterThan(Transaction.PROP_COST, 0);
		List<Transaction> transactions = search(s);
		return transactions;
	}
	
	@Override
	@Transactional
	@Loggable(LogLevel.TRACE)
	public void updateOrganizationBalances(Organization org) {
		generalDAO.refresh(org);
		TransactionSummary summary = getSummaryTransaction(org);
		org.setBalance(summary.getCost());
		org.setReserved(summary.getReserved());
		generalDAO.save(org);
	}
	
	private void updateOrganizationBalances(Organization organization, int amount, int reserved) {
		StringBuilder sb = new StringBuilder();
		sb.append("update Organization o");
		sb.append(" set o.").append(Organization.PROP_BALANCE).append(" = ")
				.append(Organization.PROP_BALANCE).append(" + :amount");
		sb.append(" ,o.").append(Organization.PROP_RESERVED).append(" = ")
		.append(Organization.PROP_RESERVED).append(" + :reserved");
		sb.append(" where o.").append(Organization.PROP_ID).append(" = :id");
		
		Query query = getSession().createQuery(sb.toString());
		query.setParameter("amount", amount);
		query.setParameter("reserved", reserved);
		query.setParameter("id", organization.getId());
		
		query.executeUpdate();
	}
	
	/**
	 * Get the basic criteria for transaction summary without any restrictions.
	 * 
	 * @return
	 */
	private Criteria getCriteriaForTransactionSummary() {
		Criteria criteria = getSession().createCriteria(Transaction.class);
		ProjectionList projections = Projections.projectionList()
			.add(Projections.sum(Transaction.PROP_COST), Transaction.PROP_COST)
			.add(Projections.sum(Transaction.PROP_RESERVED),Transaction.PROP_RESERVED)
			.add(Projections.count(Transaction.PROP_ID),TransactionSummary.PROP_TRANSACTION_COUNT);
		criteria.setProjection(projections);
		criteria.setResultTransformer(Transformers.aliasToBean(TransactionSummary.class));
		return criteria;
	}
}
