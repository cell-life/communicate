package org.celllife.mobilisr.client.admin.presenter;

import java.util.List;

import org.celllife.mobilisr.client.Constants;
import org.celllife.mobilisr.client.Messages;
import org.celllife.mobilisr.client.UserContext;
import org.celllife.mobilisr.client.admin.AdminEventBus;
import org.celllife.mobilisr.client.admin.ChannelListView;
import org.celllife.mobilisr.client.admin.view.ChannelListViewImpl;
import org.celllife.mobilisr.client.app.presenter.MobilisrBasePresenter;
import org.celllife.mobilisr.client.model.ViewModel;
import org.celllife.mobilisr.client.view.gxt.GridModelEvent;
import org.celllife.mobilisr.client.view.gxt.MessageBoxWithIds;
import org.celllife.mobilisr.client.view.gxt.MobilisrAsyncCallback;
import org.celllife.mobilisr.client.view.gxt.grid.MyGXTPaginatedGridSearch;
import org.celllife.mobilisr.constants.ChannelType;
import org.celllife.mobilisr.domain.Channel;
import org.celllife.mobilisr.domain.MobilisrPermission;
import org.celllife.mobilisr.domain.NumberInfo;
import org.celllife.mobilisr.service.gwt.ChannelServiceAsync;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.mvp4g.client.annotation.Presenter;

@Presenter(view = ChannelListViewImpl.class)
public class ChannelListPresenter extends MobilisrBasePresenter<ChannelListView, AdminEventBus> {

	@Inject
	private ChannelServiceAsync service;

	private MyGXTPaginatedGridSearch<Channel> gridSearch;

	@Override
	public void bindView() {
		
		gridSearch = new MyGXTPaginatedGridSearch<Channel>(
				Channel.PROP_NAME + "," + Channel.PROP_SHORT_CODE,
				Constants.INSTANCE.pageSize()) {
			
			@Override
			public void rpcListServiceCall(PagingLoadConfig pagingLoadConfig, AsyncCallback<PagingLoadResult<Channel>> callback) {
				service.listAllChannels(pagingLoadConfig, callback);
			}
		};
		
		getView().getPagingToolBar().bind(gridSearch.getLoader());
		getView().buildWidget(gridSearch.getStore(), gridSearch.getFilter());
		
		getView().getViewMessageLogsMenu().addListener(Events.Select, new Listener<GridModelEvent>() {
			@Override
			public void handleEvent(GridModelEvent ce) {
				Channel channel = ce.getModel().getBean();
				showMessageLogs(channel);
			}
		});
		

		getView().getViewNumberMappingMenu().addListener(Events.Select, new Listener<GridModelEvent>() {
			@Override
			public void handleEvent(GridModelEvent ce) {
				Channel channel = ce.getModel().getBean();
				showNumberMappings(channel);
			}
		});
		
		getView().getToggleStateMenu().addListener(Events.Select, new Listener<GridModelEvent>() {
			@Override
			public void handleEvent(GridModelEvent ce) {
				Channel channel = ce.getModel().getBean();
				toggleActiveState(channel);
			}
		});
		
		getView().getNewInChannelButton().addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				Channel channel = new Channel();
				channel.setType(ChannelType.IN);
				getEventBus().showChannelCreateView(new ViewModel<Channel>(channel));
			}
		});
		
		getView().getNewOutChannelButton().addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				Channel channel = new Channel();
				channel.setType(ChannelType.OUT);
				getEventBus().showChannelCreateView(new ViewModel<Channel>(channel));
			}
		});
		
		getView().getEditMenu().addListener(Events.Select, new Listener<GridModelEvent>() {
			@Override
			public void handleEvent(GridModelEvent ce) {
				Channel channel = ce.getModel().getBean();
				getEventBus().showChannelCreateView(new ViewModel<Channel>(channel));
			}
		});
	}
	
	protected void showNumberMappings(final Channel channel) {
		service.getNumberMappingsForChannel(channel.getId(), new MobilisrAsyncCallback<List<NumberInfo>>() {
			@Override
			public void onSuccess(List<NumberInfo> result) {
				String prefixes = "<ul>";
				if (result.isEmpty()){
					prefixes += "<li>No numbers are routed to this channel.</li>";
				} else {
					for (NumberInfo numberInfo : result) {
						prefixes += "<li>" + numberInfo.getName() + " (" + numberInfo.getPrefix() + ")</li>";
					}
				}
				prefixes += "</ul>";
				
				final Dialog dialog = new Dialog();  
			    dialog.setHeading("Number routing for channel: " + channel.getName());  
			    dialog.setButtons(Dialog.OK);  
			    dialog.setBodyStyleName("pad-text");  
			    dialog.addText(prefixes);  
			    dialog.getItem(0).getFocusSupport().setIgnore(true);  
			    dialog.setScrollMode(Scroll.AUTO);  
			    dialog.setHideOnButtonClick(true);  
			    dialog.show();
			 }
		});
		
	}

	public void onShowChannelListView(ViewModel<Channel> vem) {
		if (!UserContext.hasPermission(MobilisrPermission.CHANNELS_VIEW)){
			getEventBus().showErrorView(Messages.INSTANCE
					.securityAccessDenied(MobilisrPermission.CHANNELS_VIEW
							.name()));
			return;
		}

		if (vem != null && vem.getModelObject() != null){
			Channel channel = (Channel) vem.getModelObject();
			getView().displaySuccessMsg(Messages.INSTANCE.channelSaveSucess(channel.getName()));
		}else{
			getView().clearSuccessMsg();
		}
		
		getEventBus().setRegionRight(this);
		gridSearch.clearGridSearchTxt();
		gridSearch.getLoader().load(0, Constants.INSTANCE.pageSize());
	}

	private void showMessageLogs(Channel channel) {
		getEventBus().showMessageLog(new ViewModel<Channel>(channel));
	}

	private void toggleActiveState(final Channel channel) {
		if(channel.getType().equals(ChannelType.OUT)){
			MessageBoxWithIds.alert(Messages.INSTANCE.channelConfirmationActivateTitle(),
					Messages.INSTANCE.channelConfirmationActivateOutMessage(), null);
		} else {
			if (!channel.isVoided()){
				service.getActiveFilterCountForChannel(channel, new MobilisrAsyncCallback<Integer>() {
					@Override
					public void onSuccess(Integer result) {
						if (result > 0){
							MessageBoxWithIds.alert(Messages.INSTANCE.channelWarningActiveFiltersTitle(), 
									Messages.INSTANCE.channelWarningActiveFiltersMessage(result), null);
						} else {
							MessageBoxWithIds.confirm(Messages.INSTANCE.channelConfirmationDeactivateTitle(),
									Messages.INSTANCE.channelConfirmationDeactivateInMessage(),
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
				});
			} else {
				performStateToggle(channel);
			}
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
	}
}
