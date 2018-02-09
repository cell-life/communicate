package org.celllife.mobilisr.client.reporting;

import java.util.List;

import org.celllife.mobilisr.client.Messages;
import org.celllife.mobilisr.client.MobilisrEvents;
import org.celllife.mobilisr.client.URLUtil;
import org.celllife.mobilisr.client.UserContext;
import org.celllife.mobilisr.client.app.presenter.MobilisrBasePresenter;
import org.celllife.mobilisr.client.model.ViewModel;
import org.celllife.mobilisr.client.reporting.view.ReportListViewImpl;
import org.celllife.mobilisr.client.view.gxt.BusyIndicator;
import org.celllife.mobilisr.client.view.gxt.GridModelEvent;
import org.celllife.mobilisr.client.view.gxt.MobilisrAsyncCallback;
import org.celllife.mobilisr.client.view.gxt.WizardWindow;
import org.celllife.mobilisr.domain.MobilisrPermission;
import org.celllife.mobilisr.service.gwt.AdminServiceAsync;
import org.celllife.mobilisr.service.gwt.ReportServiceAsync;
import org.celllife.pconfig.model.Pconfig;
import org.celllife.pconfig.model.ScheduledPconfig;

import com.extjs.gxt.ui.client.data.BaseListLoader;
import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.data.BeanModelReader;
import com.extjs.gxt.ui.client.data.ListLoadResult;
import com.extjs.gxt.ui.client.data.ListLoader;
import com.extjs.gxt.ui.client.data.RpcProxy;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.Store;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.form.StoreFilterField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.mvp4g.client.annotation.Presenter;


@Presenter(view = ReportListViewImpl.class)
public class ReportPresenter extends MobilisrBasePresenter<ReportListView, ReportEventBus> {

	@Inject
	private AdminServiceAsync adminService;
	
	@Inject
	private ReportServiceAsync reportService;
	
	private ListLoader<ListLoadResult<Pconfig>> loader;
	private EntityStoreProvider entityStoreProvider;

	@Override
	public void bindView() {
		entityStoreProvider = new EntityStoreProviderImpl(adminService);
		
		if (!UserContext.hasPermission(MobilisrPermission.REPORTS_VIEW_ADMIN_REPORTS)){
			entityStoreProvider.restrictResultsToOrganization(UserContext.getUser().getOrganization());
		}
		
		RpcProxy<List<Pconfig>> proxy = new RpcProxy<List<Pconfig>>() {
			@Override
			protected void load(Object loadConfig, final AsyncCallback<List<Pconfig>> callback) {
				reportService.getReports(callback);
			}
		};

		StoreFilterField<BeanModel> filter = new StoreFilterField<BeanModel>() {
			@Override
			protected boolean doSelect(Store<BeanModel> store,
					BeanModel parent, BeanModel record, String property,
					String filter) {

				String name = parent.get(Pconfig.PROP_LABEL);
				name = name.toLowerCase();
				if (name.indexOf(filter.toLowerCase()) != -1) {
					return true;
				}
				return false;
			}
		};

		loader = new BaseListLoader<ListLoadResult<Pconfig>>(proxy,new BeanModelReader());
		ListStore<BeanModel> store = new ListStore<BeanModel>(loader);
		getView().buildWidget(store, filter);

		getView().getReloadCacheButton().addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				BusyIndicator.showBusyIndicator("Reloading report cache");
				reportService.refreshCache(new MobilisrAsyncCallback<Void>() {
					@Override
					public void onSuccess(Void arg0) {
						BusyIndicator.hideBusyIndicator();
						loader.load();
					}
				});
			}
		});
		
		getView().getGenerateReportAction().setListener(new SelectionListener<GridModelEvent>() {
			@Override
			public void componentSelected(GridModelEvent ce) {
				Pconfig report = ce.getModel().getBean();
				showPrametersDialog(report, false);
			}
		});
		
		getView().getViewGeneratedReportsAction().setListener(new SelectionListener<GridModelEvent>() {
			@Override
			public void componentSelected(GridModelEvent ce) {
				Pconfig report = ce.getModel().getBean();
				ViewModel<Pconfig> viewModel = new ViewModel<Pconfig>(report);
				viewModel.putProperty(ADMIN_VIEW, isAdminView());
				getEventBus().showGeneratedReportView(viewModel);
			}
		});
		
		getView().getScheduleReportAction().setListener(new SelectionListener<GridModelEvent>() {
			@Override
			public void componentSelected(GridModelEvent ce) {
				Pconfig report = ce.getModel().getBean();
				showAutomationDialog(report, false);
			}
		});
		
		getView().getViewScheduledReportsAction().setListener(new SelectionListener<GridModelEvent>() {
			@Override
			public void componentSelected(GridModelEvent ce) {
				Pconfig report = ce.getModel().getBean();
				ViewModel<Pconfig> viewModel = new ViewModel<Pconfig>(report);
				viewModel.putProperty(ADMIN_VIEW, isAdminView());
				getEventBus().showScheduledReportsView(viewModel);
			}
		});
		
	}

	public void onShowReportView(ViewModel<Void> vm) {
		isAdminView(vm);
		getEventBus().setRegionRight(this);
		getView().setFormObject(vm);
		loader.load();
	}

	private void showPrametersDialog(Pconfig pconfig, boolean viewOnly){
		final PConfigDialog dialog = new PConfigDialog(entityStoreProvider, pconfig, viewOnly);
		dialog.getSaveButton().setText(Messages.INSTANCE.reportGenerate());
		dialog.getSaveButton().addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				generateReport(dialog.getPconfig());
				dialog.hide();
			}
		});
		dialog.show();
	}	
	
	private void showAutomationDialog(final Pconfig pconfig, boolean viewOnly){
		final ScheduledPconfig model = new ScheduledPconfig(pconfig);
		WizardWindow wizard = PconfigScheduleWizard.buildWizard(model, entityStoreProvider);
		wizard.addListener(MobilisrEvents.WizardFinish, new Listener<BaseEvent>() {
			@Override
			public void handleEvent(BaseEvent be) {				
				reportService.addScheduledReport(model, new MobilisrAsyncCallback<String>() {
					@Override
					public void onSuccess(String result) {
						getView().displaySuccessMsg(pconfig.getLabel() + " scheduled", result);						
					}
				});
			}
		});
		wizard.show();
	}

	private void generateReport(Pconfig report) {
		final MessageBox wait = MessageBox.wait("Generating report", "Waiting for report to generate", "Generating");
		reportService.generateReport(report, new MobilisrAsyncCallback<String>() {
			@Override
			public void onSuccess(String reportId) {
				wait.close();
				URLUtil.downloadReport(reportId);
			}
			@Override
			public void onFailure(Throwable error) {
				wait.close();
				super.onFailure(error);
			}
		});
	}
}
