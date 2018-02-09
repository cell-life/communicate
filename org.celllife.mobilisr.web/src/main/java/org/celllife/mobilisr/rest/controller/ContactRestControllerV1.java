package org.celllife.mobilisr.rest.controller;

import org.celllife.mobilisr.constants.ApiVersion;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller("contactRestControllerV1")
@RequestMapping("/contacts")
public class ContactRestControllerV1 extends ContactRestController{

	public ContactRestControllerV1() {
		setApiVersion(ApiVersion.v1);
	}
}
