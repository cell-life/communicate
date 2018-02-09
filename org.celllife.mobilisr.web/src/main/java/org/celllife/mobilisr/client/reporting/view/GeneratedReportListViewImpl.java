package org.celllife.mobilisr.client.reporting.view;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.celllife.mobilisr.client.Messages;
import org.celllife.mobilisr.client.Resources;
import org.celllife.mobilisr.client.UserContext;
import org.celllife.mobilisr.client.app.presenter.MobilisrBasePresenter;
import org.celllife.mobilisr.client.model.ViewModel;
import org.celllife.mobilisr.client.reporting.GeneratedReportListView;
import org.celllife.mobilisr.client.template.view.EntityListTemplateImpl;
import org.celllife.mobilisr.client.view.gxt.Action;
import org.celllife.mobilisr.client.view.gxt.ButtonGridCellRenderer;
import org.celllife.mobilisr.domain.MobilisrPermission;
import org.celllife.mobilisr.service.gwt.ServiceAndUIConstants;
import org.celllife.pconfig.model.DateParameter;
import org.celllife.pconfig.model.EntityParameter;
import org.celllife.pconfig.model.FilledPconfig;
import org.celllife.pconfig.model.LabelParameter;
import org.celllife.pconfig.model.Parameter;
import org.celllife.pconfig.model.Pconfig;

import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.tips.QuickTip;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;

public class GeneratedReportListViewImpl extends EntityListTemplateImpl implements GeneratedReportListView {

	private Action regenerateReportAction;
	private Action deleteGeneratedReportAction;
	private Action downloadReportAction;

	@Override
	public void createView() {
		layoutListTemplate(Messages.INSTANCE.reportGeneratedHeader(""), null, false);
	}
	
	@Override
	public void buildWidget(ListStore<BeanModel> store) {
		createActions();
		List<ColumnConfig> configs = getColumnsConfigs(false);
		renderEntityListGrid(store, null, configs, "Note: Generated reports are deleted after 7 days", null);
		new QuickTip(entityList);
	}
	
	private void createActions(){
		regenerateReportAction = new Action(null,
				"Click to edit the parameters of this report",
				Resources.INSTANCE.refresh(), "generate");
	
		downloadReportAction = new Action(null,
				"Click to download this report", Resources.INSTANCE.download(),
				"download");
	
		deleteGeneratedReportAction = new Action(null,
				"Click to delete this generated report", Resources.INSTANCE.delete(),
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
				FilledPconfig report = model.getBean();
				
				StringBuffer tooltip = new StringBuffer();
				tooltip.append("<br/><ul>");
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
					tooltip.append(param.getLabel()).append(" ");
					if (value != null) {
						tooltip.append(value);
					}
					tooltip.append("</li>");
				}
				tooltip.append("</ul>");
				return "<span id=\"report" + "-" + model.get("id")+ "\"" + "qtitle=\"Report&nbsp;Parameters\" qtip=\"" + 
					tooltip.toString() + "\">" + model.get(property) + "</span>";
			}
		});
		configs.add(lableConfig);
		
		ColumnConfig dateGenerated = new ColumnConfig(FilledPconfig.PROP_DATE_FILLED,
				"Date Generated", 50 );
		dateGenerated.setDateTimeFormat(DateTimeFormat.getFormat(PredefinedFormat.DATE_MEDIUM));
		configs.add(dateGenerated);
		
		if (isAdminView){
			if(isAdminView){
				ColumnConfig organisation = new ColumnConfig("organisation",
						"Organisation", 50 );
				organisation.setRenderer(new GridCellRenderer<BeanModel>() {
					@Override
					public Object render(BeanModel model, String property,
							ColumnData config, int rowIndex, int colIndex,
							ListStore<BeanModel> store, Grid<BeanModel> grid) {

						FilledPconfig sp = model.getBean();
						return sp.getProperty(ServiceAndUIConstants.PROP_REPORT_ORGANISATION_NAME);
					}
				});
				organisation.setSortable(false);
				configs.add(organisation);
			}
		}
		
		ColumnConfig actions = new ColumnConfig( "reportActions", "Actions", 150 );
		ButtonGridCellRenderer renderer = new ButtonGridCellRenderer();
		actions.setRenderer(renderer);
		actions.setSortable(false);
		configs.add(actions);
		
		if (!isAdminView) {
			renderer.addAction(regenerateReportAction);
		}
		
		renderer.addAction(downloadReportAction);
		
		if ((!isAdminView && UserContext.hasPermission(MobilisrPermission.REPORTS_DELETE))
				|| (isAdminView && UserContext.hasPermission(MobilisrPermission.REPORTS_ADMIN_DELETE)) ){
			renderer.addAction(deleteGeneratedReportAction);
		}
		return configs;
	}
	
	@Override
	public void setFormObject(ViewModel<Pconfig> viewModel){
		boolean isAdminView = viewModel.isPropertyTrue(MobilisrBasePresenter.ADMIN_VIEW);
		List<ColumnConfig> columnsConfigs = getColumnsConfigs(isAdminView);
		reconfigureGrid(columnsConfigs);
		setTitleLabel(Messages.INSTANCE.reportGeneratedHeader(viewModel
				.getModelObject().getLabel()));
		clearSuccessMsg();
	}
	
	@Override
	public Action getRegenerateReportAction() {
		return regenerateReportAction;
	}
	
	@Override
	public Action getDeleteGeneratedReportAction() {
		return deleteGeneratedReportAction;
	}
	
	@Override
	public Action getDownloadReportAction() {
		return downloadReportAction;
	}
}
