package org.celllife.mobilisr.client.view.gxt.grid;

import org.celllife.mobilisr.client.view.gxt.RemoteStoreFilterField;

import com.extjs.gxt.ui.client.data.BaseFilterPagingLoadConfig;
import com.extjs.gxt.ui.client.data.BasePagingLoadConfig;
import com.extjs.gxt.ui.client.data.BasePagingLoader;
import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.data.BeanModelReader;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.extjs.gxt.ui.client.data.PagingLoader;
import com.extjs.gxt.ui.client.data.RpcProxy;
import com.extjs.gxt.ui.client.store.ListStore;
import com.google.gwt.user.client.rpc.AsyncCallback;

public abstract class MyGXTPaginatedGridSearch<T> {

	protected String gridSearchText;
	protected PagingLoader<PagingLoadResult<ModelData>> loader = null;
	protected RemoteStoreFilterField<BeanModel> filter;
	private ListStore<BeanModel> store;

	public MyGXTPaginatedGridSearch(){
		
	}
	
	/**
	 * @param filterFields a comma separated list of search fields
	 * @param maxResultsPerPage
	 */
	public MyGXTPaginatedGridSearch(final String filterFields, final int maxResultsPerPage) {
					
			RpcProxy<PagingLoadResult<T>> proxy = invokeRPCProxy(filterFields);
			
			filter = new RemoteStoreFilterField<BeanModel>() {
				
				@Override
				protected void handleOnFilter(String filterValue) {
					gridSearchText = filterValue;
					loader.load(0, maxResultsPerPage);
				}
				
				@Override
				protected void handleCancelFilter() {
					gridSearchText = null;
					loader.load(0, maxResultsPerPage);
				}
			};
		loader = new BasePagingLoader<PagingLoadResult<ModelData>>(proxy, new BeanModelReader()){
			@Override  
	        protected Object newLoadConfig() {  
	          BasePagingLoadConfig config = new BaseFilterPagingLoadConfig();  
	          return config;  
	        }
		};
		loader.setRemoteSort(true);
		store = createStore(loader);
	}

	protected ListStore<BeanModel> createStore(PagingLoader<PagingLoadResult<ModelData>> loader) {
		return new ListStore<BeanModel>(loader);
	}

	protected RpcProxy<PagingLoadResult<T>> invokeRPCProxy(final String filterFields) {
		RpcProxy<PagingLoadResult<T>> proxy = new RpcProxy<PagingLoadResult<T>>() {
			
			public void load(Object loadConfig, AsyncCallback<PagingLoadResult<T>> callback) {
				if(gridSearchText == null){
					filter.clear();
				}
				PagingLoadConfig pagingLoadConfig = (PagingLoadConfig) loadConfig;
				pagingLoadConfig.set(RemoteStoreFilterField.PARM_QUERY, gridSearchText);
				pagingLoadConfig.set(RemoteStoreFilterField.PARM_FIELDS, filterFields);
				rpcListServiceCall(pagingLoadConfig, callback);
			}
		};
		return proxy;
	}

	public abstract void rpcListServiceCall(PagingLoadConfig pagingLoadConfig, AsyncCallback<PagingLoadResult<T>> callback);

	public PagingLoader<PagingLoadResult<ModelData>> getLoader() {
		return loader;
	}

	public ListStore<BeanModel> getStore() {
		return store;
	}

	public RemoteStoreFilterField<BeanModel> getFilter() {
		return filter;
	}
	
	public void clearGridSearchTxt(){
		gridSearchText = null;
	}

	public String getGridSearchText() {
		return gridSearchText;
	}
}
