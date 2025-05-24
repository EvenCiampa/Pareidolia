package com.pareidolia.configuration.mail;

import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
@SuppressWarnings({"FieldCanBeLocal"})
@RequiredArgsConstructor(onConstructor_ = {@Autowired, @Lazy})
public class CustomMailSender {

	private final Logger log = LoggerFactory.getLogger(this.getClass());
	private final JavaMailSender emailSender;
	@Value("${app.mail.from-name}")
	private String fromName;
	@Value("${app.mail.from-email}")
	private String fromEmail;

	public void sendMail(String to, String subject, String document, Boolean html, String typeMail) {
		try {
			MimeMessage mimeMessage = emailSender.createMimeMessage();
			MimeMessageHelper message = new MimeMessageHelper(mimeMessage, "UTF-8");
			message.setTo(to);
			message.setFrom(new InternetAddress(fromEmail, fromName));
			message.setSubject(subject);
			message.setText(document, html);
			emailSender.send(mimeMessage);
			log.info("EMAIL ({}) SENDED TO: {}", typeMail, to);
		} catch (Exception ignored) {
			log.error("ERROR WHILE SENDING EMAIL ({}) TO: {}", typeMail, to);
			throw new RuntimeException("Something went wrong with email");
		}
	}

	public boolean sendResetConsumer(String to, String password) {
		String document = "New password: " + password;
		sendMail(to, "Reset Password", document, false, "sendResetPasswordConsumer");
		return true;
	}

}