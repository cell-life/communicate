package org.celllife.mobilisr.domain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.extjs.gxt.ui.client.data.BeanModelTag;

/**
 * The MobilisrPermissions enum represents all the permissions in Mobilisr. The
 * permissions are hierarchical such that each permissions may have many parent
 * permissions. Each parent permission is implied by the children.
 * 
 * Example:<br>
 * Permission hierarchy: PERM_A > PERM_B and PERM_B > PERM_C.<br>
 * Directly assigned permission: PERM_A.<br>
 * Implied permissions: PERM_A, PERM_B, PERM_C.
 * 
 * @author Simon Kelly
 * 
 */
public enum MobilisrPermission implements BeanModelTag{
	
	/**
	 * ROLE_ADMIN is a special case that implies ALL other permissions.
	 */
	ROLE_ADMIN("Super administrator",false),
	
	VIEW_ADMIN_CONSOLE("View admin console",false),
	MANAGE_USERS(PermissionGroup.GeneralAdmin, "Manage users",VIEW_ADMIN_CONSOLE),
	MANAGE_API_KEYS(PermissionGroup.GeneralAdmin, "Manage API Keys", MANAGE_USERS),
	MANAGE_ROLES(PermissionGroup.GeneralAdmin, "Manage roles",VIEW_ADMIN_CONSOLE),
	MANAGE_SETTINGS(PermissionGroup.GeneralAdmin, "Manage settings",VIEW_ADMIN_CONSOLE),
	
	ORGANISATIONS_MANAGE(PermissionGroup.OrganisationAdmin, "Manage organisations",VIEW_ADMIN_CONSOLE),
	ORGANISATIONS_CREDIT_BALANCE(PermissionGroup.OrganisationAdmin, "Credit organisation balance",ORGANISATIONS_MANAGE),
	ORGANISATIONS_SEND_NOTIFICATIONS(PermissionGroup.OrganisationAdmin, "Send organisation notifications",ORGANISATIONS_MANAGE),

	CAMPAIGNS_VIEW(PermissionGroup.Campaigns, "View campaigns"),
	CAMPAIGNS_CREATE(PermissionGroup.Campaigns, "Create campaigns", CAMPAIGNS_VIEW),
	CAMPAIGNS_EDIT(PermissionGroup.Campaigns, "Edit campaigns", CAMPAIGNS_VIEW),
	CAMPAIGNS_VOID(PermissionGroup.Campaigns, "Void campaigns", CAMPAIGNS_VIEW),
	CAMPAIGNS_MANAGE_RECIPIENTS(PermissionGroup.Campaigns, "Manage campaign recipients", CAMPAIGNS_VIEW),
	CAMPAIGNS_START_STOP(PermissionGroup.Campaigns, "Start / stop campaigns", CAMPAIGNS_VIEW),
	
	CAMPAIGNS_ADMIN_MANAGE(PermissionGroup.CampaignAdmin, "Manage campaigns for all organisations",VIEW_ADMIN_CONSOLE), 
	REBUILD_CAMPAIGN_SCHEDULES(PermissionGroup.CampaignAdmin, "Rebuild campaign schedules",CAMPAIGNS_ADMIN_MANAGE),
	
	REPORTS_VIEW(PermissionGroup.Reports, "View reports"),
	REPORTS_VIEW_ADMIN_REPORTS(PermissionGroup.Reports, "View admin reports",REPORTS_VIEW),
	REPORTS_DELETE(PermissionGroup.Reports, "Delete reports",REPORTS_VIEW),
	
	REPORT_SCHEDULES_VIEW(PermissionGroup.Reports, "View scheduled reports",REPORTS_VIEW),
	REPORT_SCHEDULES_CREATE(PermissionGroup.Reports, "Schedule reports",REPORT_SCHEDULES_VIEW),
	REPORT_SCHEDULES_EDIT(PermissionGroup.Reports, "Edit scheduled reports",REPORT_SCHEDULES_CREATE),
	REPORT_SCHEDULES_DELETE(PermissionGroup.Reports, "Delete scheduled reports",REPORT_SCHEDULES_VIEW),
	
