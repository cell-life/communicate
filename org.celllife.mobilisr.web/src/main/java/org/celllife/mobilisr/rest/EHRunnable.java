package org.celllife.mobilisr.rest;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.celllife.mobilisr.exception.MobilisrException;
import org.celllife.mobilisr.service.exception.ObjectNotFoundException;
import org.celllife.mobilisr.service.exception.UniquePropertyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Error Handling Runnable for use in REST services
 * 
 * @author Simon Kelly
 *
 * @param <T>
 */
public abstract class EHRunnable<T> {
	
	private static final Logger log = LoggerFactory.getLogger(EHRunnable.class);
	
	private final HttpServletResponse response;
	private boolean success;
	public EHRunnable(HttpServletResponse response) {
		this.response = response;
	}
	
	public T run() throws IOException{
		try {
			T returnObject = handled();
			success = true;
			return returnObject;
		} catch (ObjectNotFoundException e) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND, e.getMessage());
		} catch (UniquePropertyException e){
			response.sendError(HttpServletResponse.SC_CONFLICT, e.getMessage());
		} catch (MobilisrException e) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN, e.getMessage());
		} catch (Exception e){
			log.error("Error in EHRunnable",e);
			return handleOtherException(e);
		}
		return null;
	}


	protected T handleOtherException(Exception e) throws IOException {
		response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
		return null;
	}

	public boolean isSuccess() {
		return success;
	}

	public abstract T handled() throws Exception;
}