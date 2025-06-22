package com.pareidolia.service.reviewer;

import com.pareidolia.dto.MessageDTO;
import com.pareidolia.dto.ReviewerDTO;
import com.pareidolia.entity.Account;
import com.pareidolia.entity.Message;
import com.pareidolia.mapper.MessageMapper;
import com.pareidolia.repository.AccountRepository;
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
public class ReviewerMessageService {
	private final ReviewerService reviewerService;
	private final MessageRepository messageRepository;
	private final AccountRepository accountRepository;
	private final EventRepository eventRepository;

	/**
	 * Recupera i messaggi associati a una bozza di evento specifica, paginandoli.
	 * @param idEventDraft L'ID della bozza di evento per cui recuperare i messaggi.
	 * @return Page<MessageDTO> Una pagina contenente i messaggi sotto forma di DTO.
	 */
	public Page<MessageDTO> getEventDraftMessages(Long idEventDraft, Integer page, Integer size) {
		// ricerca messaggi relativi a una draft e paginali
		if (idEventDraft == null) {
			throw new IllegalArgumentException("Invalid EventDraft ID");
		}
		if (eventRepository.findById(idEventDraft).isEmpty()) {
			throw new IllegalArgumentException("EventDraft not found");
		}
		Page<Message> messages = messageRepository.findByIdEvent(idEventDraft,
			PageRequest.of(Math.max(0, Optional.ofNullable(page).orElse(0)), Math.max(10, Optional.ofNullable(size).orElse(10)), Sort.by(Sort.Order.desc("creationTime")))
		);
		return messages.map(MessageMapper::entityToDTO);
	}

	/**
	 * Crea un nuovo messaggio associato a una bozza di evento specifica, verificando la validità dell'ID e la lunghezza del messaggio.
	 * @param idEventDraft L'ID della bozza di evento a cui associare il nuovo messaggio.
	 * @param message Il testo del messaggio da creare.
	 * @return MessageDTO Il DTO del messaggio appena creato.
	 */
	public MessageDTO create(Long idEventDraft, String message) {
		// verifica che l'id della draft sia corretto
		if (idEventDraft == null) {
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
		ReviewerDTO reviewerDTO = reviewerService.getData();
		Account account = accountRepository.findById(reviewerDTO.getId())
			.orElseThrow(() -> new IllegalArgumentException("Invalid Account"));

		// crea un entità e salvala sul db
		Message newMessage = new Message();
		newMessage.setIdAccount(account.getId());
		newMessage.setIdEvent(idEventDraft);
		newMessage.setMessage(message);
		newMessage.setAccount(account);

		newMessage = messageRepository.save(newMessage);
		// trasforma quest'ultima in dto e ritornalo
		return MessageMapper.entityToDTO(newMessage);
	}
}
