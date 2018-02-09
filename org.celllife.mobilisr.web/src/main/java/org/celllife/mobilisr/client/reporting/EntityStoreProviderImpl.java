package org.celllife.mobilisr.client.reporting;

import org.celllife.mobilisr.client.view.gxt.RemoteStoreFilterField;
import org.celllife.mobilisr.domain.MobilisrEntity;
import org.celllife.mobilisr.domain.Organization;
import org.celllife.mobilisr.service.gwt.AdminServiceAsync;
import org.celllife.pconfig.model.Pconfig;

import com.extjs.gxt.ui.client.data.BasePagingLoader;
import com.extjs.gxt.ui.client.data.BeanModelReader;
import com.extjs.gxt.ui.client.data.ListLoader;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.extjs.gxt.ui.client.data.RpcProxy;
import com.extjs.gxt.ui.client.store.ListStore;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class EntityStoreProviderImpl implements EntityStoreProvider {

	protected AdminServiceAsync adminService;
	private Organization organization;

	public EntityStoreProviderImpl(AdminServiceAsync adminService) {
		this.adminService = adminService;
	}

	@Override
	public ListStore<ModelData> getEntityStore(final String entityName, final String searchField) {
		RpcProxy<PagingLoadResult<MobilisrEntity>> proxy = new RpcProxy<PagingLoadResult<MobilisrEntity>>() {
			@Override
			protected void load(Object loadConfig, final AsyncCallback<PagingLoadResult<MobilisrEntity>> callback) {
				PagingLoadConfig config = (PagingLoadConfig) loadConfig;
				config.set(RemoteStoreFilterField.PARM_FIELDS, searchField);
				adminService.getEntityList(organization, entityName, config, callback);
			}
		};

		ListLoader<PagingLoadResult<Pconfig>> loader = new BasePagingLoader<PagingLoadResult<Pconfig>>(proxy,new BeanModelReader());
		ListStore<ModelData> store = new ListStore<ModelData>(loader);

		return store;
	}
	
	@Override
	public void restrictResultsToOrganization(Organization organization){
		this.organization = organization;
	}
	
	@Override
	public Organization getOrganizationRestriction()	{
		return organization;
	}
	
	@Override
	public boolean isRestrctedToOrganization(){
		return this.organization != null;
	}

}