package com.pareidolia.service.admin;

import com.pareidolia.dto.EventDTO;
import com.pareidolia.dto.EventDraftDTO;
import com.pareidolia.dto.PromoterDTO;
import com.pareidolia.entity.*;
import com.pareidolia.mapper.EventDraftMapper;
import com.pareidolia.mapper.EventDraftPromoterAssociationMapper;
import com.pareidolia.repository.EventDraftPromoterAssociationRepository;
import com.pareidolia.repository.EventDraftRepository;
import com.pareidolia.repository.EventPromoterAssociationRepository;
import com.pareidolia.validator.EventDraftValidator;
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
public class AdminEventDraftService {

	private final AdminEventService eventService;
	private final AdminService adminService;
	private final EventDraftValidator eventDraftValidator;
	private final EventDraftRepository eventDraftRepository;
	private final EventPromoterAssociationRepository eventPromoterAssociationRepository;
	private final EventDraftPromoterAssociationRepository eventDraftPromoterAssociationRepository;

	public EventDraftDTO getEventDraft(Long id) {
		if (adminService.getData().getId() == null) {
			throw new IllegalArgumentException("Invalid ID");
		}
		EventDraft eventDraft = eventDraftRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid EventDraft ID"));

		List<Pair<Account, PromoterInfo>> promoters =
			eventDraftPromoterAssociationRepository.findPromotersByIdEventDraft(id);

		return EventDraftMapper.entityToDTO(eventDraft, promoters);
	}

	public Page<EventDraftDTO> getEventDrafts(Integer page, Integer size) {
		if (adminService.getData().getId() == null) {
			throw new IllegalArgumentException("Invalid ID");
		}
		return eventDraftRepository.findAll(
			PageRequest.of(Math.max(0, Optional.ofNullable(page).orElse(0)), Math.max(10, Optional.ofNullable(size).orElse(10)), Sort.by(Sort.Order.asc("id")))
		).map(eventDraft -> {
			// Per ogni bozza di evento, recupera i promoter associati
			List<Pair<Account, PromoterInfo>> promoters = eventDraftPromoterAssociationRepository.findPromotersByIdEventDraft(eventDraft.getId());
			// Converte l'EventDraft in EventDraftDTO
			return EventDraftMapper.entityToDTO(eventDraft, promoters);
		});
	}

	private List<Pair<Account, PromoterInfo>> findPromotersByEventId(Long id) {
		if (adminService.getData().getId() == null) {
			throw new IllegalArgumentException("Invalid ID");
		}
		return eventDraftPromoterAssociationRepository.findPromotersByIdEventDraft(id);
	}

	public Page<EventDraftDTO> getPromoterEventDrafts(Long idPromoter, Integer page, Integer size) {
		Page<EventDraft> eventDrafts = eventDraftRepository.findAllByPromoterId(idPromoter,
			PageRequest.of(Math.max(0, Optional.ofNullable(page).orElse(0)), Math.max(10, Optional.ofNullable(size).orElse(10)), Sort.by(Sort.Order.asc("e.id")))
		);
		return eventDrafts.map(eventDraft -> EventDraftMapper.entityToDTO(eventDraft, findPromotersByEventId(eventDraft.getId())));
	}


	public EventDraftDTO create(EventDraftDTO eventDraftDTO, Long accountId) {
		eventDraftValidator.createEventDraftValidatorWithPromoter(eventDraftDTO, accountId);

		EventDraft eventDraft = EventDraftMapper.dtoToEntity(eventDraftDTO);

		EventDraft savedEventDraft = eventDraftRepository.save(eventDraft);

		// Crea l'associazione tra l'evento e il promoter selezionato dall'admin
		EventDraftPromoterAssociation association = new EventDraftPromoterAssociation();
		association.setIdEventDraft(savedEventDraft.getId());
		association.setIdPromoter(accountId);  // Usa l'accountId selezionato
		eventDraftPromoterAssociationRepository.save(association);

		// Recupera i promoter associati all'evento
		List<Pair<Account, PromoterInfo>> promoters =
			eventDraftPromoterAssociationRepository.findPromotersByIdEventDraft(savedEventDraft.getId());

		return EventDraftMapper.entityToDTO(savedEventDraft, promoters);
	}

