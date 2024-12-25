package com.pareidolia.service.promoter;

import com.pareidolia.dto.EventDraftDTO;
import com.pareidolia.dto.PromoterDTO;
import com.pareidolia.entity.Account;
import com.pareidolia.entity.EventDraft;
import com.pareidolia.entity.EventDraftPromoterAssociation;
import com.pareidolia.entity.PromoterInfo;
import com.pareidolia.mapper.AccountMapper;
import com.pareidolia.mapper.EventDraftMapper;
import com.pareidolia.repository.*;
import com.pareidolia.validator.EventDraftValidator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class PromoterEventDraftService {

	private final PromoterService promoterService;
	private final EventDraftValidator eventDraftValidator;
	private final EventDraftRepository eventDraftRepository;
	private final EventDraftPromoterAssociationRepository eventDraftPromoterAssociationRepository;

	public EventDraftDTO getEventDraft(Long id) {
		Long promoterId = promoterService.getData().getId();
		EventDraft eventDraft = eventDraftRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid EventDraft ID"));

		// Controlla se il promoter autenticato è tra i promoter dell'evento
		eventDraftPromoterAssociationRepository.findByIdEventDraftAndIdPromoter(id, promoterId)
			.orElseThrow(() -> new IllegalArgumentException("Invalid EventDraft ID"));

		List<Pair<Account, PromoterInfo>> promoters =
			eventDraftPromoterAssociationRepository.findPromotersByIdEventDraft(id);

		return EventDraftMapper.entityToDTO(eventDraft, promoters);
	}

	public EventDraftDTO create(EventDraftDTO eventDraftDTO) {
		// Validazione dei campi tramite validator
		eventDraftValidator.createEventDraftValidator(eventDraftDTO);
		// Mappare il DTO in entità
		EventDraft eventDraft = EventDraftMapper.dtoToEntity(eventDraftDTO);
		// Recupera l'ID del promoter corrente (colui che sta creando l'evento)
		Long currentPromoterId = promoterService.getData().getId();

		// Salva l'evento
		EventDraft savedEventDraft = eventDraftRepository.save(eventDraft);

		// Crea l'associazione tra l'evento e il promotore che lo ha creato
		EventDraftPromoterAssociation association = new EventDraftPromoterAssociation();
		association.setIdEventDraft(savedEventDraft.getId());
		association.setIdPromoter(currentPromoterId);
		eventDraftPromoterAssociationRepository.save(association);

		List<Pair<Account, PromoterInfo>> promoters =
			eventDraftPromoterAssociationRepository.findPromotersByIdEventDraft(savedEventDraft.getId());

		// Restituisci il DTO dell'evento salvato
		return EventDraftMapper.entityToDTO(savedEventDraft, promoters);
	}

	public EventDraftDTO addPromoterToEventDraft(Long eventDraftId, Long accountId) {
		// Chiamata al validator
		eventDraftValidator.validateAddPromoterToEventDraft(eventDraftId, accountId);

		// Dopo la validazione, recuperiamo l'evento e l'account
		EventDraft eventDraft = eventDraftRepository.findById(eventDraftId)
			.orElseThrow(() -> new IllegalArgumentException("EventDraft not found"));

		// Crea la nuova associazione tra l'evento e l'account promotore
		EventDraftPromoterAssociation newAssociation = new EventDraftPromoterAssociation();
		newAssociation.setIdEventDraft(eventDraftId);
		newAssociation.setIdPromoter(accountId);
		eventDraftPromoterAssociationRepository.save(newAssociation);

		List<Pair<Account, PromoterInfo>> promoters =
			eventDraftPromoterAssociationRepository.findPromotersByIdEventDraft(eventDraft.getId());

		return EventDraftMapper.entityToDTO(eventDraft, promoters);
	}

	public EventDraftDTO update(EventDraftDTO eventDraftDTO) {
		Long promoterId = promoterService.getData().getId();
		if (eventDraftDTO.getId() == null) {
			throw new IllegalArgumentException("Invalid EventDraft ID");
		}

		// Controlla se il promoter autenticato è tra i promoter dell'evento
		eventDraftPromoterAssociationRepository.findByIdEventDraftAndIdPromoter(eventDraftDTO.getId(), promoterId)
			.orElseThrow(() -> new IllegalArgumentException("Invalid EventDraft ID"));

		EventDraft eventDraft = eventDraftValidator.getEventDraftAndValidateUpdate(eventDraftDTO);

		EventDraftMapper.updateEntitiesWithPromoterDTO(eventDraft, eventDraftDTO);

		eventDraft = eventDraftRepository.save(eventDraft);

		List<Pair<Account, PromoterInfo>> promoters =
			eventDraftPromoterAssociationRepository.findPromotersByIdEventDraft(eventDraft.getId());

		return EventDraftMapper.entityToDTO(eventDraft, promoters);

	}
}
