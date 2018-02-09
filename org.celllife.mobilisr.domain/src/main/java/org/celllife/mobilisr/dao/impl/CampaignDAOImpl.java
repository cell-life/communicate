package org.celllife.mobilisr.dao.impl;

import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import com.trg.search.Search;
import org.celllife.mobilisr.annotation.LogLevel;
import org.celllife.mobilisr.annotation.Loggable;
import org.celllife.mobilisr.constants.CampaignStatus;
import org.celllife.mobilisr.constants.CampaignType;
import org.celllife.mobilisr.dao.api.CampaignDAO;
import org.celllife.mobilisr.domain.*;
import org.celllife.mobilisr.util.LogUtil;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.criterion.Restrictions;
import org.hibernate.transform.Transformers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Repository("campaignDAO")
public class CampaignDAOImpl extends BaseDAOImpl<Campaign, Long> implements CampaignDAO {

	private static final Logger log = LoggerFactory.getLogger(CampaignDAOImpl.class);

	@Loggable(LogLevel.TRACE)
	@SuppressWarnings("unchecked")
	@Override
	@Transactional(readOnly=true)
	public List<ContactMsgTime> getMsgTimesForCampaignFromContacts(Campaign campaign) {

		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append("from ContactMsgTime where ")
				.append(ContactMsgTime.PROP_CAMPAIGN)
				.append(" =:campaign group by ")
				.append(ContactMsgTime.PROP_MSG_TIME).append(", ")
				.append(ContactMsgTime.PROP_MSG_SLOT)
				.append(" order by ")
				.append(ContactMsgTime.PROP_MSG_SLOT).append(" asc");

		String hql = stringBuffer.toString();
		Query query = getSession().createQuery(hql);
		query.setParameter("campaign", campaign);
		return query.list();
	}

	@Loggable(LogLevel.TRACE)
	@Override
	@SuppressWarnings("unchecked")
	@Transactional(readOnly=true)
	public List<CampaignContact> getContactsToProcessForDailyCampaign(Campaign campaign, Date msgTime, int msgSlot, Date currentDate) {

		Criteria criteria = getSession().createCriteria(CampaignContact.class);

		if(campaign.getEndDate() != null){
			criteria.add(Restrictions.le(CampaignContact.PROP_JOINING_DATE, campaign.getEndDate()));
		}

		if (currentDate != null){
			// current date parameter used to allow testing on different days
			criteria.add(Restrictions.lt(CampaignContact.PROP_JOINING_DATE, currentDate));
		}

		List<CampaignContact> campContactList = criteria.add(Restrictions.le(CampaignContact.PROP_PROGRESS, campaign.getDuration()))
        		.add(Restrictions.eq(CampaignContact.PROP_CAMPAIGN, campaign))
        		.add(Restrictions.gt(CampaignContact.PROP_PROGRESS, 0))
        		.add(Restrictions.eq(CampaignContact.PROP_INVALID, false))
        		.add(Restrictions.or(Restrictions.isNull(CampaignContact.PROP_END_DATE),
					Restrictions.ge(CampaignContact.PROP_END_DATE, new Date())))
				.createCriteria(CampaignContact.PROP_CONTACT_MSG_TIMES)
				.add(Restrictions.eq(ContactMsgTime.PROP_MSG_SLOT, msgSlot))
				.add(Restrictions.eq(ContactMsgTime.PROP_MSG_TIME, msgTime)).list();

		return campContactList;
	}

	@Loggable(LogLevel.TRACE)
	@Override
	@Transactional(readOnly=true)
	public int countNumberOfContactsForCampaign(Campaign campaign, boolean includeInvalid) {

		int totalCount = 0;
		Search search = new Search(CampaignContact.class);
		search.addFilterEqual(CampaignContact.PROP_CAMPAIGN, campaign);
		search.addFilterNull(CampaignContact.PROP_END_DATE);
		if (!includeInvalid){
			search.addFilterEqual(CampaignContact.PROP_INVALID, false);
		}
		totalCount = _count(search);

		return totalCount;
	}

