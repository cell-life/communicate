package org.celllife.mobilisr.dao.impl;

import org.celllife.mobilisr.annotation.LogLevel;
import org.celllife.mobilisr.annotation.Loggable;
import org.celllife.mobilisr.dao.api.MobilisrGeneralDAO;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.trg.dao.hibernate.GeneralDAOImpl;

@Repository("mobilisrGeneralDAO")
public class MobilisrGeneralDAOImpl extends GeneralDAOImpl implements MobilisrGeneralDAO{

	@Autowired
	@Override
	@Loggable(LogLevel.TRACE)
	public void setSessionFactory(SessionFactory sessionFactory) {
		super.setSessionFactory(sessionFactory);
	}
	
	@Loggable(LogLevel.TRACE)
	@Override
	@Transactional
	public void saveOrUpdate(Object entityObject){
		_saveOrUpdate(entityObject);
	}
	
	@Override
	public Session getSession() {
		return super.getSession();
	}
}
