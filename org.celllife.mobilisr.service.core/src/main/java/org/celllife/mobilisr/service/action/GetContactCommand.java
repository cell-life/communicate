package org.celllife.mobilisr.service.action;

import org.apache.commons.chain.Context;
import org.celllife.mobilisr.domain.Contact;
import org.celllife.mobilisr.domain.SmsLog;
import org.celllife.mobilisr.service.filter.Action;

import com.trg.search.Search;

public abstract class GetContactCommand extends BaseAction implements Action {

	protected static final String CONTACT = "contact";
	protected static final Object IS_NEW_CONTACT = "isNewContact";
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean execute(Context context) throws Exception {
		SmsLog smsLog = (SmsLog) context.get(SMS_LOG);
		
		Search search = new Search(Contact.class);
		search.addFilterEqual(Contact.PROP_MSISDN, smsLog.getMsisdn());
		search.addFilterEqual(Contact.PROP_ORGANIZATION, smsLog.getOrganization());
		Contact contact = (Contact) getDao().searchUnique(search);
		context.put(IS_NEW_CONTACT, contact == null);
		if (contact == null){
			contact = new Contact(smsLog.getMsisdn(), smsLog.getOrganization());
			getDao().save(contact);
		}
		
		context.put(CONTACT, contact);
		return CONTINUE_PROCESSING;
	}
	
}