	@Loggable(LogLevel.TRACE)
	@Override
	@Transactional(readOnly=true)
	public List<CampaignContact> getContactsInPaginationForCampaign( Campaign campaign, PagingLoadConfig pagingLoadConfig) {

        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("select cc from CampaignContact cc, Contact c ")
                .append("where cc.campaign.id =:campaign_id ")
                .append("and cc.endDate is null ")
                .append("and cc.invalid=false ")
                .append("and c.invalid=false ")
                .append("and cc.contact.id=c.id");

        String hql = stringBuffer.toString();
        Query query = getSession().createQuery(hql);
        query.setParameter("campaign_id", campaign.getId());
        query.setFirstResult(pagingLoadConfig.getOffset());
        query.setMaxResults(pagingLoadConfig.getLimit());
        return (List<CampaignContact>) query.list();
	}

	@Loggable(LogLevel.TRACE)
	@Override
	@Transactional(readOnly=true)
	public String getCampMsgForCampaign(Campaign campaign) {

		Search search = new Search(CampaignMessage.class);
		search.addFilterEqual(CampaignMessage.PROP_CAMPAIGN, campaign);
		@SuppressWarnings("unchecked")
		List<CampaignMessage> messages = _search(search);
		if (messages.isEmpty()){
			return null;
		}
		return messages.get(0).getMessage();
	}

	@Loggable(LogLevel.TRACE)
	@Override
	@Transactional(readOnly=true)
	public CampaignMessage getCampMsgForDailyCampaign(Campaign campaign, int msgSlot, int msgDay) {

		Criteria criteria = getSession().createCriteria(CampaignMessage.class);
		CampaignMessage campaignMessage = (CampaignMessage) criteria
				.add(Restrictions.eq(CampaignMessage.PROP_MSG_SLOT, msgSlot))
				.add(Restrictions.eq(CampaignMessage.PROP_CAMPAIGN, campaign))
				.add(Restrictions.eq(CampaignMessage.PROP_MSG_DAY, msgDay))
				.uniqueResult();

		return campaignMessage;
	}

	@Loggable(LogLevel.TRACE)
	@SuppressWarnings("unchecked")
	@Override
	@Transactional(readOnly=true)
	public List<CampaignMessage> getCampMsgForFlexiCampaign(Campaign campaign, Date msgTime) {

		Search search = new Search(CampaignMessage.class);
		search.addFilterEqual(CampaignMessage.PROP_CAMPAIGN, campaign);
		search.addFilterEqual(CampaignMessage.PROP_MSG_TIME, msgTime);
		List<CampaignMessage> campaignMessages = _search(search);
		return campaignMessages;
	}

	@Loggable(LogLevel.TRACE)
	@SuppressWarnings("unchecked")
	@Transactional(readOnly=true)
	public List<CampaignMessage> getAllCampMessages(Campaign campaign) {
		Search search = new Search(CampaignMessage.class);
		search.addFilterEqual(CampaignMessage.PROP_CAMPAIGN, campaign);
		search.addSortAsc(CampaignMessage.PROP_MSG_DAY);
		search.addSortAsc(CampaignMessage.PROP_MSG_TIME);
		List<CampaignMessage> campaignMessages = _search(search);
		return campaignMessages;
	}

	@Loggable(LogLevel.TRACE)
	@Override
	@SuppressWarnings("unchecked")
	@Transactional(readOnly=true)
	public List<CampaignContact> getContactsToProcessForFlexiCampaign(Campaign campaign, int progress, Date currentDate) {

		// TODO:add tests with end date for getContactsToProcessForFlexiCampaign
		
		if (progress > campaign.getDuration()) {
			log.error(LogUtil.getMarker_notifyAdmin(),
					"Attempting to fetch contacts with progress greater "
					+ "than campaign duration. [progress={}] " +
							"[campaignDuration={}] [campaignId={}]",
					new Object[] {progress, campaign.getDuration(), campaign.getId()});
			return Collections.EMPTY_LIST;
		}

		Criteria criteria = getSession().createCriteria(CampaignContact.class);
		Date endDate = campaign.getEndDate();

		criteria.add(Restrictions.eq(CampaignContact.PROP_CAMPAIGN, campaign))
				.add(Restrictions.eq(CampaignContact.PROP_PROGRESS, progress))				
				.add(Restrictions.eq(CampaignContact.PROP_INVALID, false))				
				.add(Restrictions.isNull(CampaignContact.PROP_END_DATE))
				.add(Restrictions.or(Restrictions.isNull(CampaignContact.PROP_END_DATE), 
						Restrictions.ge(CampaignContact.PROP_END_DATE, new Date())))
                .add(Restrictions.lt(CampaignContact.PROP_JOINING_DATE, currentDate))
				.list();

		if (endDate != null) {
			criteria.add(Restrictions.le(CampaignContact.PROP_JOINING_DATE, endDate));
		}

		List<CampaignContact> campContactList = criteria.list();		
		return campContactList;
	}

