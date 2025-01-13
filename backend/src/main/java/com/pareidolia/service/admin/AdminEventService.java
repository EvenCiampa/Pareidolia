package com.pareidolia.service.admin;

import com.pareidolia.dto.EventDTO;
import com.pareidolia.dto.PromoterDTO;
import com.pareidolia.entity.Account;
import com.pareidolia.entity.Event;
import com.pareidolia.entity.PromoterInfo;
import com.pareidolia.mapper.EventMapper;
import com.pareidolia.mapper.PublishedEventMapper;
import com.pareidolia.mapper.EventPromoterAssociationMapper;
import com.pareidolia.repository.EventPromoterAssociationRepository;
import com.pareidolia.repository.EventRepository;
import com.pareidolia.state.EventStateHandler;
import com.pareidolia.validator.EventDraftValidator;
import com.pareidolia.validator.EventValidator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class AdminEventService {
	private final EventValidator eventValidator;
	private final EventRepository eventRepository;
	private final EventDraftValidator eventDraftValidator;
	private final EventPromoterAssociationRepository eventPromoterAssociationRepository;

	public EventDTO getEvent(Long id) {
		Event eventDraft = eventRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid Event ID"));

		List<Pair<Account, PromoterInfo>> promoters =
			eventPromoterAssociationRepository.findPromotersByIdEvent(id);

		return PublishedEventMapper.entityToDTO(eventDraft, promoters);
	}

	public Page<EventDTO> getEvents(Integer page, Integer size, Event.EventState state) {
		return eventRepository.findAllByState(
			state,
			PageRequest.of(Math.max(0, Optional.ofNullable(page).orElse(0)), Math.max(10, Optional.ofNullable(size).orElse(10)), Sort.by(Sort.Order.asc("id")))
		).map(eventDraft -> {
			// Per ogni bozza di evento, recupera i promoter associati
			List<Pair<Account, PromoterInfo>> promoters = eventPromoterAssociationRepository.findPromotersByIdEvent(eventDraft.getId());
			// Converte l'EventDraft in EventDraftDTO
			return PublishedEventMapper.entityToDTO(eventDraft, promoters);
		});
	}

	public Page<EventDTO> getPromoterEvents(Long idPromoter, Integer page, Integer size, Event.EventState state) {
		Page<Event> eventDrafts = eventRepository.findAllByStateAndPromoterId(state, idPromoter,
			PageRequest.of(Math.max(0, Optional.ofNullable(page).orElse(0)), Math.max(10, Optional.ofNullable(size).orElse(10)), Sort.by(Sort.Order.asc("e.id")))
		);
		return eventDrafts.map(eventDraft -> {
			// Per ogni bozza di evento, recupera i promoter associati
			List<Pair<Account, PromoterInfo>> promoters = eventPromoterAssociationRepository.findPromotersByIdEvent(eventDraft.getId());
			// Converte l'EventDraft in EventDraftDTO
			return PublishedEventMapper.entityToDTO(eventDraft, promoters);
		});
	}

	public EventDTO create(EventDTO eventDraftDTO) {
		eventDraftValidator.createEventDraftValidatorWithPromoter(eventDraftDTO);

		Event eventDraft = EventMapper.dtoToEntity(eventDraftDTO);

		Event savedEventDraft = eventRepository.save(eventDraft);

		// Crea l'associazione tra l'evento e i promoter selezionati dall'admin
		EventMapper.createPromoterAssociations(savedEventDraft, eventDraftDTO.getPromoters(), eventPromoterAssociationRepository);

		// Recupera i promoter associati all'evento
		List<Pair<Account, PromoterInfo>> promoters =
			eventPromoterAssociationRepository.findPromotersByIdEvent(savedEventDraft.getId());

		return EventMapper.entityToDTO(savedEventDraft, promoters);
	}

	public EventDTO update(EventDTO eventDTO) {
		if (eventDTO.getId() == null) {
			throw new IllegalArgumentException("Invalid Event ID");
		}
		Event event = eventRepository.findById(eventDTO.getId())
			.orElseThrow(() -> new IllegalArgumentException("Event not found"));
		if (!EventStateHandler.canEdit(event)) {
			throw new IllegalArgumentException("Invalid Event state: " + event.getState());
		}

		eventValidator.getEventAndValidateUpdate(eventDTO);

		EventMapper.updateEntitiesWithEventDTO(event, eventDTO);
		eventRepository.save(event);

		updateEventPromoters(eventDTO.getId(), eventDTO.getPromoters());

		List<Pair<Account, PromoterInfo>> promoters = eventPromoterAssociationRepository.findPromotersByIdEvent(event.getId());
		return EventMapper.entityToDTO(event, promoters);
	}

	public void updateEventPromoters(Long idEvent, List<PromoterDTO> promoters) {
		// Recupera le associazioni esistenti per l'evento
		List<Pair<Account, PromoterInfo>> existingPromoters =
			eventPromoterAssociationRepository.findPromotersByIdEvent(idEvent);

		// Map degli idPromoter esistenti per confronto
		Set<Long> existingPromoterIds = existingPromoters.stream()
			.map(pair -> pair.getFirst().getId())  // Ottiene l'id dell'Account (che coincide con idPromoter)
			.collect(Collectors.toSet());

		// Set degli idPromoter forniti dal DTO
		Set<Long> newPromoterIds = promoters.stream()
			.map(PromoterDTO::getId)
			.collect(Collectors.toSet());

		// Rimuovi le associazioni per i promoter non più presenti nel DTO
		existingPromoterIds.stream()
			.filter(id -> !newPromoterIds.contains(id))
			.forEach(id -> eventPromoterAssociationRepository.deleteByIdEventAndIdPromoter(idEvent, id));

		// Aggiungi o aggiorna le associazioni per i nuovi promoter
		promoters
			.stream()
			.filter(promoterDTO -> !existingPromoterIds.contains(promoterDTO.getId()))
			.forEach(promoterDTO -> {
				// Aggiungi una nuova associazione
				eventPromoterAssociationRepository.save(
					EventPromoterAssociationMapper.promoterDTOToEntity(promoterDTO, idEvent)
				);
			});
	}

	public void delete(Long id) {
		if (id == null) {
			throw new IllegalArgumentException("Invalid Event ID");
		}
		eventRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Event not found"));

		eventRepository.deleteById(id);
	}

	public EventDTO moveToState(Long id, Event.EventState state) {
		Event event = eventRepository.findById(id)
			.orElseThrow(() -> new IllegalArgumentException("Invalid Event ID"));

		EventStateHandler.moveTo(event, state);
		Event eventDraft = eventRepository.save(event);

		List<Pair<Account, PromoterInfo>> promoters = eventPromoterAssociationRepository.findPromotersByIdEvent(eventDraft.getId());
		return EventMapper.entityToDTO(eventDraft, promoters);
	}
}
