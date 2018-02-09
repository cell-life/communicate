package org.celllife.mobilisr.service.gwt;


public class ServiceAndUIConstants {
	
// Regular Expression validation constants
	public static final String REGEX_TEXT_MESSAGE = "[a-zA-Z0-9 \n\r@$_!\"#%&'()+,\\-./:;<=>?*\\^\\[\\]{}~|\\\\]*";
	
	public static final String VALIDATION_TEXT_MESSAGE = "Messages can only use the following" +
			" characters: a-z, A-Z, 0-9, space, [LF], [CR], @ $ _ ! \" # % & ' ( ) + , - . /" +
			" : ; < = > ? * ^ [ ] { } ~ | \\";
	
	/*public static final String REGEX_MOBILE_NUMBER = "^27[1-9][0-9]{8}$";
	
	public static final String REGEX_MOBILE_NUMBERS = "27[1-9][0-9]{8}(,\\s*27[1-9][0-9]{8})*";

	public static final String VALIDATION_MOBILE_NUMBERS = "This field only accepts numbers of this format: 27821231234";*/
	
	public static final String DOWNLOAD_FOLDER = "forDownload";
	
	public static final String PROP_REPORT_ORGANISATION_ID = "organisation_id";
	public static final String PROP_REPORT_PERMISSIONS = "permissions";
	public static final String PROP_REPORT_ORGANISATION_NAME = "organisation_name";
	
}
