package org.demo.mail.receiver.builder;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

public class MailMessageContent {

	private final String messageId ;
	
	private String body ;

	private String bodyHtml ;
	
	private List<File> attachments ;

	
	public MailMessageContent(String messageId) {
		super(); 
		if ( messageId == null ) {
			throw new IllegalArgumentException("messageId is null");
		}
		this.messageId = messageId ;
		this.body = null;
		this.bodyHtml = null;
		this.attachments = new LinkedList<>();
	}

	public String getMessageId() {
		return messageId;
	}

	public String getBody() {
		return body;
	}
	public void setBody(String body) {
		this.body = body;
	}
	public boolean hasBody() {
		return body != null ;
	}

	public String getBodyHtml() {
		return bodyHtml;
	}
	public void setBodyHtml(String bodyHtml) {
		this.bodyHtml = bodyHtml;
	}

	public List<File> getAttachments() {
		return attachments;
	}
//	public void setAttachments(List<File> attachments) {
//		this.attachments = attachments;
//	}
	public void addAttachment(File file) {
		this.attachments.add(file);
	}
	
}