	@Loggable(LogLevel.TRACE)
	@Override
	@Transactional
	public void updateCampaignContactsProgress(Campaign campaign) {

		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append("update CampaignContact set ")
				.append(CampaignContact.PROP_PROGRESS)
				.append(" = ")
				.append(CampaignContact.PROP_PROGRESS)
				.append(" + 1 where ")
				.append(CampaignContact.PROP_CAMPAIGN)
				.append(" =:campaign and ")
				.append(CampaignContact.PROP_PROGRESS)
				.append(" <=:campDuration ");

		if(campaign.getEndDate() != null){
			stringBuffer.append("and " + CampaignContact.PROP_JOINING_DATE)
						.append(" <=:campEndDate");
		}

		String hql = stringBuffer.toString();

		Query query = getSession().createQuery(hql);
		query.setParameter("campaign", campaign);
		query.setParameter("campDuration", campaign.getDuration());
		if(campaign.getEndDate() != null){
			query.setDate("campEndDate", campaign.getEndDate());
		}
		query.executeUpdate();
	}

	@Loggable(LogLevel.TRACE)
	@SuppressWarnings("unchecked")
	@Override
	@Transactional(readOnly=true)
	public boolean isAllContactsProcessedForCampaign(Campaign campaign) {
		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append("select count(*) from CampaignContact where ")
				.append(CampaignContact.PROP_PROGRESS)
				.append(" <= :campDuration and ")
				.append(CampaignContact.PROP_END_DATE)
				.append(" is null and ");

		Date endDate = campaign.getEndDate();
		if (endDate != null){
			stringBuffer.append(CampaignContact.PROP_JOINING_DATE)
				.append(" <= :campEndDate and ");
		}

		stringBuffer.append(CampaignContact.PROP_CAMPAIGN)
				.append(" = :campaign");

		String hql = stringBuffer.toString();
		Query query = getSession().createQuery(hql);
		query.setParameter("campDuration", campaign.getDuration());
		if (endDate != null){
			query.setDate("campEndDate", endDate);
		}
		query.setParameter("campaign", campaign);
		List<Long> results = query.list();
		Long count = (Long) results.get(0);

		return count == 0L;
	}

	@Loggable(LogLevel.TRACE)
	@Override
	@Transactional
	public void setAllCampaignContactsEndDates(Campaign campaign){
		SQLQuery query = getSession().createSQLQuery(
				"UPDATE campaigncontact SET endDate=CURDATE() where campaign_id=:campaign");
		query.setParameter("campaign", campaign);
		query.executeUpdate();
	}

	@Loggable(LogLevel.TRACE)
	@Override
	@Transactional
	public void addAllContactsToCampaign(Campaign campaign, Boolean startOver) {
		StringBuilder sb = new StringBuilder();
		sb.append("insert into campaigncontact (msisdn, mobilenetwork, campaign_id, progress, contact_id, joinedDate) ")
			.append("select distinct c.msisdn, c.mobilenetwork , :campaignID, 0, c.id, :joinedDate from contact c ")
			.append("where c.organization_id=:orgID ")
			.append(" on duplicate key update msisdn=values(msisdn), endDate=:nullDate");
		
		if (startOver) {
			sb.append(" ,progress=0");
		}
		
		SQLQuery query = getSession().createSQLQuery(sb.toString());
		query.setLong("campaignID", campaign.getId());
		query.setLong("orgID", campaign.getOrganization().getId());
		query.setDate("joinedDate", new Date());
		query.setDate("nullDate", null);
		query.executeUpdate();
	}

