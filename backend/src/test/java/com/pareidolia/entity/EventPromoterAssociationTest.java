package com.pareidolia.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class EventPromoterAssociationTest {

	@Test
	void testNoArgsConstructor() {
		EventPromoterAssociation association = new EventPromoterAssociation();
		assertNotNull(association);
	}

	@Test
	void testAllArgsConstructor() {
		LocalDateTime now = LocalDateTime.now();
		Event event = new Event();
		PromoterInfo promoter = new PromoterInfo();

		EventPromoterAssociation association = new EventPromoterAssociation(
			1L, 2L, 3L, now, now, event, promoter);

		assertEquals(1L, association.getId());
		assertEquals(2L, association.getIdEvent());
		assertEquals(3L, association.getIdPromoter());
		assertEquals(now, association.getCreationTime());
		assertEquals(now, association.getLastUpdate());
		assertEquals(event, association.getEvent());
		assertEquals(promoter, association.getPromoter());
	}

	@Test
	void testSetters() {
		EventPromoterAssociation association = new EventPromoterAssociation();
		LocalDateTime now = LocalDateTime.now();
		Event event = new Event();
		PromoterInfo promoter = new PromoterInfo();

		association.setId(1L);
		association.setIdEvent(2L);
		association.setIdPromoter(3L);
		association.setCreationTime(now);
		association.setLastUpdate(now);
		association.setEvent(event);
		association.setPromoter(promoter);

		assertEquals(1L, association.getId());
		assertEquals(2L, association.getIdEvent());
		assertEquals(3L, association.getIdPromoter());
		assertEquals(now, association.getCreationTime());
		assertEquals(now, association.getLastUpdate());
		assertEquals(event, association.getEvent());
		assertEquals(promoter, association.getPromoter());
	}

	@Test
	void testEqualsAndHashCode() {
		LocalDateTime now = LocalDateTime.now();
		Event event1 = new Event();
		PromoterInfo promoter1 = new PromoterInfo();
		Event event2 = new Event();
		PromoterInfo promoter2 = new PromoterInfo();

		EventPromoterAssociation association1 = new EventPromoterAssociation(
			1L, 2L, 3L, now, now, event1, promoter1);

		EventPromoterAssociation association2 = new EventPromoterAssociation(
			1L, 2L, 3L, now, now, event2, promoter2);

		// Should be equal even with different event and promoter objects due to @EqualsAndHashCode(exclude = {"event", "promoter"})
		assertEquals(association1, association2);
		assertEquals(association1.hashCode(), association2.hashCode());

		// Test with different values
		EventPromoterAssociation association3 = new EventPromoterAssociation();
		association3.setId(2L);
		association3.setIdEvent(3L);
		association3.setIdPromoter(4L);

		assertNotEquals(association1, association3);
		assertNotEquals(association1.hashCode(), association3.hashCode());
	}

	@Test
	void testNullableFields() {
		EventPromoterAssociation association = new EventPromoterAssociation();

		// Event and promoter references can be null
		assertNull(association.getEvent());
		assertNull(association.getPromoter());

		// But IDs should not be null when set
		association.setIdEvent(1L);
		association.setIdPromoter(2L);

		assertNotNull(association.getIdEvent());
		assertNotNull(association.getIdPromoter());
	}
} 