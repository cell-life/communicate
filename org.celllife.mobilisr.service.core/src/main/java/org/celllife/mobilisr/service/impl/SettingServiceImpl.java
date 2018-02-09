package org.celllife.mobilisr.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.celllife.mobilisr.annotation.LogLevel;
import org.celllife.mobilisr.annotation.Loggable;
import org.celllife.mobilisr.dao.api.MobilisrGeneralDAO;
import org.celllife.mobilisr.domain.Setting;
import org.celllife.mobilisr.service.SettingService;
import org.celllife.mobilisr.service.constants.SettingsEnum;
import org.celllife.mobilisr.service.gwt.SettingViewModel;
import org.celllife.mobilisr.service.yaml.YamlUtils;
import org.celllife.pconfig.model.Parameter;
import org.celllife.pconfig.util.PconfigUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.trg.search.Search;

@Service("settingService")
@Transactional(readOnly = true)
public class SettingServiceImpl implements SettingService {
	
	private static Logger log = LoggerFactory.getLogger(SettingServiceImpl.class);

	@Autowired
	private MobilisrGeneralDAO generalDAO;
	
	private static final List<SettingsEnum> loadedSettings = new ArrayList<SettingsEnum>();
	
	public void setGeneralDAO(MobilisrGeneralDAO generalDAO) {
		this.generalDAO = generalDAO;
	}

	@Loggable(LogLevel.TRACE)
	@Override
	public Setting getSetting(String name) {
		Search settingSearch = new Search(Setting.class);
		settingSearch.addFilterEqual(Setting.PROP_NAME, name);
		return (Setting) generalDAO.searchUnique(settingSearch);
	}

	@Loggable(LogLevel.TRACE)
	@Override
	public List<SettingViewModel> getSettings() {
		List<Setting> savedSettings = generalDAO.findAll(Setting.class);
		
		ArrayList<SettingViewModel> models = new ArrayList<SettingViewModel>();
		
		List<SettingsEnum> allSettings = new ArrayList<SettingsEnum>();
		CollectionUtils.addAll(allSettings, SettingsEnum.values());
		
		for (Setting setting : savedSettings) {
			SettingsEnum cache = updateCache(setting);
			if (cache != null) {
				allSettings.remove(cache);
				models.add(new SettingViewModel(setting, cache.getConfig()));
			}
		}
		
		for (SettingsEnum setting : allSettings) {
			models.add(new SettingViewModel(new Setting(setting
					.getSettingName(), ""), setting.getConfig()));
		}
		
		return models;
	}

	@Loggable(LogLevel.TRACE)
	@Override
	@Secured({"PERM_MANAGE_SETTINGS"})
	@Transactional
	public void saveSetting(SettingViewModel model) {
		Parameter<?> config = model.getConfig();
		Setting setting = model.getSetting();
		
		setting.setValueString(YamlUtils.dumpParameterList(config));
		generalDAO.saveOrUpdate(setting);
		
		updateCache(setting);
	}

	@SuppressWarnings("unchecked")
	@Loggable(LogLevel.TRACE)
	@Override
	public <T> T getSettingValue(SettingsEnum setting) {
		int indexOf = loadedSettings.indexOf(setting);
		if (indexOf < 0){
			addToCache(setting.getSettingName());
		}
		
		Object value = setting.getSettingValue();
		return (T) value;
	}
	
	/*package private*/ void addToCache(String name){
		Setting result = getSetting(name);
		if (result == null){
			log.debug("No setting with name='{}' found", name);
			SettingsEnum setting = SettingsEnum.fromSettingName(name);
			if (setting == null){
				log.warn("No setting enum with name: {}", name);
				return;
			}
			synchronized (loadedSettings) {
				loadedSettings.add(setting);
			}
			return;
		}
		
		updateCache(result);
	}
	
	private SettingsEnum updateCache(Setting setting){
		SettingsEnum settingEnum = SettingsEnum.fromSettingName(setting.getName());
		if (settingEnum == null){
			log.warn("No setting enum with name: " + setting.getName());
			return null;
		}
		
		String valueString = setting.getValueString();
		List<Parameter<?>> list = YamlUtils.loadParameterList(valueString);
		if (list == null || list.isEmpty()){
			log.warn("Setting has not value: {}", setting.getName());
			return settingEnum;
		}
		Parameter<?> config = list.get(0);
		
		Parameter<?> template = settingEnum.getConfig();
		PconfigUtils.merge(config, template);
		settingEnum.setConfig(template);
		
		synchronized (loadedSettings) {
			loadedSettings.remove(settingEnum);
			loadedSettings.add(settingEnum);
		}
		
		return settingEnum;
	}
}
