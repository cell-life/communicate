package org.celllife.mobilisr.client.contacts.presenter;

import org.celllife.mobilisr.client.Constants;
import org.celllife.mobilisr.client.app.presenter.MobilisrBasePresenter;
import org.celllife.mobilisr.client.contacts.ContactEventBus;
import org.celllife.mobilisr.client.contacts.view.ContactCampaignStatusView;
import org.celllife.mobilisr.client.contacts.view.ContactCampaignStatusViewImpl;
import org.celllife.mobilisr.client.model.ViewModel;
import org.celllife.mobilisr.client.view.gxt.grid.MyGXTPaginatedGridSearch;
import org.celllife.mobilisr.domain.CampaignContact;
import org.celllife.mobilisr.domain.Contact;
import org.celllife.mobilisr.service.gwt.CampaignServiceAsync;

import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.mvp4g.client.annotation.Presenter;

@Presenter(view=ContactCampaignStatusViewImpl.class)
public class ContactCampaignStatusPresenter extends MobilisrBasePresenter<ContactCampaignStatusView, ContactEventBus> {

	@Inject
	private CampaignServiceAsync crudCampaignServiceAsync;
	
	private MyGXTPaginatedGridSearch<CampaignContact> gridSearch;
	private Contact contact= null;

	@Override
	public void bindView() {
	
		gridSearch = new MyGXTPaginatedGridSearch<CampaignContact>(CampaignContact.PROP_MSISDN, Constants.INSTANCE.pageSize()) {
			@Override
			public void rpcListServiceCall(PagingLoadConfig pagingLoadConfig, AsyncCallback<PagingLoadResult<CampaignContact>> callback) {
				loadCampaignsByContact(contact, pagingLoadConfig, callback);
			}
		};

		view.getPagingToolBar().bind(gridSearch.getLoader());
		view.buildWidget(gridSearch.getStore(), gridSearch.getFilter(), this);
		
		if (contact!=null)gridSearch.getLoader().load(0, Constants.INSTANCE.pageSize());
	
	}

	private void loadCampaignsByContact(Contact contact, PagingLoadConfig pagingLoadConfig,
			AsyncCallback<PagingLoadResult<CampaignContact>> callback) {
			
		crudCampaignServiceAsync.listCampaignsByContact(contact, pagingLoadConfig, callback);
	}

	public void onShowContactCampaignStatusReport(ViewModel<Contact> vem){
		this.contact = vem.getModelObject(); 
		getView().setFormObject(new ViewModel<Contact>(contact));
		getEventBus().setRegionRight(this);
		gridSearch.clearGridSearchTxt();
		gridSearch.getLoader().load(0, Constants.INSTANCE.pageSize());
		
		
	}
	
}