	public void delete(Long id) {
		if(adminService.getData().getId() == null) {
			throw new IllegalArgumentException("Invalid ID");
		}
		if (id == null) {
			throw new IllegalArgumentException("Invalid ID");
		}
		EventDraft eventDraft = eventDraftRepository.findById(id)
			.orElseThrow(() -> new IllegalArgumentException("EventDraft not found"));
		eventDraftRepository.delete(eventDraft);
	}

	public EventDraftDTO update(EventDraftDTO eventDraftDTO) {
		if(adminService.getData().getId() == null) {
			throw new IllegalArgumentException("Invalid ID");
		}
		if (eventDraftDTO.getId() == null) {
			throw new IllegalArgumentException("Invalid EventDraft ID");
		}

		EventDraft eventDraft = eventDraftValidator.getEventDraftAndValidateUpdate(eventDraftDTO);

		EventDraftMapper.updateEntitiesWithPromoterDTO(eventDraft, eventDraftDTO);

		eventDraft = eventDraftRepository.save(eventDraft);

		updateEventDraftPromoters(eventDraftDTO); //tolto: , eventDraft.getId() come parametro

		List<Pair<Account, PromoterInfo>> promoters =
			eventDraftPromoterAssociationRepository.findPromotersByIdEventDraft(eventDraft.getId());

		return EventDraftMapper.entityToDTO(eventDraft, promoters);
	}

	private void updateEventDraftPromoters(EventDraftDTO eventDraftDTO) {
		// Recupera le associazioni esistenti per l'evento
		List<Pair<Account, PromoterInfo>> existingPromoters =
			eventDraftPromoterAssociationRepository.findPromotersByIdEventDraft(eventDraftDTO.getId());

		// Map degli idPromoter esistenti per confronto
		Set<Long> existingPromoterIds = existingPromoters.stream()
			.map(pair -> pair.getFirst().getId())  // Ottiene l'id dell'Account (che coincide con idPromoter)
			.collect(Collectors.toSet());

		// Set degli idPromoter forniti dal DTO
		Set<Long> newPromoterIds = eventDraftDTO.getPromoters().stream()
			.map(PromoterDTO::getId)
			.collect(Collectors.toSet());

		// Rimuovi le associazioni per i promoter non più presenti nel DTO
		existingPromoterIds.stream()
			.filter(id -> !newPromoterIds.contains(id))
			.forEach(id -> eventPromoterAssociationRepository.deleteByIdEventAndIdPromoter(eventDraftDTO.getId(), id));

		// Aggiungi o aggiorna le associazioni per i nuovi promoter
		eventDraftDTO.getPromoters()
			.stream()
			.filter(promoterDTO -> !existingPromoterIds.contains(promoterDTO.getId()))
			.forEach(promoterDTO -> {
				// Aggiungi una nuova associazione
				eventDraftPromoterAssociationRepository.save(
					EventDraftPromoterAssociationMapper.promoterDTOToEntity(promoterDTO, eventDraftDTO.getId())
				);
		});
	}

	public EventDTO publish(Long eventDraftId) {
		EventDraft eventDraft = eventDraftRepository.findById(eventDraftId)
			.orElseThrow(() -> new IllegalArgumentException("EventDraft not found"));

		if (eventDraft.getIdEvent() != null) {
			throw new IllegalStateException("EventDraft has already been published.");
		}

		List<Pair<Account, PromoterInfo>> promoters = eventDraftPromoterAssociationRepository.findPromotersByIdEventDraft(eventDraftId);

		EventDTO eventDTO = eventService.create(EventDraftMapper.eventDraftToEventDTO(eventDraft, promoters));

		eventDraft.setIdEvent(eventDTO.getId());
		eventDraftRepository.save(eventDraft);

		return eventDTO;
	}
}
