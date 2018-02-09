package org.celllife.mobilisr.service.utility;

import java.util.ArrayList;
import java.util.List;

import org.celllife.mobilisr.domain.MobilisrPermission;

public class MobilisrPermissionUtility {

	public static String convertToCSVString(List<MobilisrPermission> permissions){
		
		String rolePermissions = null;
		
		StringBuffer stringBuffer = new StringBuffer();
		
		for(MobilisrPermission permission: permissions){
			
			stringBuffer.append(permission.name());
			stringBuffer.append(";");
		}
		
		rolePermissions = stringBuffer.substring(0, stringBuffer.length()-1);
		
		return rolePermissions;
	}
	
	public static List<MobilisrPermission> convertCSVStringToList(String permissions){
		
		List<MobilisrPermission> permissionList = new ArrayList<MobilisrPermission>();
		String[] availPermissions = permissions.split(";");
		for(String permissionName: availPermissions){
			
			permissionList.add(MobilisrPermission.valueOf(permissionName));
		}
		
		return permissionList;
	}
}
