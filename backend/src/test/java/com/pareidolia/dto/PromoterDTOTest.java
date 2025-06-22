package com.pareidolia.dto;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class PromoterDTOTest {

	@Test
	void testNoArgsConstructor() {
		PromoterDTO dto = new PromoterDTO();
		assertNotNull(dto);
	}

	@Test
	void testPhotoAndPresentationConstructor() {
		PromoterDTO dto = new PromoterDTO("photo.jpg", "My presentation");

		assertEquals("photo.jpg", dto.getPhoto());
		assertEquals("My presentation", dto.getPresentation());
	}

	@Test
	void testFullConstructor() {
		LocalDateTime now = LocalDateTime.now();
		PromoterDTO dto = new PromoterDTO(1L, "John", "Doe", "+1234567890",
			"john@example.com", "photo.jpg", "My presentation", "PROMOTER", now);

		// Test inherited fields from AccountDTO
		assertEquals(1L, dto.getId());
		assertEquals("John", dto.getName());
		assertEquals("Doe", dto.getSurname());
		assertEquals("+1234567890", dto.getPhone());
		assertEquals("john@example.com", dto.getEmail());
		assertEquals("PROMOTER", dto.getReferenceType());
		assertEquals(now, dto.getCreationTime());

		// Test PromoterDTO specific fields
		assertEquals("photo.jpg", dto.getPhoto());
		assertEquals("My presentation", dto.getPresentation());
	}

	@Test
	void testSetters() {
		PromoterDTO dto = new PromoterDTO();
		LocalDateTime now = LocalDateTime.now();

		// Set inherited fields
		dto.setId(1L);
		dto.setName("John");
		dto.setSurname("Doe");
		dto.setPhone("+1234567890");
		dto.setEmail("john@example.com");
		dto.setReferenceType("PROMOTER");
		dto.setCreationTime(now);

		// Set PromoterDTO specific fields
		dto.setPhoto("photo.jpg");
		dto.setPresentation("My presentation");

		// Test inherited fields
		assertEquals(1L, dto.getId());
		assertEquals("John", dto.getName());
		assertEquals("Doe", dto.getSurname());
		assertEquals("+1234567890", dto.getPhone());
		assertEquals("john@example.com", dto.getEmail());
		assertEquals("PROMOTER", dto.getReferenceType());
		assertEquals(now, dto.getCreationTime());

		// Test PromoterDTO specific fields
		assertEquals("photo.jpg", dto.getPhoto());
		assertEquals("My presentation", dto.getPresentation());
	}

	@Test
	void testEqualsAndHashCode() {
		LocalDateTime now = LocalDateTime.now();
		PromoterDTO dto1 = new PromoterDTO(1L, "John", "Doe", "+1234567890",
			"john@example.com", "photo.jpg", "My presentation", "PROMOTER", now);

		PromoterDTO dto2 = new PromoterDTO(1L, "John", "Doe", "+1234567890",
			"john@example.com", "photo.jpg", "My presentation", "PROMOTER", now);

		assertEquals(dto1, dto2);
		assertEquals(dto1.hashCode(), dto2.hashCode());

		// Test with different values
		PromoterDTO dto3 = new PromoterDTO(2L, "John", "Doe", "+1234567890",
			"john@example.com", "photo.jpg", "My presentation", "PROMOTER", now);
		assertNotEquals(dto1, dto3);
		assertNotEquals(dto1.hashCode(), dto3.hashCode());
	}

	@Test
	void testToString() {
		LocalDateTime now = LocalDateTime.now();
		PromoterDTO dto = new PromoterDTO(1L, "John", "Doe", "+1234567890",
			"john@example.com", "photo.jpg", "My presentation", "PROMOTER", now);

		String toString = dto.toString();
		assertNotNull(toString);
		assertTrue(toString.contains("id=1"));
		assertTrue(toString.contains("name=John"));
		assertTrue(toString.contains("surname=Doe"));
		assertTrue(toString.contains("phone=+1234567890"));
		assertTrue(toString.contains("email=john@example.com"));
		assertTrue(toString.contains("referenceType=PROMOTER"));
		assertTrue(toString.contains("photo=photo.jpg"));
		assertTrue(toString.contains("presentation=My presentation"));
	}
} 