package com.pareidolia.validator;

import com.pareidolia.dto.ReviewDTO;
import com.pareidolia.entity.Event;
import com.pareidolia.repository.EventRepository;
import com.pareidolia.repository.ReviewRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Transactional
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class ReviewValidator {
	private final EventRepository eventRepository;
	private final ReviewRepository reviewRepository;

	/**
	 * Valida che un evento specificato dal suo ID sia concluso prima di permettere l'invio di una recensione.
	 * Questo metodo verifica l'esistenza dell'evento e calcola se l'evento è finito basandosi sulla combinazione
	 * di data, ora e durata dell'evento. È essenziale per garantire che le recensioni vengano inviate solo dopo
	 * il termine dell'evento.
	 *
	 * @param idEvent ID dell'evento da validare.
	 * @throws IllegalArgumentException Se l'ID dell'evento non è valido, l'evento non è trovato, o l'evento non è ancora terminato.
	 */
	public void validateEventIsOver(Long idEvent) {
		if (idEvent == null) {
			throw new IllegalArgumentException("Invalid Event ID");
		}

		// Recupera l'evento e verifica che esista
		Event event = eventRepository.findById(idEvent)
			.orElseThrow(() -> new IllegalArgumentException("Event not found"));

		// Verifica che l'evento sia terminato
		LocalDateTime eventEndTime = event.getDate().atTime(event.getTime()).plus(event.getDuration());
		if (LocalDateTime.now().isBefore(eventEndTime)) {
			throw new IllegalArgumentException("Event has not finished yet.");
		}
	}

	/**
	 * Valida i campi di una nuova recensione ricevuta tramite un DTO. Questa validazione include il controllo
	 * del titolo, della descrizione e del punteggio della recensione. Assicura che il titolo e la descrizione
	 * non siano vuoti o troppo lunghi, e che il punteggio rientri nei limiti stabiliti. Inoltre, verifica che
	 * non esista già una recensione dello stesso consumatore per lo stesso evento, prevenendo duplicazioni.
	 *
	 * @param reviewDTO DTO della recensione da validare.
	 * @throws IllegalArgumentException Se i dati della recensione sono mancanti, se il titolo o la descrizione sono vuoti o troppo lunghi,
	 * o se il punteggio non è compreso tra 1 e 5, o se una recensione da parte dello stesso consumatore per lo stesso evento esiste già.
	 */
	public void validateNewReviewFields(ReviewDTO reviewDTO) {
		if (reviewDTO == null) {
			throw new IllegalArgumentException("Review data is missing.");
		}

		// Validazione del titolo
		if (reviewDTO.getTitle() == null || reviewDTO.getTitle().trim().isEmpty()) {
			throw new IllegalArgumentException("Title must not be empty.");
		}
		if (reviewDTO.getTitle().length() > 150) {
			throw new IllegalArgumentException("Title is too long.");
		}

		// Validazione della descrizione (opzionale)
		if (reviewDTO.getDescription() == null || reviewDTO.getDescription().trim().isEmpty()) {
			throw new IllegalArgumentException("Description must not be empty.");
		}
		if (reviewDTO.getDescription().length() > 2000) {
			throw new IllegalArgumentException("Description is too long.");
		}

		// Validazione del punteggio
		if (reviewDTO.getScore() == null || reviewDTO.getScore() < 1 || reviewDTO.getScore() > 5) {
			throw new IllegalArgumentException("Score must be between 1 and 5.");
		}

		if (reviewRepository.findByIdConsumerAndIdEvent(reviewDTO.getIdConsumer(), reviewDTO.getIdEvent()).isPresent()) {
			throw new IllegalArgumentException("Review already exists.");
		}
	}
}