	@Loggable(LogLevel.TRACE)
	@Override
	@Transactional
	public void removeMessagesFromCampaign(Campaign campaign,  List<CampaignMessage> deletedMessages) {

		List<Long> groupIds = new ArrayList<Long>();
		if (deletedMessages != null && !deletedMessages.isEmpty()) {
			for (CampaignMessage message : deletedMessages) {
				groupIds.add(message.getId());
			}
		}

		String queryString = "delete CampaignMessage where " + CampaignMessage.PROP_CAMPAIGN + " = :campaign";

		if (!groupIds.isEmpty()) {
			queryString += " and " + CampaignMessage.PROP_ID + " in (:deletedMessages)";
		}

		Query query = getSession().createQuery(queryString);
		query.setParameter("campaign", campaign);
		if (!groupIds.isEmpty()) {
			query.setParameterList("deletedMessages", groupIds);
		}

		query.executeUpdate();
	}

	@Loggable(LogLevel.TRACE)
	@Override
	@Transactional
	public void addContactsToCampaign(Campaign campaign, Organization organization, List<Contact> addedContactList, Boolean startOver) {
		if (addedContactList.size() > 0) {
			List<String> msisdnList = new ArrayList<String>();
			for (Contact contact : addedContactList) {
				msisdnList.add(contact.getMsisdn());
			}

			String queryString = "insert into campaigncontact (msisdn, mobilenetwork, campaign_id, progress, contact_id, joinedDate) "
									+ "select distinct c.msisdn, c.mobilenetwork , camp.id, 0, c.id, :joinedDate from contact c, campaign camp "
									+ "where camp.id=:campaignID and c.organization_id=:orgID and c.msisdn in (:msisdnList) "
									+ "ON duplicate key UPDATE msisdn=values(msisdn), endDate=:nullDate";
		
			if (startOver) {
				queryString.concat(" ,progress=0");
			}

			SQLQuery query = getSession().createSQLQuery(queryString);
			query.setParameter("campaignID", campaign.getId());
			query.setParameter("orgID", organization.getId());
			query.setParameter("joinedDate", new Date());
			query.setParameterList("msisdnList", msisdnList);
			query.setDate("nullDate", null);

			query.executeUpdate();
		}
	}

	@Loggable(LogLevel.TRACE)
	@Override
	@Transactional
	public void addContactGroupsToCampaignById(Campaign campaign, List<Long> groupIdList, Boolean startOver) {
		if (groupIdList == null || groupIdList.isEmpty())
			return;
		
		String queryString = "INSERT INTO campaigncontact (msisdn, mobilenetwork, campaign_id, progress, " +
				"contact_id, joinedDate) " +
			"SELECT c.msisdn, c.mobilenetwork , :campaignID, 0, c.id, :joinedDate " +
			"FROM contact c, contact_contactgroup cg " +
			"WHERE cg.contact_id=c.id " +
				"AND cg.contactGroup_id in (:groupIdList) " +
			"ON duplicate key UPDATE msisdn=values(msisdn), endDate=:nullDate";
		
		if (startOver) {
			queryString.concat(" ,progress=0");
		}

		SQLQuery query = getSession().createSQLQuery(queryString);
		query.setLong("campaignID", campaign.getId());
		query.setDate("joinedDate", new Date());
		query.setParameterList("groupIdList", groupIdList);
		query.setDate("nullDate", null);
		query.executeUpdate();
	
	}	
	
