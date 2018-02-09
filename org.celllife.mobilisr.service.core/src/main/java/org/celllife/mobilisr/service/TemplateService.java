package org.celllife.mobilisr.service;

import java.util.Map;

import org.celllife.mobilisr.service.constants.Templates;

public interface TemplateService {

	String generateContent(Map<String, Object> model, Templates template);

	String generateContent(Map<String, Object> model, String template);

	String generateDynamicContent(Map<String, Object> model, String templateBody);

}
