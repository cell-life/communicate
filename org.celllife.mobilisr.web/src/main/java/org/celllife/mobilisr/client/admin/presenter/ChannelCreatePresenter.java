package org.celllife.mobilisr.client.admin.presenter;

import java.util.List;

import org.celllife.mobilisr.client.Messages;
import org.celllife.mobilisr.client.UserContext;
import org.celllife.mobilisr.client.admin.AdminEventBus;
import org.celllife.mobilisr.client.admin.ChannelCreateView;
import org.celllife.mobilisr.client.admin.view.ChannelCreateViewImpl;
import org.celllife.mobilisr.client.app.presenter.DirtyPresenter;
import org.celllife.mobilisr.client.model.ViewModel;
import org.celllife.mobilisr.client.view.gxt.BusyIndicator;
import org.celllife.mobilisr.client.view.gxt.MobilisrAsyncCallback;
import org.celllife.mobilisr.client.view.gxt.ModelUtil;
import org.celllife.mobilisr.constants.ChannelType;
import org.celllife.mobilisr.domain.Channel;
import org.celllife.mobilisr.domain.ChannelConfig;
import org.celllife.mobilisr.domain.MobilisrPermission;
import org.celllife.mobilisr.service.gwt.ChannelServiceAsync;
import org.celllife.mobilisr.service.gwt.ChannelViewModel;
import org.celllife.pconfig.model.Pconfig;

import com.extjs.gxt.ui.client.data.BaseListLoader;
import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.data.BeanModelReader;
import com.extjs.gxt.ui.client.data.ListLoadResult;
import com.extjs.gxt.ui.client.data.RpcProxy;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.mvp4g.client.annotation.Presenter;

@Presenter(view=ChannelCreateViewImpl.class)
public class ChannelCreatePresenter extends DirtyPresenter<ChannelCreateView,AdminEventBus> {
	
	@Inject
	private ChannelServiceAsync service;
	
	private ListStore<BeanModel> configStore;

	private ChannelType channelType;
	
	private List<BeanModel> allChannelConfigs;

	private SelectionChangedListener<BeanModel> handlerChangeListener;

	@Override
	public void bindView() {
		// Channel Handlers service/combo
		RpcProxy<List<Pconfig>> channelHandlerCombo = new RpcProxy<List<Pconfig>>() {
			@Override
			protected void load(Object loadConfig, AsyncCallback<List<Pconfig>> callback) {
				service.getChannelHandlerConfigsForType(channelType, callback);
			}
		};
		BaseListLoader<ListLoadResult<Pconfig>> channelHandlerLoader = new BaseListLoader<ListLoadResult<Pconfig>>(channelHandlerCombo, new BeanModelReader() );
		ListStore<BeanModel> handlerStore = new ListStore<BeanModel>(channelHandlerLoader);
		getView().setChannelHandlerStore(handlerStore);
		
		service.listAllChannelConfigs(new MobilisrAsyncCallback<List<ChannelConfig>>() {
			@Override
			public void onSuccess(List<ChannelConfig> result) {
				allChannelConfigs = ModelUtil.convertEntityListToBeanList(result);
			}
		});
		
		configStore = new ListStore<BeanModel>();
		getView().setChannelConfigStore(configStore);
		
		getView().getFormSubmitButton().addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				submitForm();
			}
		});
		
		getView().getFormCancelButton().addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				getView().setDirty(false);
				getEventBus().showChannelListView(new ViewModel<Channel>());
			}
		});
		
		handlerChangeListener = new SelectionChangedListener<BeanModel>() {
			@Override
			public void selectionChanged(SelectionChangedEvent<BeanModel> se) {
				if (se.getSelection().isEmpty()){
					configStore.removeAll();
				} else {
					Pconfig handlerConfig = se.getSelectedItem().getBean();
					channelHandlerChanged(handlerConfig);
				}
			}
		};
		
	}
	
	private void resetConfigStore(){
		configStore.removeAll();
		configStore.add(allChannelConfigs);
	}

	protected void channelHandlerChanged(Pconfig handler) {
		configStore.removeAll();
		for (BeanModel config : allChannelConfigs) {
			if (config.get(ChannelConfig.PROP_HANDLER).equals(handler.getResource())){
				configStore.add(config);
			}
		}
		getView().enableConfigSelection(handler.getParameters() != null 
				&& !handler.getParameters().isEmpty());
	}

	protected void submitForm() {
		final ViewModel<ChannelViewModel> formObject = getView().getFormObject();
		final Channel channel = formObject.getModelObject().getChannel();
		service.saveChannel(formObject.getModelObject(), new MobilisrAsyncCallback<Void>() {
			@Override
			public void onSuccess(Void result) {
				getView().setDirty(false);
				getEventBus().showChannelListView(new ViewModel<Channel>(channel));
			}
		});
	}

	public void onShowChannelCreateView(final ViewModel<Channel> vem) {
		if (!UserContext.hasPermission(MobilisrPermission.CHANNELS_VIEW)){
			getEventBus().showErrorView(Messages.INSTANCE
					.securityAccessDenied(MobilisrPermission.CHANNELS_VIEW
							.name()));
			return;
		}
		
		getView().getChannelHandlerCombo().removeSelectionListener(handlerChangeListener);
		resetConfigStore();
		
		getEventBus().setNavigationConfirmation(this);
		eventBus.setRegionRight(this);
		channelType = vem.getModelObject().getType();
		
		if (vem.isModeUpdate()){
			BusyIndicator.showBusyIndicator();
			service.getChannelViewModel(vem.getModelObject().getId(), new MobilisrAsyncCallback<ChannelViewModel>() {
				@Override
				public void onSuccess(ChannelViewModel result) {
					BusyIndicator.hideBusyIndicator();
					getView().setFormObject(new ViewModel<ChannelViewModel>(result));
				}
			});
		} else {
			ChannelViewModel model = new ChannelViewModel(vem.getModelObject());
			getView().setFormObject(new ViewModel<ChannelViewModel>(model));
		}
		
		if (channelType.equals(ChannelType.IN) 
				&& vem.isModeUpdate()) {
			Scheduler.get().scheduleDeferred(new ScheduledCommand() {
				@Override
				public void execute() {
					service.getMessageCountForChannel(vem.getModelObject(),
							new MobilisrAsyncCallback<Integer>() {
								@Override
								public void onSuccess(Integer result) {
									/*
									 * only enable editing the shortcode if
									 * there have been no messages received on
									 * the channel
									 */
									getView().enableShortcode(result <= 0);
								}
							});
				}
			});
		}
		
		getView().getChannelHandlerCombo().addSelectionChangedListener(handlerChangeListener);
	}
}
