package com.pareidolia.configuration.mail;

import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
@SuppressWarnings({"SpellCheckingInspection", "FieldCanBeLocal"})
@RequiredArgsConstructor(onConstructor_ = {@Autowired, @Lazy})
public class CustomMailSender {

	private final String fromName = "Polimi project";
	private final String from = "5b7a69fc5f-4a2988@inbox.mailtrap.io";
	private final Logger log = LoggerFactory.getLogger(this.getClass());
	private final JavaMailSender emailSender;

	public void sendMail(String to, String subject, String document, Boolean html, String typeMail) {
		try {
			MimeMessage mimeMessage = emailSender.createMimeMessage();
			MimeMessageHelper message = new MimeMessageHelper(mimeMessage, "UTF-8");
			message.setTo(to);
			message.setFrom(new InternetAddress(from, fromName));
			message.setSubject(subject);
			message.setText(document, html);
			emailSender.send(mimeMessage);
			log.info(String.format("EMAIL (%s) SENDED TO: %s", typeMail, to));
		} catch (Exception ignored) {
			log.error(String.format("ERROR WHILE SENDING EMAIL (%s) TO: %s", typeMail, to));
			throw new RuntimeException("Something went wrong with email");
		}
	}

	public boolean sendResetConsumer(String to, String password) {

		String document = "New password: " + password;
		sendMail(to, "Reset Password", document, false, "sendResetPasswordConsumer");
		return true;
	}
}