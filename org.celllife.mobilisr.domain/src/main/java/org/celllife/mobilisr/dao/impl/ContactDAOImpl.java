package org.celllife.mobilisr.dao.impl;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.celllife.mobilisr.annotation.LogLevel;
import org.celllife.mobilisr.annotation.Loggable;
import org.celllife.mobilisr.dao.api.ContactDAO;
import org.celllife.mobilisr.dao.api.ContactGroupDAO;
import org.celllife.mobilisr.domain.Contact;
import org.celllife.mobilisr.domain.ContactGroup;
import org.celllife.mobilisr.domain.Organization;
import org.celllife.mobilisr.utilbean.ContactExportSummary;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.trg.search.Filter;
import com.trg.search.Search;

/**
 * Default implementation for the ContactDAO interface
 * @author Vikram Bindal (e-mail: vikram@cell-life.org)
 */
@Repository("contactDAO")
public class ContactDAOImpl extends BaseDAOImpl<Contact, Long> implements ContactDAO {

	@Autowired
	private ContactGroupDAO contactGroupDAO;
	
	@Loggable(LogLevel.TRACE)
	@Override
	@Transactional(readOnly=true)
	public Contact searchByOrganizationAndMSISDN(Organization organization, String msisdn) {
		Search search = new Search(Contact.class);
		search.addFilterAnd(Filter.equal(Contact.PROP_MSISDN, msisdn), 
				Filter.equal(Contact.PROP_ORGANIZATION, organization));
		
		Contact contact = searchUnique(search);
		return contact;
	}

	@Loggable(LogLevel.TRACE)
	@Override
	@Transactional
	public List<Contact> batchSaveContact(Organization organization, List<Contact> contactList, List<ContactGroup> contactGroupList) {
		
		Long insertedCount = 0L;
	    List<Contact> contactListForGroupAdd = new ArrayList<Contact>();
	    for (int i = 0; i < contactList.size(); ++i) {
	    	
	    	Contact tempContact = saveContactForOrganization(organization, contactList.get(i));
        	contactListForGroupAdd.add(tempContact);
	        
	        if (++insertedCount % 50 == 0) {
	        	flushAndClear();
	        }
	    }   	    	       
	    flushAndClear();
	    
	    for (ContactGroup contactGroup : contactGroupList) {
			contactGroupDAO.addContactsToGroup(contactListForGroupAdd, contactGroup);
		}
	    
	    return contactListForGroupAdd;
	}
	
	Contact saveContactForOrganization(Organization organization, Contact contact){
		Contact tempContact = searchByOrganizationAndMSISDN(organization, contact.getMsisdn());
		if (tempContact != null) {
			tempContact.setFirstName(contact.getFirstName());
			tempContact.setLastName(contact.getLastName());
			tempContact.setMobileNetwork(contact.getMobileNetwork());
			_saveOrUpdate(tempContact);
			return tempContact;
		} else {
			contact.setOrganization(organization);		
			_saveOrUpdate(contact);
		}
		return contact;
	}
	
	private void flushAndClear()  {
	    if (getSession().isDirty()) {
	        getSession().flush();
	        getSession().clear();
	    }
	}

	@Loggable(LogLevel.TRACE)
	@Override
	@Transactional(readOnly=true)
	public Long countContactsInGroups(List<ContactGroup> groupList) {
		if (groupList == null || groupList.isEmpty()){
			return 0l;
		}
		String queryString = "select count(distinct contact) from Contact_ContactGroup ccg where " +
				" ccg.contactGroup in (:groupList)";

		Query query = getSession().createQuery(queryString);
		query.setParameterList("groupList", groupList);
			
		Long count = (Long) query.uniqueResult();
		return count;
	}
	
	@Loggable(LogLevel.TRACE)
	@SuppressWarnings("unchecked")
	@Override
	@Transactional(readOnly=true)
	public List<ContactExportSummary> getExportContactsForOrganization(Organization organization, Integer batch, Integer batchSize) {
		// implemented as an SQL query due to MYSQL specific string concatenation function
		// (instead of performing separate queries to retrieve campaigns for each contact) 
		String queryString = "select con.firstname, con.lastname, con.msisdn, group_concat(c.name ORDER BY c.name DESC SEPARATOR ', ')"
			+ "	from campaign c"
			+ " inner join campaigncontact cc on cc.campaign_id = c.id"
			+ " right join contact con on cc.contact_id = con.id where con.organization_id= :orgId"
			+ " group by con.msisdn order by con.msisdn";
		SQLQuery query = getSession().createSQLQuery(queryString);
		query.setLong("orgId", organization.getId());
		query.setFirstResult(batch*batchSize);
		query.setMaxResults(batchSize);
		
		List<Object[]> results = query.list();
		List<ContactExportSummary> contacts = new ArrayList<ContactExportSummary>();
		for (Object[] result : results) {
			ContactExportSummary contact = new ContactExportSummary((String)result[0], (String)result[1], (String)result[2], (String)result[3]);
			contacts.add(contact);
		}
		
		return contacts;
	}

    @Loggable(LogLevel.TRACE)
    @Override
    @Transactional(readOnly=true)
    public List<ContactExportSummary> getExportContactsForGroup(ContactGroup contactGroup, Integer batch, Integer batchSize) {

        List<ContactExportSummary> contactExportSummaryList = new ArrayList<ContactExportSummary>();

        String queryString = "select c.firstname, c.lastname, c.msisdn " +
                "from contact_contactgroup cg, contact c " +
                "where cg.contactGroup_id= :contactGroupId " +
                "and c.id = cg.contact_id";

        SQLQuery query = getSession().createSQLQuery(queryString);
        query.setLong("contactGroupId", contactGroup.getId());
        query.setFirstResult(batch*batchSize);
        query.setMaxResults(batchSize);
        List<Object[]> results = query.list();

        for (Object[] result : results) {
            ContactExportSummary contactExportSummary = new ContactExportSummary((String)result[0], (String)result[1], (String)result[2], "");
            contactExportSummaryList.add(contactExportSummary);
        }

        return contactExportSummaryList;

    }
	
	@Loggable(LogLevel.TRACE)
	@Override
	@Transactional
	public void markContactsAsInvalid(String msisdn) {
		Query queryC = getSession().createQuery("update Contact set invalid = true where msisdn = :msisdn");
		queryC.setString("msisdn", msisdn);
		queryC.executeUpdate();
		
		Query queryCC = getSession().createQuery("update CampaignContact set invalid = true where msisdn = :msisdn");
		queryCC.setString("msisdn", msisdn);
		queryCC.executeUpdate();
	}

    @Loggable(LogLevel.TRACE)
    @Override
    @Transactional
    public List<Long> findCampaignsForContact(String msisdn) {

        SQLQuery query = getSession().createSQLQuery("select cc.campaign_id FROM campaigncontact cc where cc.msisdn = :msisdn");
        query.setString("msisdn", msisdn);

        List<Long> campaignIds = new ArrayList<Long>();
        List<Object> results = query.list();

        for (Object result : results) {
            campaignIds.add(((BigInteger)result).longValue());
        }
        return campaignIds;
    }
}
