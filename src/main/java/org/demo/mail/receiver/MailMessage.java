package org.demo.mail.receiver;

import java.io.File;
import java.time.LocalDate;
import java.util.List;

public class MailMessage {

	private final String from ;
	private final LocalDate date ;
	private final String subject ;
	private final String messageId ;
	
	private String sender ;
	private List<String> to ;
	private List<String> cc ;
	private List<String> bcc ;
	
	private String contentType;
	private int size ;
	private String body;
	private String bodyHtml;

	private List<File> attachements ;
	
	private boolean processingOK = false;
	
	public MailMessage(String from, LocalDate date, String subject, String messageId) {
		super();
		this.from = from;
		this.date = date;
		this.subject = subject;
		this.messageId = messageId;
	}
	
	public String getFrom() {
		return from;
	}
	public LocalDate getDate() {
		return date;
	}
	public String getSubject() {
		return subject;
	}
	public String getMessageId() {
		return messageId;
	}
	
	public void setSender(String sender) {
		this.sender = sender;
	}
	public String getSender() {
		return sender;
	}
	
	public void setTo(List<String> to) {
		this.to = to;
	}
	public List<String> getTo() {
		return to;
	}
	
	public void setCc(List<String> cc) {
		this.cc = cc;
	}
	public List<String> getCc() {
		return cc;
	}

	public void setBcc(List<String> bcc) {
		this.bcc = bcc;
	}
	public List<String> getBcc() {
		return bcc;
	}

	public int getSize() {
		return size;
	}
	public void setSize(int size) {
		this.size = size;
	}

	public String getContentType() {
		return contentType;
	}
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public void setProcessingOK(boolean processingOK) {
		this.processingOK = processingOK;
	}
	public boolean isProcessingOK() {
		return processingOK;
	}

	public String getBody() {
		return this.body;
	}
	public void setBody(String body) {
		this.body = body;
	}

	public String getBodyHtml() {
		return this.bodyHtml;
	}
	public void setBodyHtml(String bodyHtml) {
		this.bodyHtml = bodyHtml;
	}
	
	public List<File> getAttachments() {
		return this.attachements;
	}
	public boolean hasAttachments() {
		return this.attachements != null && this.attachements.size() > 0 ;
	}
	public void setAttachments(List<File> attachements) {
		this.attachements = attachements;
	}
	
	
}
