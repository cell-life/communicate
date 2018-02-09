package org.celllife.mobilisr.client.app;

public interface DirtyView extends BasicView {
	
	public boolean isDirty();
	
	public void setDirty(boolean dirty);

	void setDirty();
}

