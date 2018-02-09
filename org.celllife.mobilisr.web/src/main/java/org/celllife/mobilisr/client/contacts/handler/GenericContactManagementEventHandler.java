package org.celllife.mobilisr.client.contacts.handler;

import java.util.List;

import org.celllife.mobilisr.client.Constants;
import org.celllife.mobilisr.client.Resources;
import org.celllife.mobilisr.client.contacts.GenericContactManagementView;
import org.celllife.mobilisr.client.model.ViewModel;
import org.celllife.mobilisr.client.view.gxt.Action;
import org.celllife.mobilisr.client.view.gxt.ButtonGridCellRenderer;
import org.celllife.mobilisr.client.view.gxt.ChangeAwareListStore;
import org.celllife.mobilisr.client.view.gxt.GridModelEvent;
import org.celllife.mobilisr.client.view.gxt.MessageBoxWithIds;
import org.celllife.mobilisr.client.view.gxt.MobilisrAsyncCallback;
import org.celllife.mobilisr.client.view.gxt.RemoteStoreFilterField;
import org.celllife.mobilisr.domain.MobilisrEntity;
import org.celllife.mobilisr.service.gwt.ContactService;
import org.celllife.mobilisr.service.gwt.ContactServiceAsync;

import com.extjs.gxt.ui.client.data.BasePagingLoadResult;
import com.extjs.gxt.ui.client.data.BasePagingLoader;
import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.data.BeanModelReader;
import com.extjs.gxt.ui.client.data.LoadEvent;
import com.extjs.gxt.ui.client.data.ModelComparer;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.extjs.gxt.ui.client.data.PagingLoader;
import com.extjs.gxt.ui.client.data.RpcProxy;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.LoadListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.Store;
import com.extjs.gxt.ui.client.store.StoreEvent;
import com.extjs.gxt.ui.client.store.StoreFilter;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;

public abstract class GenericContactManagementEventHandler<T> {
	
	/**
	 * This custom load listener is used when we do the right grid through RPC
	 * in case when user selected "Add all"
	 */
	class CustomLoadListener extends LoadListener{
		
		@Override
		public void loaderLoad(LoadEvent le) {
			super.loaderLoad(le);
			BasePagingLoadResult<BeanModel> loadResult = le.getData();
			
			List<BeanModel> data = loadResult.getData();
			selectedEntityStore.addIgnoreChange(data);			
			int totalCount = loadResult.getTotalLength()-selectedEntityStore.getRemoved().size();
			totalAdded = totalCount;
			view.updatePagingLabel(selectedEntityStore.getCount(), totalCount);
			view.setNextTenButtonEnabled((selectedEntityStore.getCount() < loadResult.getTotalLength()));			
		}
	}


	private GenericContactManagementView<T> view;
	
	private ModelComparer<BeanModel> modelComparer;
	
	private PagingLoader<PagingLoadResult<ModelData>> listOfEntityLoader = null;
	private PagingLoader<PagingLoadResult<ModelData>> selectedEntityLoader = null;
	private ListStore<BeanModel> listOfEntityStore = null;
	private ChangeAwareListStore<BeanModel> selectedEntityStore = null;

	private RemoteStoreFilterField<BeanModel> selEntityFilter = null;
	private RemoteStoreFilterField<BeanModel> listEntityFilter = null;
	
	private String listEntitySearch;
	private String selEntitySearch;
	private int totalAdded = 0;
	
	private ContactServiceAsync contactsServiceAsync;
	private ViewModel<? extends MobilisrEntity> vem;

	public GenericContactManagementEventHandler(GenericContactManagementView<T> view, ModelComparer<BeanModel> modelComparer){
		this.view = view;
		this.modelComparer = modelComparer;
		totalAdded = 0;
		eventBinding();
		if(contactsServiceAsync == null){
			contactsServiceAsync = (ContactServiceAsync) GWT.create(ContactService.class);
		}
	}

	public ContactServiceAsync getContactsServiceAsync() {
		return contactsServiceAsync;
	}
	
