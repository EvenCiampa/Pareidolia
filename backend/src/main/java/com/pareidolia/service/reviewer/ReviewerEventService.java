package com.pareidolia.service.reviewer;

import com.pareidolia.dto.EventDTO;
import com.pareidolia.dto.ReviewerDTO;
import com.pareidolia.entity.Account;
import com.pareidolia.entity.Event;
import com.pareidolia.entity.PromoterInfo;
import com.pareidolia.mapper.EventMapper;
import com.pareidolia.repository.BookingRepository;
import com.pareidolia.repository.EventPromoterAssociationRepository;
import com.pareidolia.repository.EventRepository;
import com.pareidolia.repository.PromoterInfoRepository;
import com.pareidolia.repository.model.EventWithInfoForAccount;
import com.pareidolia.service.ImageService;
import com.pareidolia.validator.EventDraftValidator;
import com.pareidolia.validator.EventValidator;
import com.pareidolia.validator.ImageValidator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class ReviewerEventService {
	private final ImageService imageService;
	private final ImageValidator imageValidator;
	private final EventValidator eventValidator;
	private final EventRepository eventRepository;
	private final ReviewerService reviewerService;
	private final BookingRepository bookingRepository;
	private final EventDraftValidator eventDraftValidator;
	private final PromoterInfoRepository promoterInfoRepository;
	private final EventPromoterAssociationRepository eventPromoterAssociationRepository;

	/**
	 * Recupera un evento specifico per un reviewer basandosi sull'ID dell'evento.
	 * @return EventDTO Il DTO dell'evento, includendo informazioni sulla prenotazione e i dettagli dei promotori.
	 */
	public EventDTO getEvent(Long id) {
		Event eventDraft = eventRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid Event ID"));
		Long currentParticipants = bookingRepository.countByIdEvent(id);
		boolean booked = bookingRepository.findByIdEventAndIdAccount(id, reviewerService.getData().getId()).isPresent();

		List<Pair<Account, PromoterInfo>> promoters =
			eventPromoterAssociationRepository.findPromotersByIdEvent(id);

		return EventMapper.entityToDTO(eventDraft, booked, currentParticipants, promoters);
	}

	/**
	 * Ottiene una pagina di eventi basata sullo stato fornito e sulla pagina richiesta, filtrati per l'account del reviewer.
	 * @param state Stato degli eventi da filtrare, se fornito.
	 * @return Page<EventDTO> Una pagina di DTO degli eventi.
	 */
	public Page<EventDTO> getEvents(Integer page, Integer size, String state) {
		ReviewerDTO reviewer = reviewerService.getData();
		Page<EventWithInfoForAccount> eventPage;
		if (state == null) {
			eventPage = eventRepository.findAllByAccountIdWithCount(
				reviewer.getId(),
				PageRequest.of(Math.max(0, Optional.ofNullable(page).orElse(0)),
					Math.max(10, Optional.ofNullable(size).orElse(10)),
					Sort.by(Sort.Order.desc("id"))));
		} else {
			eventPage = eventRepository.findAllByAccountIdAndState(
				reviewer.getId(), state,
				PageRequest.of(Math.max(0, Optional.ofNullable(page).orElse(0)),
					Math.max(10, Optional.ofNullable(size).orElse(10)),
					Sort.by(Sort.Order.desc("id")))
			);
		}
		return eventPage.map(eventDraft -> {
			// Per ogni bozza di evento, recupera i promoter associati
			List<Pair<Account, PromoterInfo>> promoters = eventPromoterAssociationRepository.findPromotersByIdEvent(eventDraft.getEvent().getId());
			// Converte l'EventDraft in EventDraftDTO
			return EventMapper.entityToDTO(eventDraft.getEvent(), eventDraft.getBooked(), eventDraft.getCurrentParticipants(), promoters);
		});
	}

	/**
	 * Recupera una pagina di eventi associati a un promotore specifico e filtrati per stato, se fornito.
	 * @param idPromoter ID del promotore.
	 * @param state Stato degli eventi per il filtraggio.
	 * @return Page<EventDTO> Una pagina di eventi sotto forma di DTO.
	 */
	public Page<EventDTO> getPromoterEvents(Long idPromoter, Integer page, Integer size, String state) {
		ReviewerDTO reviewer = reviewerService.getData();
		Page<EventWithInfoForAccount> eventPage;
		if (state == null) {
			eventPage = eventRepository.findAllByAccountIdAndPromoterId(reviewer.id, idPromoter,
				PageRequest.of(Math.max(0, Optional.ofNullable(page).orElse(0)),
					Math.max(10, Optional.ofNullable(size).orElse(10)),
					Sort.by(Sort.Order.desc("id")))
			);
		} else {
			eventPage = eventRepository.findAllByAccountIdAndStateAndPromoterId(reviewer.id, state, idPromoter,
				PageRequest.of(Math.max(0, Optional.ofNullable(page).orElse(0)),
					Math.max(10, Optional.ofNullable(size).orElse(10)),
					Sort.by(Sort.Order.desc("id")))
			);
		}
		return eventPage.map(eventDraft -> {
			// Per ogni bozza di evento, recupera i promoter associati
			List<Pair<Account, PromoterInfo>> promoters = eventPromoterAssociationRepository.findPromotersByIdEvent(eventDraft.getEvent().getId());
			// Converte l'EventDraft in EventDraftDTO
			return EventMapper.entityToDTO(eventDraft.getEvent(), eventDraft.getBooked(), eventDraft.getCurrentParticipants(), promoters);
		});
	}

	/**
	 * Cambia lo stato di un evento a uno stato precedente.
	 * @param id L'ID dell'evento da modificare.
	 * @return EventDTO Il DTO dell'evento con il nuovo stato.
	 */
	public EventDTO moveBackwards(Long id) {
		Event event = eventRepository.findById(id)
			.orElseThrow(() -> new IllegalArgumentException("Invalid Event ID"));

		event.getState().moveBackwards();
		Event eventDraft = eventRepository.save(event);

		List<Pair<Account, PromoterInfo>> promoters = eventPromoterAssociationRepository.findPromotersByIdEvent(eventDraft.getId());
		boolean booked = bookingRepository.findByIdEventAndIdAccount(id, reviewerService.getData().getId()).isPresent();
		return EventMapper.entityToDTO(eventDraft, booked, bookingRepository.countByIdEvent(id), promoters);
	}

	/**
	 * Cambia lo stato di un evento a uno stato successivo.
	 * @param id L'ID dell'evento da modificare.
	 * @return EventDTO Il DTO dell'evento con il nuovo stato.
	 */
	public EventDTO moveForward(Long id) {
		Event event = eventRepository.findById(id)
			.orElseThrow(() -> new IllegalArgumentException("Invalid Event ID"));

		event.getState().moveForward();
		Event eventDraft = eventRepository.save(event);

		List<Pair<Account, PromoterInfo>> promoters = eventPromoterAssociationRepository.findPromotersByIdEvent(eventDraft.getId());
		boolean booked = bookingRepository.findByIdEventAndIdAccount(id, reviewerService.getData().getId()).isPresent();
		return EventMapper.entityToDTO(eventDraft, booked, bookingRepository.countByIdEvent(id), promoters);
	}

	/**
	 * Aggiorna l'immagine di un evento, validando l'immagine.
	 * @param id ID dell'evento di cui aggiornare l'immagine.
	 * @param file File contenente l'immagine da caricare.
	 * @return EventDTO Rappresentazione DTO dell'evento con l'immagine aggiornata.
	 */
	public EventDTO updateEventImage(Long id, MultipartFile file) {
		imageValidator.validateEventImage(file);

		Event event = eventRepository.findById(id)
			.orElseThrow(() -> new IllegalArgumentException("Event not found"));

		try {
			String filename = imageService.saveImage(file);
			event.setImage(filename);
			event = eventRepository.save(event);

			List<Pair<Account, PromoterInfo>> promoters =
				eventPromoterAssociationRepository.findPromotersByIdEvent(event.getId());
			boolean booked = bookingRepository.findByIdEventAndIdAccount(id, reviewerService.getData().getId()).isPresent();
			return EventMapper.entityToDTO(event, booked, bookingRepository.countByIdEvent(id), promoters);
		} catch (IOException e) {
			throw new RuntimeException("Failed to save image", e);
		}
	}

	/**
	 * Rimuove l'immagine da un evento specifico.
	 * @param id L'ID dell'evento da cui rimuovere l'immagine.
	 * @return EventDTO Il DTO dell'evento senza l'immagine.
	 */
	public EventDTO deleteEventImage(Long id) {
		Event event = eventRepository.findById(id)
			.orElseThrow(() -> new IllegalArgumentException("Event not found"));
		boolean booked = bookingRepository.findByIdEventAndIdAccount(id, reviewerService.getData().getId()).isPresent();

		if (event.getImage() != null) {
			event.setImage(null);
			event = eventRepository.save(event);

			List<Pair<Account, PromoterInfo>> promoters =
				eventPromoterAssociationRepository.findPromotersByIdEvent(event.getId());
			return EventMapper.entityToDTO(event, booked, bookingRepository.countByIdEvent(id), promoters);
		}

		return EventMapper.entityToDTO(event, booked, bookingRepository.countByIdEvent(id),
			eventPromoterAssociationRepository.findPromotersByIdEvent(event.getId()));
	}
}
