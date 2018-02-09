package org.celllife.mobilisr.client.reporting;

import org.celllife.mobilisr.client.app.EntityList;
import org.celllife.mobilisr.client.model.ViewModel;
import org.celllife.mobilisr.client.view.gxt.Action;
import org.celllife.pconfig.model.Pconfig;

import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.button.Button;

public interface ScheduledReportListView extends EntityList {

	public void buildWidget( ListStore<BeanModel> store);

	Action getEditScheduleAction();

	Action getDeleteScheduleAction();

	Action getSuspendScheduleAction();

	void setFormObject(ViewModel<Pconfig> viewModel);

	Button getShowAllButton();

}
