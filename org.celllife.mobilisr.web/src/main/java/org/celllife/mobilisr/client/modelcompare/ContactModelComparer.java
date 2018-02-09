package org.celllife.mobilisr.client.modelcompare;

import org.celllife.mobilisr.domain.Contact;

import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.data.ModelComparer;

public class ContactModelComparer implements ModelComparer<BeanModel> {

	@Override
	public boolean equals(BeanModel m1, BeanModel m2) {
		Contact cg1 = (Contact) m1.getBean();
		Contact cg2 = (Contact) m2.getBean();
		return m1 == m2 || (cg1 != null && cg1.getMsisdn().equals(cg2.getMsisdn()));
	}

}
