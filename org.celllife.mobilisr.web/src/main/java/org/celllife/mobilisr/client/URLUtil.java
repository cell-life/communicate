package org.celllife.mobilisr.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;

public class URLUtil {

	public static void downloadReport(String reportId) {
		String url = GWT.getHostPageBaseURL() + "api/reports/"
				+ reportId;
		Window.open(url, "_blank", "");
	}

	public static void getTextFile(String fileName) {
		String url = GWT.getHostPageBaseURL() + "api/forDownload/" + fileName;
		url += "?delete=true";
		Window.open(url, "_blank", "");
	}
}
