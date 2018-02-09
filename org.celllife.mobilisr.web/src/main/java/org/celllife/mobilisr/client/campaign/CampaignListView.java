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
import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;

public interface CampaignListView extends EntityList{

	void buildWidget(ListStore<BeanModel> store, RemoteStoreFilterField<BeanModel> filter);

	SimpleComboBox<String> getTypeFilterCombo();

	Button getRebuildSchedulesButton();

	void setFormObject(ViewModel<Campaign> viewEntityModel);

	AnchorCellRenderer getCampaignNameAnchor();

	Action getVoidAction();

	Action getStartStopAction();

	Action getViewRecipientsAction();

	Action getManageRecipientsAction();

	Action getViewMessageLogsAction();

	Button getShowVoidedButton();

	void setOrganizationStore(ListStore<BeanModel> store);

	ComboBox<BeanModel> getFilterOrgCombo();

	void clearFilterOrgCombo();
}
