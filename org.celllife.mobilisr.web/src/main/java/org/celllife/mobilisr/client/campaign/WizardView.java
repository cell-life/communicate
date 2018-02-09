package org.celllife.mobilisr.client.campaign;

import java.util.List;

import org.celllife.mobilisr.client.app.EntityCreate;
import org.celllife.mobilisr.client.model.ViewModel;
import org.celllife.mobilisr.client.view.gxt.MyGXTButton;
import org.celllife.mobilisr.domain.Campaign;

import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.store.ListStore;

public interface WizardView extends EntityCreate<Campaign> {

	public int getStepSelected();
	public List<MyGXTButton> getSteps();
	
	public void putItem(String key, Object target);
	public Object getItem(String key);
	
	public void addListener(EventType eventType, Listener<? extends BaseEvent> listener);
	public void resetForms();
	public void setOrganizationStore(ListStore<BeanModel> store);
    public void setCampaignStore(ListStore<BeanModel> store);
	public void goNext(ViewModel<Campaign> model);
	public void goPrevious(ViewModel<Campaign> model);
	public void goCurrent(ViewModel<Campaign> vem);
	public void setEditingMode(boolean editNotCreate);
}
