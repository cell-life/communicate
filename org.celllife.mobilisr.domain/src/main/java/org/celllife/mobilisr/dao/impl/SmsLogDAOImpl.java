package org.celllife.mobilisr.dao.impl;

import com.trg.search.Filter;
import com.trg.search.Search;
import javassist.tools.rmi.ObjectNotFoundException;
import org.celllife.mobilisr.annotation.LogLevel;
import org.celllife.mobilisr.annotation.Loggable;
import org.celllife.mobilisr.api.messaging.DeliveryReceipt;
import org.celllife.mobilisr.api.messaging.SmsMt;
import org.celllife.mobilisr.constants.DatePeriod;
import org.celllife.mobilisr.constants.DeliveryReceiptState;
import org.celllife.mobilisr.constants.SmsStatus;
import org.celllife.mobilisr.dao.api.SmsLogDAO;
import org.celllife.mobilisr.domain.Organization;
import org.celllife.mobilisr.domain.SmsLog;
import org.celllife.mobilisr.utilbean.LogSummary;
import org.gwttime.time.DateTime;
import org.hibernate.Query;
import org.hibernate.transform.Transformers;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.Date;
import java.util.List;

@Repository("smsLogDAO")
public class SmsLogDAOImpl extends BaseDAOImpl<SmsLog, Long> implements SmsLogDAO {

	@Override
	@Transactional(readOnly=true)
	@Loggable(LogLevel.TRACE)
	public List<LogSummary> getSmsLogsPerDay(Organization org, Date from, Date to) {
		return getSmsLogs(org, from, to, DatePeriod.DAY);
	}

	@Override
	@Transactional(readOnly=true)
	@Loggable(LogLevel.TRACE)
	public List<LogSummary> getSmsLogsPerMonth(Organization org, Date from, Date to) {
		return getSmsLogs(org, from, to, DatePeriod.MONTH);
	}

	@Override
	@Transactional(readOnly=true)
	@Loggable(LogLevel.TRACE)
	public List<LogSummary> getSmsLogsPerYear(Organization org, Date from, Date to) {
		return getSmsLogs(org, from, to, DatePeriod.YEAR);
	}

	@Override
	@Transactional(readOnly=true)
	@Loggable(LogLevel.TRACE)
	public List<LogSummary> getSmsLogs(Organization org, Date from,
			Date to, DatePeriod groupBy) {

		StringBuilder sb = new StringBuilder("select year, ");

		switch (groupBy){
		case DAY:
			sb.append("month, day,");
			break;
		case MONTH:
			sb.append(" month,");
			break;
		}

		sb.append(" count(*) as numberOfMessages, sum(error) as numberOfFailuers from (");
		sb.append(" select year(s.datetime) as year,");

		switch (groupBy){
		case DAY:
			sb.append("month(s.datetime) as month, day(s.datetime) as day,");
			break;
		case MONTH:
			sb.append(" month(s.datetime) as month,");
			break;
		}

		sb.append(" (case when s.status like :statusFail then 1 else 0 end) as error");
		sb.append(" from smslog s");
		sb.append(" where s.datetime between :from and :to");

		if (org != null) {
			sb.append(" and s.organization_id = :orgId");
		}

		sb.append(" order by s.datetime) as c group by year");

		switch (groupBy){
		case DAY:
			sb.append(", month, day");
			break;
		case MONTH:
			sb.append(", month");
			break;
		}

		String queryString = sb.toString();
		Query query = getSession().createSQLQuery(queryString);
		query.setString("statusFail", "%FAIL");
		query.setParameter("from", from);
		query.setParameter("to", to);
		if (org != null){
			query.setParameter("orgId", org.getId());
		}
		query.setResultTransformer(Transformers.aliasToBean(LogSummary.class));

		@SuppressWarnings("unchecked")
		List<LogSummary> list = query.list();
		return list;
	}

	@Override
	@Transactional(readOnly=false)
	@Loggable(LogLevel.TRACE)
	public void updateSmsLog(SmsMt mt) {
		StringBuilder sb = new StringBuilder("update SmsLog l");
		sb.append(" set l.").append(SmsLog.PROP_MOBILE_NETWORK).append(" = :network");
		sb.append(" , l.").append(SmsLog.PROP_STATUS).append(" = :status");
		sb.append(" , l.").append(SmsLog.PROP_FAIL_REASON).append(" = :error");
		sb.append(" , l.").append(SmsLog.PROP_TRACKING_NUM).append(" = :trackNum");
		if (mt.getSendingAttempts() != null)
			sb.append(" , l.").append(SmsLog.PROP_ATTEMPTS).append(" = :attempts");
		sb.append(" where l.id = :id");

		Query query = getSession().createQuery(sb.toString());
		query.setString("network", mt.getMobileNetwork());
		query.setParameter("status", mt.getStatus());
		query.setParameter("error", mt.getErrorMessage());
		query.setParameter("trackNum", mt.getMessageTrackingNumber());
		if (mt.getSendingAttempts() != null)
			query.setParameter("attempts", mt.getSendingAttempts());
		query.setParameter("id", mt.getMessageLogId());

		query.executeUpdate();
	}

