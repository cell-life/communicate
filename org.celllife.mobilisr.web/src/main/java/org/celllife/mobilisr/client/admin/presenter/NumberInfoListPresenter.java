package org.celllife.mobilisr.client.admin.presenter;

import java.util.List;

import org.celllife.mobilisr.client.Constants;
import org.celllife.mobilisr.client.Messages;
import org.celllife.mobilisr.client.UserContext;
import org.celllife.mobilisr.client.admin.AdminEventBus;
import org.celllife.mobilisr.client.admin.NumberInfoListView;
import org.celllife.mobilisr.client.admin.view.NumberInfoListViewImpl;
import org.celllife.mobilisr.client.admin.view.NumberInfoWizard;
import org.celllife.mobilisr.client.app.presenter.MobilisrBasePresenter;
import org.celllife.mobilisr.client.model.ViewModel;
import org.celllife.mobilisr.client.view.gxt.BusyIndicator;
import org.celllife.mobilisr.client.view.gxt.GridModelEvent;
import org.celllife.mobilisr.client.view.gxt.MessageBoxWithIds;
import org.celllife.mobilisr.client.view.gxt.MobilisrAsyncCallback;
import org.celllife.mobilisr.client.view.gxt.StepValidator;
import org.celllife.mobilisr.client.view.gxt.WizardCard;
import org.celllife.mobilisr.client.view.gxt.grid.MyGXTPaginatedGridSearch;
import org.celllife.mobilisr.domain.Channel;
import org.celllife.mobilisr.domain.MobilisrPermission;
import org.celllife.mobilisr.domain.NumberInfo;
import org.celllife.mobilisr.service.gwt.ChannelServiceAsync;

import com.extjs.gxt.ui.client.data.BaseListLoader;
import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.data.BeanModelReader;
import com.extjs.gxt.ui.client.data.ListLoadResult;
import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.extjs.gxt.ui.client.data.RpcProxy;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.mvp4g.client.annotation.Presenter;

@Presenter(view = NumberInfoListViewImpl.class)
public class NumberInfoListPresenter extends MobilisrBasePresenter<NumberInfoListView, AdminEventBus> {

	@Inject
	private ChannelServiceAsync service;

	private MyGXTPaginatedGridSearch<NumberInfo> gridSearch;

	private ListStore<BeanModel> channelStore;

	@Override
	public void bindView() {
		// Channel combo
		RpcProxy<List<Channel>> channelCombo = new RpcProxy<List<Channel>>() {
			@Override
			protected void load(Object loadConfig, AsyncCallback<List<Channel>> callback) {
				service.listOutgoingChannels(callback);
			}
		};
		BaseListLoader<ListLoadResult<Channel>> channelLoader = new BaseListLoader<ListLoadResult<Channel>>(channelCombo, new BeanModelReader() );
		channelStore = new ListStore<BeanModel>(channelLoader);
		
		gridSearch = new MyGXTPaginatedGridSearch<NumberInfo>(
				NumberInfo.PROP_NAME + "," + NumberInfo.PROP_PREFIX,
				Constants.INSTANCE.pageSize()) {
			
			@Override
			public void rpcListServiceCall(PagingLoadConfig pagingLoadConfig, AsyncCallback<PagingLoadResult<NumberInfo>> callback) {
				service.listAllNumberInfo(pagingLoadConfig, callback);
			}
		};
		
		getView().getPagingToolBar().bind(gridSearch.getLoader());
		getView().buildWidget(gridSearch.getStore(), gridSearch.getFilter());
		
		getView().getNewEntityButton().addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				showWizard(new NumberInfo());
			}
		});
		
		getView().getEditMenu().addListener(Events.Select, new Listener<GridModelEvent>() {
			@Override
			public void handleEvent(GridModelEvent ce) {
				NumberInfo info = ce.getModel().getBean();
				showWizard(info);
			}
		});
		
		getView().getVoidMenu().addListener(Events.Select, new Listener<GridModelEvent>() {
			@Override
			public void handleEvent(GridModelEvent ce) {
				final NumberInfo info = ce.getModel().getBean();
				if (!info.isVoided()){
					// warn if deactivating
					MessageBoxWithIds.confirm(Messages.INSTANCE.numberInfoConfirmationDeactivateTitle(),
							Messages.INSTANCE.numberInfoConfirmationDeactivateMessage(info.getPrefix()),
							new Listener<MessageBoxEvent>() {
						@Override
						public void handleEvent(MessageBoxEvent be) {
							if (be.getButtonClicked().getItemId().equals(Dialog.YES)) {
								info.setVoided(!info.getVoided());
								saveNumberInfo(info, null);
							}
						}
					});
				} else {
					info.setVoided(!info.getVoided());
					saveNumberInfo(info, null);
				}
			}
		});
	}
	
	public void onShowNumberInfoList(ViewModel<NumberInfo> vem) {
		if (!UserContext.hasPermission(MobilisrPermission.NUMBER_INFO_VIEW)){
			getEventBus().showErrorView(Messages.INSTANCE
					.securityAccessDenied(MobilisrPermission.NUMBER_INFO_VIEW
							.name()));
			return;
		}

		if (vem != null && vem.getModelObject() != null){
			NumberInfo info = vem.getModelObject();
			getView().displaySuccessMsg(Messages.INSTANCE.channelSaveSucess(info.getName()));
		}else{
			getView().clearSuccessMsg();
		}
		
		getEventBus().setRegionRight(this);
		gridSearch.clearGridSearchTxt();
		gridSearch.getLoader().load(0, Constants.INSTANCE.pageSize());
	}
	
	private void showWizard(final NumberInfo model) {
		final NumberInfoWizard wizard = new NumberInfoWizard(model, channelStore);
		wizard.setSaveCallback(new StepValidator() {
			@Override
			public void validate(WizardCard wizardCard, int currentStep,
					AsyncCallback<Void> callback) {
				saveNumberInfo(model, callback);
			}
		});
		wizard.show();
	}
	
	private void saveNumberInfo(final NumberInfo model, final AsyncCallback<Void> callback) {
		BusyIndicator.showBusyIndicator("Saving " + model.getName());
		service.saveNumberInfo(model, new MobilisrAsyncCallback<Void>() {
			@Override
			public void onSuccess(Void result) {
				BusyIndicator.hideBusyIndicator();
				if (callback != null){
					callback.onSuccess(null);
				}
				gridSearch.getLoader().load(0, Constants.INSTANCE.pageSize());
			}
			
			@Override
			protected void handleExpectedException(Throwable error) {
				if (callback != null){
					callback.onFailure(error);
				} else {
					super.handleExpectedException(error);
				}
			}
		});
	}

}
