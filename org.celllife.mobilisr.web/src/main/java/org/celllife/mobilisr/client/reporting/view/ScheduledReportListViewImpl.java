package org.celllife.mobilisr.client.reporting.view;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.celllife.mobilisr.client.Messages;
import org.celllife.mobilisr.client.Resources;
import org.celllife.mobilisr.client.UserContext;
import org.celllife.mobilisr.client.app.presenter.MobilisrBasePresenter;
import org.celllife.mobilisr.client.model.ViewModel;
import org.celllife.mobilisr.client.reporting.ScheduledReportListView;
import org.celllife.mobilisr.client.template.view.EntityListTemplateImpl;
import org.celllife.mobilisr.client.view.gxt.Action;
import org.celllife.mobilisr.client.view.gxt.ButtonGridCellRenderer;
import org.celllife.mobilisr.client.view.gxt.MyGXTToggleButton;
import org.celllife.mobilisr.domain.MobilisrPermission;
import org.celllife.mobilisr.service.gwt.ServiceAndUIConstants;
import org.celllife.pconfig.model.DateParameter;
import org.celllife.pconfig.model.EntityParameter;
import org.celllife.pconfig.model.FilledPconfig;
import org.celllife.pconfig.model.LabelParameter;
import org.celllife.pconfig.model.Parameter;
import org.celllife.pconfig.model.Pconfig;
import org.celllife.pconfig.model.ScheduledPconfig;

import com.extjs.gxt.ui.client.Style.ButtonScale;
import com.extjs.gxt.ui.client.Style.IconAlign;
import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.tips.QuickTip;
import com.extjs.gxt.ui.client.widget.toolbar.FillToolItem;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;

public class ScheduledReportListViewImpl extends EntityListTemplateImpl implements ScheduledReportListView {

	private Action editScheduleAction;
	private Action deleteScheduleAction;
	private Action suspendScheduleAction;
	
	private MyGXTToggleButton showAll;
	private Pconfig pconfig;
	
	@Override
	public void createView() {
		showAll = new MyGXTToggleButton("showAll", null,
				Resources.INSTANCE.listAdd(), IconAlign.LEFT, ButtonScale.SMALL);
		showAll.setToolTip("Show all scheduled reports");
		showAll.setToggledIcon(Resources.INSTANCE.listRemove());
		showAll.addListener(Events.Toggle, new Listener<BaseEvent>() {
			@Override
			public void handleEvent(BaseEvent be) {
				if (showAll.isPressed()){
					setTitleLabel(Messages.INSTANCE.reportScheduledHeaderAll());
				} else {
					setTitleLabel(Messages.INSTANCE.reportScheduledHeader(pconfig.getLabel()));
				}
			}
		});

		layoutListTemplate(Messages.INSTANCE.reportScheduledHeader(""), null, false);
	}
	
	@Override
	public void buildWidget(ListStore<BeanModel> store) {
		createActions();
		List<ColumnConfig> configs = getColumnsConfigs(false);
		renderEntityListGrid(store, null, configs, null, "Search for Report");
		new QuickTip(entityList);
		
		super.topToolBar.add(new FillToolItem());
		super.topToolBar.add(showAll);
	}
	
	private void createActions(){
		editScheduleAction = new Action(null,
				"Click to edit this report schedule",
				Resources.INSTANCE.applicationForm(), "edit");
		
		suspendScheduleAction = new Action(null,
				"Click to suspend this report schedule",
				Resources.INSTANCE.schedulePause(), "edit");
	
		deleteScheduleAction = new Action(null,
				"Click to delete this report schedule", Resources.INSTANCE.delete(),
				"delete");
	
	}