	REPORTS_ADMIN_VIEW(PermissionGroup.ReportAdmin, "View all reports", VIEW_ADMIN_CONSOLE),
	REPORTS_ADMIN_DELETE(PermissionGroup.ReportAdmin, "Delete reports belonging to other organisations", REPORTS_ADMIN_VIEW),
	REPORTS_ADMIN_RELOAD_CACHE(PermissionGroup.ReportAdmin, "Reload report cache",REPORTS_ADMIN_VIEW),

	REPORT_SCHEDULES_ADMIN_VIEW(PermissionGroup.ReportAdmin, "View all report schedules",REPORTS_ADMIN_VIEW),
	REPORT_SCHEDULES_ADMIN_EDIT(PermissionGroup.ReportAdmin, "Edit scheduled reports belonging to other organisations",REPORT_SCHEDULES_ADMIN_VIEW),
	REPORT_SCHEDULES_ADMIN_DELETE(PermissionGroup.ReportAdmin, "Delete scheduled reports belonging to other organisations",REPORT_SCHEDULES_ADMIN_VIEW),
	
	CHANNELS_VIEW(PermissionGroup.Channels, "View channels",VIEW_ADMIN_CONSOLE),
	CHANNELS_IN_CREATE(PermissionGroup.Channels, "Create IN channels",CHANNELS_VIEW),
	CHANNELS_IN_EDIT(PermissionGroup.Channels, "Edit IN channels",CHANNELS_VIEW),
	CHANNELS_IN_START_STOP(PermissionGroup.Channels, "Activate / deactive IN channels",CHANNELS_VIEW),
	CHANNELS_OUT_CREATE(PermissionGroup.Channels, "Create OUT channels",CHANNELS_VIEW),
	CHANNELS_OUT_EDIT(PermissionGroup.Channels, "Edit OUT channels",CHANNELS_VIEW),
	CHANNELS_OUT_START_STOP(PermissionGroup.Channels, "Activate / deactivate OUT channels",CHANNELS_VIEW),
	
	CHANNEL_CONFIG_MANAGE(PermissionGroup.Channels, "Manage channel configs",VIEW_ADMIN_CONSOLE),
	
	FILTERS_VIEW(PermissionGroup.Filters, "View filters"),
	FILTERS_EDIT(PermissionGroup.Filters, "Edit filters",FILTERS_VIEW),
	FILTERS_START_STOP(PermissionGroup.Filters, "Activate / deactivete filters",FILTERS_VIEW),
	
	FILTERS_ADMIN_VIEW(PermissionGroup.FiltersAdmin, "View all filters", VIEW_ADMIN_CONSOLE, FILTERS_VIEW),
	FILTERS_ADMIN_CREATE(PermissionGroup.FiltersAdmin, "Create new filters", FILTERS_ADMIN_VIEW),
	FILTERS_ADMIN_EDIT(PermissionGroup.FiltersAdmin, "Edit all filters", FILTERS_ADMIN_VIEW, FILTERS_EDIT),
	FILTERS_ADMIN_START_STOP(PermissionGroup.FiltersAdmin, "Activate / deactivete all filters", FILTERS_ADMIN_VIEW, FILTERS_START_STOP),
	
	MANAGE_LOST_MESSAGES(PermissionGroup.GeneralAdmin, "Manage lost messages",VIEW_ADMIN_CONSOLE),
	NUMBER_INFO_VIEW(PermissionGroup.GeneralAdmin, "View supported number prefix list",VIEW_ADMIN_CONSOLE),
	NUMBER_INFO_EDIT(PermissionGroup.GeneralAdmin, "Edit number configs",NUMBER_INFO_VIEW),
	
	/**
	 * ROLE_SYSTEM is used when the system needs to execute functions that are protected
	 * but there is no user logged in.
	 */
	ROLE_SYSTEM(PermissionGroup.GeneralAdmin,"Role for system",false,
			CAMPAIGNS_MANAGE_RECIPIENTS);
	
	public enum PermissionGroup{
		GeneralAdmin,
		Filters,
		FiltersAdmin,
		Channels,
		Campaigns,
		CampaignAdmin,
		Reports,
		ReportAdmin, 
		OrganisationAdmin
	}
	
