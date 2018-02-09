package org.celllife.mobilisr.client.reporting;

import java.util.List;

import org.celllife.mobilisr.client.Messages;
import org.celllife.mobilisr.client.URLUtil;
import org.celllife.mobilisr.client.UserContext;
import org.celllife.mobilisr.client.app.presenter.MobilisrBasePresenter;
import org.celllife.mobilisr.client.model.ViewModel;
import org.celllife.mobilisr.client.reporting.view.GeneratedReportListViewImpl;
import org.celllife.mobilisr.client.view.gxt.GridModelEvent;
import org.celllife.mobilisr.client.view.gxt.MessageBoxWithIds;
import org.celllife.mobilisr.client.view.gxt.MobilisrAsyncCallback;
import org.celllife.mobilisr.domain.MobilisrPermission;
import org.celllife.mobilisr.service.gwt.AdminServiceAsync;
import org.celllife.mobilisr.service.gwt.ReportServiceAsync;
import org.celllife.pconfig.model.FilledPconfig;
import org.celllife.pconfig.model.Pconfig;

import com.extjs.gxt.ui.client.data.BaseListLoader;
import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.data.BeanModelReader;
import com.extjs.gxt.ui.client.data.ListLoadResult;
import com.extjs.gxt.ui.client.data.ListLoader;
import com.extjs.gxt.ui.client.data.RpcProxy;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.mvp4g.client.annotation.Presenter;

@Presenter(view = GeneratedReportListViewImpl.class)
public class GeneratedReportPresenter extends MobilisrBasePresenter<GeneratedReportListView, ReportEventBus> {

	@Inject
	private AdminServiceAsync adminService;
	
	@Inject
	private ReportServiceAsync reportService;
	
	private ListLoader<ListLoadResult<FilledPconfig>> loader;
	
	private EntityStoreProvider entityStoreProvider;

	private Pconfig reportFilter;

	@Override
	public void bindView() {
		entityStoreProvider = new EntityStoreProviderImpl(adminService);
		
		if (!UserContext.hasPermission(MobilisrPermission.REPORTS_VIEW_ADMIN_REPORTS)){
			entityStoreProvider.restrictResultsToOrganization(UserContext.getUser().getOrganization());
		}
		
		RpcProxy<List<FilledPconfig>> proxy = new RpcProxy<List<FilledPconfig>>() {
			@Override
			protected void load(Object loadConfig, final AsyncCallback<List<FilledPconfig>> callback) {
				String id = reportFilter == null ? null : reportFilter.getId();
				boolean showAll = isAdminView()
					&& UserContext.hasPermission(MobilisrPermission.REPORTS_ADMIN_VIEW);
				reportService.getGeneratedReports(id, showAll, callback);
			}
		};

		loader = new BaseListLoader<ListLoadResult<FilledPconfig>>(proxy,new BeanModelReader());
		ListStore<BeanModel> store = new ListStore<BeanModel>(loader);
		getView().buildWidget(store);

		getView().getRegenerateReportAction().setListener(new SelectionListener<GridModelEvent>() {
			@Override
			public void componentSelected(GridModelEvent ce) {
				FilledPconfig report = ce.getModel().getBean();
				showPrametersDialog(report.getPconfig(), false);
			}
		});
		
		getView().getDownloadReportAction().setListener(new SelectionListener<GridModelEvent>() {
			@Override
			public void componentSelected(GridModelEvent ce) {
				FilledPconfig report = ce.getModel().getBean();
				downloadReport(report);
			}
		});
		
		getView().getDeleteGeneratedReportAction().setListener(new SelectionListener<GridModelEvent>() {
			@Override
			public void componentSelected(GridModelEvent ce) {
				final FilledPconfig report = ce.getModel().getBean();
				MessageBoxWithIds.confirm("Confirm delete", 
						"Are you sure you want to delete this report?", 
						new Listener<MessageBoxEvent>() {
					@Override
					public void handleEvent(MessageBoxEvent be) {
						if (be.getButtonClicked().getItemId().equals(Dialog.YES)){
							deleteReport(report);
						}
					}
				});
			}
		});
	}

	public void onShowGeneratedReportView(ViewModel<Pconfig> vm) {
		isAdminView(vm);
		this.reportFilter = vm.getModelObject();
		eventBus.setRegionRight(this);
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
	

	private void generateReport(Pconfig report) {
		final MessageBox wait = MessageBox.wait("Generating report", "Waiting for report to generate", "Generating");
		reportService.generateReport(report, new MobilisrAsyncCallback<String>() {
			@Override
			public void onSuccess(String reportId) {
				wait.close();
				URLUtil.downloadReport(reportId);
				loader.load();
			}
			@Override
			public void onFailure(Throwable error) {
				wait.close();
				super.onFailure(error);
			}
		});
	}

	private void deleteReport(final FilledPconfig report) {
		String id = report.getId();
		reportService.deleteGeneratedReport(id, new MobilisrAsyncCallback<Void>() {
			@Override
			public void onSuccess(Void arg0) {
				getView().displaySuccessMsg("Your report has been deleted");
				loader.load();
			}
		});
	}

	private void downloadReport(FilledPconfig report) {
		String id = report.getId();
		URLUtil.downloadReport(id);
	}
}