	@Loggable(LogLevel.TRACE)
	@Override
	@Transactional
	public void createDefaultMessageTimesForContacts(Campaign campaign, List<ContactMsgTime> defaultTimes){
		if (defaultTimes == null || defaultTimes.isEmpty())
			return;
		
		SQLQuery select = getSession().createSQLQuery("SELECT id FROM campaigncontact cc " +
				"WHERE cc.campaign_id = :campaignId " +
				"AND cc.id NOT IN (SELECT campcontact_id FROM contactmsgtime WHERE campaign_id = :campaignId)");
		select.setLong("campaignId", campaign.getId());
		
		String queryString = "INSERT INTO contactmsgtime (msgSlot, msgTime, campaign_id, campcontact_id) " +
				"VALUES (:msgSlot,:msgTime,:campaignId,:campaignContactId)";

		@SuppressWarnings("unchecked")
		List<Number> list = select.list();
		
		
		for (Number number : list) {
			for (ContactMsgTime time : defaultTimes) {
				SQLQuery insert = getSession().createSQLQuery(queryString);
				insert.setTime("msgTime", time.getMsgTime());
				insert.setInteger("msgSlot", time.getMsgSlot());
				insert.setParameter("campaignContactId", number);
				insert.setLong("campaignId", campaign.getId());
				insert.executeUpdate();
			}
		}
	}

	@Loggable(LogLevel.TRACE)
	@Override
	@Transactional
	public void setEndDateForCampaignContactsByGroup(Campaign campaign, List<Long> groupIdList) {				
		SQLQuery query = getSession().createSQLQuery(
				"UPDATE campaigncontact cc, contact_contactgroup cg, contact c SET cc.endDate=CURDATE() " +
				"WHERE cc.campaign_id=:campaignID " + 
				"AND cc.contact_id = c.id " + 
				"AND c.id = cg.contact_id " + 
				"AND cg.contactGroup_id in (:groupIdList)" 
				);  
		query.setLong("campaignID", campaign.getId());
		query.setParameterList("groupIdList", groupIdList);
		query.executeUpdate();		
	}

	@Loggable(LogLevel.TRACE)
	@SuppressWarnings("unchecked")
	@Override
	@Transactional(readOnly=true)
	public List<CampaignMessage> findDefaultTimesForRelativeCampaign(Campaign campaign){
		
		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append("from CampaignMessage where ")
				.append(CampaignMessage.PROP_CAMPAIGN)
				.append(" = :campaign ")
				.append(" group by ").append(CampaignMessage.PROP_MSG_TIME)
				.append(" order by ").append(CampaignMessage.PROP_MSG_TIME)
				.append(" asc");

		String hql = stringBuffer.toString();
		Query query = getSession().createQuery(hql);
		query.setParameter("campaign", campaign);
		return query.list();
	}

	@Loggable(LogLevel.TRACE)
	@Override
	@Transactional
	public void setCampaignContactsEndDate(Campaign campaign, List<? extends Messagable> contacts) {		
				
		if (contacts.isEmpty())
			return;
		
		List<String> msisdnlist = new ArrayList<String>();		
		for (Messagable contact : contacts) {
			msisdnlist.add(contact.getMsisdn());
		}
		
		String sqlString = "UPDATE CampaignContact cc SET cc.endDate=CURDATE() WHERE cc.msisdn in (:msisdnlist)" +
				" and cc.campaign = :campaign";
		
		Query query = getSession().createQuery(sqlString);
		query.setParameterList("msisdnlist", msisdnlist);
		query.setParameter("campaign", campaign);
		query.executeUpdate();

	}

    @Loggable(LogLevel.TRACE)
    @Override
    @Transactional
    public void setCampaignContactsDateLastMessage(Campaign campaign, List<? extends Messagable> contacts) {

        if (contacts.isEmpty())
            return;

        List<String> msisdnlist = new ArrayList<String>();
        for (Messagable contact : contacts) {
            msisdnlist.add(contact.getMsisdn());
        }

        String sqlString = "UPDATE CampaignContact cc SET cc.dateLastMessage=CURDATE() WHERE cc.msisdn in (:msisdnlist)" +
                " and cc.campaign = :campaign";

        Query query = getSession().createQuery(sqlString);
        query.setParameterList("msisdnlist", msisdnlist);
        query.setParameter("campaign", campaign);
        query.executeUpdate();

    }

