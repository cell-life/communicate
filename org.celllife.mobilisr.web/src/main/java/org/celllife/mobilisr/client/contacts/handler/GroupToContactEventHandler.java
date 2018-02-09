package org.celllife.mobilisr.client.contacts.handler;

import java.util.ArrayList;
import java.util.List;

import org.celllife.mobilisr.client.UserContext;
import org.celllife.mobilisr.client.contacts.GenericContactManagementView;
import org.celllife.mobilisr.client.model.ViewModel;
import org.celllife.mobilisr.client.modelcompare.ContactGroupModelComparer;
import org.celllife.mobilisr.client.view.gxt.ChangeAwareListStore;
import org.celllife.mobilisr.client.view.gxt.MessageBoxWithIds;
import org.celllife.mobilisr.domain.Campaign;
import org.celllife.mobilisr.domain.Contact;
import org.celllife.mobilisr.domain.ContactGroup;
import org.celllife.mobilisr.domain.MobilisrEntity;
import org.celllife.mobilisr.domain.Organization;
import org.celllife.mobilisr.service.gwt.ContactContactGroupViewModel;
import org.celllife.mobilisr.service.gwt.ContactGroupContactViewModel;

import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class GroupToContactEventHandler extends GenericContactManagementEventHandler<ContactGroup> {

	private GenericContactManagementView<ContactGroup> view;
	private static ContactGroupModelComparer modelComparer = new ContactGroupModelComparer();
	
	
	public GroupToContactEventHandler( GenericContactManagementView<ContactGroup> view) {
		super(view, modelComparer);
		this.view = view;
	}

	public void handleFilterInRightGridStore(String filterValue) {
		view.filterInRightGridStore(filterValue, ContactGroup.PROP_GROUP_NAME);
	}

	public void handleObjectDisplaySaveMsg(ContactGroup contactGroup) {
		MessageBoxWithIds.info("", "New Group " + contactGroup.getGroupName() + " successfully created and added to Selected Groups", null);
	}
	
	@Override
	public void handleObjectDisplayFailMessage(Throwable error) {
		MessageBoxWithIds.alert("Error saving", error.getMessage(), null);
	}

	public void handleRPCLoadListAllEntities(ViewModel<? extends MobilisrEntity> vem, String searchVal, Object loadConfig, AsyncCallback<PagingLoadResult<ContactGroup>> callback) {
		PagingLoadConfig pagingLoadConfig = getLoadingConfig(searchVal, ContactGroup.PROP_GROUP_NAME, loadConfig);
		Organization org = UserContext.getUser().getOrganization();
		if (vem != null){
			MobilisrEntity entity = vem.getModelObject();
			if (entity != null && entity instanceof Campaign){
				org = ((Campaign)entity).getOrganization();
			}
		}
		getContactsServiceAsync().listAllGroupsForOrganization(org, pagingLoadConfig, callback);
	}

	public void handleRPCLoadListAssociationForObject( ViewModel<? extends MobilisrEntity> viewEntityModel, Object loadConfig, AsyncCallback<PagingLoadResult<ContactGroup>> callback) {
		PagingLoadConfig pagingLoadConfig = getLoadingConfig(getSelEntitySearch(), ContactGroup.PROP_GROUP_NAME, loadConfig);	
		Contact contact = (Contact) viewEntityModel.getModelObject();
		getContactsServiceAsync().listAllGroupsForContact(UserContext.getUser().getOrganization(), contact, pagingLoadConfig, callback);
	}

	public void handleRPCObjectSave(ViewModel<? extends MobilisrEntity> vem, String newEntityFieldValue, AsyncCallback<ContactGroup> callback) {
		ContactGroup newContactGroup = new ContactGroup();
		newContactGroup.setGroupName(newEntityFieldValue);
		ContactGroupContactViewModel contactModel = new ContactGroupContactViewModel(newContactGroup, null, null, false, false);
		Organization org = UserContext.getUser().getOrganization();
		if (vem != null){
			MobilisrEntity entity = vem.getModelObject();
			if (entity != null && entity instanceof Campaign){
				org = ((Campaign)entity).getOrganization();
			}
		}
		getContactsServiceAsync().saveOrUpdateContactGroup(org, contactModel, callback);
	}

	public ContactContactGroupViewModel getContactModel(Contact contact){
		
		ChangeAwareListStore<BeanModel> selectedEntityStore = getSelectedEntityStore();
		List<BeanModel> addedGroupList = selectedEntityStore.getAdded();
		List<BeanModel> removedGroupList = selectedEntityStore.getRemoved();
		List<ContactGroup> addedGroupsList = convertModelListToGroupList(addedGroupList);
		List<ContactGroup> removedGroupsList = convertModelListToGroupList(removedGroupList);
		boolean isAddAll = isAddAllSelected();
		boolean isRemoveAll = isRemoveAllSelected();
		
		ContactContactGroupViewModel contactModel = new ContactContactGroupViewModel(contact, addedGroupsList, removedGroupsList, 
																									isAddAll, isRemoveAll);
			
		return contactModel;
	}
	
	private List<ContactGroup> convertModelListToGroupList(List<BeanModel> beanModels){
		List<ContactGroup> contactGroups = new ArrayList<ContactGroup>(); 
		
		for (BeanModel beanModel : beanModels) {
			ContactGroup contactGroup = beanModel.getBean();
			contactGroups.add(contactGroup);
		}
		return contactGroups;
		
	}
}
