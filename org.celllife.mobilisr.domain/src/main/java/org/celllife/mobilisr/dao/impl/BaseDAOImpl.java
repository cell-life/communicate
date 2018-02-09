package org.celllife.mobilisr.dao.impl;

import java.io.Serializable;
import java.util.List;

import org.celllife.mobilisr.annotation.LogLevel;
import org.celllife.mobilisr.annotation.Loggable;
import org.celllife.mobilisr.dao.api.BaseDAO;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.trg.dao.hibernate.GenericDAOImpl;
import com.trg.search.Search;

public class BaseDAOImpl<T, ID extends Serializable> extends GenericDAOImpl<T, ID> implements BaseDAO<T, ID> {

	@Autowired
	@Override
	public void setSessionFactory(SessionFactory sessionFactory) {
		super.setSessionFactory(sessionFactory);
	}
	
	@Loggable(LogLevel.TRACE)
	// Using Propagation.SUPPORTS here since it doesn't close the session
	// at the end of the transaction which was causing lazy initialization
	// exceptions in various places
	@Transactional(propagation=Propagation.SUPPORTS, readOnly=true)
	public List<T> searchByPropertyEqual(String property, Object value){
		Search search = new Search();
		search.addFilterEqual(property, value);
		return search(search);
	}

	@Loggable(LogLevel.TRACE)
	@Transactional
	public void saveOrUpdate(Object entityObject) {
		_saveOrUpdate(entityObject);
	}

	@Loggable(LogLevel.TRACE)
	@Override
	@Transactional
	public void detach(Object entityObject) {
		getSession().evict(entityObject);
	}
	
	@Loggable(LogLevel.TRACE)
	@Override
	@Transactional
	public void merge(Object entityObject) {
		_merge(entityObject);
	}
	
}