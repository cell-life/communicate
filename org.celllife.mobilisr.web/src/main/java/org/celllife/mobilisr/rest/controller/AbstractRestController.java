package org.celllife.mobilisr.rest.controller;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.celllife.mobilisr.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.util.UriTemplate;

public class AbstractRestController {

	protected static final String LOCATION = "Location";
	
	@Autowired
	protected UserService userService;
	
	@InitBinder
	public void initBinder(WebDataBinder binder) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		binder.registerCustomEditor(Date.class, new CustomDateEditor(
				dateFormat, false));
	}

	protected String buildLocation(HttpServletRequest rqst, Object id) {
		StringBuffer url = rqst.getRequestURL();
		UriTemplate ut = new UriTemplate(url.append("/{Id}").toString());
		return ut.expand(id).toASCIIString();
	}

	void setUserService(UserService userService) {
		this.userService = userService;
	}
}
