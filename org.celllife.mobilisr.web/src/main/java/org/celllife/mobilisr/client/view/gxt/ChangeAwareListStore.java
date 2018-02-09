package org.celllife.mobilisr.client.view.gxt;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.data.LoadEvent;
import com.extjs.gxt.ui.client.data.ModelComparer;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.extjs.gxt.ui.client.data.PagingLoader;
import com.extjs.gxt.ui.client.store.ListStore;

public class ChangeAwareListStore<M extends BeanModel> extends ListStore<M> {

	List<M> added = new ArrayList<M>();
	List<M> removed = new ArrayList<M>();

	public ChangeAwareListStore(ModelComparer<M> modelComparer) {
		super();
		init(modelComparer);
	}
	 
	private void init(ModelComparer<M> modelComparer) {
		setModelComparer(modelComparer);
	}
	
	public ChangeAwareListStore(PagingLoader<PagingLoadResult<ModelData>> selGroupLoader, ModelComparer<M> modelComparer) {
		super(selGroupLoader);
		init(modelComparer);
	}

	@Override
	public void add(M model) {
		super.insert(model, 0);
		if (removed.contains(model)) {
			removed.remove(model);
		} 
		added.add(model);
	}
	
	@Override
	public void add(List<? extends M> models) {
		super.add(models);
		for (M m : models) {
			if (removed.contains(m)) {
				removed.remove(m);
			} else {
				added.add(m);
			}
		}
	}

	/**
	 * Doesn't record changes made to the store in the added or remove list. 
	 * Only adds models if they aren't in the removed list.
	 */
	public void addIgnoreChange(List<? extends M> models) {
		super.add(models);
		removeRemoved();
	}
	
	/**
	 * Doesn't record changes made to the store in the added or remove list
	 */
	public void addIgnoreChange(M model) {
		super.add(model);
	}
	
	@Override
	public void remove(M model) {
		int index = indexOf(model);
		super.remove(model);
		if (added.contains(model)) {
			added.remove(model);
		} /*else {
			removed.add(model);
		}*/
		if (index > -1){
			removed.add(model);
		}
	}
	
	public void removeAllIgnoreChanges() {
		super.removeAll();
	}

	public void removeAllClearChanges() {
		super.removeAll();
		removed.clear();
		added.clear();
	}

	@Override
	protected void onLoad(LoadEvent le) {
		super.onLoad(le);
		removeRemoved();
		super.add(added);
	}

	private void removeRemoved() {
		for (M model : removed) {
			int index = super.indexOf(model);
			if (index > -1) {
				M at = super.getAt(index);
				super.remove(at);
			}
		}
	}

	public List<M> getAdded() {
		return added;
	}

	public List<M> getRemoved() {
		return removed;
	}
	
	public void clearAdded(){
		added = new ArrayList<M>();
	}
	
	public void clearRemoved(){
		removed = new ArrayList<M>();
	}
	
	public boolean isDirty(){
		return (added.size() > 0) || (removed.size() > 0);
	}
}
