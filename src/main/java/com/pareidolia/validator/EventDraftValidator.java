package com.pareidolia.validator;

import com.pareidolia.dto.PromoterDTO;
import com.pareidolia.dto.EventDTO;
import com.pareidolia.entity.Account;
import com.pareidolia.entity.Event;
import com.pareidolia.repository.AccountRepository;
import com.pareidolia.repository.EventPromoterAssociationRepository;
import com.pareidolia.repository.EventRepository;
import com.pareidolia.state.EventStateHandler;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
@Transactional
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class EventDraftValidator {

	private final EventRepository eventRepository;
	private final AccountValidator accountValidator;
	private final AccountRepository accountRepository;
	private final EventPromoterAssociationRepository eventPromoterAssociationRepository;

	public void createEventDraftValidator(EventDTO eventDraftDTO) {
		titleValidation(eventDraftDTO.getTitle());
		descriptionValidation(eventDraftDTO.getDescription());
		placeValidation(eventDraftDTO.getPlace());
		dateValidation(eventDraftDTO.getDate());
		timeValidation(eventDraftDTO.getTime());
		durationValidation(eventDraftDTO.getDuration());
		maxNumberOfParticipantsValidation(eventDraftDTO.getMaxNumberOfParticipants());
	}

	private void titleValidation(String title) {
		if (title == null || title.trim().isEmpty()) {
			throw new IllegalArgumentException("Event title must not be empty.");
		}
	}

	private void descriptionValidation(String description) {
		if (description == null || description.trim().isEmpty()) {
			throw new IllegalArgumentException("Event description must not be empty.");
		}
	}

	private void placeValidation(String place) {
		if (place == null || place.trim().isEmpty()) {
			throw new IllegalArgumentException("Event place must not be empty.");
		}
	}

	private void dateValidation(LocalDate date) {
		if (date == null) {
			throw new IllegalArgumentException("Event date must be specified.");
		}
		if (date.isBefore(LocalDate.now())) {
			throw new IllegalArgumentException("Event date must be in the future.");
		}
	}

	private void timeValidation(LocalTime time) {
		if (time == null) {
			throw new IllegalArgumentException("Event time must be specified.");
		}
	}

	private void durationValidation(Duration duration) {
		if (duration == null || duration.isNegative() || duration.isZero()) {
			throw new IllegalArgumentException("Event duration must be positive.");
		}
	}

	private void maxNumberOfParticipantsValidation(Long maxNumberOfParticipants) {
		if (maxNumberOfParticipants != null && maxNumberOfParticipants <= 0) {
			throw new IllegalArgumentException("Maximum number of participants must be greater than zero.");
		}
	}

	// Metodo principale per la validazione dell'aggiunta di un Promoter all'EventDraft
	public void validateAddPromoterToEventDraft(Long eventDraftId, Long accountId) {
		Event eventDraft = eventRepository.findById(eventDraftId)
			.orElseThrow(() -> new IllegalArgumentException("Invalid EventDraft ID"));
		Account account = validateAccountExists(accountId);
		accountValidator.accountTypeValidation(account.getReferenceType(), Account.Type.PROMOTER);
		validatePromoterNotAlreadyAssociated(eventDraftId, accountId);
		validateEditable(eventDraft);
	}

	// Controlla se l'account esiste
	private Account validateAccountExists(Long accountId) {
		return accountRepository.findById(accountId)
			.orElseThrow(() -> new IllegalArgumentException("Account not found."));
	}


	// Controlla che l'account non sia già associato all'evento
	public void validatePromoterNotAlreadyAssociated(Long eventDraftId, Long accountId) {
		// findByIdEventDraftAndIdPromoter per verificare se l'associazione esiste già
		boolean isAlreadyAssociated = eventPromoterAssociationRepository
			.findByIdEventAndIdPromoter(eventDraftId, accountId)
			.isPresent();

		if (isAlreadyAssociated) {
			throw new IllegalArgumentException("Promoter is already associated with this event.");
		}
	}

	// Controlla che l'EventDraft non sia già associato all'evento (publicato)
	public void validateEditable(Event eventDraft) {
		if (EventStateHandler.canEdit(eventDraft)) {
			throw new IllegalArgumentException("Event cannot be edited in the current state: " + eventDraft.getState());
		}
	}

	//aggiorna i dati di eventDraft
	public Event getEventDraftAndValidateUpdate(EventDTO eventDraftDTO) {
		Event eventDraft = eventRepository.findById(eventDraftDTO.getId())
			.orElseThrow(() -> new IllegalArgumentException("EventDraft not found"));

		// validate update like: AccountValidator.getPromoterAndValidateUpdate-> fatto diversamente
		validateEditable(eventDraft);

		// Step 3: Validate the fields in EventDraftDTO
		titleValidation(eventDraftDTO.getTitle());
		descriptionValidation(eventDraftDTO.getDescription());
		placeValidation(eventDraftDTO.getPlace());
		dateValidation(eventDraftDTO.getDate());
		timeValidation(eventDraftDTO.getTime());
		durationValidation(eventDraftDTO.getDuration());
		maxNumberOfParticipantsValidation(eventDraftDTO.getMaxNumberOfParticipants());

		updatePromoterAssociations(eventDraft, eventDraftDTO.getPromoters());

		return eventDraft;
	}

	//aggiorna la lista promoters associata all'eventDraft
	private void updatePromoterAssociations(Event eventDraft, List<PromoterDTO> promoterDTOs) {
		//  Recupera gli ID dei promoter ATTUALI associati all'evento
		Set<Long> currentPromoterIds = eventPromoterAssociationRepository
			.findPromotersByIdEvent(eventDraft.getId()).stream()
			.map(pair -> pair.getSecond().getIdPromoter())
			.collect(Collectors.toSet());

		//  Crea un set per gli ID dei NUOVI promoter
		Set<Long> newPromoterIds = promoterDTOs.stream()
			.map(PromoterDTO::getId)
			.collect(Collectors.toSet());

		// Trova i promoter da aggiungere
		Set<Long> promotersToAdd = newPromoterIds.stream()
			.filter(id -> !currentPromoterIds.contains(id))
			.collect(Collectors.toSet());

		//  Trova i promoter da rimuovere
		Set<Long> promotersToRemove = currentPromoterIds.stream()
			.filter(id -> !newPromoterIds.contains(id))
			.collect(Collectors.toSet());

		// Aggiungi i nuovi promoter
		for (Long promoterId : promotersToAdd) {
			// Utilizza il tuo metodo di aggiunta del promoter, ad esempio:
			validateAddPromoterToEventDraft(eventDraft.getId(), promoterId);
		}

		//  Rimuovi i promoter non più associati
		for (Long promoterId : promotersToRemove) {
			// Utilizza il tuo metodo di rimozione del promoter, ad esempio:
			removePromoterToEventDraft(eventDraft.getId(), promoterId);
		}
	}

	//Implementazione della rimozione di un promoter durante aggiornamento eventDraft
	public void removePromoterToEventDraft(Long eventDraftId, Long accountId) {
		eventRepository.findById(eventDraftId).orElseThrow(() -> new IllegalArgumentException("Invalid EventDraft ID"));

		validatePromoterAssociated(eventDraftId, accountId);

		eventPromoterAssociationRepository.deleteByIdEventAndIdPromoter(eventDraftId, accountId);
	}

	// Controlla se l'associazione Promoter-EventDraft esiste già
	public void validatePromoterAssociated(Long eventDraftId, Long accountId) {
		// Controlla se l'associazione esiste
		boolean isAlreadyAssociated = eventPromoterAssociationRepository
			.findByIdEventAndIdPromoter(eventDraftId, accountId)
			.isPresent();

		if (!isAlreadyAssociated) {
			throw new IllegalArgumentException("Promoter is not associated with this event, cannot remove.");
		}
	}

	public void createEventDraftValidatorWithPromoter(EventDTO eventDraftDTO) {
		titleValidation(eventDraftDTO.getTitle());
		descriptionValidation(eventDraftDTO.getDescription());
		placeValidation(eventDraftDTO.getPlace());
		dateValidation(eventDraftDTO.getDate());
		timeValidation(eventDraftDTO.getTime());
		durationValidation(eventDraftDTO.getDuration());
		maxNumberOfParticipantsValidation(eventDraftDTO.getMaxNumberOfParticipants());

		// 2. Validazione del promoter associato (come nel metodo validateAddPromoterToEventDraft)
		if (eventDraftDTO.getPromoters() == null || eventDraftDTO.getPromoters().isEmpty()) {
			throw new IllegalArgumentException("Event promoters must not be empty.");
		} else if (!eventDraftDTO.getPromoters().stream().allMatch(ConcurrentHashMap.newKeySet()::add)) {
			throw new IllegalArgumentException("Event promoters must contain no duplicates.");
		}

		eventDraftDTO.getPromoters().forEach(accountValidator::getPromoterAndValidate);
	}
}

