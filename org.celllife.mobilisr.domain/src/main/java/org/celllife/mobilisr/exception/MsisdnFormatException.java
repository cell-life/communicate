package org.celllife.mobilisr.exception;



public class MsisdnFormatException extends MobilisrException {

	private static final long serialVersionUID = -1016439842518333356L;

	public MsisdnFormatException() {
		super();
	}
	
	public MsisdnFormatException(String msisdn, Throwable cause) {
		super(formatMessage(msisdn), cause);
	}

	public MsisdnFormatException(String msisdn) {
		super(formatMessage(msisdn));
	}

	public MsisdnFormatException(Throwable cause) {
		super(cause);
	}
	
	private static String formatMessage(String msisdn) {
		return "Misidn "+msisdn+" is invalid";
	}
}
