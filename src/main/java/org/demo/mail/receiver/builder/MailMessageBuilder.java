package org.demo.mail.receiver.builder;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.demo.mail.receiver.MailMessage;
import org.demo.mail.receiver.MailReceiverException;

import jakarta.mail.Address;
import jakarta.mail.Message;
import jakarta.mail.Message.RecipientType;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

public class MailMessageBuilder {

	/**
	 * Builds a MailMessage from the given Message
	 * @param message
	 * @return
	 */
	public MailMessage buildMailMessage(Message message, String attachementsDirectory) throws MailReceiverException {

		String messageId = getMessageId(message);

		LocalDate sentDate = getSentDate(message);
		String subject = getSubject(message);

		// A Message object's message number is the relative position of this Message in its Folder.
		// Note that the message number for a particular Message can change during a session
		// if other messages in the Folder are deleted and expunged. => useless 
		// msg.getMessageNumber();

		// Senders
		String from = getFrom(message);
		// for getSender() use MimeMessage

		MailMessage messageReceived = new MailMessage(from, sentDate, subject, messageId);

		messageReceived.setSender(getSender(message));

		// Recipients
		messageReceived.setTo( addressesAsString(getRecipients(message,RecipientType.TO)  ) );
		messageReceived.setCc( addressesAsString(getRecipients(message,RecipientType.CC)  ) );
		messageReceived.setBcc(addressesAsString(getRecipients(message,RecipientType.BCC) ) );

		messageReceived.setSize(getSize(message));
		
		String contentType = getContentType(message);
		messageReceived.setContentType(contentType);
		
		// Parse message content : body (text and html), attachments, etc
		MailMessageContentParser parser = new MailMessageContentParser(attachementsDirectory);
		MailMessageContent content = parser.parseMessageContent(message, messageId) ;
		messageReceived.setBody(content.getBody() );
		messageReceived.setBodyHtml(content.getBodyHtml() );
		messageReceived.setAttachments(content.getAttachments());
		
		// messageReceived.setBody(getBody(message));
		
		return messageReceived;
	}

	private String getFrom(Message message) {
		Address[] addresses;
		try {
			addresses = message.getFrom();
			if (addresses.length >= 1) {
				// only the first address is used
				return addressAsString(addresses[0]);
			} else {
				return null;
			}
		} catch (MessagingException e) {
			return null;
		}
	}

	private Address[] getRecipients(Message message, RecipientType recipientType) {
		try {
			return message.getRecipients(recipientType);
		} catch (MessagingException e1) {
			return null;
		}
	}

	private String getSubject(Message message) {
		try {
			return message.getSubject();
		} catch (MessagingException e) {
			return null;
		}
	}
	
	private LocalDate getSentDate(Message message) {
		try {
			Date date = message.getSentDate();
			if ( date != null ) {
				return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
			}
			else {
				return null;
			}
		} catch (MessagingException e) {
			return null;
		}
	}

	private String getMessageId(Message message) {
		if (message instanceof MimeMessage) {
			MimeMessage mimeMessage = (MimeMessage) message;
			String messageId;
			try {
				messageId = mimeMessage.getMessageID();
			} catch (MessagingException e) {
				messageId = null;
			}
			return messageId;
		} else {
			return null;
		}
	}

	private String getSender(Message message)  {
		if (message instanceof MimeMessage) { // class POP3Message extends MimeMessage
			MimeMessage mimeMessage = (MimeMessage) message;
			try {
				return addressAsString(mimeMessage.getSender()); // "Sender" header field (can be null)
			} catch (MessagingException e) {
				return null;
			}
		} else {
			return null;
		}
	}

	private int getSize(Message message)  {
		try {
			return message.getSize();
		} catch (MessagingException e) {
			return -1;
		}
	}

