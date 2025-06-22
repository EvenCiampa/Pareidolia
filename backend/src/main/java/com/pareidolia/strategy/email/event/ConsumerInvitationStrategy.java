package com.pareidolia.strategy.email.event;

import com.pareidolia.entity.Event;

/**
 * Strategia per la generazione del contenuto delle email per i consumatori
 */
public class ConsumerInvitationStrategy implements EmailContentStrategy {
	@Override
	public String generateSubject(EmailEventType emailEventType, Event event) {
		return switch (emailEventType) {
			case CREATED -> "Nuovo evento: " + event.getTitle();
			case UPDATED -> "Aggiornamento evento: " + event.getTitle();
			// Add other cases as needed
		};
	}

	@Override
	public String generateBody(EmailEventType emailEventType, Event event) {
		String baseContent = getDefaultEventEmailBody(event);

		String eventTypeMessage = switch (emailEventType) {
			case CREATED -> "È stato creato un nuovo evento che potrebbe interessarti!";
			case UPDATED -> "Un evento a cui potresti essere interessato è stato aggiornato!";
			// Add other cases as needed
		};

		return baseContent + "\n\n" + eventTypeMessage + "\n\nPartecipa all'evento e invita i tuoi amici!";
	}
} 