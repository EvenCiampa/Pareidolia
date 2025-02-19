package com.pareidolia.service.consumer;

import com.pareidolia.dto.BookingDTO;
import com.pareidolia.dto.ConsumerDTO;
import com.pareidolia.entity.Account;
import com.pareidolia.entity.Booking;
import com.pareidolia.entity.Event;
import com.pareidolia.entity.PromoterInfo;
import com.pareidolia.mapper.BookingMapper;
import com.pareidolia.repository.AccountRepository;
import com.pareidolia.repository.BookingRepository;
import com.pareidolia.repository.EventRepository;
import com.pareidolia.service.generic.PublicService;
import com.pareidolia.validator.BookingValidator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class ConsumerBookingService {

	private final PublicService publicService;
	private final ConsumerService consumerService;
	private final EventRepository eventRepository;
	private final BookingValidator bookingValidator;
	private final BookingRepository bookingRepository;
	private final AccountRepository accountRepository;

	/**
	 * Recupera una specifica prenotazione basata sull'ID fornito, verificando che la prenotazione appartenga al consumatore autenticato.
	 */
	public BookingDTO getBooking(Long id) {
		Booking booking = bookingRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid booking ID"));
		if (!Objects.equals(booking.getIdAccount(), consumerService.getData().getId())) {
			throw new IllegalArgumentException("Invalid booking ID");
		}

		Account account = accountRepository.findById(booking.getIdAccount())
			.orElseThrow(() -> new IllegalArgumentException("Account not found"));
		Event event = eventRepository.findById(booking.getIdEvent())
			.orElseThrow(() -> new IllegalArgumentException("Event not found"));
		Long currentParticipants = bookingRepository.countByIdEvent(event.getId());

		List<Pair<Account, PromoterInfo>> promoterPairs = publicService.findPromotersByEventId(booking.getIdEvent());
		return BookingMapper.entityToDTO(booking, account, event, currentParticipants, promoterPairs);
	}

	/**
	 * Recupera una pagina di tutte le prenotazioni esistenti, filtrate per pagina e dimensione.
	 */
	public Page<BookingDTO> getBookings(Integer page, Integer size) {
		return bookingRepository.findAll(
			PageRequest.of(Math.max(0, Optional.ofNullable(page).orElse(0)), Math.max(20, Optional.ofNullable(size).orElse(20)))
		).map(booking -> {
			Account account = accountRepository.findById(booking.getIdAccount()).orElseThrow(() -> new IllegalArgumentException("Invalid Account"));
			Event event = eventRepository.findById(booking.getIdEvent()).orElseThrow(() -> new IllegalArgumentException("Invalid Event ID"));
			Long currentParticipants = bookingRepository.countByIdEvent(event.getId());
			List<Pair<Account, PromoterInfo>> promoters = publicService.findPromotersByEventId(event.getId());
			return BookingMapper.entityToDTO(booking, account, event, currentParticipants, promoters);
		});
	}

	/**
	 * Crea una nuova prenotazione per un evento, validando i dettagli dell'account e dell'evento.
	 * @param id L'ID dell'evento per cui effettuare la prenotazione.
	 * @return BookingDTO Il DTO della nuova prenotazione creata.
	 * @throws IllegalArgumentException Se l'account o l'ID dell'evento non sono validi.
	 */
	public BookingDTO create(Long id) {
		ConsumerDTO consumerDTO = consumerService.getData();
		Account account = accountRepository.findById(consumerDTO.getId()).orElseThrow(() -> new IllegalArgumentException("Invalid Account"));
		Event event = eventRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid Event ID"));

		bookingValidator.createBookingValidator(account, event);

		Booking booking = new Booking();
		booking.setIdAccount(account.getId());
		booking.setIdEvent(event.getId());
		booking = bookingRepository.save(booking);

		Long currentParticipants = bookingRepository.countByIdEvent(event.getId());

		List<Pair<Account, PromoterInfo>> promoters = publicService.findPromotersByEventId(id);
		return BookingMapper.entityToDTO(booking, account, event, currentParticipants, promoters);
	}

	/**
	 * Elimina una prenotazione specifica, verificando che la prenotazione appartenga al consumatore autenticato.
	 * @param id L'ID della prenotazione da eliminare.
	 * @throws IllegalArgumentException Se l'ID della prenotazione non è valido o se l'account non corrisponde.
	 */
	public void delete(Long id) {
		ConsumerDTO consumerDTO = consumerService.getData();
		Account account = accountRepository.findById(consumerDTO.getId()).orElseThrow(() -> new IllegalArgumentException("Invalid Account"));

		Booking booking = bookingRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid booking ID"));
		if (!Objects.equals(booking.getIdAccount(), account.getId())) {
			throw new IllegalArgumentException("Invalid booking ID");
		}

		bookingRepository.deleteById(id);
	}

	/**
	 * Elimina la prenotazione di un consumatore per un evento specifico, verificando che la prenotazione esista.
	 * @param eventId L'ID dell'evento da cui eliminare la prenotazione.
	 * @throws IllegalArgumentException Se l'account o l'ID dell'evento non sono validi o se non esiste una prenotazione corrispondente.
	 */
	public void deleteFromEvent(Long eventId) {
		ConsumerDTO consumerDTO = consumerService.getData();
		Account account = accountRepository.findById(consumerDTO.getId()).orElseThrow(() -> new IllegalArgumentException("Invalid Account"));
		Event event = eventRepository.findById(eventId).orElseThrow(() -> new IllegalArgumentException("Invalid Event ID"));

		Booking booking = bookingRepository.findByIdEventAndIdAccount(event.getId(), account.getId())
			.orElseThrow(() -> new IllegalArgumentException("Invalid booking"));

		bookingRepository.delete(booking);
	}
}
