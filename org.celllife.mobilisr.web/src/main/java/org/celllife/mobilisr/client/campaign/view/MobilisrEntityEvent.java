package org.celllife.mobilisr.client.campaign.view;

import org.celllife.mobilisr.domain.MobilisrEntity;

import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.EventType;


public class MobilisrEntityEvent extends BaseEvent {
	
	private MobilisrEntity entityObject;
	private boolean goNext;
	
	public MobilisrEntityEvent(EventType type, MobilisrEntity entityObject){
		super(type);
		setEntityObject(entityObject);
	}
	
	public MobilisrEntityEvent(EventType type, MobilisrEntity entityObject, boolean goNext) {
		super(type);
		setEntityObject(entityObject);
		setGoNext(goNext);
	}
	
	public MobilisrEntity getEntityObject() {
		return entityObject;
	}

	public void setEntityObject(MobilisrEntity entityObject) {
		this.entityObject = entityObject;
	}

	public boolean isGoNext() {
		return goNext;
	}

	public void setGoNext(boolean goNext) {
		this.goNext = goNext;
	}	
}
