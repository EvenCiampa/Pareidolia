package com.pareidolia.mapper;

import com.pareidolia.dto.PromoterDTO;
import com.pareidolia.entity.EventPromoterAssociation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EventPromoterAssociationMapperTest {

	private static final Long TEST_PROMOTER_ID = 1L;
	private static final Long TEST_EVENT_ID = 2L;

	@Test
	void testPromoterDTOToEntity() {
		// Create test PromoterDTO
		PromoterDTO promoterDTO = new PromoterDTO();
		promoterDTO.setId(TEST_PROMOTER_ID);

		// Map DTO to entity
		EventPromoterAssociation result = EventPromoterAssociationMapper.promoterDTOToEntity(promoterDTO, TEST_EVENT_ID);

		// Verify mapping
		assertNotNull(result);
		assertEquals(TEST_PROMOTER_ID, result.getIdPromoter());
		assertEquals(TEST_EVENT_ID, result.getIdEvent());
	}

	@Test
	void testPromoterDTOToEntityWithNullDTO() {
		assertThrows(NullPointerException.class,
			() -> EventPromoterAssociationMapper.promoterDTOToEntity(null, TEST_EVENT_ID));
	}

	@Test
	void testPromoterDTOToEntityWithNullEventId() {
		PromoterDTO promoterDTO = new PromoterDTO();
		promoterDTO.setId(TEST_PROMOTER_ID);

		EventPromoterAssociation result = EventPromoterAssociationMapper.promoterDTOToEntity(promoterDTO, null);

		assertNotNull(result);
		assertEquals(TEST_PROMOTER_ID, result.getIdPromoter());
		assertNull(result.getIdEvent());
	}

	@Test
	void testPromoterDTOToEntityWithNullPromoterId() {
		PromoterDTO promoterDTO = new PromoterDTO();
		// id is not set, so it will be null

		EventPromoterAssociation result = EventPromoterAssociationMapper.promoterDTOToEntity(promoterDTO, TEST_EVENT_ID);

		assertNotNull(result);
		assertNull(result.getIdPromoter());
		assertEquals(TEST_EVENT_ID, result.getIdEvent());
	}
} 