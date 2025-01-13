package com.pareidolia.mapper;

import com.pareidolia.dto.EventDTO;
import com.pareidolia.dto.PromoterDTO;
import com.pareidolia.dto.PublishedEventDTO;
import com.pareidolia.entity.Account;
import com.pareidolia.entity.Event;
import com.pareidolia.entity.EventPromoterAssociation;
import com.pareidolia.entity.PromoterInfo;
import com.pareidolia.repository.EventPromoterAssociationRepository;
import org.springframework.data.util.Pair;

import java.util.List;

public class PublishedEventMapper {

	public static PublishedEventDTO entityToDTO(Event entity, List<Pair<Account, PromoterInfo>> promoters) {
		PublishedEventDTO dto = new PublishedEventDTO();
		dto.setId(entity.getId());
		dto.setTitle(entity.getTitle());
		dto.setDescription(entity.getDescription());
		dto.setPlace(entity.getPlace());
		dto.setDate(entity.getDate());
		dto.setTime(entity.getTime());
		dto.setDuration(entity.getDuration());
		dto.setPromoters(promoters.stream().map(promoter -> AccountMapper.entityToPromoterDTO(promoter.getFirst(), promoter.getSecond())).toList());
		dto.setMaxNumberOfParticipants(entity.getMaxNumberOfParticipants());
		dto.setScore(entity.getAverageScore());
		dto.setState(entity.getState());
		return dto;
	}

	public static Event dtoToEntity(PublishedEventDTO dto) {
		Event entity = new Event();
		entity.setId(dto.getId());
		entity.setTitle(dto.getTitle());
		entity.setDescription(dto.getDescription());
		entity.setPlace(dto.getPlace());
		entity.setDate(dto.getDate());
		entity.setTime(dto.getTime());
		entity.setDuration(dto.getDuration());
		entity.setAverageScore(0.0);
		entity.setMaxNumberOfParticipants(dto.getMaxNumberOfParticipants());
		if (dto.getState() != null) {
			entity.setState(dto.getState());
		} else {
			entity.setState(Event.EventState.DRAFT); // default state
		}
		return entity;
	}

	public static void updateEntitiesWithPublishedEventDTO(Event entity, PublishedEventDTO dto) {
		entity.setTitle(dto.getTitle());
		entity.setDescription(dto.getDescription());
		entity.setPlace(dto.getPlace());
		entity.setDate(dto.getDate());
		entity.setTime(dto.getTime());
		entity.setDuration(dto.getDuration());
		entity.setMaxNumberOfParticipants(dto.getMaxNumberOfParticipants());
	}

}
