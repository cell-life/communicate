package org.celllife.mobilisr.client.reporting;

import org.celllife.mobilisr.client.app.EntityList;
import org.celllife.mobilisr.client.model.ViewModel;
import org.celllife.mobilisr.client.view.gxt.Action;

import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.StoreFilterField;

public interface ReportListView extends EntityList {

	public void buildWidget( ListStore<BeanModel> store, StoreFilterField<BeanModel> filter );

	Button getReloadCacheButton();

	Action getGenerateReportAction();

	Action getViewGeneratedReportsAction();

	Action getScheduleReportAction();

	Action getViewScheduledReportsAction();

	void setFormObject(ViewModel<Void> viewModel);
	
}
