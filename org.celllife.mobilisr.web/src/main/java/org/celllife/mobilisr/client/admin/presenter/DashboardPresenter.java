package org.celllife.mobilisr.client.admin.presenter;

import org.celllife.mobilisr.client.Messages;
import org.celllife.mobilisr.client.UserContext;
import org.celllife.mobilisr.client.admin.AdminEventBus;
import org.celllife.mobilisr.client.admin.DashoardView;
import org.celllife.mobilisr.client.admin.view.DashoardViewImpl;
import org.celllife.mobilisr.client.admin.view.InfoPortlet;
import org.celllife.mobilisr.client.admin.view.SmsReportPortlet;
import org.celllife.mobilisr.client.app.presenter.MobilisrBasePresenter;
import org.celllife.mobilisr.domain.MobilisrPermission;
import org.celllife.mobilisr.service.gwt.AdminServiceAsync;
import org.celllife.mobilisr.service.gwt.OrganizationServiceAsync;
import org.celllife.mobilisr.service.gwt.ReportServiceAsync;

import com.google.inject.Inject;
import com.mvp4g.client.annotation.Presenter;

@Presenter(view = DashoardViewImpl.class)
public class DashboardPresenter extends MobilisrBasePresenter<DashoardView, AdminEventBus> {

	@Inject
	private AdminServiceAsync adminService;
	
	@Inject
	private OrganizationServiceAsync orgService;
	
	@Inject
	private ReportServiceAsync reportService;
	private SmsReportPortlet portlet;
	
	@Override
	public void bindView() {
		if (portlet == null){
			portlet = new SmsReportPortlet(orgService, reportService);
			getView().addPortlet(new InfoPortlet(adminService), 0);
			getView().addPortlet(portlet, 1);
		}
	}

	public void onShowAdminDashboard() {
		if (!UserContext.hasPermission(MobilisrPermission.VIEW_ADMIN_CONSOLE)){
			getEventBus().showErrorView(Messages.INSTANCE
					.securityAccessDenied(MobilisrPermission.VIEW_ADMIN_CONSOLE
							.name()));
			return;
		}
		getEventBus().setRegionRight(this);
	}
	
}
