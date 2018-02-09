package org.celllife.mobilisr.dao.api;

import javassist.tools.rmi.ObjectNotFoundException;
import org.celllife.mobilisr.api.messaging.DeliveryReceipt;
import org.celllife.mobilisr.api.messaging.SmsMt;
import org.celllife.mobilisr.constants.DatePeriod;
import org.celllife.mobilisr.constants.SmsStatus;
import org.celllife.mobilisr.domain.Organization;
import org.celllife.mobilisr.domain.SmsLog;
import org.celllife.mobilisr.utilbean.LogSummary;

import java.util.Date;
import java.util.List;

public interface SmsLogDAO extends BaseDAO<SmsLog, Long> {

	List<LogSummary> getSmsLogsPerDay(Organization org, Date from, Date to);

	List<LogSummary> getSmsLogsPerMonth(Organization org, Date from, Date to);

	List<LogSummary> getSmsLogsPerYear(Organization org, Date from, Date to);

	List<LogSummary> getSmsLogs(Organization org, Date from, Date to, DatePeriod period);

	void updateSmsLog(SmsMt mt);

	SmsLog updateSmsLog(DeliveryReceipt receipt) throws ObjectNotFoundException;

	void updateSmsLogStatus(SmsStatus status, List<Long> idList);

	void updateUndeliveredMessages(int validityTime);

	void updateSmsLogVoided(boolean voided, List<Long> idList);

    Long countFailedMessages(Integer numberOfMessagesToCheck, String msisdn);

    SmsLog getSmsLog(Long id);

}
