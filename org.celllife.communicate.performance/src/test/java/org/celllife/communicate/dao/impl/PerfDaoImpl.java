package org.celllife.communicate.dao.impl;

import java.util.List;

import org.celllife.communicate.GroupStats;
import org.celllife.communicate.dao.PerfDao;
import org.celllife.mobilisr.annotation.LogLevel;
import org.celllife.mobilisr.annotation.Loggable;
import org.hibernate.SQLQuery;
import org.hibernate.SessionFactory;
import org.hibernate.transform.Transformers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.trg.dao.hibernate.GeneralDAOImpl;

@Repository("perfDao")
public class PerfDaoImpl extends GeneralDAOImpl implements PerfDao {

	@Override
	@Transactional(readOnly=true)
	public List<GroupStats> getSmsLogAttemptStats() {
		SQLQuery query = getSession().createSQLQuery("select count(id) as count, cast(attempts as char) as value" +
				" from smslog group by attempts");
		
		query.setResultTransformer(Transformers.aliasToBean(GroupStats.class));
		@SuppressWarnings("unchecked")
		List<GroupStats> list = query.list();
		return list;
	}
	
	@Override
	@Transactional(readOnly=true)
	public List<GroupStats> getSmsLogStatusStats() {
		SQLQuery query = getSession().createSQLQuery(
				"select count(id) as count, cast(status as char) as value"
						+ " from smslog group by status");

		query.setResultTransformer(Transformers.aliasToBean(GroupStats.class));
		@SuppressWarnings("unchecked")
		List<GroupStats> list = query.list();
		return list;
	}
	
	@Override
	@Transactional(readOnly=true)
	public List<GroupStats> getSmsLogWaspStatusStats() {
		SQLQuery query = getSession().createSQLQuery(
				"select count(id) as count, cast(waspstatus as char) as value"
						+ " from smslog group by waspstatus");

		query.setResultTransformer(Transformers.aliasToBean(GroupStats.class));
		@SuppressWarnings("unchecked")
		List<GroupStats> list = query.list();
		return list;
	}
	
	@Autowired
	@Override
	public void setSessionFactory(SessionFactory sessionFactory) {
		super.setSessionFactory(sessionFactory);
	}
	
	@Loggable(LogLevel.TRACE)
	@Override
	@Transactional
	public void saveOrUpdate(Object entityObject){
		_saveOrUpdate(entityObject);
	}
}
