package org.celllife.mobilisr.api.rest;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.celllife.mobilisr.api.MobilisrDto;
import org.celllife.mobilisr.constants.ErrorCode;

@XmlRootElement(name = "err")
@XmlType(name="error",propOrder={"errorCode", "message"})
public class ErrorDto implements MobilisrDto {

	private static final long serialVersionUID = 2386189444895903044L;
	
	private ErrorCode errorCode;
	private String message;
	
	public ErrorDto() {
	}

	public ErrorDto(ErrorCode errorCode, String message) {
		this.errorCode = errorCode;
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

	@XmlElement(name = "msg", required = false)
	public void setMessage(String message) {
		this.message = message;
	}
	
	public ErrorCode getErrorCode() {
		return errorCode;
	}

	@XmlElement(name = "code", required = true)
	public void setErrorCode(ErrorCode errorCode) {
		this.errorCode = errorCode;
	}

	@Override
	public String toString() {
		return "ErrorDto [errorCode=" + errorCode + ", message=" + message + "]";
	}
}
