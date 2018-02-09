package org.celllife.mobilisr.client.modelcompare;

import org.celllife.mobilisr.domain.ContactGroup;

import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.data.ModelComparer;

public class ContactGroupModelComparer implements ModelComparer<BeanModel> {

	@Override
	public boolean equals(BeanModel m1, BeanModel m2) {
		ContactGroup cg1 = (ContactGroup) m1.getBean();
		ContactGroup cg2 = (ContactGroup) m2.getBean();
		return m1 == m2 || (cg1 != null && cg1.getGroupName().equals(cg2.getGroupName()));
	}

}
