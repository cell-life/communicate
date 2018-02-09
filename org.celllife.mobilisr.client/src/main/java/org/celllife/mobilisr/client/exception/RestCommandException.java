package org.celllife.mobilisr.client.exception;

import javax.ws.rs.core.Response.Status;

import org.celllife.mobilisr.api.rest.ErrorDto;
import org.celllife.mobilisr.api.rest.PagedListDto;

public class RestCommandException extends Exception {

	private static final long serialVersionUID = -1488271349646351485L;
	
	private Status status;
	private String requestUrl;
	private PagedListDto<ErrorDto> errors;
	
	public RestCommandException() {
	}
	
	public RestCommandException(PagedListDto<ErrorDto> errors) {
		super(errors.size() + " errors returned. First message: " +
				errors.getElements().get(0).getMessage());
		this.errors = errors;
	}
	
	public RestCommandException(String error, Throwable t) {
		super(error, t);
	}
	
	public RestCommandException(String error, int statusCode) {
		super(error);
		this.status = Status.fromStatusCode(statusCode);
	}
	
	public RestCommandException(int statusCode, String requestUrl) {
		super();
		this.requestUrl = requestUrl;
		this.status = Status.fromStatusCode(statusCode);
	}
	
	public RestCommandException(String error, Status status) {
		super(error);
		this.status = status;
	}
	
	public RestCommandException(String error) {
		super(error);
	}
	
	@Override
	public String getMessage() {
		return super.getMessage() == null ? getReasonPhrase() : super.getMessage();
	}

	public Status getStatus(){
		return status;
	}
	
	public int getStatusCode(){
		return status.getStatusCode();
	}

	public void setRequestUrl(String requestUrl) {
		this.requestUrl = requestUrl;
	}

	public String getRequestUrl() {
		return requestUrl;
	}
	
	public String getReasonPhrase(){
		if (status != null){
			return status.getReasonPhrase();
		}
		return "Unknown";
	}

	public PagedListDto<ErrorDto> getErrors() {
		return errors;
	}
	
	public void setErrors(PagedListDto<ErrorDto> errors) {
		this.errors = errors;
	}
}
