package org.celllife.mobilisr.util;

import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public class LogUtil {
	
	private static final String MARKER_NOTIFY_ADMIN = "NOTIFY_ADMIN";
	
	public static Marker getMarker_notifyAdmin(){
		return MarkerFactory.getMarker(MARKER_NOTIFY_ADMIN);
	}

}
