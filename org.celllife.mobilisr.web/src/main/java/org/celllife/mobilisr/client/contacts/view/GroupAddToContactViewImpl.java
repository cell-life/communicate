package org.celllife.mobilisr.client.contacts.view;

import org.celllife.mobilisr.client.Messages;
import org.celllife.mobilisr.client.MobilisrUIConstants;
import org.celllife.mobilisr.domain.ContactGroup;

import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;

public class GroupAddToContactViewImpl extends GenericContactManagementViewImpl<ContactGroup>{

	public GroupAddToContactViewImpl() {
		ContactManagementText text = new ContactManagementText();
		text.listEntityPanelHeading = MobilisrUIConstants.GROUP_TO_CONTACT_LEFTGRID_HEADING;
		text.addAllAnchorText = Messages.INSTANCE.contactAddToAllGroups();
		text.selEntityPanelHeading = MobilisrUIConstants.GROUP_TO_CONTACT_RIGHTGRID_HEADING;
		text.removeAllAnchorText = Messages.INSTANCE.contactRemoveAllGroups();
		text.addNewEntityFieldSetHeading = MobilisrUIConstants.GROUP_TO_CONTACT_NEW_GROUP;
		text.addNewEntityBtnText = Messages.INSTANCE.groupAdd();
		text.popupHeading = MobilisrUIConstants.GROUPS_TO_CONTACT_HEADING;
		init(text);
	}
	
	protected String getEntityName() {
		return "Group";
	}
	
	protected ColumnConfig getGridColumnConfig() {
		return new ColumnConfig( ContactGroup.PROP_GROUP_NAME, "Group Name", 150 );
	}

	@SuppressWarnings("unchecked")
	@Override
	protected TextField<String> getNewEntityField() {
		TextField<String> groupName = new TextField<String>();
		groupName.setAllowBlank(false);
		groupName.setMinLength(3);
		groupName.setFieldLabel(Messages.INSTANCE.compulsory()+
				Messages.INSTANCE.groupName());
		return groupName;
	}
}
