package org.celllife.mobilisr.service.gwt;

import java.util.List;

import org.celllife.mobilisr.exception.MobilisrException;
import org.celllife.mobilisr.exception.MobilisrRuntimeException;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * @see SettingService
 */
@RemoteServiceRelativePath("settingService.rpc")
public interface SettingService extends RemoteService {

	/**
	 * @return 
	 * @see SettingService#saveSetting(SettingViewModel)
	 */
	void saveSetting(SettingViewModel model) throws MobilisrException, MobilisrRuntimeException;

	/**
	 * @see SettingService#getSettigns()
	 */
	List<SettingViewModel> getSettings() throws MobilisrException, MobilisrRuntimeException;

}
