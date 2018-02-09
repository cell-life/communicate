package org.celllife.mobilisr.client.impl;

import org.celllife.mobilisr.api.validation.ValidatorFactory;
import org.celllife.mobilisr.client.CampaignService;
import org.celllife.mobilisr.client.ContactService;
import org.celllife.mobilisr.client.MessageLogService;
import org.celllife.mobilisr.client.MobilisrClient;
import org.celllife.mobilisr.client.command.BasicAuthenticator;
import org.celllife.mobilisr.client.command.RestCommandFactory;
import org.celllife.mobilisr.constants.ApiVersion;


public class MobilisrClientImpl implements MobilisrClient{

	private static final String API_PATH = "api";
	private String baseUrl;
	private BasicAuthenticator authenticator;
	private final ValidatorFactory vfactory;

	public MobilisrClientImpl(String baseUrl, String username, String password, ValidatorFactory vfactory) {
		this.vfactory = vfactory;
		this.baseUrl = String.format("%s/%s/%s",baseUrl,API_PATH,ApiVersion.getLatest());
		authenticator = new BasicAuthenticator(username, password);
	}

	@Override
	public CampaignService getCampaignService() {
		return new CampaignServiceImpl(getCommandFactory(), vfactory);
	}

	@Override
	public ContactService getContactService() {
		return new ContactServiceImpl(getCommandFactory(), vfactory);
	}

    @Override
    public MessageLogService getMessageLogService() {
        return new MessageLogServiceImpl(getCommandFactory(), vfactory);
    }

	private RestCommandFactory getCommandFactory() {
		return new RestCommandFactory(baseUrl, authenticator);
	}


}