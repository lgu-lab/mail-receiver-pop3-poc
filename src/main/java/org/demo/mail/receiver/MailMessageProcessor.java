package org.demo.mail.receiver;

public interface MailMessageProcessor {

	boolean processMessage(MailMessage mailMessage);
	
}
