package com.pareidolia.service.promoter;

import com.pareidolia.dto.MessageDTO;
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

import java.util.Objects;
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
			PageRequest.of(Math.max(0, Optional.ofNullable(page).orElse(0)), Math.max(10, Optional.ofNullable(size).orElse(10)), Sort.by(Sort.Order.asc("m.id")))
		);
		return messages.map(MessageMapper::entityToDTO);
	}


	public MessageDTO create(MessageDTO createMessageDTO) {
		Message message = MessageMapper.dtoToEntity(createMessageDTO);
		message.setId(null);
		if (message.getIdEvent() == null) {
			throw new IllegalArgumentException("Invalid EventDraft ID");
		}
		// controlla che il promoter autenticato sia il creatore del messaggio
		Long accountId = promoterService.getData().getId();
		if (accountRepository.findById(accountId).isEmpty()) {
			throw new IllegalArgumentException("Invalid Account");
		}
		if (!Objects.equals(message.getIdAccount(), accountId)) {
			throw new IllegalArgumentException("Invalid Promoter ID");
		}

		// Controlla se il promoter autenticato è tra i promoter dell'evento
		if (eventRepository.findById(message.getIdEvent()).isEmpty()) {
			throw new IllegalArgumentException("Invalid EventDraft ID");
		}
		if (eventPromoterAssociationRepository.findByIdEventAndIdPromoter(message.getIdEvent(), accountId).isEmpty()) {
			throw new IllegalArgumentException("Invalid EventDraft ID");
		}

		if (message.getMessage() == null || message.getMessage().trim().isEmpty()) {
			throw new IllegalArgumentException("Message must not be empty.");
		}
		if (createMessageDTO.getMessage().length() > 1000) {
			throw new IllegalArgumentException("Message is too long.");
		}
		// crea un entità e salvala sul db
		message = messageRepository.save(message);
		// trasforma quest'ultima in dto e ritornalo
		return MessageMapper.entityToDTO(message);
	}
}
