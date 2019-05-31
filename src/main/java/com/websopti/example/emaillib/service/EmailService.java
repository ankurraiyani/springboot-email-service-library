package com.websopti.example.emaillib.service;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Map;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.websopti.example.emaillib.model.Mail;

@Service
public class EmailService {
	
	private final Logger logger = LogManager.getFormatterLogger(EmailService.class);

	@Autowired
	private JavaMailSender javaMailSender;
	
	@Value("${mail.service.turn.on}")
	private boolean isServiceOn;
	
	@Value("${spring.mail.username}")
	private String fromEmail;
	
	@Value("${spring.mail.fromName}")
	private String fromName;
	
	@Autowired
	private TemplateEngine templateEngine;
	
	/**
	 * Send email in synchronous mode
	 * 
	 * @param mail
	 * @throws UnsupportedEncodingException
	 * @throws MessagingException
	 */
	public void sendMail(Mail mail) throws UnsupportedEncodingException, MessagingException {
		this.sendMail(createMineMessage(mail));
	}
	
	/**
	 * Send email in asynchronous mode
	 * 
	 * @param mail
	 * @throws UnsupportedEncodingException
	 * @throws MessagingException
	 */
	@Async
	public void sendMailAsync(Mail mail) throws UnsupportedEncodingException, MessagingException {
		this.sendMail(createMineMessage(mail));
	}
	
	/**
	 * Prepare MimeMessage using Mail object
	 * 
	 * @param fromName
	 * @param fromEmail
	 * @param subject
	 * @param htmlMailBody
	 * @param toEmails
	 * @return
	 * @throws UnsupportedEncodingException
	 * @throws MessagingException
	 */
	private MimeMessage createMineMessage(Mail mail) throws UnsupportedEncodingException, MessagingException {
		
		MimeMessage mimeMessage = javaMailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, 
				MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                StandardCharsets.UTF_8.name());
		
		helper.setFrom(fromEmail, fromName);
		helper.setTo(mail.getToEmails());
		
		if(mail.getCcEmails() != null && mail.getCcEmails().length > 0)
			helper.setCc(mail.getCcEmails());
		
		helper.setSubject(mail.getSubject());
		
		//If email body exists then use it else build the template
		if(mail.getEmailBody() != null)
			helper.setText(mail.getEmailBody(), true);
		else if(mail.getTemplateName() != null)
			helper.setText(this.buid(mail.getTemplateName(), mail.getTemplateVariables()), true);
			
		//Add logo image
		try {
			File logoFile = new ClassPathResource("static/images/logo.png").getFile();
			final InputStreamSource imageSource = new ByteArrayResource(Files.readAllBytes(logoFile.toPath()));
		    helper.addInline("logo", imageSource, Files.probeContentType(logoFile.toPath()));			
		} catch (IOException e) {
			logger.error("Error while adding logo", e);
		}
		
		//Add attachments
		for (String attachment : mail.getAttachments()) {
			FileSystemResource file = new FileSystemResource(attachment);
			if(file.exists())
				helper.addAttachment(file.getFilename(), file);
        }		

		return helper.getMimeMessage();
	}
	

	/**
	 * Provide template name and the model which should contains values for variables of templates.
	 * This will return the final email body.
	 * 
	 * @param templateName
	 * @param model
	 * @return
	 */
	private String buid(String templateName, Map<String, Object> model) {
		Context context = new Context();
		context.setVariables(model);
		return templateEngine.process(templateName, context);
	}
	
	/**
	 * @param mimeMessage
	 */
	private void sendMail(MimeMessage mimeMessage) {
		if(isServiceOn) 
			javaMailSender.send(mimeMessage);
		else
			logger.info("Email services is turned off.");
	}
}
