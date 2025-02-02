package com.pareidolia.service.admin;

import com.pareidolia.dto.AdminDTO;
import com.pareidolia.dto.EventDTO;
import com.pareidolia.dto.EventUpdateDTO;
import com.pareidolia.dto.PromoterDTO;
import com.pareidolia.entity.Account;
import com.pareidolia.entity.Event;
import com.pareidolia.entity.PromoterInfo;
import com.pareidolia.mapper.AccountMapper;
import com.pareidolia.mapper.EventMapper;
import com.pareidolia.mapper.EventPromoterAssociationMapper;
import com.pareidolia.repository.BookingRepository;
import com.pareidolia.repository.EventPromoterAssociationRepository;
import com.pareidolia.repository.EventRepository;
import com.pareidolia.repository.PromoterInfoRepository;
import com.pareidolia.repository.model.EventWithInfoForAccount;
import com.pareidolia.service.ImageService;
import com.pareidolia.state.EventStateHandler;
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
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class AdminEventService {
	private final ImageService imageService;
	private final AdminService adminService;
	private final ImageValidator imageValidator;
	private final EventValidator eventValidator;
	private final EventRepository eventRepository;
	private final BookingRepository bookingRepository;
	private final EventDraftValidator eventDraftValidator;
	private final PromoterInfoRepository promoterInfoRepository;
	private final EventPromoterAssociationRepository eventPromoterAssociationRepository;

	public EventDTO getEvent(Long id) {
		Event eventDraft = eventRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid Event ID"));
		Long currentParticipants = bookingRepository.countByIdEvent(id);
		boolean booked = bookingRepository.findByIdEventAndIdAccount(id, adminService.getData().getId()).isPresent();

		List<Pair<Account, PromoterInfo>> promoters =
			eventPromoterAssociationRepository.findPromotersByIdEvent(id);

		return EventMapper.entityToDTO(eventDraft, booked, currentParticipants, promoters);
	}

	public Page<EventDTO> getEvents(Integer page, Integer size, Event.EventState state) {
		AdminDTO admin = adminService.getData();
		Page<EventWithInfoForAccount> eventPage;
		if (state == null) {
			eventPage = eventRepository.findAllByAccountIdWithCount(
				admin.getId(),
				PageRequest.of(Math.max(0, Optional.ofNullable(page).orElse(0)),
					Math.max(10, Optional.ofNullable(size).orElse(10)),
					Sort.by(Sort.Order.desc("id"))));
		} else {
			eventPage = eventRepository.findAllByAccountIdAndState(
				admin.getId(), state,
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

	public Page<EventDTO> getPromoterEvents(Long idPromoter, Integer page, Integer size, Event.EventState state) {
		AdminDTO admin = adminService.getData();
		Page<EventWithInfoForAccount> eventPage;
		if (state == null) {
			eventPage = eventRepository.findAllByAccountIdAndPromoterId(admin.id, idPromoter,
				PageRequest.of(Math.max(0, Optional.ofNullable(page).orElse(0)),
					Math.max(10, Optional.ofNullable(size).orElse(10)),
					Sort.by(Sort.Order.desc("id")))
			);
		} else {
			eventPage = eventRepository.findAllByAccountIdAndStateAndPromoterId(admin.id, state, idPromoter,
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

		eventDraftValidator.createEventDraftValidatorWithPromoter(eventDraftDTO);

		Event eventDraft = EventMapper.dtoToEntity(eventDraftDTO);

		Event savedEventDraft = eventRepository.save(eventDraft);

		// Crea l'associazione tra l'evento e i promoter selezionati dall'admin
		EventMapper.createPromoterAssociations(savedEventDraft, eventDraftDTO.getPromoters(), eventPromoterAssociationRepository);

		// Recupera i promoter associati all'evento
		List<Pair<Account, PromoterInfo>> promoters =
			eventPromoterAssociationRepository.findPromotersByIdEvent(savedEventDraft.getId());

		return EventMapper.entityToDTO(savedEventDraft, false, 0L, promoters);
	}

	public EventDTO update(EventUpdateDTO eventUpdateDraftDTO) {
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

		EventDTO eventDTO = EventMapper.updateDTOtoDTO(eventUpdateDraftDTO, promoterDTOList);

		if (eventDTO.getId() == null) {
			throw new IllegalArgumentException("Invalid Event ID");
		}
		Event event = eventRepository.findById(eventDTO.getId())
			.orElseThrow(() -> new IllegalArgumentException("Event not found"));

		eventValidator.getEventAndValidateUpdate(eventDTO);

		EventMapper.updateEntitiesWithEventDTO(event, eventDTO);
		eventRepository.save(event);

		updateEventPromoters(eventDTO.getId(), eventDTO.getPromoters());

		List<Pair<Account, PromoterInfo>> promoters = eventPromoterAssociationRepository.findPromotersByIdEvent(event.getId());
		boolean booked = bookingRepository.findByIdEventAndIdAccount(event.getId(), adminService.getData().getId()).isPresent();
		return EventMapper.entityToDTO(event, booked, bookingRepository.countByIdEvent(eventDTO.getId()), promoters);
	}

	private void updateEventPromoters(Long idEvent, List<PromoterDTO> promoters) {
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
		boolean booked = bookingRepository.findByIdEventAndIdAccount(id, adminService.getData().getId()).isPresent();
		return EventMapper.entityToDTO(eventDraft, booked, bookingRepository.countByIdEvent(id), promoters);
	}

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
			boolean booked = bookingRepository.findByIdEventAndIdAccount(id, adminService.getData().getId()).isPresent();
			return EventMapper.entityToDTO(event, booked, bookingRepository.countByIdEvent(id), promoters);
		} catch (IOException e) {
			throw new RuntimeException("Failed to save image", e);
		}
	}

	public EventDTO deleteEventImage(Long id) {
		Event event = eventRepository.findById(id)
			.orElseThrow(() -> new IllegalArgumentException("Event not found"));
		boolean booked = bookingRepository.findByIdEventAndIdAccount(id, adminService.getData().getId()).isPresent();

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
