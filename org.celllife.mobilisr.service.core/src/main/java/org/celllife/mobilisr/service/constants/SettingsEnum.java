package org.celllife.mobilisr.service.constants;

import org.celllife.pconfig.model.BooleanParameter;
import org.celllife.pconfig.model.IntegerParameter;
import org.celllife.pconfig.model.Parameter;
import org.celllife.pconfig.model.StringParameter;

public enum SettingsEnum {
	
	USER_REQUEST_EMAIL("User request email address", new Pbuilder(){
		@Override
		public Parameter<?> build() {
			StringParameter mailto = new StringParameter("Email","Email to:");
			mailto.setTooltip("Comma separated list of email addresses");
			mailto.setRegex("([a-zA-Z0-9_.\\-+])+@(([a-zA-Z0-9\\-])+\\.)+[a-zA-Z0-9]{2,4}" +
					"(,\\s*([a-zA-Z0-9_.\\-+])+@(([a-zA-Z0-9\\-])+\\.)+[a-zA-Z0-9]{2,4})*");
			mailto.setErrorMessage("Invalid email addresses");
			mailto.setDefaultValue("campaign.request@demo.com");
			return mailto;
		}
	}),
	SYSTEM_NOTIFICATIONS_EMAIL("System notifications email address", new Pbuilder(){
		@Override
		public Parameter<?> build() {
			StringParameter mailto = new StringParameter("Email","Email to:");
			mailto.setTooltip("Comma separated list of email addresses");
			mailto.setRegex("([a-zA-Z0-9_.\\-+])+@(([a-zA-Z0-9\\-])+\\.)+[a-zA-Z0-9]{2,4}" +
					"(,\\s*([a-zA-Z0-9_.\\-+])+@(([a-zA-Z0-9\\-])+\\.)+[a-zA-Z0-9]{2,4})*");
			mailto.setErrorMessage("Invalid email addresses");
			mailto.setDefaultValue("notifications@demo.com");
			return mailto;
		}
	}),
	CREDIT_NOTIFICATIONS_EMAIL("Credit notifications email address", new Pbuilder(){
		@Override
		public Parameter<?> build() {
			StringParameter mailto = new StringParameter("Email","Email to:");
			mailto.setTooltip("Comma separated list of email addresses");
			mailto.setRegex("([a-zA-Z0-9_.\\-+])+@(([a-zA-Z0-9\\-])+\\.)+[a-zA-Z0-9]{2,4}" +
					"(,\\s*([a-zA-Z0-9_.\\-+])+@(([a-zA-Z0-9\\-])+\\.)+[a-zA-Z0-9]{2,4})*");
			mailto.setErrorMessage("Invalid email addresses");
			mailto.setDefaultValue("credit.added@demo.com");
			return mailto;
		}
	}),
	MESSAGE_BATCH_SIZE("Message batch size", new Pbuilder(){
		@Override
		public Parameter<?> build() {
			IntegerParameter size = new IntegerParameter("size", "Batch size:");
			size.setDefaultValue(50);
			return size;
		}
	}),
	MESSAGE_VALIDITY_TIME("Message Validity Time", new Pbuilder(){
		@Override
		public Parameter<?> build() {
			IntegerParameter valid = new IntegerParameter("validFor", "Validity period:");
			valid.setTooltip("The number of days messages are valid for before expiring.");
			valid.setDefaultValue(10);
			return valid;
		}
	}),
	ENABLE_MAIL_QUEUE_PROCESSING("Enable mail queue processing", new Pbuilder(){
		@Override
		public Parameter<?> build() {
			BooleanParameter valid = new BooleanParameter("processMailQueue", "Process Mail Queue:");
			valid.setTooltip("If unselected then the mail queue will not be processed.");
			valid.setDefaultValue(true);
			return valid;
		}
	});
	
	private interface Pbuilder{
		public Parameter<?> build();
	}

	private String settingName;
	private Parameter<?> config;

	private SettingsEnum(String name, Pbuilder builder) {
		this.settingName = name;
		config = builder.build();
	}

	public String getSettingName() {
		return settingName;
	}
	
	public Parameter<?> getConfig() {
		return config;
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getSettingValue(){
		Object value = config.getValue();
		if (value == null){
			return (T) config.getDefaultValue();
		}
		return (T) value;
	}

	public void setConfig(Parameter<?> config) {
		this.config = config;
	}
	
	public static SettingsEnum fromSettingName(String name){
		for (SettingsEnum setting : values()) {
			if (setting.getSettingName().equals(name)){
				return setting;
			}
		}
		
		return null;
	}
}
