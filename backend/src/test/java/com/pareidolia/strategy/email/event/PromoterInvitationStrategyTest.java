package com.pareidolia.strategy.email.event;

import com.pareidolia.entity.Event;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

class PromoterInvitationStrategyTest {

	private PromoterInvitationStrategy strategy;

	@Mock
	private Event event;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		strategy = new PromoterInvitationStrategy();

		// Setup mock event
		when(event.getTitle()).thenReturn("Test Event");
		when(event.getDate()).thenReturn(LocalDate.of(2024, 3, 15));
		when(event.getPlace()).thenReturn("Test Venue");
		when(event.getDescription()).thenReturn("Test Description");
	}

	@Test
	void testGenerateSubjectForCreatedEvent() {
		String subject = strategy.generateSubject(EmailEventType.CREATED, event);
		assertEquals("Nuovo evento da promuovere: Test Event", subject);
	}

	@Test
	void testGenerateSubjectForUpdatedEvent() {
		String subject = strategy.generateSubject(EmailEventType.UPDATED, event);
		assertEquals("Aggiornamento evento da promuovere: Test Event", subject);
	}

	@Test
	void testGenerateBodyForCreatedEvent() {
		String body = strategy.generateBody(EmailEventType.CREATED, event);

		// Verify the body contains all required parts
		assertTrue(body.contains("Test Event"));
		assertTrue(body.contains("2024-03-15"));
		assertTrue(body.contains("Test Venue"));
		assertTrue(body.contains("Test Description"));
		assertTrue(body.contains("È stato creato un nuovo evento che richiede la tua promozione!"));
		assertTrue(body.contains("Sei stato invitato a promuovere questo evento!"));
	}

	@Test
	void testGenerateBodyForUpdatedEvent() {
		String body = strategy.generateBody(EmailEventType.UPDATED, event);

		// Verify the body contains all required parts
		assertTrue(body.contains("Test Event"));
		assertTrue(body.contains("2024-03-15"));
		assertTrue(body.contains("Test Venue"));
		assertTrue(body.contains("Test Description"));
		assertTrue(body.contains("Un evento che stai promuovendo è stato aggiornato!"));
		assertTrue(body.contains("Sei stato invitato a promuovere questo evento!"));
	}

	@Test
	void testGetDefaultEventEmailBody() {
		String defaultBody = strategy.getDefaultEventEmailBody(event);

		// Verify the default body format
		assertTrue(defaultBody.contains("Evento: Test Event"));
		assertTrue(defaultBody.contains("Data: 2024-03-15"));
		assertTrue(defaultBody.contains("Luogo: Test Venue"));
		assertTrue(defaultBody.contains("Descrizione: Test Description"));
	}
} 