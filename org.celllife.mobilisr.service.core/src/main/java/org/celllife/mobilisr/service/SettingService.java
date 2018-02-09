package org.celllife.mobilisr.service;

import java.util.List;

import org.celllife.mobilisr.domain.Setting;
import org.celllife.mobilisr.service.constants.SettingsEnum;
import org.celllife.mobilisr.service.gwt.SettingViewModel;

public interface SettingService {
	
	public List<SettingViewModel> getSettings();
	
	public <T> T getSettingValue(SettingsEnum setting);
	
	public void saveSetting(SettingViewModel setting);

	Setting getSetting(String name);

}