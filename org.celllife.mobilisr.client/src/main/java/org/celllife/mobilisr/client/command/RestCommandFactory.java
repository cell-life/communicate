package org.celllife.mobilisr.client.command;

import java.net.Authenticator;

public class RestCommandFactory {

	private final Authenticator authenticator;
	private final String baseUrl;

	public RestCommandFactory(String baseUrl, Authenticator authenticator){
		this.baseUrl = baseUrl;
		this.authenticator = authenticator;
	}
	
	public GetCommand getCommandGet(String relativeUrl, Object... parameters){
		GetCommand command = new GetCommand(baseUrl, relativeUrl, parameters);
		command.setAuthenticator(authenticator);
		return command;
	}
	
	public PostCommand getPostCommand(String relativeUrl, Object post){
		PostCommand command = new PostCommand(baseUrl, relativeUrl, post);
		command.setAuthenticator(authenticator);
		return command;
	}
	public PostCommand getPostCommand(String relativeUrl, Object post, Object... urlParameters) {
		PostCommand command = new PostCommand(baseUrl, relativeUrl, post, urlParameters);
		command.setAuthenticator(authenticator);
		return command;
	}
	
	public DeleteCommand getDeleteCommand(String relativeUrl){
		DeleteCommand command = new DeleteCommand(baseUrl, relativeUrl);
		command.setAuthenticator(authenticator);
		return command;
	}
	
	public DeleteCommand getDeleteCommand(String relativeUrl, Object... urlParameters){
		DeleteCommand command = new DeleteCommand(baseUrl, relativeUrl, urlParameters);
		command.setAuthenticator(authenticator);
		return command;
	}

	public PutCommand getPutCommand(String relativeUrl, Object post){
		PutCommand command = new PutCommand(baseUrl, relativeUrl, post);
		command.setAuthenticator(authenticator);
		return command;
	}
	public PutCommand getPutCommand(String relativeUrl, Object post, Object... urlParameters) {
		PutCommand command = new PutCommand(baseUrl, relativeUrl, post, urlParameters);
		command.setAuthenticator(authenticator);
		return command;
	}
}
