package org.celllife.mobilisr.service.gwt;

import java.io.Serializable;
import java.util.List;


public abstract class ParentChildViewModel<P,C> implements Serializable{

	private static final long serialVersionUID = -8686437059293018404L;
	
	private P parentObject;
	private List<C> addedChildList;
	private List<C> removedChildList;
	private boolean addAll;
	private boolean removeAll;
	
	public ParentChildViewModel() {
	}

	public ParentChildViewModel(P contact,
			List<C> addedChildList,
			List<C> removedChildList, boolean addAll,
			boolean removeAll) {
		super();
		this.parentObject = contact;
		this.addedChildList = addedChildList;
		this.removedChildList = removedChildList;
		this.addAll = addAll;
		this.removeAll = removeAll;
	}
	

	public P getParentObject() {
		return parentObject;
	}

	public void setParentObject(P contact) {
		this.parentObject = contact;
	}

	public List<C> getAddedChildList() {
		return addedChildList;
	}

	public void setAddedChildList(List<C> addedChildList) {
		this.addedChildList = addedChildList;
	}

	public List<C> getRemovedChildList() {
		return removedChildList;
	}

	public void setRemovedChildList(List<C> removedChildList) {
		this.removedChildList = removedChildList;
	}

	public boolean isAddAll() {
		return addAll;
	}

	public void setAddAll(boolean addAll) {
		this.addAll = addAll;
	}

	public boolean isRemoveAll() {
		return removeAll;
	}

	public void setRemoveAll(boolean removeAll) {
		this.removeAll = removeAll;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (addAll ? 1231 : 1237);
		result = prime * result
				+ ((addedChildList == null) ? 0 : addedChildList.hashCode());
		result = prime * result
				+ ((parentObject == null) ? 0 : parentObject.hashCode());
		result = prime * result + (removeAll ? 1231 : 1237);
		result = prime
				* result
				+ ((removedChildList == null) ? 0 : removedChildList.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		@SuppressWarnings("rawtypes")
		ParentChildViewModel other = (ParentChildViewModel) obj;
		if (addAll != other.addAll)
			return false;
		if (addedChildList == null) {
			if (other.addedChildList != null)
				return false;
		} else if (!addedChildList.equals(other.addedChildList))
			return false;
		if (parentObject == null) {
			if (other.parentObject != null)
				return false;
		} else if (!parentObject.equals(other.parentObject))
			return false;
		if (removeAll != other.removeAll)
			return false;
		if (removedChildList == null) {
			if (other.removedChildList != null)
				return false;
		} else if (!removedChildList.equals(other.removedChildList))
			return false;
		return true;
	}
}
