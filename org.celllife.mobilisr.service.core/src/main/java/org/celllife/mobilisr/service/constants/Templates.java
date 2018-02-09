package org.celllife.mobilisr.service.constants;

public enum Templates {
	
	INSUFFICIENT_BALANCE("org/celllife/mobilisr/templates/insufficientBalance.vm"), 
	BALANCE_LOW("org/celllife/mobilisr/templates/balanceLow.vm"),
	CREDIT_NOTIFICATION("org/celllife/mobilisr/templates/creditNotification.vm"),
	USER_REQUEST("org/celllife/mobilisr/templates/userRequest.vm"),
	NEW_PASSWORD("org/celllife/mobilisr/templates/newPassword.vm"),
	INCOMING_MESSAGE("org/celllife/mobilisr/templates/incomingMessage.vm"),
	REPORT_MESSAGE("org/celllife/mobilisr/templates/scheduledReportMessage.vm"), 
	NOTIFICATION_EMAIL("org/celllife/mobilisr/templates/notificationEmail.vm");
	
	private String templatePath;

	private Templates(String templatePath) {
		this.templatePath = templatePath;
	}
	
	public String getTemplatePath() {
		return templatePath;
	}

}
