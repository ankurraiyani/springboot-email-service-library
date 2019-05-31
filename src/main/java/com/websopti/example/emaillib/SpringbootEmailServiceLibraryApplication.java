package com.websopti.example.emaillib;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.mail.MessagingException;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.EnableAsync;

import com.websopti.example.emaillib.model.Mail;
import com.websopti.example.emaillib.service.EmailService;

@SpringBootApplication
@EnableAsync
public class SpringbootEmailServiceLibraryApplication {
	
	public static void main(String[] args) throws MessagingException, IOException {
		ApplicationContext context = SpringApplication.run(SpringbootEmailServiceLibraryApplication.class, args);
		
		EmailService emailService = context.getBean(EmailService.class);
		
		Mail mail = new Mail();
		mail.setToEmails("ankur@websoptimization.com");
		mail.setCcEmails("ankur@websoptimization.com");
		mail.setSubject("Test Email");
		mail.setTemplateName("samplemail");
		mail.setAttachments("src/main/resources/templates/mail/samplemail.html");
		
		Map<String, Object> model = new HashMap<>();
		model.put("message", "Hello World !!");
		mail.setTemplateVariables(model);
		
		emailService.sendMailAsync(mail);
	}
}
