package org.celllife.mobilisr.client.campaign.view;

import org.celllife.mobilisr.client.model.ViewModel;
import org.celllife.mobilisr.domain.Campaign;

import com.google.gwt.user.client.ui.Widget;


public interface WizardStep {
	
	public void goNext();
	public void goPrevious();
	public void save();
	public void cancel();
	
	public ViewModel<Campaign> getFormObject() ;
	public void setFormObject(ViewModel<Campaign> vem);
	public Widget getViewWidget();
	public void createView();
	public void resetForm();
}
