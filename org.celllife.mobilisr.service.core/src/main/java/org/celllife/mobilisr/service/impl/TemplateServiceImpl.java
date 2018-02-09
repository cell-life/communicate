package org.celllife.mobilisr.service.impl;

import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.VelocityException;
import org.apache.velocity.tools.ToolContext;
import org.apache.velocity.tools.ToolManager;
import org.celllife.mobilisr.annotation.LogLevel;
import org.celllife.mobilisr.annotation.Loggable;
import org.celllife.mobilisr.service.SettingService;
import org.celllife.mobilisr.service.TemplateService;
import org.celllife.mobilisr.service.constants.SettingsEnum;
import org.celllife.mobilisr.service.constants.Templates;
import org.celllife.mobilisr.service.utility.MobilisrUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.velocity.VelocityEngineUtils;

@Service("templateService")
public class TemplateServiceImpl implements TemplateService {

	@Autowired
	private VelocityEngine engine;
	
	@Autowired
	private SettingService settingService;
	
	@Loggable(LogLevel.TRACE)
	@Override
	public String generateDynamicContent(Map<String, Object> model, String templateBody) {
		VelocityContext context = getContext(model);
		return evaluate(engine, templateBody, context);
	}

	@Loggable(LogLevel.TRACE)
	@Override
	public String generateContent(Map<String, Object> model, String templatePath) {
		VelocityContext context = getContext(model);
		return mergeTemplate(engine, templatePath, context);
	}

	@Loggable(LogLevel.TRACE)
	@Override
	public String generateContent(Map<String, Object> model, Templates template) {
		return generateContent(model, template.getTemplatePath());
	}

	private VelocityContext getContext(Map<String, Object> model) {
		model.put("hostname", MobilisrUtility.getHostname());
		
		String userRequestEmail = settingService.getSettingValue(SettingsEnum.USER_REQUEST_EMAIL);
		model.put("userRequestEmail", userRequestEmail);
		
		ToolManager velocityToolManager = new ToolManager();
		// velocityToolManager.configure("velocity-tools.xml");
		ToolContext toolContext = velocityToolManager.createContext();
		return new VelocityContext(model, toolContext);
	}

	/**
	 * @see {@link VelocityEngineUtils#mergeTemplate(VelocityEngine, String, Map, Writer)}
	 */
	public String mergeTemplate(VelocityEngine velocityEngine,
			String templateLocation, VelocityContext context)
			throws VelocityException {

		try {
			StringWriter result = new StringWriter();
			velocityEngine.mergeTemplate(templateLocation, "ISO-8859-1",
					context, result);
			return result.toString();
		} catch (VelocityException ex) {
			throw ex;
		} catch (RuntimeException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new VelocityException(ex.toString());
		}
	}
	
	public String evaluate(VelocityEngine velocityEngine,
			String templateBody, VelocityContext context)
			throws VelocityException {

		try {
			StringWriter result = new StringWriter();
			velocityEngine.evaluate(context, result, "dynamicTemplate", templateBody);
			return result.toString();
		} catch (VelocityException ex) {
			throw ex;
		} catch (RuntimeException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new VelocityException(ex.toString());
		}
	}

	public void setVelocityEngine(VelocityEngine engine) {
		this.engine = engine;
	}
	
	public void setSettingService(SettingService settingService) {
		this.settingService = settingService;
	}
}
