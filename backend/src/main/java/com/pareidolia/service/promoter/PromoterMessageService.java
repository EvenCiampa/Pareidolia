package com.pareidolia.service.promoter;

import com.pareidolia.dto.MessageDTO;
import com.pareidolia.entity.Account;
import com.pareidolia.entity.Message;
import com.pareidolia.mapper.MessageMapper;
import com.pareidolia.repository.AccountRepository;
import com.pareidolia.repository.EventPromoterAssociationRepository;
import com.pareidolia.repository.EventRepository;
import com.pareidolia.repository.MessageRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class PromoterMessageService {
	private final PromoterService promoterService;
	private final MessageRepository messageRepository;
	private final AccountRepository accountRepository;
	private final EventRepository eventRepository;
	private final EventPromoterAssociationRepository eventPromoterAssociationRepository;

	/**
	 * Recupera i messaggi associati a una bozza di evento, paginati e ordinati.
	 * @param idEventDraft L'ID della bozza di evento per cui recuperare i messaggi.
	 * @param page Il numero della pagina da recuperare.
	 * @param size La dimensione della pagina.
	 * @return Page<MessageDTO> Una pagina contenente i messaggi sotto forma di DTO.
	 * @throws IllegalArgumentException Se l'ID della bozza di evento non è valido o non appartiene al promoter autenticato.
	 */
	public Page<MessageDTO> getEventDraftMessages(Long idEventDraft, Integer page, Integer size) {
		if (idEventDraft == null) {
			throw new IllegalArgumentException("Invalid EventDraft ID");
		}
		// Controlla se il promoter autenticato è tra i promoter dell'evento
		Long promoterId = promoterService.getData().getId();

		if (eventRepository.findById(idEventDraft).isEmpty()) {
			throw new IllegalArgumentException("Invalid EventDraft ID");
		}
		if (eventPromoterAssociationRepository.findByIdEventAndIdPromoter(idEventDraft, promoterId).isEmpty()) {
			throw new IllegalArgumentException("Invalid EventDraft ID");
		}

		// ricerca messaggi relativi a una draft e paginali
		Page<Message> messages = messageRepository.findByIdEvent(idEventDraft,
			PageRequest.of(Math.max(0, Optional.ofNullable(page).orElse(0)), Math.max(10, Optional.ofNullable(size).orElse(10)), Sort.by(Sort.Order.desc("id")))
		);
		return messages.map(MessageMapper::entityToDTO);
	}

	/**
	 * Crea un nuovo messaggio associato a una bozza di evento, dopo aver verificato che l'ID dell'evento e l'account del promoter siano validi.
	 * @param idEventDraft L'ID della bozza di evento a cui associare il messaggio.
	 * @param message Il testo del messaggio da creare.
	 * @return MessageDTO Il DTO del messaggio creato.
	 * @throws IllegalArgumentException Se l'ID della bozza di evento non è valido, se il messaggio è vuoto o troppo lungo, o se l'evento non esiste.
	 */
	public MessageDTO create(Long idEventDraft, String message) {
		// verifica che l'id della draft sia corretto
		if (idEventDraft == null) {
			throw new IllegalArgumentException("Invalid EventDraft ID");
		}
		// Controlla se il promoter autenticato è tra i promoter dell'evento
		Long accountId = promoterService.getData().getId();
		Optional<Account> account = accountRepository.findById(accountId);
		if (account.isEmpty()) {
			throw new IllegalArgumentException("Invalid Account");
		}
		if (eventPromoterAssociationRepository.findByIdEventAndIdPromoter(idEventDraft, accountId).isEmpty()) {
			throw new IllegalArgumentException("Invalid EventDraft ID");
		}
		// verifica che la lunghezza del messaggio sia valida
		if (message == null || message.trim().isEmpty()) {
			throw new IllegalArgumentException("Message must not be empty.");
		}
		if (message.length() > 1000) {
			throw new IllegalArgumentException("Message is too long.");
		}
		if (eventRepository.findById(idEventDraft).isEmpty()) {
			throw new IllegalArgumentException("EventDraft not found");
		}

		// crea un entità e salvala sul db
		Message newMessage = new Message();
		newMessage.setIdAccount(accountId);
		newMessage.setIdEvent(idEventDraft);
		newMessage.setMessage(message);
		newMessage.setAccount(account.get());

		newMessage = messageRepository.save(newMessage);
		// trasforma quest'ultima in dto e ritornalo
		return MessageMapper.entityToDTO(newMessage);
	}
}
