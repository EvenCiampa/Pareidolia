package com.pareidolia.strategy.email.event;

import com.pareidolia.entity.Event;

/**
 * Strategia per la generazione del contenuto delle email per i promotori
 */
public class PromoterInvitationStrategy implements EmailContentStrategy {
	@Override
	public String generateSubject(EmailEventType emailEventType, Event event) {
		return switch (emailEventType) {
			case CREATED -> "Nuovo evento da promuovere: " + event.getTitle();
			case UPDATED -> "Aggiornamento evento da promuovere: " + event.getTitle();
			// Add other cases as needed
		};
	}

	@Override
	public String generateBody(EmailEventType emailEventType, Event event) {
		String baseContent = getDefaultEventEmailBody(event);

		String eventTypeMessage = switch (emailEventType) {
			case CREATED -> "È stato creato un nuovo evento che richiede la tua promozione!";
			case UPDATED -> "Un evento che stai promuovendo è stato aggiornato!";
			// Add other cases as needed
		};

		return baseContent + "\n\n" + eventTypeMessage + "\n\nSei stato invitato a promuovere questo evento!";
	}
} 