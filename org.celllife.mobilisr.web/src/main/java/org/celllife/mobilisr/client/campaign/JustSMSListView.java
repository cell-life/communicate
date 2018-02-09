package org.celllife.mobilisr.client.campaign;

import org.celllife.mobilisr.client.app.EntityList;
import org.celllife.mobilisr.client.model.ViewModel;
import org.celllife.mobilisr.client.view.gxt.Action;
import org.celllife.mobilisr.client.view.gxt.RemoteStoreFilterField;
import org.celllife.mobilisr.client.view.gxt.grid.AnchorCellRenderer;
import org.celllife.mobilisr.domain.Campaign;

import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.ComboBox;

public interface JustSMSListView extends EntityList{
	
	void buildWidget(ListStore<BeanModel> store, RemoteStoreFilterField<BeanModel> filter);

	void setTitleLabel(String headerTitle);

	Action getToggleVoidAction();

	Action getViewRecipientsAction();

	Action getViewCampaignSummaryAction();

	Action getScheduleAction();

	Action getViewMessageLogsAction();

	AnchorCellRenderer getCampaignNameAnchor();

	void setFormObject(ViewModel<Campaign> viewEntityModel);

	Button getShowVoidedButton();

	ComboBox<BeanModel> getFilterOrgCombo();

	void setOrganizationStore(ListStore<BeanModel> store);

	void clearFilterOrgCombo();

}