	@Override
	@Transactional(readOnly=false)
	@Loggable(LogLevel.TRACE)
	public SmsLog updateSmsLog(DeliveryReceipt receipt) throws ObjectNotFoundException {
		DeliveryReceiptState finalStatus = receipt.getFinalStatus();
		SmsStatus smsStatus = DeliveryReceiptState.DELIVRD.equals(finalStatus) ? SmsStatus.TX_SUCCESS
				: SmsStatus.TX_FAIL;

		// update log in one query i.e. without having to load it and save it

		/*
		 * append new wasp status to existing wasp status if existing wasp
		 * status is not null
		 */
		StringBuilder sb = new StringBuilder("update SmsLog set ");
		sb.append("waspstatus = concat(coalesce(waspstatus,''), ")
				.append("case when waspstatus is null then '' else ',' end, ")
				.append(":waspStatus), ");

		String failReason = finalStatus.getMessage();
		if (smsStatus.isFailure() && failReason != null) {
			/*
			 * append new failReason to existing failReason if existing
			 * failReason is not null
			 */
			sb.append("failreason = concat(coalesce(failreason,''), ")
					.append("case when failreason is null then '' else ',' end, ")
					.append(":failreason), ");
		}

		// TODO only change status from success to fail, not from fail to success
		sb.append(SmsLog.PROP_STATUS).append(" = :status").append(" where ")
				.append(SmsLog.PROP_TRACKING_NUM).append(" like :id and ")
				.append(SmsLog.PROP_MSISDN).append(" = :msisdn");

		Query query = getSession().createQuery(sb.toString());
		query.setParameter("waspStatus", finalStatus.name());
		if (smsStatus.isFailure() && failReason != null) {
			query.setParameter("failreason", failReason);
		}
		query.setParameter("status", smsStatus);
		query.setParameter("id", "%" + receipt.getId() + "%");
		query.setParameter("msisdn", receipt.getSourceAddr());

		query.executeUpdate();

		return null;
	}

	@Override
	@Transactional(readOnly=false)
	@Loggable(LogLevel.TRACE)
	public void updateSmsLogStatus(SmsStatus status, List<Long> idList) {
		if (idList.isEmpty()){
			return;
		}
		Query query = getSession().createQuery("UPDATE SmsLog l SET status = :status " +
				"WHERE l.id IN (:list)");

		query.setParameter("status", status);
		query.setParameterList("list", idList);

		query.executeUpdate();
	}
	
	@Override
	@Transactional(readOnly=false)
	@Loggable(LogLevel.TRACE)
	public void updateSmsLogVoided(boolean voided, List<Long> idList) {
		if (idList.isEmpty()){
			return;
		}
		Query query = getSession().createQuery("UPDATE SmsLog l SET voided = :voided " +
				"WHERE l.id IN (:list)");

		query.setParameter("voided", voided);
		query.setParameterList("list", idList);

		query.executeUpdate();
	}

	@Override
	@Transactional(readOnly=false)
	@Loggable(LogLevel.TRACE)
	public void updateUndeliveredMessages(int validityTime) {
		
		Date cutoffdate = new DateTime().minusDays(validityTime).toDate();
		
		Query query = getSession().createQuery("update SmsLog l set status = :failedStatus, " +
				"failreason = :failReason " +
				"where l.status = :sentStatus and l.datetime < :cutoffdate");		
			
		query.setParameter("cutoffdate", cutoffdate);
		query.setParameter("failedStatus", SmsStatus.TX_FAIL);
		query.setParameter("sentStatus", SmsStatus.WASP_SUCCESS);
		query.setParameter("failReason", "Message Validity Time Exceeded");

		query.executeUpdate();
	}

    @Override
    @Transactional(readOnly = true)
    @Loggable(LogLevel.TRACE)
    public Long countFailedMessages(Integer numberOfMessagesToCheck, String msisdn) {

        String queryString = "select count(*) from " +
                "((select * from smslog where msisdn = :msisdn " +
                "order by datetime desc " +
                "limit " +
                numberOfMessagesToCheck +
                ") as temp) " +
                "where status='TX_FAIL'";

        Query query = getSession().createSQLQuery(queryString);
        query.setParameter("msisdn", msisdn);

        return ((BigInteger)query.uniqueResult()).longValue();

    }

    @Override
    @Transactional(readOnly = true)
    @Loggable(LogLevel.TRACE)
    public SmsLog getSmsLog(Long id) {

        Search search = new Search(SmsLog.class);
        search.addFilterAnd(Filter.equal(SmsLog.PROP_ID, id));
        SmsLog smsLog = searchUnique(search);
        return smsLog;

    }

}
