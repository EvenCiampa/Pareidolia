package com.pareidolia.service.promoter;

import com.pareidolia.dto.EventDTO;
import com.pareidolia.dto.EventUpdateDTO;
import com.pareidolia.dto.PromoterDTO;
import com.pareidolia.entity.Account;
import com.pareidolia.entity.Event;
import com.pareidolia.entity.EventPromoterAssociation;
import com.pareidolia.entity.PromoterInfo;
import com.pareidolia.mapper.AccountMapper;
import com.pareidolia.mapper.EventMapper;
import com.pareidolia.repository.BookingRepository;
import com.pareidolia.repository.EventPromoterAssociationRepository;
import com.pareidolia.repository.EventRepository;
import com.pareidolia.repository.PromoterInfoRepository;
import com.pareidolia.repository.model.EventWithInfoForAccount;
import com.pareidolia.service.ImageService;
import com.pareidolia.state.EventStateHandler;
import com.pareidolia.validator.EventDraftValidator;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class PromoterEventService {

	private final ImageService imageService;
	private final ImageValidator imageValidator;
	private final PromoterService promoterService;
	private final EventRepository eventRepository;
	private final BookingRepository bookingRepository;
	private final EventDraftValidator eventDraftValidator;
	private final PromoterInfoRepository promoterInfoRepository;
	private final EventPromoterAssociationRepository eventPromoterAssociationRepository;

	public EventDTO getEventDraft(Long id) {
		Long promoterId = promoterService.getData().getId();
		Event eventDraft = eventRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid EventDraft ID"));

		// Controlla se il promoter autenticato è tra i promoter dell'evento
		eventPromoterAssociationRepository.findByIdEventAndIdPromoter(id, promoterId)
			.orElseThrow(() -> new IllegalArgumentException("Invalid EventDraft ID"));

		List<Pair<Account, PromoterInfo>> promoters =
			eventPromoterAssociationRepository.findPromotersByIdEvent(id);
		boolean booked = bookingRepository.findByIdEventAndIdAccount(id, promoterId).isPresent();

		return EventMapper.entityToDTO(eventDraft, booked, bookingRepository.countByIdEvent(id), promoters);
	}

	public Page<EventDTO> getEvents(Integer page, Integer size, Event.EventState state) {
		Long promoterId = promoterService.getData().getId();
		Page<EventWithInfoForAccount> eventPage;
		if (state == null) {
			eventPage = eventRepository.findAllByAccountIdAndPromoterId(promoterId, promoterId,
				PageRequest.of(Math.max(0, Optional.ofNullable(page).orElse(0)),
					Math.max(10, Optional.ofNullable(size).orElse(10)),
					Sort.by(Sort.Order.desc("id"))));
		} else {
			eventPage = eventRepository.findAllByAccountIdAndStateAndPromoterId(promoterId, state, promoterId,
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

	public EventDTO create(EventUpdateDTO eventUpdateDraftDTO) {
		List<PromoterInfo> promoterInfoList = promoterInfoRepository.findAllByEmailIn(eventUpdateDraftDTO.promoterEmails);
		if (eventUpdateDraftDTO.promoterEmails != null && promoterInfoList.size() != eventUpdateDraftDTO.promoterEmails.size()) {
			List<String> invalidEmails = eventUpdateDraftDTO.promoterEmails.stream()
				.filter(email -> promoterInfoList.stream()
					.noneMatch(promoter -> Objects.equals(email, promoter.getAccount().getEmail())))
				.toList();
			throw new IllegalArgumentException("Promoter emails do not exists: " + String.join(", ", invalidEmails));
		}
		List<PromoterDTO> promoterDTOList = promoterInfoList.stream()
			.map(promoter -> AccountMapper.entityToPromoterDTO(promoter.getAccount(), promoter)).toList();

		EventDTO eventDraftDTO = EventMapper.updateDTOtoDTO(eventUpdateDraftDTO, promoterDTOList);

		// Validazione dei campi tramite validator
		eventDraftValidator.createEventDraftValidator(eventDraftDTO);
		// Mappare il DTO in entità
		Event eventDraft = EventMapper.dtoToEntity(eventDraftDTO);
		// Recupera l'ID del promoter corrente (colui che sta creando l'evento)
		Long promoterId = promoterService.getData().getId();

		// Salva l'evento
		eventDraft.setState(Event.EventState.DRAFT);
		Event savedEventDraft = eventRepository.save(eventDraft);

		// Crea l'associazione tra l'evento e il promotore che lo ha creato
		EventPromoterAssociation association = new EventPromoterAssociation();
		association.setIdEvent(savedEventDraft.getId());
		association.setIdPromoter(promoterId);
		eventPromoterAssociationRepository.save(association);

		List<Pair<Account, PromoterInfo>> promoters =
			eventPromoterAssociationRepository.findPromotersByIdEvent(savedEventDraft.getId());

		// Restituisci il DTO dell'evento salvato
		return EventMapper.entityToDTO(savedEventDraft, false, 0L, promoters);
	}

	public EventDTO addPromoterToEventDraft(Long eventDraftId, Long accountId) {
		Long promoterId = promoterService.getData().getId();
		// Chiamata al validator
		eventDraftValidator.validateAddPromoterToEventDraft(eventDraftId, accountId);

		// Dopo la validazione, recuperiamo l'evento e l'account
		Event eventDraft = eventRepository.findById(eventDraftId)
			.orElseThrow(() -> new IllegalArgumentException("EventDraft not found"));

		eventDraftValidator.validateEditable(eventDraft);

		// Crea la nuova associazione tra l'evento e l'account promotore
		EventPromoterAssociation newAssociation = new EventPromoterAssociation();
		newAssociation.setIdEvent(eventDraftId);
		newAssociation.setIdPromoter(accountId);
		eventPromoterAssociationRepository.save(newAssociation);

		List<Pair<Account, PromoterInfo>> promoters =
			eventPromoterAssociationRepository.findPromotersByIdEvent(eventDraft.getId());
		boolean booked = bookingRepository.findByIdEventAndIdAccount(eventDraft.getId(), promoterId).isPresent();

		return EventMapper.entityToDTO(eventDraft, booked, bookingRepository.countByIdEvent(eventDraftId), promoters);
	}

	public EventDTO update(EventUpdateDTO eventUpdateDraftDTO) {
		PromoterDTO promoterData = promoterService.getData();
		Long promoterId = promoterData.getId();

		if (eventUpdateDraftDTO.getId() == null) {
			throw new IllegalArgumentException("Invalid EventDraft ID");
		}

		Event event = eventRepository.findById(eventUpdateDraftDTO.getId())
			.orElseThrow(() -> new IllegalArgumentException("Event not found"));

		// Controlla se il promoter autenticato è tra i promoter dell'evento
		eventPromoterAssociationRepository.findByIdEventAndIdPromoter(eventUpdateDraftDTO.getId(), promoterId)
			.orElseThrow(() -> new IllegalArgumentException("Invalid EventDraft ID"));

		eventDraftValidator.validateEditable(event);

		List<PromoterInfo> promoterInfoList = promoterInfoRepository.findAllByEmailIn(eventUpdateDraftDTO.promoterEmails);
		if (eventUpdateDraftDTO.promoterEmails != null && promoterInfoList.size() != eventUpdateDraftDTO.promoterEmails.size()) {
			List<String> invalidEmails = eventUpdateDraftDTO.promoterEmails.stream()
				.filter(email -> promoterInfoList.stream()
					.noneMatch(promoter -> Objects.equals(email, promoter.getAccount().getEmail())))
				.toList();
			throw new IllegalArgumentException("Promoter emails do not exists: " + String.join(", ", invalidEmails));
		}
		List<PromoterDTO> promoterDTOList = promoterInfoList.stream()
			.map(promoter -> AccountMapper.entityToPromoterDTO(promoter.getAccount(), promoter)).toList();

		EventDTO eventDraftDTO = EventMapper.updateDTOtoDTO(eventUpdateDraftDTO, promoterDTOList);

		if (eventDraftDTO.getPromoters() == null) {
			eventDraftDTO.setPromoters(List.of(promoterData));
		} else if (eventDraftDTO.getPromoters().stream().noneMatch(it -> Objects.equals(it.id, promoterId))) {
			eventDraftDTO.setPromoters(new ArrayList<>(eventDraftDTO.getPromoters()));
			eventDraftDTO.getPromoters().add(promoterData);
		}

		Event eventDraft = eventDraftValidator.getEventDraftAndValidateUpdate(eventDraftDTO);

		EventMapper.updateEntitiesWithEventDTO(eventDraft, eventDraftDTO);

		eventDraft = eventRepository.save(eventDraft);

		List<Pair<Account, PromoterInfo>> promoters =
			eventPromoterAssociationRepository.findPromotersByIdEvent(eventDraft.getId());
		boolean booked = bookingRepository.findByIdEventAndIdAccount(eventDraft.getId(), promoterId).isPresent();

		return EventMapper.entityToDTO(eventDraft, booked, bookingRepository.countByIdEvent(eventDraftDTO.getId()), promoters);
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
		boolean booked = bookingRepository.findByIdEventAndIdAccount(id, promoterId).isPresent();

		return EventMapper.entityToDTO(eventDraft, booked, bookingRepository.countByIdEvent(id), promoters);
	}

	public EventDTO updateEventImage(Long id, MultipartFile file) {
		imageValidator.validateEventImage(file);

		PromoterDTO promoterDTO = promoterService.getData();
		Event event = eventRepository.findById(id)
			.orElseThrow(() -> new IllegalArgumentException("Event not found"));

		// Verify promoter has access to this event
		if (eventPromoterAssociationRepository.findByIdEventAndIdPromoter(id, promoterDTO.getId()).isEmpty()) {
			throw new IllegalArgumentException("Unauthorized to modify this event");
		}

		eventDraftValidator.validateEditable(event);

		try {
			String filename = imageService.saveImage(file);
			event.setImage(filename);
			event = eventRepository.save(event);

			List<Pair<Account, PromoterInfo>> promoters =
				eventPromoterAssociationRepository.findPromotersByIdEvent(event.getId());
			boolean booked = bookingRepository.findByIdEventAndIdAccount(id, promoterDTO.getId()).isPresent();
			return EventMapper.entityToDTO(event, booked, bookingRepository.countByIdEvent(id), promoters);
		} catch (IOException e) {
			throw new RuntimeException("Failed to save image", e);
		}
	}

	public EventDTO deleteEventImage(Long id) {
		PromoterDTO promoterDTO = promoterService.getData();
		Event event = eventRepository.findById(id)
			.orElseThrow(() -> new IllegalArgumentException("Event not found"));

		boolean booked = bookingRepository.findByIdEventAndIdAccount(id, promoterDTO.getId()).isPresent();

		// Verify promoter has access to this event
		if (eventPromoterAssociationRepository.findByIdEventAndIdPromoter(id, promoterDTO.getId()).isEmpty()) {
			throw new IllegalArgumentException("Unauthorized to modify this event");
		}

		eventDraftValidator.validateEditable(event);

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