	public void eventBinding(){
		ButtonGridCellRenderer addBtnListRenderer = new ButtonGridCellRenderer();
		addBtnListRenderer.addAction(new Action(null, "Add", Resources.INSTANCE.add(), "add",new SelectionListener<GridModelEvent>() {
			@Override
			public void componentSelected(GridModelEvent ce) {
				BeanModel model = ce.getModel();
				//Check the mode
				if(view.getAddAllMode() == GenericContactManagementView.MODE_ADDALL){
					if(view.addEntityToRightGrid(model)){
						totalAdded++;
					}
					
					if(totalAdded == listOfEntityLoader.getTotalCount()){
						MessageBoxWithIds.info("", "All the items have been added", null);
					}
				}else if(view.getAddAllMode() == GenericContactManagementView.MODE_MAX_ADD){
					if(totalAdded >= view.getMaxAddAllValue()){
						MessageBoxWithIds.info("","Sorry, maximum number of " + view.getMaxAddAllValue() + " items are allowed to be added", null);
					}else{
						if(view.addEntityToRightGrid(model)){
							totalAdded++;
						}
					}
				}
			}
		}));
		
		ButtonGridCellRenderer delBtnListRenderer = new ButtonGridCellRenderer();
		delBtnListRenderer.addAction(new Action(null, "Remove", Resources.INSTANCE.delete(), "remove", new SelectionListener<GridModelEvent>() {
			@Override
			public void componentSelected(GridModelEvent ce) {
				BeanModel model = ce.getModel();
				view.removeEntityFromSelectedGrid(model);
				totalAdded--;
				if(totalAdded < 0){ totalAdded = 0; }
			}
		}));
		
		RpcProxy<PagingLoadResult<T>> listGroupProxy = new RpcProxy<PagingLoadResult<T>>() {
			public void load(Object loadConfig,	AsyncCallback<PagingLoadResult<T>> callback) {
				handleRPCLoadListAllEntities(vem, listEntitySearch, loadConfig, callback);
			}
		};
		
		RpcProxy<PagingLoadResult<T>> selGroupProxy = new RpcProxy<PagingLoadResult<T>>() {
			public void load(Object loadConfig,	AsyncCallback<PagingLoadResult<T>> callback) {
				if(isAddAllSelected()){
					handleRPCLoadListAllEntities(vem,selEntitySearch, loadConfig, callback);
				}else{
					if(vem != null){
						handleRPCLoadListAssociationForObject(vem, loadConfig, callback);
					}
				}
			}
		};

		listEntityFilter = new RemoteStoreFilterField<BeanModel> () {
				/* handle filtering - this is a call after each key pressed - it might be improved */
	           @Override
	           protected void handleOnFilter (String filterValue) {
	        	   listEntitySearch = filterValue;
	               listOfEntityLoader.load(0, Constants.INSTANCE.pageSize());
	           }
	           /* handles the filtering cancellation */
	           @Override
	           protected void handleCancelFilter () {
	        	   listEntitySearch = null;
	        	   listOfEntityLoader.load(0, Constants.INSTANCE.pageSize());
	            }
	        };
		
	    selEntityFilter = new RemoteStoreFilterField<BeanModel> () {
	    			/* handle filtering - this is a call after each key pressed - it might be improved */
		           @Override
		           protected void handleOnFilter (String filterValue) {
		        	   selEntitySearch = filterValue;
		        	   
		        	   //If add all group is selected, then we use RPC search, otherwise its local search in liststore
		        	   if(isAddAllSelected()){
		        		   selectedEntityLoader.load(0, Constants.INSTANCE.pageSize());
		        	   }else{
		        		   handleFilterInRightGridStore(filterValue);
		        	   }
		           }
		           
		           /* handles the filtering cancellation */
		           @Override
		           protected void handleCancelFilter () {
		        	   selEntitySearch = null;
		        	   //If add all group is selected, then we use RPC search to clear and obtain the list, otherwise its clear values in liststore
		        	   if(isAddAllSelected()){
		        		   view.clearRightGrid();
						   view.setNextTenButtonEnabled(false);
						   int totalRecords = listOfEntityLoader.getTotalCount()-selectedEntityStore.getRemoved().size();
						   view.updatePagingLabel(0, totalRecords);
		        	   }else{
		        		   selectedEntityStore.clearFilters();
		        	   }
		            }

		        };    
	        
	    listOfEntityLoader = new BasePagingLoader<PagingLoadResult<ModelData>>(listGroupProxy,new BeanModelReader());
		listOfEntityStore = new ListStore<BeanModel>(listOfEntityLoader);
		selectedEntityLoader = new BasePagingLoader<PagingLoadResult<ModelData>>(selGroupProxy,new BeanModelReader());
		selectedEntityLoader.addLoadListener(new CustomLoadListener());
		selectedEntityStore = new ChangeAwareListStore<BeanModel>(modelComparer);
		
		
		//If add to All groups is not selected then only we use local search, otherwise its RPC search
		if(!isAddAllSelected()){
			selectedEntityStore.addFilter(new StoreFilter<BeanModel>() {
			
				@Override
				public boolean select(Store<BeanModel> store, BeanModel parent,
						BeanModel item, String property) {
					String beanValue = (String)parent.get(property);
					beanValue = beanValue.toLowerCase();
					if (beanValue.indexOf(selEntitySearch.toLowerCase()) != -1) {
						return true;
					}
					return false;
				}
			});
		
			selectedEntityStore.addListener(Store.Filter, new Listener<StoreEvent<BeanModel>>() {

				@SuppressWarnings("unchecked")
				@Override
				public void handleEvent(StoreEvent<BeanModel> se) {
					ChangeAwareListStore<BeanModel> storeList = (ChangeAwareListStore<BeanModel>) se.getStore();
					int totalRecords = storeList.getCount();
					view.updatePagingLabel(totalRecords, totalRecords);
				}
			});
		}
		
		view.getListOfEntityToolBar().bind(listOfEntityLoader);
		view.buildLeftGroupGridPanel(listOfEntityStore, listEntityFilter, addBtnListRenderer);
		view.buildRightGroupGridPanel(selectedEntityStore, selEntityFilter, delBtnListRenderer);
		view.getAddAllAnchor().addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent ce) {
				int totalCount = listOfEntityLoader.getTotalCount();
				totalAdded = totalCount;
				view.addAllSelected(totalCount);
				
				MessageBoxWithIds.info("","All the items have been added", null);
				//Update the text for the empty grid
				selectedEntityLoader.load(0, Constants.INSTANCE.pageSize());
			}
		});
		
		view.getRemoveAllAnchor().addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent ce) {
				view.removeAllSelected();
				totalAdded = 0;
			}
		});
		
		//When the operator wants to create a new group using the quick new group add feature
		view.getAddNewEntityButton().addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				AsyncCallback<T> callback = new MobilisrAsyncCallback<T>() {
					@Override
					protected void handleExpectedException(Throwable error) {
						handleObjectDisplayFailMessage(error);
					}
					
					@Override
					public void onSuccess(T object) {
						handleObjectCreateSuccess(object);
					}
				};
				
				handleRPCObjectSave(vem,view.getAddNewEntityFieldValue(), callback);
			}
		});
		
		view.getNextTenButton().addSelectionListener(new SelectionListener<ButtonEvent>() {

			@Override
			public void componentSelected(ButtonEvent ce) {
				
				//Check if "Add Contact to All Groups" is selected
				if( isAddAllSelected()){
					
					//We do a RPC call for obtaining the next results
					//NOTE: these results will be paginated with search so it does the trick
					selectedEntityLoader.load((selectedEntityLoader.getOffset() + 10), Constants.INSTANCE.pageSize());
				}
				
				if( vem != null){
					if(vem.isModeUpdate()){
						selectedEntityLoader.load((selectedEntityLoader.getOffset() + 10), Constants.INSTANCE.pageSize());
					}
				}
			}
		});
	}

	public PagingLoader<PagingLoadResult<ModelData>> getListEntityLoader() {
		return listOfEntityLoader;
	}

	public PagingLoader<PagingLoadResult<ModelData>> getSelEntityLoader() {
		return selectedEntityLoader;
	}
	
	public ChangeAwareListStore<BeanModel> getSelectedEntityStore() {
		return selectedEntityStore;
	}

	public void clearFormValues() {
		selectedEntityStore.clearAdded();
		selectedEntityStore.clearRemoved();
		view.clearUI();
		listEntityFilter.clear();
		selEntityFilter.clear();
		totalAdded = 0;
	}
	
	public void setViewModel(ViewModel<? extends MobilisrEntity> vem) {
		this.vem = vem;
	}

	public void handleObjectCreateSuccess(T entityObject){
		view.clearAddNewEntityFieldValue();
		view.addEntityToLeftGrid(entityObject);
		if(!isAddAllSelected()){
			view.addEntityToRightGrid(entityObject);
		}else{
			view.updatePagingLabel(view.getCurrentRecordsDisplayed(), view.getTotalRecords() + 1);
		}
		handleObjectDisplaySaveMsg(entityObject);
	}
	
	protected PagingLoadConfig getLoadingConfig(String searchVal, String fields, Object loadConfig) {
		PagingLoadConfig pagingLoadConfig = (PagingLoadConfig) loadConfig;
		pagingLoadConfig.set(RemoteStoreFilterField.PARM_QUERY, searchVal);
		pagingLoadConfig.set(RemoteStoreFilterField.PARM_FIELDS, fields);
		return pagingLoadConfig;
	}
	
	public boolean isAddAllSelected() {
		return view.isAddAllSelected();
	}

	public boolean isRemoveAllSelected() {
		return view.isRemoveAllSelected();
	}
	
	protected String getSelEntitySearch() {
		return selEntitySearch;
	}
	
	public abstract void handleRPCObjectSave(ViewModel<? extends MobilisrEntity> vem, String newEntityFieldValue, AsyncCallback<T> callback);	
	public abstract void handleObjectDisplaySaveMsg(T entityObject);
	public abstract void handleObjectDisplayFailMessage(Throwable error);
	
	
	/**
	 * Happens when the RPC is invoked for listing all the entities
	 *  
	 * @param searchVal
	 * @param loadConfig
	 * @param callback
	 */
	public abstract void handleRPCLoadListAllEntities(ViewModel<? extends MobilisrEntity> vem,String searchVal, Object loadConfig, AsyncCallback<PagingLoadResult<T>> callback);
	
	/**
	 * Happens when "addAll" isn't selected and we want to load collection of n that belong to m
	 * 
	 * @param viewEntityModel
	 * @param loadConfig
	 * @param callback
	 */
	public abstract void handleRPCLoadListAssociationForObject(ViewModel<? extends MobilisrEntity> viewEntityModel, Object loadConfig, AsyncCallback<PagingLoadResult<T>> callback);
	
	/**
	 * Happens when the search is invoked on the right grid on the client itself not RPC
	 * 
	 * @param filterValue
	 */
	public abstract void handleFilterInRightGridStore(String filterValue);

	public ViewModel<? extends MobilisrEntity> getViewModel() {
		return vem; 
	}

	

}
