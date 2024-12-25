package com.pareidolia.service.generic;

import com.pareidolia.dto.EventDTO;
import com.pareidolia.dto.PromoterDTO;
import com.pareidolia.entity.Account;
import com.pareidolia.entity.Event;
import com.pareidolia.entity.PromoterInfo;
import com.pareidolia.mapper.AccountMapper;
import com.pareidolia.mapper.EventMapper;
import com.pareidolia.repository.AccountRepository;
import com.pareidolia.repository.EventPromoterAssociationRepository;
import com.pareidolia.repository.EventRepository;
import com.pareidolia.repository.PromoterInfoRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class PublicService {

	private final EventRepository eventRepository;
	private final PromoterInfoRepository promoterInfoRepository;
	private final EventPromoterAssociationRepository eventPromoterAssociationRepository;
	private final AccountRepository accountRepository;

	public EventDTO getEvent(Long id) {
		Event event = eventRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Event not found"));
		List<Pair<Account, PromoterInfo>> promoters = findPromotersByEventId(id); // Trova i promotori associati all'evento
		return EventMapper.entityToDTO(event, promoters);
	}

	public Page<EventDTO> getEvents(Integer page, Integer size) {
		return eventRepository.findAll(
			PageRequest.of(Math.max(0, Optional.ofNullable(page).orElse(0)), Math.max(10, Optional.ofNullable(size).orElse(10)), Sort.by(Sort.Order.asc("id")))
		).map(event -> {
			List<Pair<Account, PromoterInfo>> promoters = findPromotersByEventId(event.getId());
			return EventMapper.entityToDTO(event, promoters);
		});
	}

	public List<Pair<Account, PromoterInfo>> findPromotersByEventId(Long eventId) {
		return eventPromoterAssociationRepository.findPromotersByIdEvent(eventId);
	}

	public PromoterDTO getPromoter(Long id) {
		PromoterInfo promoterInfo = promoterInfoRepository.findByIdPromoter(id).orElseThrow(() -> new IllegalArgumentException("Promoter not found"));
		Account account = accountRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Account not found"));
		return AccountMapper.entityToPromoterDTO(account, promoterInfo);
	}

	public Page<PromoterDTO> getPromoters(Integer page, Integer size) {
		return promoterInfoRepository.findAll(
			PageRequest.of(Math.max(0, Optional.ofNullable(page).orElse(0)), Math.max(10, Optional.ofNullable(size).orElse(10)), Sort.by(Sort.Order.asc("id")))
		).map(promoterInfo -> {
			Account account = accountRepository.findById(promoterInfo.getIdPromoter()).orElseThrow(() -> new IllegalArgumentException("Account not found"));
			return AccountMapper.entityToPromoterDTO(account, promoterInfo);
		});
	}

	public Page<EventDTO> getPromoterEvents(Long idPromoter, Integer page, Integer size) {
		Page<Event> events = eventRepository.findAllByPromoterId(idPromoter,
			PageRequest.of(Math.max(0, Optional.ofNullable(page).orElse(0)), Math.max(10, Optional.ofNullable(size).orElse(10)), Sort.by(Sort.Order.asc("e.id")))
		);
		return events.map(event -> EventMapper.entityToDTO(event, findPromotersByEventId(event.getId())));
	}
}
