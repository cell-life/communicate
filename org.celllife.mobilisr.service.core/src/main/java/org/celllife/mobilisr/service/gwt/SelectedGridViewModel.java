package org.celllife.mobilisr.service.gwt;

import java.util.List;

public class SelectedGridViewModel<S> {

	private boolean isRemoveAll;
	private List<S> addedList;
	private List<S> removedList;

	public SelectedGridViewModel() {
	}

	public SelectedGridViewModel(boolean isRemoveAll, List<S> addedList, List<S> removedList) {
		super();
		this.isRemoveAll = isRemoveAll;
		this.addedList = addedList;
		this.removedList = removedList;
	}

	public boolean isRemoveAll() {
		return isRemoveAll;
	}

	public void setRemoveAll(boolean isRemoveAll) {
		this.isRemoveAll = isRemoveAll;
	}

	public List<S> getAddedList() {
		return addedList;
	}

	public void setAddedList(List<S> addedList) {
		this.addedList = addedList;
	}

	public List<S> getRemovedList() {
		return removedList;
	}

	public void setRemovedList(List<S> removedList) {
		this.removedList = removedList;
	}
}