    @Loggable(LogLevel.TRACE)
    @Override
    @Transactional
    public void setCampaignContactsDateLastMessageByMsisdn(Campaign campaign, List<String> msisdnList, Date lastDate) {

        String queryString = "UPDATE campaigncontact " +
                "SET dateLastMessage =:lastDate " +
                "where campaign_id =:campaign " +
                "and msisdn in (:msisdnList)";

        Query query = getSession().createSQLQuery(queryString);
        query.setParameter("campaign", campaign.getId());
        query.setParameterList("msisdnList", msisdnList);
        query.setParameter("lastDate", lastDate);
		query.executeUpdate();

	}
	
	@Loggable(LogLevel.TRACE)
	@Override
	@Transactional
	public int updateCampaignContactMsisdn(long contactId, String newMsisdn) {
		String queryString = "update campaigncontact cc set cc.msisdn = :newMsisdn, " +
				"cc.invalid = false " +
				"where cc.contact_id = :contactId";

		Query query = getSession().createSQLQuery(queryString);
		query.setString("newMsisdn", newMsisdn);
		query.setParameter("contactId", contactId);
		return query.executeUpdate();
	}

	@SuppressWarnings("unchecked")
	@Override
	@Transactional(readOnly=true)
	@Loggable(LogLevel.TRACE)
	public List<CampaignContact> getCampaignContactsNeedingWelcomeMessage(Campaign campaign) {
		Search search = new Search(CampaignContact.class);
		search.addFilterEqual(CampaignContact.PROP_CAMPAIGN, campaign);
		search.addFilterEqual(CampaignContact.PROP_RECEIVED_WELCOME, false);
		search.addFilterEqual(CampaignContact.PROP_INVALID, false);
		return _search(search);
	}

	@Loggable(LogLevel.TRACE)
	@Override
	@Transactional
	public void markCampaignContactsAsReceivedWelcomeMessage(List<CampaignContact> contacts) {
		if (contacts.isEmpty()){
			return;
		}
		List<Long> idlist = new ArrayList<Long>();
		for (CampaignContact contact : contacts) {
			idlist.add(contact.getId());
		}

		String queryString = "update CampaignContact cc set " + CampaignContact.PROP_RECEIVED_WELCOME
			+ " = true where id in (:idlist)";

		Query query = getSession().createQuery(queryString);
		query.setParameterList("idlist", idlist);
		query.executeUpdate();
	}

	@Loggable(LogLevel.TRACE)
	@Override
	@Transactional
	public void setCampaignContactsEndDateByMsisdn(Campaign campaign, List<String> msisdnList) {
		
		String queryString = "UPDATE campaigncontact " + 
			"SET endDate=CURDATE() " +	
			"where campaign_id =:campaign " +
			"and msisdn in (:msisdnList)";
		
		Query query = getSession().createSQLQuery(queryString);
		query.setParameter("campaign", campaign.getId());
		query.setParameterList("msisdnList", msisdnList);
		query.executeUpdate();
	}
	
	@Loggable(LogLevel.TRACE)
	@Override
	@Transactional(readOnly=true)
	public CampaignContact getCampaignContact(Campaign campaign, Contact contact) {
		Search search = new Search(CampaignContact.class);
		search.addFilterEqual(CampaignContact.PROP_CONTACT, contact);
		search.addFilterEqual(CampaignContact.PROP_CAMPAIGN, campaign);
		search.addFetch(CampaignContact.PROP_CONTACT);
		CampaignContact campContact = (CampaignContact) _searchUnique(search);
		return campContact;
	}
	
	@Loggable(LogLevel.TRACE)
	@Override
	@Transactional(readOnly=true)
	public CampaignContact getCampaignContact(Campaign campaign, String msisdn) {
		Search search = new Search(CampaignContact.class);
		search.addFilterEqual(CampaignContact.PROP_MSISDN, msisdn);
		search.addFilterEqual(CampaignContact.PROP_CAMPAIGN, campaign);
		search.addFetch(CampaignContact.PROP_CONTACT);
		CampaignContact campContact = (CampaignContact) _searchUnique(search);
		return campContact;
	}

