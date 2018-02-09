package org.celllife.mobilisr.dao.impl;

import org.celllife.mobilisr.dao.api.RoleDAO;
import org.celllife.mobilisr.domain.Role;
import org.springframework.stereotype.Repository;

@Repository("roleDAO")
public class RoleDAOImpl extends BaseDAOImpl<Role, Long> implements RoleDAO {

}
