package org.celllife.mobilisr.client.admin.presenter;

import java.util.List;

import org.celllife.mobilisr.client.Messages;
import org.celllife.mobilisr.client.UserContext;
import org.celllife.mobilisr.client.admin.AdminEventBus;
import org.celllife.mobilisr.client.admin.view.SettingsCreateViewImpl;
import org.celllife.mobilisr.client.admin.view.SettingsListViewImpl;
import org.celllife.mobilisr.client.admin.view.SettingsView;
import org.celllife.mobilisr.client.admin.view.SettingsViewImpl;
import org.celllife.mobilisr.client.app.presenter.DirtyPresenter;
import org.celllife.mobilisr.client.model.ViewModel;
import org.celllife.mobilisr.client.view.gxt.MobilisrAsyncCallback;
import org.celllife.mobilisr.domain.MobilisrPermission;
import org.celllife.mobilisr.service.gwt.SettingServiceAsync;
import org.celllife.mobilisr.service.gwt.SettingViewModel;

import com.extjs.gxt.ui.client.data.BaseListLoader;
import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.data.BeanModelReader;
import com.extjs.gxt.ui.client.data.ListLoadResult;
import com.extjs.gxt.ui.client.data.RpcProxy;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.mvp4g.client.annotation.Presenter;

@Presenter(view = SettingsViewImpl.class)
public class SettingsPresenter extends DirtyPresenter<SettingsView,AdminEventBus>{

	private SettingsCreateViewImpl createView;
	private SettingsListViewImpl listView;
	
	@Inject
	protected SettingServiceAsync settingService;
	private BaseListLoader<ListLoadResult<SettingViewModel>> loader;


	@Override
	public void bindView() {
		listView = getView().getListView();
		createView = getView().getCreateView();

		RpcProxy<List<SettingViewModel>> proxy = new RpcProxy<List<SettingViewModel>>() {
			@Override
			protected void load(Object loadConfig, AsyncCallback<List<SettingViewModel>> callback) {
				settingService.getSettings(callback);
			}
		};

		loader = new BaseListLoader<ListLoadResult<SettingViewModel>>(proxy,new BeanModelReader());
		ListStore<BeanModel> store = new ListStore<BeanModel>(loader);
		listView.buildWidget(store);

		listView.getEntityListGrid().addListener(Events.RowClick, new Listener<GridEvent<BeanModel>>() {

					@Override
					public void handleEvent(GridEvent<BeanModel> gridEvent) {
						BeanModel beanModel = gridEvent.getModel();
						SettingViewModel setting = beanModel.getBean();
						ViewModel<SettingViewModel> viewEntityModel = new ViewModel<SettingViewModel>();
						viewEntityModel.setModelObject(setting );

						listView.clearSuccessMsg();
						createView.setFormObject(viewEntityModel);
					}

				});

		createView.getFormSubmitButton().addSelectionListener(	new SelectionListener<ButtonEvent>() {

					@Override
					public void componentSelected(ButtonEvent ce) {

						ViewModel<SettingViewModel> viewEntityModel = (ViewModel<SettingViewModel>) createView.getFormObject();
						final SettingViewModel setting = viewEntityModel.getModelObject();
						settingService.saveSetting(setting, new MobilisrAsyncCallback<Void>() {
									@Override
									public void onSuccess(Void v) {
										listView.displaySuccessMsg("Setting: \'" +  setting.getName() + "\' saved successfully");
										loader.load();
									}

									@Override
									public void onFailure(Throwable cause) {
										createView.setErrorMessage(cause.getMessage());
									}
								});
					}

				});

		createView.getFormCancelButton().addSelectionListener( new SelectionListener<ButtonEvent>() {

					@Override
					public void componentSelected(ButtonEvent ce) {
						getView().setDirty(false);
						listView.clearSuccessMsg();
						createView.clearFormValues();
					}
				});

	}

	public void onShowSettingsView() {
		if (!UserContext.hasPermission(MobilisrPermission.MANAGE_SETTINGS)){
			getEventBus().showErrorView(Messages.INSTANCE
					.securityAccessDenied(MobilisrPermission.MANAGE_SETTINGS
							.name()));
			return;
		}
		getEventBus().setNavigationConfirmation(this);
		getEventBus().setRegionRight(this);
		createView.setErrorMessage(null);
		createView.clearFormValues();
		listView.clearSuccessMsg();
		loader.load();
	}
}
