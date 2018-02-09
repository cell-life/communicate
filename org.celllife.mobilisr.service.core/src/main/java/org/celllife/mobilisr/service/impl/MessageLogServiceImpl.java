package org.celllife.mobilisr.service.impl;

import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.trg.search.Filter;
import com.trg.search.Search;
import com.trg.search.SearchResult;
import org.celllife.mobilisr.annotation.LogLevel;
import org.celllife.mobilisr.annotation.Loggable;
import org.celllife.mobilisr.constants.SmsStatus;
import org.celllife.mobilisr.dao.api.SmsLogDAO;
import org.celllife.mobilisr.domain.*;
import org.celllife.mobilisr.service.MessageLogService;
import org.celllife.mobilisr.service.qrtz.BackgroundServices;
import org.celllife.mobilisr.service.utility.ServiceUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("messageLogService")
public class MessageLogServiceImpl implements MessageLogService {

	@Autowired
	private SmsLogDAO smslogDao;

	@Autowired
	private BackgroundServices backgroundService;

	@Loggable(LogLevel.TRACE)
	@Override
	public PagingLoadResult<SmsLog> getMessageLogsForEntity(MobilisrEntity entity, PagingLoadConfig loadConfig){
		Search search = ServiceUtil.getSearchFromLoadConfig(SmsLog.class, loadConfig, null);

		if (loadConfig.getSortField() == null)
			search.addSort(SmsLog.PROP_DATE_TIME, true);
		
		if (entity instanceof Channel) {
			search.addFilterEqual(SmsLog.PROP_CHANNEL, entity);
		} else if (entity instanceof Contact) {
			search.addFilterEqual(SmsLog.PROP_CONTACT, entity);
		} else if (entity instanceof Organization) {
			search.addFilterEqual(SmsLog.PROP_ORGANIZATION, entity);
		} else {
			search.addFilterEqual(SmsLog.PROP_CREATEDFOR, entity.getIdentifierString());
		}

		SearchResult<SmsLog> searchResult = smslogDao.searchAndCount(search);
		return ServiceUtil.getPagingLoadResult(loadConfig, searchResult);
	}

	@Loggable(LogLevel.TRACE)
	@Override
	public PagingLoadResult<SmsLog> getMessageLogsByStatus(SmsStatus[] statuses,
			PagingLoadConfig loadConfig){
		Search search = ServiceUtil.getSearchFromLoadConfig(SmsLog.class,
				loadConfig, SmsLog.PROP_MSISDN);
		Filter[] filters = new Filter[statuses.length];
		for (int i=0; i < statuses.length; i++) {
			filters[i] = Filter.equal(SmsLog.PROP_STATUS, statuses[i]);
		}
		search.addFilterOr(filters);
		SearchResult<SmsLog> searchResult = smslogDao.searchAndCount(search);
		return ServiceUtil.getPagingLoadResult(loadConfig, searchResult);
	}

	@Loggable(LogLevel.TRACE)
	@Override
	public void reprocessMessages(List<Long> logEntryIds) {
		smslogDao.updateSmsLogStatus(SmsStatus.QUEUED_SUCCESS, logEntryIds);
		backgroundService.triggerMessageProcessing();
	}

    @Loggable(LogLevel.TRACE)
    @Override
    public SmsLog getMessageLog(Long id) {

        return smslogDao.getSmsLog(id);

    }

	@Loggable(LogLevel.TRACE)
	@Override
	public void voidMessages(List<Long> logEntryIds) {
		smslogDao.updateSmsLogVoided(true, logEntryIds);
	}

}
