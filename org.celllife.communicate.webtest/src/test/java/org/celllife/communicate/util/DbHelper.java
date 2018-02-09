package org.celllife.communicate.util;

import java.util.List;

import org.celllife.mobilisr.dao.api.MobilisrGeneralDAO;
import org.celllife.mobilisr.domain.Campaign;
import org.celllife.mobilisr.domain.ContactGroup;
import org.celllife.mobilisr.domain.MessageFilter;
import org.celllife.mobilisr.domain.MobilisrEntity;
import org.celllife.mobilisr.domain.Organization;
import org.celllife.mobilisr.domain.User;
import org.celllife.mobilisr.domain.mock.DomainMockFactory;
import org.celllife.mobilisr.util.MobilisrSecurityUtility;
import org.springframework.security.authentication.encoding.ShaPasswordEncoder;

import com.trg.search.Search;

public class DbHelper {
	
	private MobilisrGeneralDAO generalDao;
	
	public DbHelper(MobilisrGeneralDAO generalDao) {
		this.generalDao = generalDao;
	}

	public <T extends MobilisrEntity> List<T> getEntities(Class<T> type, String property, Object value){
		Search search = new Search(type);
		search.addFilterEqual(property, value);
		@SuppressWarnings("unchecked")
		List<T> list = generalDao.search(search);
		return list;
	}
	
	public <T extends MobilisrEntity> T getUniqueEntity(Class<T> type, String property, Object value){
		Search search = new Search(type);
		search.addFilterEqual(property, value);
		@SuppressWarnings("unchecked")
		T result = (T) generalDao.searchUnique(search);
		return result;
	}
	
	public <T extends MobilisrEntity> T getUniqueEntity(Class<T> type, Long id){
		return getUniqueEntity(type, "id", id);
	}
	
	public boolean checkEntityExists(Class<? extends MobilisrEntity> type, String property, Object value){
		return getUniqueEntity(type, property, value) != null;
	}

	public <T extends MobilisrEntity> T createTestEntity(Class<T> entityType) {
		T entity = DomainMockFactory._().on(entityType).create();
		if (entityType.equals(User.class)){
			User user = (User) entity;
			generalDao.save(user.getOrganization());
			generalDao.save(user);
		} else if (entityType.equals(Campaign.class)){
			Campaign campaign = (Campaign) entity;
			generalDao.save(campaign.getOrganization());
			generalDao.save(campaign);
		} else if (entityType.equals(MessageFilter.class)){
			MessageFilter messageFilter = (MessageFilter) entity;
			generalDao.save(messageFilter.getOrganization());
			generalDao.save(messageFilter);
		} else if (entityType.equals(ContactGroup.class)){
			ContactGroup contactGroup = (ContactGroup) entity;
			generalDao.save(contactGroup.getOrganization());
			generalDao.save(contactGroup);
		} 
		else {
			generalDao.save(entity);
		}
		return entity;
	}
	
	public void saveOrgUpdate(MobilisrEntity entity){
		generalDao.saveOrUpdate(entity);
	}
	
	public User createUser(String password) {
		User user = createTestEntity(User.class);
		String salt = MobilisrSecurityUtility.getRandomToken();
		
		ShaPasswordEncoder encoder = new ShaPasswordEncoder();
		encoder.setEncodeHashAsBase64(false);
		String encodedPwd = encoder.encodePassword(password, salt);
		
		user.setPassword(encodedPwd);
		user.setSalt(salt);
		saveOrgUpdate(user);
		
		return user;
	}

	public Organization getAdminOrganization() {
		return getUniqueEntity(Organization.class, Organization.PROP_NAME, "Admin organisation");
	}
	
	public User getAdminUser() {
		return getUniqueEntity(User.class, User.PROP_USERNAME, "admin");
	}

}
