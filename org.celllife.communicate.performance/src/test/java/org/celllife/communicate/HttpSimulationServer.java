package org.celllife.communicate;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;

public class HttpSimulationServer {
	
	private Server server;

	public HttpSimulationServer(int port) {
		server = new Server(port);
	}
	
	public void start() throws Exception{
	    server.start();
	}
	
	public void stop() throws Exception{
		server.stop();
	}
	
	public void setHandler(Handler handler){
		server.setHandler(handler);
	}

}
