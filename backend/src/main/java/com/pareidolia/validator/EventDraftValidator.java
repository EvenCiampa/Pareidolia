package com.pareidolia.validator;

import com.pareidolia.dto.EventDTO;
import com.pareidolia.dto.PromoterDTO;
import com.pareidolia.entity.Account;
import com.pareidolia.entity.Event;
import com.pareidolia.repository.AccountRepository;
import com.pareidolia.repository.EventPromoterAssociationRepository;
import com.pareidolia.repository.EventRepository;
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

	/**
	 * Valida un EventDTO prima della sua creazione o aggiornamento. Questo metodo assicura che tutti gli attributi
	 * essenziali dell'evento siano presenti e corretti, come titolo, descrizione, luogo, data, tempo, durata e numero
	 * massimo di partecipanti. Viene utilizzato per garantire che i dati inseriti rispettino le regole di business
	 * prima di procedere con la creazione o l'aggiornamento dell'evento nel database.
	 * @param eventDraftDTO L'oggetto EventDTO che contiene i dati dell'evento da validare.
	 */
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

	/**
	 * Valida se un promotore può essere aggiunto a una bozza di evento. Questo processo include la verifica dell'esistenza
	 * dell'evento e dell'account del promotore, la conferma che l'account sia di tipo promotore, e che non sia già associato
	 * all'evento in questione. Inoltre, verifica che l'evento sia in uno stato modificabile secondo le regole definite
	 * dal pattern State. Questo metodo è cruciale per mantenere l'integrità dei dati e prevenire situazioni non valide
	 * come duplicazioni o modifiche non autorizzate.
	 * @param eventDraftId ID dell'evento a cui il promotore potrebbe essere aggiunto.
	 * @param accountId ID del promotore da aggiungere all'evento.
	 */
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
/**
 // Controlla che l'EventDraft non sia già associato all'evento (publicato)
 public void validateEditable(Event eventDraft) {
 if (!EventStateHandler.canEdit(eventDraft)) {
 throw new IllegalArgumentException("Event cannot be edited in the current state: " + eventDraft.getState());
 }
 }
 */

	/**
	 * Valida se l'evento è modificabile in base al suo stato attuale utilizzando il pattern State.
	 * @param event L'evento da validare.
	 */
	public void validateEditable(Event event) {
		if (!event.getState().canEdit()) {
			throw new IllegalArgumentException("Event cannot be edited in the current state: " + event.getState().getStateName());
		}
	}

	/**
	 * Recupera un evento dal database utilizzando il suo ID e valida tutti i campi dell'EventDTO prima di un aggiornamento.
	 * Questo include la convalida di titolo, descrizione, luogo, data, orario, durata e numero massimo di partecipanti, oltre
	 * a verificare che l'evento sia in uno stato che permette modifiche. Gestisce anche l'aggiornamento delle associazioni
	 * dei promotori, aggiungendo o rimuovendo promotori secondo le necessità. Questo metodo è essenziale per assicurare che
	 * le modifiche agli eventi siano coerenti e valide secondo le regole del business e lo stato dell'evento.
	 * @param eventDraftDTO DTO dell'evento con i dati aggiornati.
	 * @return Event L'evento aggiornato e validato pronto per essere salvato nel database.
	 */
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

		if (eventDraftDTO.getPromoters() == null || eventDraftDTO.getPromoters().isEmpty()) {
			throw new IllegalArgumentException("Promoters must not be empty.");
		}

		updatePromoterAssociations(eventDraft, eventDraftDTO.getPromoters());

		return eventDraft;
	}

	/**
	 * Aggiorna le associazioni dei promotori per un evento, aggiungendo nuovi promotori e rimuovendo quelli non più necessari.
	 * Utilizza gli ID dei promotori attuali per determinare quali promotori aggiungere o rimuovere, assicurando che le associazioni
	 * siano aggiornate in modo coerente e riflettano accuratamente le intenzioni dell'amministratore o dell'organizzatore dell'evento.
	 * Questo metodo è critico per mantenere accurate le informazioni sui promotori associati agli eventi.
	 * @param eventDraft L'evento per cui aggiornare le associazioni.
	 * @param promoterDTOs Lista dei DTO dei promotori che dovrebbero essere attualmente associati all'evento.
	 */
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

	/**
	 * Rimuove un promotore da un evento durante l'aggiornamento della bozza di un evento.
	 * Questo metodo verifica prima che l'evento esista e che il promotore sia effettivamente associato all'evento.
	 * Se il promotore è associato, procede con la rimozione dell'associazione nel repository.
	 * @param eventDraftId L'ID della bozza di evento da cui rimuovere il promotore.
	 * @param accountId L'ID del promotore da rimuovere dall'evento.
	 */
	public void removePromoterToEventDraft(Long eventDraftId, Long accountId) {
		eventRepository.findById(eventDraftId).orElseThrow(() -> new IllegalArgumentException("Invalid EventDraft ID"));

		validatePromoterAssociated(eventDraftId, accountId);

		eventPromoterAssociationRepository.deleteByIdEventAndIdPromoter(eventDraftId, accountId);
	}

	/**
	 * Controlla se un promotore è attualmente associato a una bozza di evento.
	 * Questo metodo è utilizzato per verificare l'esistenza di un'associazione prima di tentare operazioni che richiedono che il promotore sia associato,
	 * come la rimozione da un evento.
	 * @param eventDraftId L'ID dell'evento per il quale verificare l'associazione.
	 * @param accountId L'ID del promotore da verificare.
	 */
	public void validatePromoterAssociated(Long eventDraftId, Long accountId) {
		// Controlla se l'associazione esiste
		boolean isAlreadyAssociated = eventPromoterAssociationRepository
			.findByIdEventAndIdPromoter(eventDraftId, accountId)
			.isPresent();

		if (!isAlreadyAssociated) {
			throw new IllegalArgumentException("Promoter is not associated with this event, cannot remove.");
		}
	}

	/**
	 * Valida una bozza di evento con i dettagli del promotore inclusi, garantendo che tutti gli attributi necessari siano presenti e corretti,
	 * e che i promotori elencati non contengano duplicati. Questo metodo estende la validazione di base degli eventi per includere la verifica
	 * specifica dei promotori associati, assicurando che siano validi e univoci prima di procedere con ulteriori operazioni di creazione o aggiornamento.
	 * @param eventDraftDTO DTO dell'evento che include i dati dei promotori.
	 */
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