	/**
	 * This prefix is appended to the permission name so that Spring's RoleVoter
	 * works correctly. 
	 * 
	 * @see http://static.springsource.org/spring-security/site/docs/3.1.x/reference/authz-arch.html#authz-role-voter
	 */
	private static final String PREFIX = "PERM_";
	public static final String PROP_DISPLAY_NAME = "displayName";
	public static final String PROP_GROUP = "group";

	// parents are more general than their children.
	private final Collection<MobilisrPermission> parents = new HashSet<MobilisrPermission>();
	private final boolean assignable;
	private final String displayName;
	private final PermissionGroup group;
	
	private MobilisrPermission(PermissionGroup group, String displayName, 
			MobilisrPermission... parents) {
		this.group = group;
		this.displayName = displayName;
		this.assignable = true;
		this.parents.addAll(Arrays.asList(parents));
	}
	
	private MobilisrPermission(PermissionGroup group, String displayName, 
			boolean assignable,	MobilisrPermission... parents) {
		this.group = group;
		this.displayName = displayName;
		this.assignable = assignable;
		this.parents.addAll(Arrays.asList(parents));
	}
	
	private MobilisrPermission(String displayName,boolean assignable) {
		this.displayName = "";
		this.assignable = assignable;
		group = PermissionGroup.GeneralAdmin;
	}
	
	/**
	 * This method is used by the UI to get the name to display to the users.
	 * 
	 * @return
	 */
	public String getDisplayName(){
		return displayName;
	}
	
	/**
	 * Permission groups are used for display purposes only.
	 * 
	 * @return
	 */
	public PermissionGroup getGroup() {
		return group;
	}
	
	/**
	 * A permission is implied if it is an ancestor of this permission.
	 * 
	 * @param permission
	 * @return true if the permission parameter is implied by this permission
	 */
	public boolean implies(MobilisrPermission permission){
		if ((this == permission ) || (ROLE_ADMIN == this)){return true;}
		else{
			for (MobilisrPermission perm: parents){
				if (perm.implies(permission)){
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * Same as {@link #valueOf(String)} but will return null instead
	 * of throw IllegalArgumentException if value is not valid.
	 * 
	 * @param value
	 * @return {@link MobilisrPermission} or null
	 */
	public static MobilisrPermission safeValueOf(String value) {
		MobilisrPermission status = null;
		try {
			if (value.startsWith(PREFIX)){
				value = value.substring(PREFIX.length());
			}
			status = MobilisrPermission.valueOf(value);
		} catch (Exception ignore) {
			// ignore exception
		}
		return status;
	}
	
	public String getPrefixedName(){
		return PREFIX + this.name();		
	}

	/**
	 * Gets a collection of permissions that are implied by this permission.
	 * 
	 * The collection is guaranteed not to have any duplicates.
	 * 
	 * @param includeSelf
	 *            if true the returned collection of permissions will include
	 *            this permission.
	 * @return Collection of implied permissions
	 */
	public Collection<MobilisrPermission> getImpliedPermissions(boolean includeSelf) {
		if (this.equals(ROLE_ADMIN)){
			return Arrays.asList(values());
		}
		return getImpliedPermissions(this, includeSelf);
	}

	private Collection<MobilisrPermission> getImpliedPermissions(MobilisrPermission permission, boolean includeSelf) {
		Set<MobilisrPermission> implied = new HashSet<MobilisrPermission>();
		if (includeSelf)
			implied.add(permission);
		
		for (MobilisrPermission parent : permission.parents) {
			implied.addAll(getImpliedPermissions(parent, true));
		}
		return implied;
	}

	public static MobilisrPermission[] getAssignablePermissions() {
		MobilisrPermission[] values = values();
		List<MobilisrPermission> assignable = new ArrayList<MobilisrPermission>();
		for (MobilisrPermission p : values) {
			if (p.assignable){
				assignable.add(p);
			}
		}
		
		Collections.sort(assignable);
		return assignable.toArray(new MobilisrPermission[assignable.size()]);
	}
}
