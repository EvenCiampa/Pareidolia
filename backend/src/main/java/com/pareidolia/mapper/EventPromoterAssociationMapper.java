package com.pareidolia.mapper;

import com.pareidolia.dto.PromoterDTO;
import com.pareidolia.entity.EventPromoterAssociation;

public class EventPromoterAssociationMapper {
	public static EventPromoterAssociation promoterDTOToEntity(PromoterDTO dto, Long eventId) {
		EventPromoterAssociation entity = new EventPromoterAssociation();
		entity.setIdPromoter(dto.getId());
		entity.setIdEvent(eventId);
		return entity;
	}
}