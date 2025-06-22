package com.pareidolia.mapper;

import com.pareidolia.dto.EventDTO;
import com.pareidolia.dto.EventUpdateDTO;
import com.pareidolia.dto.PromoterDTO;
import com.pareidolia.entity.Account;
import com.pareidolia.entity.Event;
import com.pareidolia.entity.EventPromoterAssociation;
import com.pareidolia.entity.PromoterInfo;
import com.pareidolia.repository.EventPromoterAssociationRepository;
import com.pareidolia.state.DraftState;
import com.pareidolia.state.State;
import org.springframework.data.util.Pair;

import java.util.List;

public class EventMapper {

	public static EventDTO entityToDTO(Event entity, Boolean booked, Long currentParticipants, List<Pair<Account, PromoterInfo>> promoters) {
		EventDTO dto = new EventDTO();
		dto.setId(entity.getId());
		dto.setTitle(entity.getTitle());
		dto.setDescription(entity.getDescription());
		dto.setImage(entity.getImage());
		dto.setPlace(entity.getPlace());
		dto.setDate(entity.getDate());
		dto.setTime(entity.getTime());
		dto.setDuration(entity.getDuration());
		dto.setPromoters(promoters.stream().map(promoter -> AccountMapper.entityToPromoterDTO(promoter.getFirst(), promoter.getSecond())).toList());
		dto.setMaxNumberOfParticipants(entity.getMaxNumberOfParticipants());
		dto.setCurrentParticipants(currentParticipants);
		dto.setState(entity.getState().getStateName());
		dto.setScore(entity.getAverageScore());
		dto.setBooked(booked);
		dto.setCreationTime(entity.getCreationTime());
		return dto;
	}

	public static Event dtoToEntity(EventDTO dto) {
		Event entity = new Event();
		entity.setId(dto.getId());
		entity.setTitle(dto.getTitle());
		entity.setDescription(dto.getDescription());
		entity.setPlace(dto.getPlace());
		entity.setDate(dto.getDate());
		entity.setTime(dto.getTime());
		entity.setDuration(dto.getDuration());
		entity.setMaxNumberOfParticipants(dto.getMaxNumberOfParticipants());
		if (dto.getState() != null) {
			entity.setState(State.fromString(dto.getState(), entity));
		} else {
			entity.setState(State.fromString(DraftState.name, entity));
		}
		return entity;
	}

	public static void updateEntitiesWithEventDTO(Event entity, EventDTO dto) {
		entity.setTitle(dto.getTitle());
		entity.setDescription(dto.getDescription());
		entity.setPlace(dto.getPlace());
		entity.setDate(dto.getDate());
		entity.setTime(dto.getTime());
		entity.setDuration(dto.getDuration());
		entity.setMaxNumberOfParticipants(dto.getMaxNumberOfParticipants());
		if (dto.getState() != null) {
			entity.setState(State.fromString(dto.getState(), entity));
		}
	}

	public static void createPromoterAssociations(Event savedEvent, List<PromoterDTO> promoterDTOs, EventPromoterAssociationRepository eventPromoterAssociationRepository) {
		for (PromoterDTO promoterDTO : promoterDTOs) {
			EventPromoterAssociation association = new EventPromoterAssociation();
			association.setIdEvent(savedEvent.getId());
			association.setIdPromoter(promoterDTO.getId());
			eventPromoterAssociationRepository.save(association);
		}
	}

	public static EventDTO updateDTOtoDTO(EventUpdateDTO updateDTO, List<PromoterDTO> promoters) {
		EventDTO dto = new EventDTO();
		dto.setId(updateDTO.getId());
		dto.setTitle(updateDTO.getTitle());
		dto.setDescription(updateDTO.getDescription());
		dto.setPlace(updateDTO.getPlace());
		dto.setDate(updateDTO.getDate());
		dto.setTime(updateDTO.getTime());
		dto.setDuration(updateDTO.getDuration());
		dto.setPromoters(promoters);
		dto.setMaxNumberOfParticipants(updateDTO.getMaxNumberOfParticipants());
		dto.setState(updateDTO.getState());
		return dto;
	}
}