package com.pareidolia.validator;

import com.pareidolia.entity.Account;
import com.pareidolia.entity.Event;
import com.pareidolia.repository.BookingRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Transactional
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class BookingValidator {

	private final BookingRepository bookingRepository;

	/**
	 * Valida la possibilità di creare una nuova prenotazione per un evento da parte di un account.
	 * Verifica che non esista già una prenotazione per l'evento da parte dell'account e che l'evento non sia già completamente prenotato.
	 * @param account L'account che intende effettuare la prenotazione.
	 * @param event L'evento per cui si intende prenotare.
	 */
	public void createBookingValidator(Account account, Event event) {
		if (bookingRepository.findByIdEventAndIdAccount(event.getId(), account.getId()).isPresent()) {
			throw new IllegalArgumentException("Booking for this event already exists");
		}

		Long bookingCount = bookingRepository.countByIdEvent(event.getId());
		if (bookingCount >= event.getMaxNumberOfParticipants()) {
			throw new IllegalArgumentException("Fully booked event");
		}
	}
}