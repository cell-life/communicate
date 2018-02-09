package org.celllife.mobilisr.client.reporting;

import java.util.Date;
import java.util.List;

import org.celllife.mobilisr.client.MobilisrEvents;
import org.celllife.mobilisr.client.UserContext;
import org.celllife.mobilisr.client.app.presenter.MobilisrBasePresenter;
import org.celllife.mobilisr.client.model.ViewModel;
import org.celllife.mobilisr.client.reporting.view.ScheduledReportListViewImpl;
import org.celllife.mobilisr.client.view.gxt.GridModelEvent;
import org.celllife.mobilisr.client.view.gxt.MessageBoxWithIds;
import org.celllife.mobilisr.client.view.gxt.MobilisrAsyncCallback;
import org.celllife.mobilisr.client.view.gxt.WizardWindow;
import org.celllife.mobilisr.domain.MobilisrPermission;
import org.celllife.mobilisr.service.gwt.AdminServiceAsync;
import org.celllife.mobilisr.service.gwt.ReportServiceAsync;
import org.celllife.pconfig.model.DateParameter;
import org.celllife.pconfig.model.FilledPconfig;
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
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.mvp4g.client.annotation.Presenter;

@Presenter(view = ScheduledReportListViewImpl.class)
public class ScheduledReportPresenter extends MobilisrBasePresenter<ScheduledReportListView, ReportEventBus> {

	@Inject
	private AdminServiceAsync adminService;
	
	@Inject
	private ReportServiceAsync reportService;
	
	private ListLoader<ListLoadResult<FilledPconfig>> loader;
	
	private EntityStoreProvider entityStoreProvider;

	private Pconfig reportFilter;

	protected boolean showAll;

	@Override
	public void bindView() {
		showAll = false;
		
		entityStoreProvider = new EntityStoreProviderImpl(adminService);
		
		if (!UserContext.hasPermission(MobilisrPermission.REPORTS_VIEW_ADMIN_REPORTS)){
			entityStoreProvider.restrictResultsToOrganization(UserContext.getUser().getOrganization());
		}
		
		RpcProxy<List<ScheduledPconfig>> proxy = new RpcProxy<List<ScheduledPconfig>>() {
			@Override
			protected void load(Object loadConfig, final AsyncCallback<List<ScheduledPconfig>> callback) {
				String id = reportFilter == null ? null : reportFilter.getId();
				boolean showForAllOrgs = isAdminView()
						&& UserContext.hasPermission(MobilisrPermission.REPORT_SCHEDULES_ADMIN_VIEW); 
				reportService.getScheduledReports(showAll ? null : id,
						showForAllOrgs, callback);
			}
		};

		loader = new BaseListLoader<ListLoadResult<FilledPconfig>>(proxy,new BeanModelReader());
		ListStore<BeanModel> store = new ListStore<BeanModel>(loader);
		getView().buildWidget(store);
		
		getView().getEditScheduleAction().setListener(new SelectionListener<GridModelEvent>() {
			@Override
			public void componentSelected(GridModelEvent ce) {
				ScheduledPconfig report = ce.getModel().getBean();
				showScheduleWizard(report);
			}
		});
		
		getView().getSuspendScheduleAction().setListener(new SelectionListener<GridModelEvent>() {
			@Override
			public void componentSelected(GridModelEvent ce) {
				final ScheduledPconfig report = ce.getModel().getBean();
				suspendSchedule(report);
			}
		});
		
		getView().getDeleteScheduleAction().setListener(new SelectionListener<GridModelEvent>() {
			@Override
			public void componentSelected(final GridModelEvent ce) {
				MessageBoxWithIds.confirm("Confirm delete",
										"Are you sure you want to delete this report schedule?",
										new Listener<MessageBoxEvent>() {
					@Override
					public void handleEvent(MessageBoxEvent be) {
						if (be.getButtonClicked().getItemId().equals(Dialog.YES)){
							ScheduledPconfig report = ce.getModel().getBean();
							deleteReportSchedule(report);
						}
					}
				});
			}
		});
		
		getView().getShowAllButton().addListener(Events.Toggle, new Listener<BaseEvent>() {
			@Override
			public void handleEvent(BaseEvent be) {
				showAll = !showAll;
				loader.load();
			}
		});
	}

	protected void showScheduleWizard(final ScheduledPconfig report) {
		WizardWindow wizard = PconfigScheduleWizard.buildWizard(report, entityStoreProvider);
		wizard.addListener(MobilisrEvents.WizardFinish, new Listener<BaseEvent>() {
			@Override
			public void handleEvent(BaseEvent be) {
				saveSchedule(report, report.getPconfig().getLabel() + " report scheduled");
			}
		});
		wizard.show();
	}
	
	private void saveSchedule(final ScheduledPconfig report, final String message) {
		    reportService.addScheduledReport(report, new MobilisrAsyncCallback<String>() {
			@Override
			public void onSuccess(String result) {
				getView().displaySuccessMsg(message);
				loader.load();
				
			}
		});
	}

	public void onShowScheduledReportsView(ViewModel<Pconfig> vm) {
		isAdminView(vm);
		this.reportFilter = vm.getModelObject();
		eventBus.setRegionRight(this);
		getView().setFormObject(vm);
		loader.load();
	}

	private void deleteReportSchedule(ScheduledPconfig report) {
		String id = report.getId();
		reportService.deleteScheduledReport(id, new MobilisrAsyncCallback<Void>() {
			@Override
			public void onSuccess(Void arg0) {
				loader.load();
			}
		});
	}
	
	private void suspendSchedule(final ScheduledPconfig report) {
		Pconfig pconfig = new Pconfig();
		final DateParameter param = new DateParameter();
		param.setName("date");
		param.setLabel("Suspend until:");
		param.setOptional(false);
		param.setAllowPast(false);
		pconfig.addParameter(param);
		
		final PConfigDialog dialog = new PConfigDialog(null, pconfig, false);
		dialog.setHeading("Enter date to suspend report until");
		dialog.getSaveButton().setText("Suspend");
		dialog.getSaveButton().addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				dialog.hide();
				Date value = param.getValue();
				if (value.compareTo(report.getEndDate()) > 1){
					MessageBoxWithIds.alert("Suspend date after schedule end date", 
							"The date you have entered is after the schedule's end date.", null);
				} else {
					report.setStartDate(value);
					String date =  DateTimeFormat.getFormat("dd-MM-yyyy").format(value);
					saveSchedule(report,report.getPconfig().getLabel() + " suspended until " + date);
				}
			}
		});
		dialog.show();
	}
}
