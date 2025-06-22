package com.pareidolia.configuration.mail;

import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomMailSenderTest {

	@Mock
	private JavaMailSender emailSender;

	@Mock
	private MimeMessage mimeMessage;

	@InjectMocks
	private CustomMailSender customMailSender;

	@BeforeEach
	void setUp() {
		ReflectionTestUtils.setField(customMailSender, "fromName", "Test Sender");
		ReflectionTestUtils.setField(customMailSender, "fromEmail", "test@example.com");
		when(emailSender.createMimeMessage()).thenReturn(mimeMessage);
	}

	@Test
	void testSendMailSuccess() {
		doNothing().when(emailSender).send(any(MimeMessage.class));

		assertDoesNotThrow(() ->
			customMailSender.sendMail(
				"recipient@example.com",
				"Test Subject",
				"Test Content",
				false,
				"TEST_MAIL"
			)
		);

		verify(emailSender).createMimeMessage();
		verify(emailSender).send(any(MimeMessage.class));
	}

	@Test
	void testSendMailFailure() {
		doThrow(new RuntimeException("Mail server error"))
			.when(emailSender).send(any(MimeMessage.class));

		Exception exception = assertThrows(RuntimeException.class, () ->
			customMailSender.sendMail(
				"recipient@example.com",
				"Test Subject",
				"Test Content",
				false,
				"TEST_MAIL"
			)
		);

		assertEquals("Something went wrong with email", exception.getMessage());
		verify(emailSender).createMimeMessage();
		verify(emailSender).send(any(MimeMessage.class));
	}

	@Test
	void testSendResetConsumer() {
		doNothing().when(emailSender).send(any(MimeMessage.class));

		boolean result = customMailSender.sendResetConsumer("user@example.com", "newpassword123");

		assertTrue(result);
		verify(emailSender).createMimeMessage();
		verify(emailSender).send(any(MimeMessage.class));
	}

	@Test
	void testSendResetConsumerFailure() {
		doThrow(new RuntimeException("Mail server error"))
			.when(emailSender).send(any(MimeMessage.class));

		Exception exception = assertThrows(RuntimeException.class, () ->
			customMailSender.sendResetConsumer("user@example.com", "newpassword123")
		);

		assertEquals("Something went wrong with email", exception.getMessage());
		verify(emailSender).createMimeMessage();
		verify(emailSender).send(any(MimeMessage.class));
	}
} 