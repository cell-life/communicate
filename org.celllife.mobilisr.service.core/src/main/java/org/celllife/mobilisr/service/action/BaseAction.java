package org.celllife.mobilisr.service.action;

import org.celllife.mobilisr.dao.api.MobilisrGeneralDAO;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class BaseAction {

	@Autowired
	private MobilisrGeneralDAO generalDao;
	
	public MobilisrGeneralDAO getDao(){
		return generalDao;
	}
	
	// setter for unit test
	void setDao(MobilisrGeneralDAO generalDao) {
		this.generalDao = generalDao;
	}
}
