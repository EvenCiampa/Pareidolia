package com.pareidolia.mapper;

import com.pareidolia.dto.BookingDTO;
import com.pareidolia.entity.Account;
import com.pareidolia.entity.Booking;
import com.pareidolia.entity.Event;
import com.pareidolia.entity.PromoterInfo;
import org.springframework.data.util.Pair;

import java.io.Serializable;
import java.util.List;

public class BookingMapper implements Serializable {
	public static BookingDTO entityToDTO(Booking entity, Account consumer, Event event, Long currentParticipants, List<Pair<Account, PromoterInfo>> promoters) {
		if (entity == null) return null;
		BookingDTO dto = new BookingDTO();
		dto.setId(entity.getId());
		dto.setConsumer(AccountMapper.entityToConsumerDTO(consumer));
		dto.setEvent(EventMapper.entityToDTO(event, true, currentParticipants, promoters));
		dto.setCreationTime(entity.getCreationTime());
		return dto;
	}

	public static Booking dtoToEntity(BookingDTO dto) {
		if (dto == null) return null;
		Booking entity = new Booking();
		entity.setId(dto.getId());
		entity.setIdAccount(dto.getConsumer().getId());
		entity.setIdEvent(dto.getEvent().getId());
		return entity;
	}
}