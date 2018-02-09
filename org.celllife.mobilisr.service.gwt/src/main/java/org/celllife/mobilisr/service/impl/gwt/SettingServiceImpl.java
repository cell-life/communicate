package org.celllife.mobilisr.service.impl.gwt;

import java.util.List;

import javax.servlet.ServletException;

import org.celllife.mobilisr.service.gwt.SettingService;
import org.celllife.mobilisr.service.gwt.SettingViewModel;

public class SettingServiceImpl extends AbstractMobilisrService implements SettingService {
	
	private static final long serialVersionUID = 1783347235337931539L;
	
	private org.celllife.mobilisr.service.SettingService service;
	
	@Override
	public void init() throws ServletException {
		super.init();
		service = (org.celllife.mobilisr.service.SettingService) getBean("settingService");
	}
	
	@Override
	public List<SettingViewModel> getSettings(){
		return service.getSettings();
	}

	@Override
	public void saveSetting(SettingViewModel model){
		service.saveSetting(model);
	}
}
