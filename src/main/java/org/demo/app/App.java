package org.demo.app;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.demo.app.processor.MessageProcessorImpl;
import org.demo.mail.receiver.MailAccount;
import org.demo.mail.receiver.MailMessage;
import org.demo.mail.receiver.MailReceiver;
import org.demo.mail.receiver.MailReceiverException;

public class App {

	private static final String attachementsDirectory = "D:\\Z\\TMP\\TMPTMP\\MAILS" ;
	
	private static Properties getMailServerProperties() {
		Properties properties = new Properties();

		// cf
		// https://javaee.github.io/javamail/docs/api/com/sun/mail/pop3/package-summary.html
		// If "pop3s" protocol to access POP3 over SSL => all the properties would be
		// named "mail.pop3s.*".
		// server setting
		properties.put("mail.pop3.host", "pop.free.fr");
		properties.put("mail.pop3.port", "110"); // default 110

		return properties;
	}
	
	private static MailAccount buildAccount(String[] args) {
		if (args.length < 2 ) {
			throw new IllegalArgumentException("2 arguments expected : userName and userPassword");
		}
		return new MailAccount(args[0], args[1]);
	}
	
	public static void main(String[] args) {
		System.out.println("Main : MailReceiver creation ");
		MailAccount mailAccount = buildAccount(args);
		MailReceiver mailReceiver = new MailReceiver(getMailServerProperties(), attachementsDirectory, new MessageProcessorImpl() );
		System.out.println("Main : mailReceiver.readMessages(user,password)...");
		List<MailMessage> messages = new LinkedList<MailMessage>();
		try {
			//mailReceiver.receiveMessages("user", "password");
			// or 
			messages = mailReceiver.readMessages(mailAccount, MailReceiver.DO_NOT_DELETE_MSG );
		} catch (MailReceiverException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println("" );  
		System.out.println("" );  
		System.out.println("=============================================================" );  
		System.out.println("=============================================================" );  
		System.out.println("Main : " + messages.size() + " message(s) in list : ");
		for ( MailMessage msg : messages ) {
			System.out.println("=============================================================" );  
			System.out.println(" . " + msg.getMessageId() + " : from " + msg.getFrom() + "\n"  
					+ "  isProcessingOK : " + msg.isProcessingOK() );
			System.out.println("BODY : \n" + msg.getBody()  );
			System.out.println("BODY HTML : \n" + msg.getBodyHtml()  );
			if ( msg.hasAttachments() ) {
				System.out.println("ATTACHMENTS : ");
				for ( File a : msg.getAttachments() ) {
					System.out.println(" . " + a.getAbsolutePath() );
				}
			}
			else {
				System.out.println("NO ATTACHMENT");
			}
		}
		
	}

}
