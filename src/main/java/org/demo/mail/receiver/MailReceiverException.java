package org.demo.mail.receiver;

public class MailReceiverException extends Exception {

	private static final long serialVersionUID = 1L;

	public MailReceiverException(String message) {
		super(message);
	}

	public MailReceiverException(String message, Throwable cause) {
		super(message, cause);
	}
}
