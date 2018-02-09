package org.celllife.mobilisr.service.message.processors;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.celllife.mobilisr.constants.SmsStatus;
import org.celllife.mobilisr.dao.api.SmsLogDAO;
import org.celllife.mobilisr.domain.SmsLog;
import org.celllife.mobilisr.service.message.route.MessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.trg.search.Search;

/**
 * This class selects all incoming messages from the database that have a status
 * QUEUED_SUCCESS and sends them for processing.
 *
 * Calling the {@link #triggerMessageProcessing()} method launches a worker thread
 * to fetch the messages from the database and send them to the processor. A new worker
 * is only launched if there isn't one already running.
 *
 * @author Simon Kelly
 *
 */
@Component("MessageReprocessor")
public class MessageReprocessor implements InitializingBean, DisposableBean {

	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(MessageReprocessor.class);

	@Autowired
	private SmsLogDAO smslogDao;

	@Autowired
	private MessageService messageService;

	private ThreadPoolExecutor queueTaskExecutor;

	public void triggerMessageProcessing(){
		queueTaskExecutor.execute(new MessageProcessingLauncherTask());
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		/*
		 * Thread pool for running the MessageProcessingLauncherTask
		 *
		 * - Only has one thread to ensure only one task running at a time.
		 * - Queue size is two to allow only two queued tasks
		 */
		queueTaskExecutor = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
				new LinkedBlockingQueue<Runnable>(2));
		queueTaskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardPolicy());
	}

	@Override
	public void destroy() throws Exception {
		queueTaskExecutor.shutdown();
	}

	public class MessageProcessingLauncherTask implements Runnable {

		@Override
		public void run() {
			List<SmsLog> queuedLogs = null;
			int maxResults = 100;
			do {
				Search search = new Search();
				search.addFilterEqual(SmsLog.PROP_STATUS, SmsStatus.QUEUED_SUCCESS);
				search.addFilterEqual(SmsLog.PROP_DIR, SmsLog.SMS_DIR_IN);
				search.addFetch(SmsLog.PROP_CHANNEL);
				search.addSort(SmsLog.PROP_ID, false);
				search.setMaxResults(maxResults);

				queuedLogs = smslogDao.search(search);
				List<Long> queuedLogIds = new ArrayList<Long>();
				for (SmsLog smsLog : queuedLogs) {
					queuedLogIds.add(smsLog.getId());
				}
				smslogDao.updateSmsLogStatus(SmsStatus.QUEUED_PROCESSING, queuedLogIds);

				for (final SmsLog smsLog : queuedLogs) {
					messageService.processMessage(smsLog);
				}

			} while (queuedLogs != null && !queuedLogs.isEmpty());
		}
	}
}
