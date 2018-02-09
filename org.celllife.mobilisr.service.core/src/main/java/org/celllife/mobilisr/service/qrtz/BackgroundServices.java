package org.celllife.mobilisr.service.qrtz;


/**
 * Interface for services that run in the background
 * 
 * @author Vikram Bindal (e-mail: vikram@cell-life.org)
 *
 */
public interface BackgroundServices {

	public void processCampContactProgress();

	public void updateOrganizationBalances();

	public void processCampFinish();
	
	public void processMailQueue();

	void triggerMessageProcessing();

}
