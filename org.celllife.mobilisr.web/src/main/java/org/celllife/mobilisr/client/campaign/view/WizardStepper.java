package org.celllife.mobilisr.client.campaign.view;

import org.celllife.mobilisr.client.model.ViewModel;
import org.celllife.mobilisr.domain.Campaign;

public interface WizardStepper {

	public void goNext(ViewModel<Campaign> viewEntityModel);
	public void goPrevious(ViewModel<Campaign> viewEntityModel);
	public void save(boolean goNext);
	public void cancel();
	
	public void putItem(String key, Object target);
	public Object getItem(String key);

}
