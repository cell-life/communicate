package org.celllife.mobilisr.client;

import com.extjs.gxt.ui.client.event.EventType;

public class MobilisrEvents {

	public static final EventType SAVE = new EventType();
	
	public static final EventType CANCEL = new EventType();
	
	public static final EventType WizardStep = new EventType();
	public static final EventType WizardFinish = new EventType();

	public static final EventType ActionComplete = new EventType();
	public static final EventType NavigationChange = new EventType();

	private MobilisrEvents() {
	}
}
