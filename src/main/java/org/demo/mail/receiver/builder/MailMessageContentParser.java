package org.demo.mail.receiver.builder;

import java.io.File;
import java.io.IOException;

import org.demo.mail.receiver.MailReceiverException;

import jakarta.mail.BodyPart;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Part;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMultipart;

public class MailMessageContentParser {

	private static void log(String s) {
		System.out.println("[LOG] " + s);
	}
	
	private final String attachementsDirectory ;
	
	private static final String TEXT_PLAIN            = "text/plain";
	private static final String TEXT_HTML             = "text/html";
	private static final String MULTIPART_ALTERNATIVE = "multipart/alternative";
	private static final String MULTIPART_RELATED     = "multipart/related";
	private static final String MULTIPART_MIXED       = "multipart/mixed";
	
	public MailMessageContentParser(String attachementsDirectory) {
		super();
		if ( attachementsDirectory == null ) {
			throw new IllegalArgumentException("attachementsDirectory is null");
		}		
		this.attachementsDirectory = attachementsDirectory;
	}

	protected MailMessageContent parseMessageContent(Message message, String messageId) throws MailReceiverException {
		try {
			return parse(message, messageId) ;
		} catch (IOException | MessagingException e) {
			throw new MailReceiverException("Cannot parse message content" , e);
		}
	}
	
	private MailMessageContent parse(Message message, String messageId) throws IOException, MessagingException, MailReceiverException {
		log ( "message content-type = " + message.getContentType() );
		MailMessageContent mailMessageContent = new MailMessageContent(messageId);
		Object contentObject = message.getContent();
		log( "content class : " + contentObject.getClass().getCanonicalName() );
		if ( message.isMimeType(TEXT_PLAIN) ) {
			// Basic text message : just basic text body (no html, no attachments) 
			mailMessageContent.setBody((String) contentObject);
		} else if ( message.isMimeType(MULTIPART_ALTERNATIVE) ) {
			// HTML message without images (no attachments) 
			parseMultipartAlternative(getMultipart(message.getContent()), mailMessageContent);
		} else if ( message.isMimeType(MULTIPART_RELATED ) ) {
			// HTML message with images (no attachments) 
			parseMultipartRelated(getMultipart(message.getContent()), mailMessageContent);
		} else if ( message.isMimeType(MULTIPART_MIXED) ) {
			// message with attachments  
			parseMultipartMixed(getMultipart(message.getContent()), mailMessageContent);
		} else {
			throw new MailReceiverException("Unknown message content. class = " + contentObject.getClass().getCanonicalName());
		}
		return mailMessageContent;
	}
	
	private MimeMultipart getMultipart(Object content) throws IOException, MessagingException, MailReceiverException {
    	if ( content instanceof MimeMultipart ) {
    		return (MimeMultipart) content;
    	} else {
			throw new MailReceiverException("Cannot convert to MimeMultipart : Unexpected class " + content.getClass().getName() );
    	}		
	}

	/**
	 * Parsing for "multipart/mixed", expected content : <br>
	 * - the message : multipart/related or multipart/alternative or text/plain <br>
	 * - attachments : 1..N files  <br>
	 * @param multipart
	 * @param mailMessageContent
	 * @throws IOException
	 * @throws MessagingException
	 * @throws MailReceiverException
	 */
	private void parseMultipartMixed(MimeMultipart multipart, MailMessageContent mailMessageContent) throws IOException, MessagingException, MailReceiverException {
		log("parseMultipartMixed..."  );
		String contentType = multipart.getContentType();
		if ( contentType.startsWith(MULTIPART_MIXED)) {
			// 
			for (int i = 0; i < multipart.getCount(); i++) {
			    BodyPart bodyPart = multipart.getBodyPart(i);
				log("parseMultipartMixed : part #" + i + " - content type : " + bodyPart.getContentType() );
			    if ( bodyPart.isMimeType(MULTIPART_RELATED)) {
			    	// Message body (text + html with images)
			    	parseMultipartRelated(getMultipart(bodyPart.getContent()), mailMessageContent);
			    }
			    else if ( bodyPart.isMimeType(MULTIPART_ALTERNATIVE)) {
			    	// Message body (text + html)
			    	parseMultipartAlternative(getMultipart(bodyPart.getContent()), mailMessageContent);
			    }
			    else if ( bodyPart.isMimeType(TEXT_PLAIN)) {
			    	if ( mailMessageContent.hasBody() ) { 
			    		// message body already set => attachment
			    		saveAttachment(bodyPart, mailMessageContent); 
			    	}
			    	else {
			    		// message body not yet set => use this part as message body
			    		mailMessageContent.setBody((String)bodyPart.getContent());
			    	}
			    }
			    else {
					log("parseMultipartMixed : image ");
					saveAttachment(bodyPart, mailMessageContent); 
			    }
			}
		}
		else {
			throw new MailReceiverException("Unexpected content type '" + contentType + "' (" + MULTIPART_MIXED + " expected)");
		}
	}
	
	private File saveAttachment(BodyPart bodyPart, MailMessageContent mailMessageContent) throws IOException, MessagingException, MailReceiverException {
		log("parseAttachment..."  );
		File attachmentFile = buildAttachmentFile(bodyPart, mailMessageContent);
		// Create folder if not exist
		File folder = attachmentFile.getParentFile();
		if ( ! folder.exists() ) {
			boolean created = folder.mkdir();
			if (!created) {
				throw new MailReceiverException("Cannot create folder " + folder.getAbsolutePath() );
			}
		}
		// Save attachment as file (folder must exist)
		MimeBodyPart part = (MimeBodyPart)bodyPart ;
		part.saveFile(attachmentFile);
		mailMessageContent.addAttachment(attachmentFile);
		return attachmentFile;
	}
	
