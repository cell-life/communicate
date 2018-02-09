package org.celllife.communicate.dao;

import java.util.List;

import org.celllife.communicate.GroupStats;

import com.trg.dao.hibernate.GeneralDAO;

public interface PerfDao extends GeneralDAO {
	
	void saveOrUpdate(Object entityObject);

	List<GroupStats> getSmsLogAttemptStats();

	List<GroupStats> getSmsLogStatusStats();

	List<GroupStats> getSmsLogWaspStatusStats();

}
