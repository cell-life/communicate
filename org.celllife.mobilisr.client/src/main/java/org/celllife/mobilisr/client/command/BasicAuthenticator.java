package org.celllife.mobilisr.client.command;

import java.net.Authenticator;
import java.net.PasswordAuthentication;

public class BasicAuthenticator extends Authenticator {
	private final String username;
	private final String password;

	public BasicAuthenticator(String username, String password) {
		this.username = username;
		this.password = password;
	}

	public PasswordAuthentication getPasswordAuthentication() {
		return (new PasswordAuthentication(username, password.toCharArray()));
	}
}