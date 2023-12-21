package org.demo.app.processor;

import org.demo.mail.receiver.MailMessage;
import org.demo.mail.receiver.MailMessageProcessor;

public class MessageProcessorImpl implements MailMessageProcessor {

	@Override
	public boolean processMessage(MailMessage mailMessage) {
		// Mail message processing 
		
		System.out.println(" -> message ( id = '" + mailMessage.getMessageId() + "' ) :" );
		System.out.println("    from    : " + mailMessage.getFrom() );
		System.out.println("    subject : " + mailMessage.getSubject() );
		System.out.println("    content-type : " + mailMessage.getContentType()  );
		System.out.println("    body : "  );
		System.out.println( mailMessage.getBody() );
		System.out.println("---" );
		return true;
	}

}
