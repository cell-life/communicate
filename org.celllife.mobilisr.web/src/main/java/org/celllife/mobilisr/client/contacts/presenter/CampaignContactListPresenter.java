package org.celllife.mobilisr.client.contacts.presenter;

import org.celllife.mobilisr.client.Constants;
import org.celllife.mobilisr.client.URLUtil;
import org.celllife.mobilisr.client.app.presenter.MobilisrBasePresenter;
import org.celllife.mobilisr.client.campaign.CampaignEventBus;
import org.celllife.mobilisr.client.contacts.CampaignContactListView;
import org.celllife.mobilisr.client.contacts.view.CampaignContactListViewImpl;
import org.celllife.mobilisr.client.model.ViewModel;
import org.celllife.mobilisr.client.view.gxt.MobilisrAsyncCallback;
import org.celllife.mobilisr.client.view.gxt.grid.MyGXTPaginatedGridSearch;
import org.celllife.mobilisr.domain.Campaign;
import org.celllife.mobilisr.domain.CampaignContact;
import org.celllife.mobilisr.service.gwt.ContactServiceAsync;
import org.celllife.mobilisr.service.gwt.ExportServiceAsync;

import com.extjs.gxt.ui.client.Style.SortDir;
import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.mvp4g.client.annotation.Presenter;

@Presenter(view=CampaignContactListViewImpl.class)
public class CampaignContactListPresenter extends MobilisrBasePresenter<CampaignContactListView, CampaignEventBus> {
	
	@Inject
	private ContactServiceAsync service;
	
	@Inject
	private ExportServiceAsync exportService;
	
	private MyGXTPaginatedGridSearch<CampaignContact> gridSearch;
	
	private Campaign campaign;
	
	@Override
	public void bindView() {
		gridSearch = new MyGXTPaginatedGridSearch<CampaignContact>(
				CampaignContact.PROP_MSISDN, Constants.INSTANCE.pageSize()) {
			
			@Override
			public void rpcListServiceCall(PagingLoadConfig pagingLoadConfig,
					AsyncCallback<PagingLoadResult<CampaignContact>> callback) {
				service.listAllCampaignContactsForCampaign(campaign,
						pagingLoadConfig, true, true, callback);
			}
		};
		
		gridSearch.getStore().setDefaultSort(CampaignContact.PROP_PROGRESS, SortDir.ASC);
		
		getView().getPagingToolBar().bind(gridSearch.getLoader());
		getView().buildWidget(gridSearch.getStore(), gridSearch.getFilter());

		getView().getExportButton().addListener(Events.OnClick, new Listener<ButtonEvent>() {
			public void handleEvent(ButtonEvent be) {
				final MessageBox wait = MessageBox.wait("Exporting", "Exporting contacts", null);
				exportService.exportCampaignContacts(campaign.getId(), new MobilisrAsyncCallback<String>() {
					@Override
					public void onFailure(Throwable error) {
						wait.close();
						super.onFailure(error);
					}
					
					@Override
					public void onSuccess(String result) {
						wait.close();
						URLUtil.getTextFile(result);
					}
				});
			}
		});
		
	}
	
	public void onShowCampaignContactList(ViewModel<Campaign> vem) {
		if (vem != null && vem.getModelObject() != null) {
			campaign = vem.getModelObject();
			gridSearch.clearGridSearchTxt();
			gridSearch.getLoader().load(0, Constants.INSTANCE.pageSize());
			getView().setFormObject(vem);
		}
		
		getEventBus().setRegionRight(this);
	}
	
}