	private List<ColumnConfig> getColumnsConfigs(boolean isAdminView) {
		List<ColumnConfig> configs = new ArrayList<ColumnConfig>();
		ColumnConfig lableConfig = new ColumnConfig(FilledPconfig.PROP_PCONFIG + "."
				+ Pconfig.PROP_LABEL, Messages.INSTANCE.reportName(), 50);
		lableConfig.setRenderer(new GridCellRenderer<BeanModel>() {
			@Override
			public Object render(BeanModel model, String property,
					ColumnData config, int rowIndex, int colIndex,
					ListStore<BeanModel> store, Grid<BeanModel> grid) {
				ScheduledPconfig report = model.getBean();
				
				StringBuffer tooltip = new StringBuffer();
				tooltip.append("<ul>");
				for (Parameter<?> param : report.getPconfig().getParameters()) {
					if (param instanceof LabelParameter){
						continue;
					}
					Object value = param.getValue();
					if (param instanceof DateParameter && value != null){
						value = DateTimeFormat.getFormat(PredefinedFormat.DATE_MEDIUM).format((Date)value);
					} else if (param instanceof EntityParameter){
						value = ((EntityParameter) param).getValueLabel();
					}
					tooltip.append("<li>");
					tooltip.append(param.getLabel()).append(": ");
					if (value != null) {
						tooltip.append(value);
					}
					tooltip.append("</li>");
				}
				tooltip.append("</ul><br/>");
				tooltip.append("<b>Schedule&nbsp;Parameters</b><br/>");
				int count = report.getIntervalCount();
				tooltip.append("<ul><li>Repeat every: ").append(count);
				tooltip.append(" ").append(report.getRepeatInterval().getText(count));
				tooltip.append("</li>");

				DateTimeFormat format = DateTimeFormat.getFormat("dd-MM-yyyy");
				tooltip.append("<li>From: ").append(format.format(report.getStartDate())).append("</li>");
				if (report.getEndDate() != null) {
					tooltip.append("<li>To: ").append(format.format(report.getEndDate()));
				} else {
					tooltip.append("<li>To: ").append("No end date");
				}
				tooltip.append("<li>Email: ").append(report.getScheduledFor());
				tooltip.append("</li></ul>");
				
				return "<span " + "id=\"report-" + report.getId() + "\"" + "qtitle=\"Report&nbsp;Parameters\" qtip=\"" + 
					tooltip.toString() + "\">" + model.get(property) + "</span>";
			}
		});
		configs.add(lableConfig);
		
		ColumnConfig startDate = new ColumnConfig(ScheduledPconfig.PROP_START_DATE,
				"Start Date", 50 );
		startDate.setDateTimeFormat(DateTimeFormat.getFormat(PredefinedFormat.DATE_MEDIUM));
		configs.add(startDate);
		
		ColumnConfig endDate = new ColumnConfig(ScheduledPconfig.PROP_END_DATE,
				"End Date", 50 );
		endDate.setDateTimeFormat(DateTimeFormat.getFormat(PredefinedFormat.DATE_MEDIUM));
		configs.add(endDate);
		
		if(isAdminView){
			ColumnConfig organisation = new ColumnConfig("organisation",
					"Organisation", 50 );
			organisation.setRenderer(new GridCellRenderer<BeanModel>() {
				@Override
				public Object render(BeanModel model, String property,
						ColumnData config, int rowIndex, int colIndex,
						ListStore<BeanModel> store, Grid<BeanModel> grid) {

					ScheduledPconfig sp = model.getBean();
					return sp.getProperty(ServiceAndUIConstants.PROP_REPORT_ORGANISATION_NAME);
				}
			});
			organisation.setSortable(false);
			configs.add(organisation);
		}
		
		ColumnConfig actions = new ColumnConfig( "reportActions", "Actions", 150 );
		ButtonGridCellRenderer renderer = new ButtonGridCellRenderer();
		actions.setRenderer(renderer);
		actions.setSortable(false);
		configs.add(actions);
		
		if ( (!isAdminView && UserContext.hasPermission(MobilisrPermission.REPORT_SCHEDULES_EDIT))
				|| (isAdminView && UserContext.hasPermission(MobilisrPermission.REPORT_SCHEDULES_ADMIN_EDIT)) ){
			renderer.addAction(editScheduleAction);
		}
		if ( (!isAdminView && UserContext.hasPermission(MobilisrPermission.REPORT_SCHEDULES_EDIT))
				|| (isAdminView && UserContext.hasPermission(MobilisrPermission.REPORT_SCHEDULES_ADMIN_EDIT)) ){
			renderer.addAction(suspendScheduleAction);
		}
		if ((!isAdminView && UserContext.hasPermission(MobilisrPermission.REPORT_SCHEDULES_DELETE))
				|| (isAdminView && UserContext.hasPermission(MobilisrPermission.REPORT_SCHEDULES_ADMIN_DELETE)) ){
			renderer.addAction(deleteScheduleAction);
		}
		return configs;
	}
	
	@Override
	public void setFormObject(ViewModel<Pconfig> viewModel){
		boolean isAdminView = viewModel.isPropertyTrue(MobilisrBasePresenter.ADMIN_VIEW);
		List<ColumnConfig> columnsConfigs = getColumnsConfigs(isAdminView);
		reconfigureGrid(columnsConfigs);
		pconfig = viewModel.getModelObject();
		setTitleLabel(Messages.INSTANCE.reportScheduledHeader(pconfig.getLabel()));
		showAll.setToggledTooltip("Only show scheduled reports for "
				+ pconfig.getLabel());
		clearSuccessMsg();
	}

	@Override
	public Action getEditScheduleAction() {
		return editScheduleAction;
	}
	
	@Override
	public Action getSuspendScheduleAction() {
		return suspendScheduleAction;
	}

	@Override
	public Action getDeleteScheduleAction() {
		return deleteScheduleAction;
	}
	
	@Override
	public Button getShowAllButton(){
		return showAll;
	}
}
