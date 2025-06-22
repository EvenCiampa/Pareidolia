package com.pareidolia.service.promoter;

import com.pareidolia.dto.BookingDTO;
import com.pareidolia.dto.PromoterDTO;
import com.pareidolia.entity.Account;
import com.pareidolia.entity.Booking;
import com.pareidolia.entity.Event;
import com.pareidolia.entity.PromoterInfo;
import com.pareidolia.mapper.BookingMapper;
import com.pareidolia.repository.AccountRepository;
import com.pareidolia.repository.BookingRepository;
import com.pareidolia.repository.EventPromoterAssociationRepository;
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
public class PromoterBookingService {

	private final PublicService publicService;
	private final EventRepository eventRepository;
	private final PromoterService promoterService;
	private final AccountRepository accountRepository;
	private final BookingRepository bookingRepository;
	private final EventPromoterAssociationRepository eventPromoterAssociationRepository;

	public BookingDTO getBooking(Long id) {
		PromoterDTO promoterDTO = promoterService.getData();
		Booking booking = bookingRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid booking ID"));
		if (eventPromoterAssociationRepository.findByIdEventAndIdPromoter(booking.getIdEvent(), promoterDTO.getId()).isEmpty()) {
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

	public Page<BookingDTO> getBookings(Long idEvent, Integer page, Integer size) {
		PromoterDTO promoterDTO = promoterService.getData();
		Event event = eventRepository.findById(idEvent).orElseThrow(() -> new IllegalArgumentException("Invalid Event ID"));
		Long currentParticipants = bookingRepository.countByIdEvent(event.getId());
		if (eventPromoterAssociationRepository.findByIdEventAndIdPromoter(idEvent, promoterDTO.getId()).isEmpty()) {
			throw new IllegalArgumentException("Invalid Event ID");
		}

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

	public void delete(Long id) {
		PromoterDTO promoterDTO = promoterService.getData();
		Booking booking = bookingRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid booking ID"));
		if (eventPromoterAssociationRepository.findByIdEventAndIdPromoter(booking.getIdEvent(), promoterDTO.getId()).isEmpty()) {
			throw new IllegalArgumentException("Invalid booking ID");
		}

		bookingRepository.delete(booking);
	}
}