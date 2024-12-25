package com.pareidolia.mapper;

import com.pareidolia.dto.EventDTO;
import com.pareidolia.dto.EventDraftDTO;
import com.pareidolia.entity.Account;
import com.pareidolia.entity.EventDraft;
import com.pareidolia.entity.PromoterInfo;
import org.springframework.data.util.Pair;

import java.util.List;

public class EventDraftMapper {

	public static EventDraftDTO entityToDTO(EventDraft entity, List<Pair<Account, PromoterInfo>> promoters) {
		EventDraftDTO dto = new EventDraftDTO();
		dto.setId(entity.getId());
		dto.setTitle(entity.getTitle());
		dto.setDescription(entity.getDescription());
		dto.setPlace(entity.getPlace());
		dto.setDate(entity.getDate());
		dto.setTime(entity.getTime());
		dto.setDuration(entity.getDuration());
		dto.setPromoters(promoters.stream().map(promoter -> AccountMapper.entityToPromoterDTO(promoter.getFirst(), promoter.getSecond())).toList());
		dto.setMaxNumberOfParticipants(entity.getMaxNumberOfParticipants());
		return dto;
	}

	public static EventDraft dtoToEntity(EventDraftDTO dto) {
		EventDraft entity = new EventDraft();
		entity.setId(dto.getId());
		entity.setTitle(dto.getTitle());
		entity.setDescription(dto.getDescription());
		entity.setPlace(dto.getPlace());
		entity.setDate(dto.getDate());
		entity.setTime(dto.getTime());
		entity.setDuration(dto.getDuration());
		entity.setMaxNumberOfParticipants(dto.getMaxNumberOfParticipants());
		return entity;
	}

	public static void updateEntitiesWithPromoterDTO(EventDraft entity, EventDraftDTO dto) {
		entity.setTitle(dto.getTitle());
		entity.setDescription(dto.getDescription());
		entity.setPlace(dto.getPlace());
		entity.setDate(dto.getDate());
		entity.setTime(dto.getTime());
		entity.setDuration(dto.getDuration());
		entity.setMaxNumberOfParticipants(dto.getMaxNumberOfParticipants());
	}

	public static EventDTO eventDraftToEventDTO(EventDraft draft, List<Pair<Account, PromoterInfo>> promoters) {
		EventDTO eventDTO = new EventDTO();
		eventDTO.setTitle(draft.getTitle());
		eventDTO.setDescription(draft.getDescription());
		eventDTO.setPlace(draft.getPlace());
		eventDTO.setDate(draft.getDate());
		eventDTO.setTime(draft.getTime());
		eventDTO.setDuration(draft.getDuration());
		eventDTO.setPromoters(promoters.stream().map(promoter -> AccountMapper.entityToPromoterDTO(promoter.getFirst(), promoter.getSecond())).toList());
		eventDTO.setMaxNumberOfParticipants(draft.getMaxNumberOfParticipants());
		return eventDTO;
	}
}