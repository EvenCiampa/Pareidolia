package com.pareidolia.service.consumer;

import com.pareidolia.dto.ConsumerDTO;
import com.pareidolia.dto.EventDTO;
import com.pareidolia.entity.Account;
import com.pareidolia.entity.Event;
import com.pareidolia.entity.PromoterInfo;
import com.pareidolia.mapper.EventMapper;
import com.pareidolia.repository.BookingRepository;
import com.pareidolia.repository.EventPromoterAssociationRepository;
import com.pareidolia.repository.EventRepository;
import com.pareidolia.repository.model.EventWithInfoForAccount;
import com.pareidolia.state.PublishedState;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class ConsumerEventService {
	private final EventRepository eventRepository;
	private final ConsumerService consumerService;
	private final BookingRepository bookingRepository;
	private final EventPromoterAssociationRepository eventPromoterAssociationRepository;

	/**
	 * Recupera un evento specifico per un consumatore basandosi sull'ID dell'evento, verificando che l'evento sia pubblicato.
	 * Include dettagli se il consumatore ha prenotato l'evento e il numero attuale di partecipanti.
	 * @return EventDTO Il DTO dell'evento, includendo informazioni sulla prenotazione del consumatore e dettagli dei promotori.
	 */
	public EventDTO getEvent(Long id) {
		Event event = eventRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid Event ID"));

		if (!Objects.equals(event.getState().getStateName(), PublishedState.name)) {
			throw new IllegalArgumentException("Event not found");
		}

		Long currentParticipants = bookingRepository.countByIdEvent(id);
		boolean booked = bookingRepository.findByIdEventAndIdAccount(id, consumerService.getData().getId()).isPresent();

		List<Pair<Account, PromoterInfo>> promoters =
			eventPromoterAssociationRepository.findPromotersByIdEvent(id);

		return EventMapper.entityToDTO(event, booked, currentParticipants, promoters);
	}

	/**
	 * Ottiene una pagina di eventi pubblicati associati al consumatore autenticato, ordinati per ID decrescente.
	 * Includono dettagli sulle prenotazioni e sui partecipanti attuali.
	 * @return Page<EventDTO> Una pagina di eventi sotto forma di DTO, che mostra anche se il consumatore ha prenotato l'evento e i dettagli dei promotori.
	 */
	public Page<EventDTO> getEvents(Integer page, Integer size) {
		ConsumerDTO consumerDTO = consumerService.getData();
		Page<EventWithInfoForAccount> eventPage = eventRepository.findAllByAccountIdAndState(
			consumerDTO.getId(), PublishedState.name,
			PageRequest.of(Math.max(0, Optional.ofNullable(page).orElse(0)),
				Math.max(10, Optional.ofNullable(size).orElse(10)),
				Sort.by(Sort.Order.desc("id"))));
		return eventPage.map(event -> {
			// Per ogni bozza di evento, recupera i promoter associati
			List<Pair<Account, PromoterInfo>> promoters = eventPromoterAssociationRepository.findPromotersByIdEvent(event.getEvent().getId());
			// Converte l'EventDraft in EventDraftDTO
			return EventMapper.entityToDTO(event.getEvent(), event.getBooked(), event.getCurrentParticipants(), promoters);
		});
	}
}
