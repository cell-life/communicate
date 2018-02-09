package org.celllife.mobilisr.dao.api;

import org.hibernate.Session;

import com.trg.dao.hibernate.GeneralDAO;

public interface MobilisrGeneralDAO extends GeneralDAO {

	void saveOrUpdate(Object entityObject);
	
	/**
	 * Should ONLY be used in unit tests.
	 * @return
	 */
	Session getSession();
}