	private String getContentType(Message message) {
		try {
			return message.getContentType();
		} catch (MessagingException e) {
			return null;
		}
	}

//	private Object getContent(Message message) throws MailReceiverException {
//        try {
//			return message.getContent();
//		} catch (IOException | MessagingException e) {
//			throw new MailReceiverException("Cannot get message content", e);
//		}
//	}
//	private boolean isText(Message message) throws MailReceiverException {
//        try {
//			return ( message.isMimeType("text/plain") || message.isMimeType("text/html") ) ;
//		} catch (MessagingException e) {
//			throw new MailReceiverException("Cannot get message MimeType", e);
//		}
//	}
//	private boolean isMimeMultipart(Message message) throws MailReceiverException {
//        try {
//			return ( message.isMimeType("multipart/*") ) ;
//		} catch (MessagingException e) {
//			throw new MailReceiverException("Cannot get message MimeType", e);
//		}
//	}

//	private void parseContent_OLD(Message message) throws IOException, MessagingException, MailReceiverException {
//		log ( "message content-type = " + message.getContentType() );
//		String messageBodyAsString ;
//		Object contentObject = message.getContent();
//		log( "content class : " + contentObject.getClass().getCanonicalName() );
//		if ( contentObject instanceof String ) {
//			// when "content-type : text/plain"
//			String content = (String) contentObject;
//		} else if ( contentObject instanceof Multipart ) {
//			Multipart multipart = (Multipart) contentObject;
//			for (int i = 0; i < multipart.getCount(); i++) {
//			    BodyPart bodyPart = multipart.getBodyPart(i);
//			    log( "bodyPart #" + i + " : content-type = " + bodyPart.getContentType() );
//				log( "bodyPart #" + i + " : java class : " + bodyPart.getClass().getCanonicalName() );
//			    if ( bodyPart.isMimeType("multipart/*") ) {
//			    	bodyPart.get
//			    }
//			}
//			
//		} else {
//			throw new MailReceiverException("Unknown message content. class = " + contentObject.getClass().getCanonicalName());
//		}
//	}
	
	
//	private MimeMultipart getMultipart(Object content) throws IOException, MessagingException, MailReceiverException {
//    	if ( content instanceof MimeMultipart ) {
//    		return (MimeMultipart) content;
//    	} else {
//			throw new MailReceiverException("Cannot convert to MimeMultipart : Unexpected class " + content.getClass().getName() );
//    	}		
//	}

//	private String getBody(Message message) throws MailReceiverException {
//	    if ( isText(message) ) {
//	    	return getContent(message).toString();
//	    }
//	    if ( isMimeMultipart(message) ) {
//	    	Object content = getContent(message);
//	    	if ( content instanceof MimeMultipart ) {
//	    		MimeMultipart mimeMultipart = (MimeMultipart) content;
//		        return getBodyFromMimeMultipart(mimeMultipart);
//	    	} else {
//				throw new MailReceiverException("Cannot get body. Unexpected content type " + content.getClass().getName() );
//	    	}
//	    }
//	    return "";
//	}
//	private String getBodyFromMimeMultipart(Multipart mimeMultipart) throws MailReceiverException {
//	    try {
//			for (int i = 0; i < mimeMultipart.getCount(); i++) {
//			    BodyPart bodyPart = mimeMultipart.getBodyPart(i);
//			    if (bodyPart.isMimeType("text/plain")) {
//			        return bodyPart.getContent().toString();
//			    } 
//			}
//		} catch (MessagingException | IOException e) {
//		    throw new MailReceiverException("Cannot get body from multipart", e );
//		}
//	    throw new MailReceiverException("Cannot found body in multipart" );
//	}
	
//	private String getContent(Message message, String contentType) throws MailReceiverException {
//		if ( contentType != null ) {
//			if ( contentType.contains("text/plain") || contentType.contains("text/html")) {
//                Object content;
//				try {
//					content = message.getContent();
//	                if (content != null) {
//	                    return content.toString();
//	                } else {
//	                	return "";
//	                }
//				} catch (IOException | MessagingException e) {
//					throw new MailReceiverException("Cannot get message body (content-type '"+contentType+"')", e);
//				}
//			} else {
//				throw new MailReceiverException("Cannot get message body (unsupported content-type '"+contentType+"')");
//			}
//		} else {
//			throw new MailReceiverException("Cannot get message body : content-type is null");
//		}
//	}
	
	private List<String> addressesAsString(Address[] addresses) {
		List<String> list = new LinkedList<>();
		if (addresses != null) {
			for ( Address address : addresses ) {
				if ( address != null ) {
					list.add(addressAsString(address)); 
				}
			}
		}
		return list ;
	}
	
	private String addressAsString(Address address) {
		if (address != null) {
			return address.toString();
		} else {
			return null;
		}
	}
}
