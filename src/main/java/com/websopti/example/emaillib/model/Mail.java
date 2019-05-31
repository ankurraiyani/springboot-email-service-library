package com.websopti.example.emaillib.model;

import java.util.Map;

import lombok.Data;

@Data
public class Mail {

	private String[] toEmails;
	
	private String[] ccEmails;
	
	private String subject;
	
	private String emailBody;
	
	private String[] attachments;
	
	private String templateName;
	
	private Map<String, Object> templateVariables;

	public void setToEmails(String... emails) {
		this.toEmails = emails;
	}

	public void setCcEmails(String... emails) {
		this.ccEmails = emails;
	}
	
	public void setAttachments(String... filePaths) {
		this.attachments = filePaths;
	}
}
