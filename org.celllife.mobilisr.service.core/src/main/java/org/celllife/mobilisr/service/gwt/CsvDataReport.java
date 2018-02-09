package org.celllife.mobilisr.service.gwt;

import java.io.Serializable;
import java.util.List;

import org.celllife.mobilisr.domain.Contact;

public class CsvDataReport implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Integer numOfRecordsStored;
	private List<Contact> errorContactList;
	private Integer numOfErrors;
	
	
	public CsvDataReport() {
	}

	public CsvDataReport(Integer numOfRecordsStored, Integer numOfErrors) {
		super();
		this.numOfRecordsStored = numOfRecordsStored;
		this.numOfErrors = numOfErrors;
	}

	public Integer getNumOfRecordsStored() {
		return numOfRecordsStored;
	}

	public void setNumOfRecordsStored(Integer numOfRecordsStored) {
		this.numOfRecordsStored = numOfRecordsStored;
	}

	public List<Contact> getErrorContactList() {
		return errorContactList;
	}

	public void setErrorContactList(List<Contact> errorContactList) {
		this.errorContactList = errorContactList;
	}

	public void setNumOfErrors(Integer numOfErrors) {
		this.numOfErrors = numOfErrors;
	}

	public Integer getNumOfErrors() {
		return numOfErrors;
	}
	
}
