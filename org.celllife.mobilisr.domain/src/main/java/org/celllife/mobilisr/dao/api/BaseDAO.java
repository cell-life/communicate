package org.celllife.mobilisr.dao.api;

import java.io.Serializable;
import java.util.List;

import com.trg.dao.hibernate.GenericDAO;

public interface BaseDAO<T, ID extends Serializable> extends GenericDAO<T, ID>{

	public abstract List<T> searchByPropertyEqual(String property, Object value);

	public abstract void saveOrUpdate(Object entityObject);
	
	public abstract void detach(Object entityObject);
	
	public abstract void merge(Object entityObject);
	
}