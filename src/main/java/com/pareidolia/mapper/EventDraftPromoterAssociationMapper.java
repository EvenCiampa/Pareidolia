package com.pareidolia.mapper;

import com.pareidolia.dto.PromoterDTO;
import com.pareidolia.entity.EventDraftPromoterAssociation;

public class EventDraftPromoterAssociationMapper {
	public static EventDraftPromoterAssociation promoterDTOToEntity(PromoterDTO dto, Long eventDraftId) {
		EventDraftPromoterAssociation entity = new EventDraftPromoterAssociation();
		entity.setIdPromoter(dto.getId());
		entity.setIdEventDraft(eventDraftId);
		return entity;
	}
}