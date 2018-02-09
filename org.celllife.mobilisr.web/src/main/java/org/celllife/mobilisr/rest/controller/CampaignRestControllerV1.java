package org.celllife.mobilisr.rest.controller;

import org.celllife.mobilisr.constants.ApiVersion;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller("campaignRestControllerV1")
@RequestMapping("/campaigns")
public class CampaignRestControllerV1 extends CampaignRestController {

	public CampaignRestControllerV1() {
		setApiVersion(ApiVersion.v1);
	}
	
}
