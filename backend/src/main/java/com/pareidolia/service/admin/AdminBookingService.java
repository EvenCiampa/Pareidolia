package com.pareidolia.service.admin;

import com.pareidolia.dto.BookingDTO;
import com.pareidolia.entity.Account;
import com.pareidolia.entity.Booking;
import com.pareidolia.entity.Event;
import com.pareidolia.entity.PromoterInfo;
import com.pareidolia.mapper.BookingMapper;
import com.pareidolia.repository.AccountRepository;
import com.pareidolia.repository.BookingRepository;
import com.pareidolia.repository.EventRepository;
import com.pareidolia.service.generic.PublicService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class AdminBookingService {

	private final PublicService publicService;
	private final EventRepository eventRepository;
	private final AccountRepository accountRepository;
	private final BookingRepository bookingRepository;

	/**
	 * Recupera una specifica prenotazione basata sull'ID fornito, verificando che la prenotazione appartenga al consumatore autenticato.
	 */
	public BookingDTO getBooking(Long id) {
		Booking booking = bookingRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid booking ID"));

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
	public Page<BookingDTO> getBookings(Long idEvent, Integer page, Integer size) {
		Event event = eventRepository.findById(idEvent).orElseThrow(() -> new IllegalArgumentException("Invalid Event ID"));
		Long currentParticipants = bookingRepository.countByIdEvent(event.getId());

		return bookingRepository.findByIdEvent(
			idEvent, PageRequest.of(
				Math.max(0, Optional.ofNullable(page).orElse(0)),
				Math.max(20, Optional.ofNullable(size).orElse(20)))
		).map(booking -> {
			Account account = accountRepository.findById(booking.getIdAccount()).orElseThrow(() -> new IllegalArgumentException("Invalid Account"));
			List<Pair<Account, PromoterInfo>> promoters = publicService.findPromotersByEventId(event.getId());
			return BookingMapper.entityToDTO(booking, account, event, currentParticipants, promoters);
		});
	}

	/**
	 * Elimina una prenotazione, verificando che la prenotazione esista.
	 * @param id della prenotazione.
	 */
	public void delete(Long id) {
		Booking booking = bookingRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid booking ID"));

		bookingRepository.delete(booking);
	}
}