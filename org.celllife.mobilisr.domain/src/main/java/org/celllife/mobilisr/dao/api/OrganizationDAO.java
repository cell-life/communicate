package org.celllife.mobilisr.dao.api;

import org.celllife.mobilisr.domain.Organization;
import org.celllife.mobilisr.domain.User;

/**
 * Inteface for activities related to organization such as balance management, etc
 * @author Vikram Bindal (e-mail: vikram@cell-life.org)
 *
 */
public interface OrganizationDAO extends BaseDAO<Organization, Long> {

	User getLastLoggedInUser(Organization organization);

}
