package org.celllife.mobilisr.client.reporting.view;

import java.util.ArrayList;
import java.util.List;

import org.celllife.mobilisr.client.Messages;
import org.celllife.mobilisr.client.Resources;
import org.celllife.mobilisr.client.UserContext;
import org.celllife.mobilisr.client.app.presenter.MobilisrBasePresenter;
import org.celllife.mobilisr.client.model.ViewModel;
import org.celllife.mobilisr.client.reporting.ReportListView;
import org.celllife.mobilisr.client.template.view.EntityListTemplateImpl;
import org.celllife.mobilisr.client.view.gxt.Action;
import org.celllife.mobilisr.client.view.gxt.ButtonGridCellRenderer;
import org.celllife.mobilisr.client.view.gxt.MyGXTButton;
import org.celllife.mobilisr.domain.MobilisrPermission;
import org.celllife.pconfig.model.Pconfig;

import com.extjs.gxt.ui.client.Style.ButtonScale;
import com.extjs.gxt.ui.client.Style.IconAlign;
import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.StoreFilterField;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;

public class ReportListViewImpl extends EntityListTemplateImpl implements ReportListView {

	private MyGXTButton reloadCacheButton;
	private Action generateReportAction;
	private Action viewGeneratedReportsAction;
	private Action scheduleReportAction;
	private Action viewScheduledReportsAction;

	@Override
	public void createView() {
		 reloadCacheButton = new MyGXTButton(
					"reloadCacheButton", Messages.INSTANCE.reportReloadCache(),
					Resources.INSTANCE.refresh(), IconAlign.LEFT, ButtonScale.SMALL);
		 
		Button[] buttons = null;
		if (UserContext.hasPermission(MobilisrPermission.REPORTS_ADMIN_RELOAD_CACHE)){
			buttons = new Button[]{reloadCacheButton};
		}
		layoutListTemplate(Messages.INSTANCE.reportListHeader(), buttons, false);
	}
	
	@Override
	public void buildWidget(ListStore<BeanModel> store, StoreFilterField<BeanModel> filter) {
		createActions();
		List<ColumnConfig> configs = getColumnsConfigs(false);
		renderEntityListGrid(store, filter, configs, null, "Search for Report");
	}
	
	private void createActions(){
		generateReportAction = new Action(null,
				"Click to generate this report", Resources.INSTANCE.start(), "generate");
	
		viewGeneratedReportsAction = new Action(null,
				"Click to view previously generated reports",
				Resources.INSTANCE.folderTable(), "view_generated");
		
		scheduleReportAction = new Action(null,
				"Click to Schedule Report",
				Resources.INSTANCE.schedule(), "view_automation");
		
		viewScheduledReportsAction = new Action(null,
				"Click to view scheduled reports",
				Resources.INSTANCE.folderClock(), "view_scheduled");
	}

	private List<ColumnConfig> getColumnsConfigs(boolean isAdminView) {
		List<ColumnConfig> configs = new ArrayList<ColumnConfig>();
		configs.add( new ColumnConfig( Pconfig.PROP_LABEL, Messages.INSTANCE.reportName(), 50 ));

		ColumnConfig actions = new ColumnConfig( "reportActions", "Actions", 150 );
		ButtonGridCellRenderer renderer = new ButtonGridCellRenderer();
		actions.setRenderer(renderer);
		actions.setSortable(false);
		configs.add(actions);
		
		if (!isAdminView){
			renderer.addAction(generateReportAction);
		}
		
		if (!isAdminView || UserContext.hasPermission(MobilisrPermission.REPORTS_ADMIN_VIEW)){
			renderer.addAction(viewGeneratedReportsAction);
		}
		
		if (!isAdminView){
			renderer.addAction(scheduleReportAction);
		}
		
		if ( (!isAdminView && UserContext.hasPermission(MobilisrPermission.REPORT_SCHEDULES_VIEW))
				|| (isAdminView && UserContext.hasPermission(MobilisrPermission.REPORT_SCHEDULES_ADMIN_VIEW)) ){
			renderer.addAction(viewScheduledReportsAction);
		}
		
		return configs;
	}
	
	@Override
	public void setFormObject(ViewModel<Void> viewModel){
		boolean isAdminView = viewModel.isPropertyTrue(MobilisrBasePresenter.ADMIN_VIEW);
		List<ColumnConfig> columnsConfigs = getColumnsConfigs(isAdminView);
		reconfigureGrid(columnsConfigs);
		clearSuccessMsg();
	}
	
	@Override
	public Button getReloadCacheButton() {
		return reloadCacheButton;
	}
	
	@Override
	public Action getGenerateReportAction() {
		return generateReportAction;
	}
	
	@Override
	public Action getViewGeneratedReportsAction() {
		return viewGeneratedReportsAction;
	}
	
	@Override
	public Action getScheduleReportAction() {
		return scheduleReportAction;
	}
	
	@Override
	public Action getViewScheduledReportsAction() {
		return viewScheduledReportsAction;
	}
}
