package org.celllife.mobilisr.client.user;

import java.util.List;

import org.celllife.mobilisr.client.app.EntityCreate;
import org.celllife.mobilisr.domain.ApiKey;
import org.celllife.mobilisr.domain.Role;
import org.celllife.mobilisr.domain.User;

import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.button.Button;

public interface AdminUserCreateView extends EntityCreate<User> {

	void setRolesStore(ListStore<BeanModel> listOfRoles);

	void setOrganizationStore(ListStore<BeanModel> store);

	List<Role> getSelectedRoles();

	Button getCreateKeyButton();

	void addApiKey(ApiKey result);

}