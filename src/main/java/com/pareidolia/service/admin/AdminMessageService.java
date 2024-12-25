package com.pareidolia.service.admin;

import com.pareidolia.dto.AdminDTO;
import com.pareidolia.dto.MessageDTO;
import com.pareidolia.entity.Account;
import com.pareidolia.entity.Message;
import com.pareidolia.mapper.MessageMapper;
import com.pareidolia.repository.AccountRepository;
import com.pareidolia.repository.EventDraftRepository;
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
public class AdminMessageService {
	private final AdminService adminService;
	private final MessageRepository messageRepository;
	private final AccountRepository accountRepository;
	private final EventDraftRepository eventDraftRepository;

	public Page<MessageDTO> getEventDraftMessages(Long idEventDraft, Integer page, Integer size) {
		// ricerca messaggi relativi a una draft e paginali
		if (idEventDraft == null) {
			throw new IllegalArgumentException("Invalid EventDraft ID");
		}
		Page<Message> messages = messageRepository.findMessagesByIdEventDraft(idEventDraft,
			PageRequest.of(Math.max(0, Optional.ofNullable(page).orElse(0)), Math.max(10, Optional.ofNullable(size).orElse(10)), Sort.by(Sort.Order.asc("m.id")))
		);
		return messages.map(MessageMapper::entityToDTO);
	}

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
		if (eventDraftRepository.findById(idEventDraft).isEmpty()) {
			throw new IllegalArgumentException("EventDraft not found");
		}
		AdminDTO adminDTO = adminService.getData();
		Account account = accountRepository.findById(adminDTO.getId()).orElseThrow(() -> new IllegalArgumentException("Invalid Account"));

		// crea un entità e salvala sul db
		Message newMessage = new Message();
		newMessage.setIdAccount(account.getId());
		newMessage.setIdEventDraft(idEventDraft);
		newMessage.setMessage(message);

		newMessage = messageRepository.save(newMessage);
		// trasforma quest'ultima in dto e ritornalo
		return MessageMapper.entityToDTO(newMessage);
	}

	public void delete(Long id) {
		if (id == null) {
			throw new IllegalArgumentException("Invalid Message ID");
		}
		if (messageRepository.findById(id).isEmpty()) {
			throw new IllegalArgumentException("Message not found");
		}

		messageRepository.deleteById(id);
	}
}
