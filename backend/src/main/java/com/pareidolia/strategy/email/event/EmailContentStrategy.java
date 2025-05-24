package com.pareidolia.strategy.email.event;

import com.pareidolia.entity.Event;

/**
 * Strategy per la generazione del contenuto delle email
 * Ogni strategia implementa un modo diverso di generare il contenuto in base al tipo di destinatario
 */
public interface EmailContentStrategy {
	/**
	 * Genera l'oggetto dell'email in base al tipo di evento
	 * @param emailEventType Il tipo di evento (creato, modificato, etc.)
	 * @param event L'evento per cui generare l'oggetto
	 * @return L'oggetto dell'email
	 */
	String generateSubject(EmailEventType emailEventType, Event event);

	/**
	 * Genera il corpo dell'email in base al tipo di evento e all'evento stesso
	 * @param emailEventType Il tipo di evento (creato, modificato, etc.)
	 * @param event L'evento per cui generare il contenuto
	 * @return Il corpo dell'email formattato
	 */
	String generateBody(EmailEventType emailEventType, Event event);

	/**
	 * Genera il contenuto base dell'evento che è comune a tutti i tipi di email
	 * @param event L'evento per cui generare il contenuto base
	 * @return Il contenuto base dell'email
	 */
	default String getDefaultEventEmailBody(Event event) {
		return String.format(
			"Evento: %s\nData: %s\nLuogo: %s\nDescrizione: %s",
			event.getTitle(),
			event.getDate(),
			event.getPlace(),
			event.getDescription()
		);
	}
}