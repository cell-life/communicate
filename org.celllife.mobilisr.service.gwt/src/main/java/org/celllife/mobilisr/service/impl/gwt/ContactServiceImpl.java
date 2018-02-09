package org.celllife.mobilisr.service.impl.gwt;

import java.util.List;

import javax.servlet.ServletException;

import org.celllife.mobilisr.domain.Campaign;
import org.celllife.mobilisr.domain.CampaignContact;
import org.celllife.mobilisr.domain.Contact;
import org.celllife.mobilisr.domain.ContactGroup;
import org.celllife.mobilisr.domain.Organization;
import org.celllife.mobilisr.exception.MobilisrException;
import org.celllife.mobilisr.exception.MobilisrRuntimeException;
import org.celllife.mobilisr.service.ContactsService;
import org.celllife.mobilisr.service.gwt.ContactContactGroupViewModel;
import org.celllife.mobilisr.service.gwt.ContactGroupContactViewModel;
import org.celllife.mobilisr.service.gwt.ContactService;
import org.celllife.mobilisr.service.gwt.CsvDataReport;

import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import com.extjs.gxt.ui.client.data.PagingLoadResult;

public class ContactServiceImpl extends AbstractMobilisrService implements
		ContactService {

	private static final long serialVersionUID = 6405731819687343491L;
	private ContactsService service;

	@Override
	public void init() throws ServletException {
		super.init();
		service = (ContactsService) getBean("crudContactsService");
	}
	
	@Override
	public Contact saveOrUpdateContact(Organization organization,
			ContactContactGroupViewModel contactModel) throws MobilisrException {
		return service.saveOrUpdateContact(organization, contactModel);
	}

	@Override
	public PagingLoadResult<Contact> listAllContactsForOrganization(
			Organization organization, PagingLoadConfig loadConfig)
			throws MobilisrException {
		return service.listAllContactsForOrganization(organization,  loadConfig);
	}

	@Override
	public PagingLoadResult<ContactGroup> listAllGroupsForContact(
			Organization organization, Contact contact,
			PagingLoadConfig loadConfig) throws MobilisrException {
		return service.listAllGroupsForContact(organization, contact, loadConfig);
	}

	@Override
	public ContactGroup saveOrUpdateContactGroup(Organization organization,
			ContactGroupContactViewModel contactModel) throws MobilisrException {
		return service.saveOrUpdateContactGroup(organization, contactModel);
	}
	
	@Override
	public PagingLoadResult<ContactGroup> listAllGroupsForOrganization(
			Organization organization, PagingLoadConfig pagingLoadConfig)
			throws MobilisrException {
		return service.listAllGroupsForOrganization(organization, pagingLoadConfig);
	}

	@Override
	public PagingLoadResult<Contact> listAllContactsForGroup(
			ContactGroup contactGroup, PagingLoadConfig loadConfig) throws MobilisrException {
		return service.listAllContactsForGroup(contactGroup, loadConfig);
	}

	@Override
	public PagingLoadResult<CampaignContact> listAllCampaignContactsForCampaign(
			Campaign campaign, PagingLoadConfig pagingLoadConfig,
			boolean includeComplete, boolean includeRemoved) throws MobilisrException {
		return service.listAllCampaignContactsForCampaign(campaign,
				pagingLoadConfig, includeComplete, includeRemoved);
	}

	@Override
	public List<List<String>> readCSVFixedRecordLength(String filePath,
			int maxRead) throws MobilisrException {
		return service.readCSVFixedRecordLength(filePath, maxRead);
	}

	@Override
	public Long saveCSVContacts(List<String> fieldOrder, String filePath,
			Organization organization, List<ContactGroup> groupList)
			throws MobilisrException {
		return service.saveCSVContacts(fieldOrder, filePath, organization, groupList);
	}

	@Override
	public CsvDataReport getNumOfRecordsStoredForCsvImport(String filePath,
			Long jobId) throws MobilisrException {
		return service.getNumOfRecordsStoredForCsvImport(filePath, jobId);
	}
	
	@Override
	public boolean deleteContactGroup(ContactGroup contactGroup) throws MobilisrException, MobilisrRuntimeException {
		return service.deleteContactGroup(contactGroup);
	}
	
	@Override
	public boolean checkMsisdnExists(Organization organization, String msisdn)
			throws MobilisrException, MobilisrRuntimeException {
		return service.checkMsisdnExists(organization, msisdn);
	}

    @Override
    public List<Long> listAllCampaignsForContact(String msisdn) {
        return service.listAllCampaignsForContact(msisdn);
    }
}