	private File buildAttachmentFile(BodyPart bodyPart, MailMessageContent mailMessageContent) throws IOException, MessagingException {
		String fileName = bodyPart.getFileName();
		// message folder without invalid chars in folde name
		String messageFolder = mailMessageContent.getMessageId().replaceAll("[<>\\\\]", "-");
		File folder = new File(attachementsDirectory, messageFolder);
		return new File(folder, fileName);
	}
	
	/**
	 * Parsing for "multipart/related", expected content : <br>
	 * - message body in 2 formats (text and html) : multipart/alternative  <br>
	 * - resources embedded in message body : 0..N resources ( image/jpeg, image/png, etc ) <br>
	 * @param multipart
	 * @param mailMessageContent
	 * @throws IOException
	 * @throws MessagingException
	 * @throws MailReceiverException
	 */
	private void parseMultipartRelated(MimeMultipart multipart, MailMessageContent mailMessageContent) throws IOException, MessagingException, MailReceiverException {
		log("parseMultipartRelated... ");
		String contentType = multipart.getContentType();
		if ( contentType.startsWith(MULTIPART_RELATED)) {
			// 1 multipart/alternative + 1..N images expected 
			for (int i = 0; i < multipart.getCount(); i++) {
			    BodyPart bodyPart = multipart.getBodyPart(i);
				log("parseMultipartRelated : part #" + i + " - content type : " + bodyPart.getContentType() );
			    if ( bodyPart.isMimeType(MULTIPART_ALTERNATIVE)) {
			    	parseMultipartAlternative(getMultipart(bodyPart.getContent()), mailMessageContent);
			    }
//			    else if ( bodyPart.isMimeType(TEXT_PLAIN)) {
//			    	// Not expected
//					log("parseMultipartRelated : text ");
//			    }
//			    else if ( bodyPart.isMimeType(TEXT_HTML)) {
//			    	// Not expected
//					log("parseMultipartRelated : html ");
//			    }
			    else if ( bodyPart.isMimeType("image/*")) {
			    	// Images embedded in message body (if any)
			    }
			    else {
					throw new MailReceiverException("Unexpected part type in '" + MULTIPART_RELATED  + "' : " + bodyPart.getContentType() );
			    }
			}
		}
		else {
			throw new MailReceiverException("Unexpected content type '" + contentType + "' (" + MULTIPART_RELATED + " expected)");
		}
	}

	/**
	 * Parsing for "multipart/alternative", expected content (2 parts) : <br>
	 * - the message body in 'text/plain' <br>
	 * - the message body in 'text/html'  <br>
	 * @param multipart
	 * @param mailMessageContent
	 * @throws IOException
	 * @throws MessagingException
	 * @throws MailReceiverException
	 */
	private void parseMultipartAlternative(MimeMultipart multipart, MailMessageContent mailMessageContent) throws IOException, MessagingException, MailReceiverException {
		log("parseMultipartAlternative..." );
		String contentType = multipart.getContentType();
		if ( contentType.contains(MULTIPART_ALTERNATIVE)) {
			// 2 parts expected : text/plain and text/html 
			if ( multipart.getCount() == 2 ) {
				for (int i = 0; i < multipart.getCount(); i++) {
				    BodyPart bodyPart = multipart.getBodyPart(i);
					log("parseMultipartAlternative : part #" + i + " - content type : " + bodyPart.getContentType() );
				    if ( bodyPart.isMimeType(TEXT_PLAIN)) {
				    	parseBodyPart(bodyPart, mailMessageContent);
				    }
				    else if ( bodyPart.isMimeType(TEXT_HTML)) {
				    	parseBodyPart(bodyPart, mailMessageContent);
				    }
				    else {
						throw new MailReceiverException("Unexpected part type in '" + MULTIPART_ALTERNATIVE  + "' : " + bodyPart.getContentType() );
				    }
				}
			}
			else {
				throw new MailReceiverException("Unexpected number of parts in '" + MULTIPART_ALTERNATIVE + "' : " + multipart.getCount() + " (2 expected) ");
			}
		}
		else {
			throw new MailReceiverException("Unexpected content type '" + contentType + "' (" + MULTIPART_ALTERNATIVE + " expected)");
		}
	}

	private void parseBodyPart(Part part, MailMessageContent mailMessageContent) throws IOException, MessagingException, MailReceiverException {
		log("parseBodyPart content type : " + part.getContentType() );
		if ( part.isMimeType(TEXT_PLAIN) ) {
			Object o = part.getContent();
			if ( o instanceof String ) {
				mailMessageContent.setBody((String) o);
			} else {
				throw new MailReceiverException("Unexpected class for 'text/plain' part : " + o.getClass().getCanonicalName());
			}
		}
		else if ( part.isMimeType(TEXT_HTML)) {
			Object o = part.getContent();
			if ( o instanceof String ) {
				mailMessageContent.setBodyHtml((String) o);
			} else {
				throw new MailReceiverException("Unexpected class for 'text/html' part : " + o.getClass().getCanonicalName());
			}
		} 
//		else if ( part.isMimeType(MULTIPART_ALTERNATIVE)) {
//			parseMultipartAlternative( getMultipart(part.getContent()) , mailMessageContent );
//		} 
		
		
//		else if ( part.isMimeType(MULTIPART_RELATED)) {
//			parseMultipartRelated( getMultipart(part.getContent()) , mailMessageContent );
//		} 
//		else if ( part.isMimeType(MULTIPART_MIXED)) {
//			parseMultipartMixed( getMultipart(part.getContent()) , mailMessageContent );
//		}
		else {
			throw new MailReceiverException("Unexpected content type '" + part.getContentType()  + "' for message body");
		}
	}

}
