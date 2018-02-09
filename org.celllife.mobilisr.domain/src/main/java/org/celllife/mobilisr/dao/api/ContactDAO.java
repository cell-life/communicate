package org.celllife.mobilisr.dao.api;

import java.util.List;

import org.celllife.mobilisr.domain.CampaignContact;
import org.celllife.mobilisr.domain.Contact;
import org.celllife.mobilisr.domain.ContactGroup;
import org.celllife.mobilisr.domain.Organization;
import org.celllife.mobilisr.utilbean.ContactExportSummary;

public interface ContactDAO extends BaseDAO<Contact, Long> {

	/**
	 * The following method searches a Contact for a given Organization and MSISDN
	 * @param organization				the organization
	 * @param msisdn					Mobile number of the person to search for
	 * @return							Contact for a given organization or null if not found
	 */
	public abstract Contact searchByOrganizationAndMSISDN(Organization organization, String msisdn);

	List<Contact> batchSaveContact(Organization organization, List<Contact> contactList, List<ContactGroup> contactGroupList);

	/**
	 * Count contacts in groups.
	 * @param addedChildList
	 * 
	 * @return
	 */
	public abstract Long countContactsInGroups(List<ContactGroup> addedChildList);

	/**
	 * Retrieves the Contacts (in batches) for an Organisation formatted for export purposes
	 * @param organization Organisation for which the contact must be listed
	 * @param batch Integer number of the current batch (e.g. 0 is first batch, 1 is the second)
	 * @param batchSize Integer maximum number of Contacts returned in the batch
	 * @return List of ContactExportSummary
	 */
	public List<ContactExportSummary> getExportContactsForOrganization(Organization organization, Integer batch, Integer batchSize);

	public void markContactsAsInvalid(String msisdn);

    public List<ContactExportSummary> getExportContactsForGroup(ContactGroup contactGroup, Integer batch, Integer batchSize);

    /**
     * Returns a list of all campaign contacts for a certain MSISDN.
     * @param msisdn
     * @return List of campaign contacts.
     */
    public List<Long> findCampaignsForContact(String msisdn);
	
}
