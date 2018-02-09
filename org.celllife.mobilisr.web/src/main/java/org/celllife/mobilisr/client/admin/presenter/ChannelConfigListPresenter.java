package org.celllife.mobilisr.client.admin.presenter;

import java.util.List;

import org.celllife.mobilisr.client.Messages;
import org.celllife.mobilisr.client.MobilisrEvents;
import org.celllife.mobilisr.client.UserContext;
import org.celllife.mobilisr.client.admin.AdminEventBus;
import org.celllife.mobilisr.client.admin.ChannelConfigListView;
import org.celllife.mobilisr.client.admin.view.ChannelConfigListViewImpl;
import org.celllife.mobilisr.client.admin.view.ChannelConfigWizard;
import org.celllife.mobilisr.client.app.presenter.MobilisrBasePresenter;
import org.celllife.mobilisr.client.model.ViewModel;
import org.celllife.mobilisr.client.view.gxt.BusyIndicator;
import org.celllife.mobilisr.client.view.gxt.GridModelEvent;
import org.celllife.mobilisr.client.view.gxt.MobilisrAsyncCallback;
import org.celllife.mobilisr.domain.ChannelConfig;
import org.celllife.mobilisr.domain.MobilisrPermission;
import org.celllife.mobilisr.service.gwt.ChannelConfigViewModel;
import org.celllife.mobilisr.service.gwt.ChannelServiceAsync;
import org.celllife.pconfig.model.Pconfig;

import com.extjs.gxt.ui.client.data.BaseListLoader;
import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.data.BeanModelReader;
import com.extjs.gxt.ui.client.data.ListLoadResult;
import com.extjs.gxt.ui.client.data.RpcProxy;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.mvp4g.client.annotation.Presenter;

@Presenter(view = ChannelConfigListViewImpl.class)
public class ChannelConfigListPresenter extends MobilisrBasePresenter<ChannelConfigListView, AdminEventBus> {

	@Inject
	private ChannelServiceAsync service;
	private BaseListLoader<ListLoadResult<ChannelConfig>> configLoader;
	private ListStore<BeanModel> handlerStore;

	@Override
	public void bindView() {
		// Channel Handlers service/combo
		RpcProxy<List<Pconfig>> channelHandlerCombo = new RpcProxy<List<Pconfig>>() {
			@Override
			protected void load(Object loadConfig, AsyncCallback<List<Pconfig>> callback) {
				service.getConfigurableChannelHandlerConfigs(callback);
			}
		};
		BaseListLoader<ListLoadResult<Pconfig>> channelHandlerLoader = new BaseListLoader<ListLoadResult<Pconfig>>(channelHandlerCombo, new BeanModelReader() );
		handlerStore = new ListStore<BeanModel>(channelHandlerLoader);
		
		// Channel configs 
		RpcProxy<List<ChannelConfig>> channelConfigs = new RpcProxy<List<ChannelConfig>>() {
			@Override
			protected void load(Object loadConfig, AsyncCallback<List<ChannelConfig>> callback) {
				service.listAllChannelConfigs(callback);
			}
		};
		configLoader = new BaseListLoader<ListLoadResult<ChannelConfig>>(channelConfigs, new BeanModelReader() );
		ListStore<BeanModel> configStore = new ListStore<BeanModel>(configLoader);
		
		getView().buildWidget(configStore);
		
		/*getView().getToggleStateAction().setListener(new SelectionListener<GridModelEvent>() {
			@Override
			public void componentSelected(GridModelEvent ce) {
				ChannelConfig config = ce.getModel().getBean();
				toggleActiveState(config);
			}
		});*/
		
		getView().getNewChannelConfigButton().addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				ChannelConfig config = new ChannelConfig();
				editChannelConfig(config);
			}
		});
		
		getView().getEditMenu().addListener(Events.Select, new Listener<GridModelEvent>() {
			@Override
			public void handleEvent(GridModelEvent ce) {
				ChannelConfig config = ce.getModel().getBean();
				editChannelConfig(config);
			}
		});
	}
	
	protected void editChannelConfig(ChannelConfig config) {
		if (config.isPersisted()) {
			BusyIndicator.showBusyIndicator();
			service.getChannelConfigViewModel(config.getId(), new MobilisrAsyncCallback<ChannelConfigViewModel>() {
				@Override
				public void onSuccess(final ChannelConfigViewModel result) {
					BusyIndicator.hideBusyIndicator();
					showWizard(result);
				}
			});
		} else {
			showWizard(new ChannelConfigViewModel(config));
		}
	}
	
	private void showWizard(final ChannelConfigViewModel model) {
		ChannelConfigWizard wizard = new ChannelConfigWizard(model, handlerStore);
		wizard.addListener(MobilisrEvents.WizardFinish, new Listener<BaseEvent>() {
			@Override
			public void handleEvent(BaseEvent be) {
				saveChannelConfig(model);
			}
		});
		wizard.show();
	}

	private void saveChannelConfig(ChannelConfigViewModel model) {
		BusyIndicator.showBusyIndicator();
		service.saveChannelConfig(model, new MobilisrAsyncCallback<Void>() {
			@Override
			public void onSuccess(Void result) {
				BusyIndicator.hideBusyIndicator();
				configLoader.load();
			}
		});
	}
	
	public void onShowChannelConfigListView(ViewModel<ChannelConfig> vem) {
		if (!UserContext.hasPermission(MobilisrPermission.CHANNEL_CONFIG_MANAGE)){
			getEventBus().showErrorView(Messages.INSTANCE
					.securityAccessDenied(MobilisrPermission.CHANNEL_CONFIG_MANAGE
							.name()));
			return;
		}

		if (vem != null && vem.getModelObject() != null){
			ChannelConfig config = (ChannelConfig) vem.getModelObject();
			getView().displaySuccessMsg(Messages.INSTANCE.channelSaveSucess(config.getName()));
		}else{
			getView().clearSuccessMsg();
		}
		
		getEventBus().setRegionRight(this);
		configLoader.load();
	}

	/*private void toggleActiveState(final ChannelConfig config) {
		if (config.isVoided()){
			MessageBoxWithIds.confirm(Messages.INSTANCE.channelConfirmationActivateTitle(),
					Messages.INSTANCE.channelConfirmationActivateOutMessage(),
					new Listener<MessageBoxEvent>() {
				@Override
				public void handleEvent(MessageBoxEvent be) {
					if (be.getButtonClicked().getItemId().equals(Dialog.YES)) {
						performStateToggle(channel);
					}
				}
			});
		} else {
			MessageBoxWithIds.confirm(Messages.INSTANCE.channelConfirmationDeactivateTitle(),
					Messages.INSTANCE.channelConfirmationDeactivateOutMessage(),
					new Listener<MessageBoxEvent>() {
				@Override
				public void handleEvent(MessageBoxEvent be) {
					if (be.getButtonClicked().getItemId().equals(Dialog.YES)) {
						performStateToggle(channel);
					}
				}
			});
		}
	}
	
	private void performStateToggle(final Channel channel) {
		service.toggleActiveState(channel, new MobilisrAsyncCallback<Void>() {
			@Override
			public void onFailure(Throwable error) {
				gridSearch.getLoader().load(0, Constants.INSTANCE.pageSize());
				super.onFailure(error);
			}
			@Override
			public void onSuccess(Void arg0) {
				gridSearch.getLoader().load(0, Constants.INSTANCE.pageSize());
			}
		});
	}*/
}