	@Loggable(LogLevel.TRACE)
	@Override
	@Transactional(readOnly=true)
	public List<Campaign> getRunningRelativeCampaignsWithEndDate() {
		Search search = new Search();
		search.addFilterNotEqual(Campaign.PROP_TYPE, CampaignType.FIXED);
		search.addFilterNotNull(Campaign.PROP_END_DATE);
		search.addFilterNotEqual(Campaign.PROP_STATUS, CampaignStatus.FINISHED);
		List<Campaign> campaigns = search(search);
		return campaigns;
	}

	@Loggable(LogLevel.TRACE)
	@Override
	@Transactional(readOnly=true)
	public CampaignStatus getCampaignStatus(Long id) {
		Search search = new Search();
		search.addFilterEqual(Campaign.PROP_ID, id);
		search.addField(Campaign.PROP_STATUS);
		CampaignStatus status = searchUnique(search);
		return status;
	}

	@Loggable(LogLevel.TRACE)
	@Override
	@Transactional(readOnly=true)
	public List<CampaignMessage> getCampaignMessageLengthsAndDay(Long id) {
		if (id == null){
			return new ArrayList<CampaignMessage>();
		}
		StringBuilder sb = new StringBuilder();
		sb.append("select length(m.").append(CampaignMessage.PROP_MESSAGE).append(") as msgLength")
				.append(", m.").append(CampaignMessage.PROP_MSG_DAY).append(" as ").append(CampaignMessage.PROP_MSG_DAY)
				.append(" from CampaignMessage as m where m.")
				.append(CampaignMessage.PROP_CAMPAIGN)
				.append(".id = :id");

		Query query = getSession().createQuery(sb.toString());
		query.setResultTransformer(Transformers.aliasToBean(CampaignMessage.class));
		query.setLong("id", id);

		@SuppressWarnings("unchecked")
		List<CampaignMessage> lengths = query.list();
		return lengths;
	}

	@Loggable(LogLevel.TRACE)
	@Override
	@Transactional(readOnly=true)
	public Campaign getCampaign(String campaignName){
		Search search = new Search();
		search.addFilterEqual(Campaign.PROP_NAME, campaignName);
		Campaign campaign = searchUnique(search);
		return campaign;
	}

	@Loggable(LogLevel.TRACE)
	@Override
	@Transactional(readOnly=false)
	public void updateCampaignStatus(Long campaignId, CampaignStatus newStatus) {
		Query query = getSession().createQuery("update Campaign set status = :status where id = :id");
		query.setParameter("status", newStatus);
		query.setParameter("id", campaignId);
		query.executeUpdate();
	}

    @Loggable(LogLevel.TRACE)
    @Override
    @Transactional(readOnly=false)
    public void setLinkedCampaignId(Long campaignId, Long linkedCampaignId) {
        Query query = getSession().createQuery("update Campaign set linkedCampaignId = :linkedCampaignId where id = :id");
        query.setParameter("linkedCampaignId", linkedCampaignId);
        query.setParameter("id", campaignId);
        query.executeUpdate();
    }

    @Loggable(LogLevel.TRACE)
    @Override
    @Transactional
    public void setCampaignContactsEndDateByMsisdn(Campaign campaign, List<String> msisdnList, Date endDate) {

        String queryString = "UPDATE campaigncontact " +
                "SET endDate =:endDate " +
                "where campaign_id =:campaign " +
                "and msisdn in (:msisdnList)";

        Query query = getSession().createSQLQuery(queryString);
        query.setParameter("campaign", campaign.getId());
        query.setParameterList("msisdnList", msisdnList);
        query.setParameter("endDate", endDate);
        query.executeUpdate();
    }

    @Loggable(LogLevel.TRACE)
    @Override
    @Transactional
    public void setContactCount(Campaign campaign, int contactCount) {
        SQLQuery query = getSession().createSQLQuery(
                "UPDATE campaign SET contactCount =:contactCount where id =:campaignId");
        query.setParameter("campaignId", campaign.getId());
        query.setParameter("contactCount", contactCount);
        query.executeUpdate();
    }



}
