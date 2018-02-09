package org.celllife.mobilisr.service.wasp;


public abstract class RetryCommand {
	
	private int maxRetries = 10;
	private int retryDelay = 2000;
	private int attempts = 0;
	
	public void run() {
		attempts = 1;
		for (; attempts <= maxRetries; attempts++) {
			try {
				if(execute(attempts)){
					break;
				}
				if (attempts < maxRetries)
					Thread.sleep(retryDelay);
			} catch (InterruptedException e) {
				if (!handleInterrupt(e, attempts)){
					break;
				}
			} catch (Exception e) {
				if (!handleError(e, attempts)){
					break;
				}
			} 
		}
		
		if (attempts > maxRetries){
			retriesExceeded();
		}
	}


	protected void retriesExceeded() {
		// defualt implemention does nothing		
	}

	protected abstract boolean execute(int attempt) throws Exception;

	/**
	 * @param e
	 * @param attempt
	 * @return true if more attempts should be made, false to stop
	 */
	protected boolean handleError(Exception e, int attempt) {
		// defualt implemention does nothing	
		return true;
	}


	/**
	 * @param e
	 * @param attempt
	 * @return true if more attempts should be made, false to stop
	 */
	protected boolean handleInterrupt(InterruptedException e, int attempt) {
		// defualt implemention does nothing
		return true;
	}
	
	public void setMaxRetries(int maxRetries) {
		this.maxRetries = maxRetries;
	}
	
	public void setRetryDelay(int retryDelay) {
		this.retryDelay = retryDelay;
	}
	
	public int getAttempts() {
		return attempts;
	}
}
