package org.celllife.mobilisr.service.writer;

import java.util.List;

import org.celllife.mobilisr.domain.ContactGroup;
import org.celllife.mobilisr.domain.Organization;

public class CsvDataJobBean {

	private Organization organization;
	private List<ContactGroup> listOfGroups;
	
	public CsvDataJobBean(Organization organization,
			List<ContactGroup> listOfGroups) {
		super();
		this.organization = organization;
		this.listOfGroups = listOfGroups;
	}

	public Organization getOrganization() {
		return organization;
	}

	public void setOrganization(Organization organization) {
		this.organization = organization;
	}

	public List<ContactGroup> getListOfGroups() {
		return listOfGroups;
	}

	public void setListOfGroups(List<ContactGroup> listOfGroups) {
		this.listOfGroups = listOfGroups;
	}

}
