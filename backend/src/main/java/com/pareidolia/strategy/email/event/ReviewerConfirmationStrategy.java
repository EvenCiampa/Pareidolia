package com.pareidolia.strategy.email.event;

import com.pareidolia.entity.Event;

/**
 * Strategia per la generazione del contenuto delle email per i reviewer
 */
public class ReviewerConfirmationStrategy implements EmailContentStrategy {
	@Override
	public String generateSubject(EmailEventType emailEventType, Event event) {
		return switch (emailEventType) {
			case CREATED -> "Nuovo evento da revisionare: " + event.getTitle();
			case UPDATED -> "Aggiornamento evento da revisionare: " + event.getTitle();
			// Add other cases as needed
		};
	}

	@Override
	public String generateBody(EmailEventType emailEventType, Event event) {
		String baseContent = getDefaultEventEmailBody(event);

		String eventTypeMessage = switch (emailEventType) {
			case CREATED -> "È stato creato un nuovo evento che richiede la tua revisione!";
			case UPDATED -> "Un evento che stai revisionando è stato aggiornato!";
			// Add other cases as needed
		};

		return baseContent + "\n\n" + eventTypeMessage + "\n\nGrazie per aver accettato di essere un reviewer!";
	}
} 