package org.demo.mail.receiver;

public class MailAccount {

	private final String userName ;
	private final String userPassword ;
	
	public MailAccount(String userName, String userPassword ) {
		super();
		this.userName = userName;
		this.userPassword = userPassword;
	}

	public String getUserName() {
		return userName;
	}

	public String getUserPassword() {
		return userPassword;
	}

}
