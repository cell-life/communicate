package org.celllife.mobilisr.service.wasp;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;

public class HttpClientFactory {
	
	public static HttpClient getClient() {
		HttpParams params = new BasicHttpParams();
		ThreadSafeClientConnManager connManager = new ThreadSafeClientConnManager();
		connManager.setMaxTotal(10);
		connManager.setDefaultMaxPerRoute(8);
		DefaultHttpClient client = new DefaultHttpClient(connManager, params);
		client.setHttpRequestRetryHandler(new DefaultHttpRequestRetryHandler(4,false));
		return client;
	}
}
