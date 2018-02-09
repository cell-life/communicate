package org.celllife.communicate;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.celllife.mobilisr.service.utility.MobilisrUtility;
import org.celllife.mobilisr.service.wasp.IntegratHttpOutHandler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class IntegratSimulationServer extends HttpSimulationServer {
	
	private static final Logger log = LoggerFactory.getLogger(IntegratSimulationServer.class); 
	
	private ExecutorService pool;
	private DefaultHttpClient client;
	private String baseUrl = "http://localhost:8181/communicate";
	private String userName = "admin";
	private String password = "admin";

	private long responseDelay = 0;
	private long deliveryReceiptDelay = 1000;

	private AtomicInteger requestCounter = new AtomicInteger();
	private AtomicInteger responseSuccessCounter = new AtomicInteger();
	private AtomicInteger responseFailCounter = new AtomicInteger();
	private AtomicInteger totalRequestCounter = new AtomicInteger();
	private AtomicInteger totalResponseFailCounter = new AtomicInteger();
	private AtomicInteger totalResponseSuccessCounter = new AtomicInteger();
	private AtomicBoolean exit = new AtomicBoolean();	

	public IntegratSimulationServer(int port, int nThreads) {
		super(port);
		pool = Executors.newFixedThreadPool(nThreads);
		
		initHttpClient();
		
		setHandler(new IntegratHandler());
	}
	
	@Override
	public void start() throws Exception {
		super.start();
		new TrafficWatcherThread().start();
	}
	
	@Override
	public void stop() throws Exception {
		super.stop();
		exit.set(true);
		pool.shutdown();
		client.getConnectionManager().shutdown();
	}
	
	public void setDeliveryReceiptDelay(long deliveryReceiptDelay) {
		this.deliveryReceiptDelay = deliveryReceiptDelay;
	}
	
	public void setResponseDelay(long responseDelay) {
		this.responseDelay = responseDelay;
	}
	
	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}
	
	public void setUserName(String userName) {
		this.userName = userName;
	}
	
	public void setPassword(String password) {
		this.password = password;
	}

	private void initHttpClient() {
		HttpParams params = new BasicHttpParams();
		SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(new Scheme("https", 80, PlainSocketFactory.getSocketFactory()));
		ThreadSafeClientConnManager connManager = new ThreadSafeClientConnManager();
		connManager.setMaxTotal(10);
		connManager.setDefaultMaxPerRoute(10);
		DefaultHttpClient tmpClient = new DefaultHttpClient(connManager, params);
		tmpClient.setHttpRequestRetryHandler(new DefaultHttpRequestRetryHandler(4,
				false));
		tmpClient.getCredentialsProvider().setCredentials(
                new AuthScope(null, -1),
                new UsernamePasswordCredentials(userName, password));
		client = tmpClient;
	}
	
	public class IntegratHandler extends AbstractHandler {
		
		public void handle(String target, Request baseRequest,
				HttpServletRequest request, HttpServletResponse response)
				throws IOException, ServletException {

			String requestString = IOUtils.toString(request.getInputStream());
			String refNum = MobilisrUtility.findValueForRegExp(requestString,
					IntegratHttpOutHandler.MT_WASPRSP_REGEXP_REFNUM);

			String seqNum = RandomStringUtils.randomNumeric(10);

			String msgPostFromWasp = MessageFormat
					.format("<Response status_code=\"0\" token=\"#%#TOK_I1JlZjwwLjAuMC4xMTg5ND4=\">"
							+ "<Data name=\"msg_generic_rsp\"><field name=\"msg_no\" value=\"2\"/>"
							+ "<field name=\"seq_no\" value=\"{0}\"/></Data></Response>",
							seqNum);
			
			try {
				Thread.sleep(responseDelay );
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}


			response.setContentType("text/html;charset=utf-8");
			response.setStatus(HttpServletResponse.SC_OK);
			baseRequest.setHandled(true);
			response.getWriter().println(msgPostFromWasp);
			
			log.debug("Message received: [refNum={}] [seqNum={}]", refNum, seqNum);
			requestCounter.incrementAndGet();
			pool.execute(new DeliveryReceiptRunner(seqNum, refNum));
		}
	}
	
	public class DeliveryReceiptRunner implements Runnable {

		private final String seqNum;
		private final String refNum;
		private String receiptTemplate;

		public DeliveryReceiptRunner(String seqNum, String refNum) {
			this.seqNum = seqNum;
			this.refNum = refNum;
			buildReceiptTemplate();
		}

		private void buildReceiptTemplate() {
			StringBuilder mtRspTemplateBuilder = new StringBuilder();
			mtRspTemplateBuilder
					.append("<Message><Version Version=\"1.0\"/><CreditBalance Account=\"-10\" Client=\"0\"/>");
			mtRspTemplateBuilder
					.append("<Response Type=\"OnResult\" TOC=\"SMS\" RefNo=\"{0}\" SeqNo=\"{1}\">");
			mtRspTemplateBuilder
					.append("<SystemID>Higate</SystemID><UserID>cell-lifesim</UserID>");
			mtRspTemplateBuilder
					.append("<Service>CLFESIM</Service><NetworkID>{2}</NetworkID>");
			mtRspTemplateBuilder
					.append("<OnResult Flags=\"0\" Code=\"{3}\" SubCode=\"0\" Text=\"{4}\"/>");
			mtRspTemplateBuilder.append("</Response></Message>");
			receiptTemplate = mtRspTemplateBuilder.toString();
		}

		@Override
		public void run() {
			try {
				Thread.sleep(deliveryReceiptDelay );
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}

			try{
				String message = MessageFormat.format(receiptTemplate, refNum,
						seqNum, 1, 4,
						IntegratHttpOutHandler.IntegratStatus.RC_RECEIPTED);
				HttpPost post = new HttpPost(baseUrl + "/api/callback/integrat");
				StringEntity entity = new StringEntity(message, "ISO-8859-1");
				entity.setContentType("text/xml");
				post.setEntity(entity);
	
				ResponseHandler<Integer> handler = new ResponseHandler<Integer>() {
					public Integer handleResponse(HttpResponse response)
							throws ClientProtocolException, IOException {
						return response.getStatusLine().getStatusCode();
					}
				};
				
				int statusCode = client.execute(post, handler);
				log.debug("Delivery receipt sent to server: [seqNum={}], [refNum={}], [statusCode={}]", 
						new Object[]{seqNum, refNum, statusCode});
				if (statusCode != 200){
					responseFailCounter.incrementAndGet();
					throw new RuntimeException("Bad status code: " + statusCode);
				} else {
					responseSuccessCounter.incrementAndGet();
				}
			} catch (Exception e){
				e.printStackTrace();
			}
		}
	}

    private class TrafficWatcherThread extends Thread {
        @Override
        public void run() {
            log.info("Starting traffic watcher...");
            while (!exit.get()) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                }
                int requestPerSecond = requestCounter.getAndSet(0);
                int responsePerSecondFail = responseFailCounter.getAndSet(0);
                int responsePerSecondSuccess = responseSuccessCounter.getAndSet(0);
                int totalRequest = totalRequestCounter.addAndGet(requestPerSecond);
                int totalFail = totalResponseFailCounter.addAndGet(responsePerSecondFail);
                int totalSuccess = totalResponseSuccessCounter.addAndGet(responsePerSecondSuccess);
                
                log.info("Request per second : {} / {}. Response per second : success : {} / {} : fail : {} / {}", 
                		new Object[]{requestPerSecond, totalRequest, responsePerSecondSuccess,
                		totalSuccess, responsePerSecondFail, totalFail});
            }
        }
    }

	public int getTotalResponses() {
		return totalResponseFailCounter.get() + totalResponseSuccessCounter.get();
	}
	
	public static void main(String[] args) throws Exception {
		IntegratSimulationServer simulationServer = new IntegratSimulationServer(8065, 20);
		simulationServer.start();
	}
}
