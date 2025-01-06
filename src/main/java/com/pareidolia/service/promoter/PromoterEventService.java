package com.pareidolia.service.promoter;

import com.pareidolia.dto.EventDTO;
import com.pareidolia.entity.Account;
import com.pareidolia.entity.Event;
import com.pareidolia.entity.EventPromoterAssociation;
import com.pareidolia.entity.PromoterInfo;
import com.pareidolia.mapper.EventMapper;
import com.pareidolia.repository.EventPromoterAssociationRepository;
import com.pareidolia.repository.EventRepository;
import com.pareidolia.state.EventStateHandler;
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
public class PromoterEventService {

    private final PromoterService promoterService;
    private final EventRepository eventRepository;
    private final EventDraftValidator eventDraftValidator;
    private final EventPromoterAssociationRepository eventPromoterAssociationRepository;

    public EventDTO getEventDraft(Long id) {
        Long promoterId = promoterService.getData().getId();
        Event eventDraft = eventRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid EventDraft ID"));

        // Controlla se il promoter autenticato è tra i promoter dell'evento
        eventPromoterAssociationRepository.findByIdEventAndIdPromoter(id, promoterId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid EventDraft ID"));

        List<Pair<Account, PromoterInfo>> promoters =
                eventPromoterAssociationRepository.findPromotersByIdEvent(id);

        return EventMapper.entityToDTO(eventDraft, promoters);
    }

    public EventDTO create(EventDTO eventDraftDTO) {
        // Validazione dei campi tramite validator
        eventDraftValidator.createEventDraftValidator(eventDraftDTO);
        // Mappare il DTO in entità
        Event eventDraft = EventMapper.dtoToEntity(eventDraftDTO);
        // Recupera l'ID del promoter corrente (colui che sta creando l'evento)
        Long currentPromoterId = promoterService.getData().getId();

        // Salva l'evento
        Event savedEventDraft = eventRepository.save(eventDraft);

        // Crea l'associazione tra l'evento e il promotore che lo ha creato
        EventPromoterAssociation association = new EventPromoterAssociation();
        association.setIdEvent(savedEventDraft.getId());
        association.setIdPromoter(currentPromoterId);
        eventPromoterAssociationRepository.save(association);

        List<Pair<Account, PromoterInfo>> promoters =
                eventPromoterAssociationRepository.findPromotersByIdEvent(savedEventDraft.getId());

        // Restituisci il DTO dell'evento salvato
        return EventMapper.entityToDTO(savedEventDraft, promoters);
    }

    public EventDTO addPromoterToEventDraft(Long eventDraftId, Long accountId) {
        // Chiamata al validator
        eventDraftValidator.validateAddPromoterToEventDraft(eventDraftId, accountId);

        // Dopo la validazione, recuperiamo l'evento e l'account
        Event eventDraft = eventRepository.findById(eventDraftId)
                .orElseThrow(() -> new IllegalArgumentException("EventDraft not found"));

        // Crea la nuova associazione tra l'evento e l'account promotore
        EventPromoterAssociation newAssociation = new EventPromoterAssociation();
        newAssociation.setIdEvent(eventDraftId);
        newAssociation.setIdPromoter(accountId);
        eventPromoterAssociationRepository.save(newAssociation);

        List<Pair<Account, PromoterInfo>> promoters =
                eventPromoterAssociationRepository.findPromotersByIdEvent(eventDraft.getId());

        return EventMapper.entityToDTO(eventDraft, promoters);
    }

    public EventDTO update(EventDTO eventDraftDTO) {
        Long promoterId = promoterService.getData().getId();
        if (eventDraftDTO.getId() == null) {
            throw new IllegalArgumentException("Invalid EventDraft ID");
        }

        // Controlla se il promoter autenticato è tra i promoter dell'evento
        eventPromoterAssociationRepository.findByIdEventAndIdPromoter(eventDraftDTO.getId(), promoterId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid EventDraft ID"));

        Event eventDraft = eventDraftValidator.getEventDraftAndValidateUpdate(eventDraftDTO);

        EventMapper.updateEntitiesWithEventDTO(eventDraft, eventDraftDTO);

        eventDraft = eventRepository.save(eventDraft);

        List<Pair<Account, PromoterInfo>> promoters =
                eventPromoterAssociationRepository.findPromotersByIdEvent(eventDraft.getId());

        return EventMapper.entityToDTO(eventDraft, promoters);
    }

    public EventDTO submitForReview(Long id) {
        Long promoterId = promoterService.getData().getId();
        Event eventDraft = eventRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));

        // Controlla se il promoter autenticato è tra i promoter dell'evento
        eventPromoterAssociationRepository.findByIdEventAndIdPromoter(eventDraft.getId(), promoterId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid EventDraft ID"));

		EventStateHandler.moveToReview(eventDraft);

        eventDraft = eventRepository.save(eventDraft);

        List<Pair<Account, PromoterInfo>> promoters =
                eventPromoterAssociationRepository.findPromotersByIdEvent(eventDraft.getId());

        return EventMapper.entityToDTO(eventDraft, promoters);
    }
}
