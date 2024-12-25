package com.pareidolia.service.admin;


import com.pareidolia.dto.EventDTO;
import com.pareidolia.dto.PromoterDTO;
import com.pareidolia.entity.Account;
import com.pareidolia.entity.Event;
import com.pareidolia.entity.PromoterInfo;
import com.pareidolia.mapper.EventMapper;
import com.pareidolia.mapper.EventPromoterAssociationMapper;
import com.pareidolia.repository.EventPromoterAssociationRepository;
import com.pareidolia.repository.EventRepository;
import com.pareidolia.validator.EventValidator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class AdminEventService {
	private final EventValidator eventValidator;
	private final EventRepository eventRepository;
	private final EventPromoterAssociationRepository eventPromoterAssociationRepository;

	//questo metodo viene chiamato da publish, unico modo per creare un evento è quindi crearne una bozza prima
	public EventDTO create(EventDTO eventDTO) {
		if (eventDTO.getId() == null) {
			throw new IllegalArgumentException("Invalid Event ID");
		}
		eventValidator.validateEventDate(eventDTO.getDate(), eventDTO.getTime());
		Event event = EventMapper.dtoToEntity(eventDTO);

		Event savedEvent = eventRepository.save(event);

		EventMapper.createPromoterAssociations(savedEvent, eventDTO.getPromoters(), eventPromoterAssociationRepository);

		List<Pair<Account, PromoterInfo>> promoters = eventPromoterAssociationRepository.findPromotersByIdEvent(savedEvent.getId());

		return EventMapper.entityToDTO(savedEvent, promoters);
	}

	public EventDTO update(EventDTO eventDTO) {
		if (eventDTO.getId() == null) {
			throw new IllegalArgumentException("Invalid Event ID");
		}
		Event event = eventRepository.findById(eventDTO.getId())
			.orElseThrow(() -> new IllegalArgumentException("Event not found"));
		eventValidator.getEventAndValidateUpdate(eventDTO);

		EventMapper.updateEntitiesWithEventDTO(event, eventDTO);
		eventRepository.save(event);

		updateEventPromoters(eventDTO);

		List<Pair<Account, PromoterInfo>> promoters = eventPromoterAssociationRepository.findPromotersByIdEvent(event.getId());
		return EventMapper.entityToDTO(event, promoters);
	}

	private void updateEventPromoters(EventDTO eventDTO) {
		// Recupera le associazioni esistenti per l'evento
		List<Pair<Account, PromoterInfo>> existingPromoters =
			eventPromoterAssociationRepository.findPromotersByIdEvent(eventDTO.getId());

		// Map degli idPromoter esistenti per confronto
		Set<Long> existingPromoterIds = existingPromoters.stream()
			.map(pair -> pair.getFirst().getId())  // Ottiene l'id dell'Account (che coincide con idPromoter)
			.collect(Collectors.toSet());

		// Set degli idPromoter forniti dal DTO
		Set<Long> newPromoterIds = eventDTO.getPromoters().stream()
			.map(PromoterDTO::getId)
			.collect(Collectors.toSet());

		// Rimuovi le associazioni per i promoter non più presenti nel DTO
		existingPromoterIds.stream()
			.filter(id -> !newPromoterIds.contains(id))
			.forEach(id -> eventPromoterAssociationRepository.deleteByIdEventAndIdPromoter(eventDTO.getId(), id));

		// Aggiungi o aggiorna le associazioni per i nuovi promoter
		eventDTO.getPromoters()
			.stream()
			.filter(promoterDTO -> !existingPromoterIds.contains(promoterDTO.getId()))
			.forEach(promoterDTO -> {
				// Aggiungi una nuova associazione
				eventPromoterAssociationRepository.save(
					EventPromoterAssociationMapper.promoterDTOToEntity(promoterDTO, eventDTO.getId())
				);
			});
	}

	public void delete(Long id) {
		if (id == null) {
			throw new IllegalArgumentException("Invalid Event ID");
		}
		eventRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Event not found"));

		eventRepository.findById(id);
	}
}
