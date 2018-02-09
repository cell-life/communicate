package org.celllife.mobilisr.service.gwt;

import java.io.Serializable;

import com.extjs.gxt.ui.client.data.BeanModelTag;

public class SmsReportData implements BeanModelTag, Serializable {
	
	public static final String PROP_LABEL = "label";
	public static final String PROP_TOTAL = "numberOfMessages";
	public static final String PROP_FAILURES = "numberOfFailures";
	
	private static final long serialVersionUID = 2103424615978497334L;
	private String label;
	private Long numberOfMessages;
	private Long numberOfFailures;
	
	/**
	 * No arg constructor for GWT serialization
	 */
	public SmsReportData() {
	}

	public SmsReportData(String label, Long numberOfMessages, Long numberOfFailures) {
		this.label = label;
		this.numberOfMessages = numberOfMessages;
		this.numberOfFailures = numberOfFailures;
	}
	
	public String getLabel() {
		return label;
	}

	public Long getNumberOfMessages() {
		return numberOfMessages;
	}
	
	public Long getNumberOfFailures() {
		return numberOfFailures;
	}

	@Override
	public String toString() {
		return "SmsReportData [label=" + label + ", numberOfMessages="
				+ numberOfMessages + ", numberOfFailures=" + numberOfFailures
				+ "]";
	}

	public void clearLabel() {
		label = null;
	}
}
