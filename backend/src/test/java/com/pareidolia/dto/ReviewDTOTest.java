package com.pareidolia.dto;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ReviewDTOTest {

	@Test
	void testConstructorAndGetters() {
		ReviewDTO dto = new ReviewDTO();
		assertNotNull(dto);

		LocalDateTime now = LocalDateTime.now();
		dto = new ReviewDTO(1L, "Great Event", "Amazing experience", 5L,
			2L, 3L, "John", "CONSUMER", now, false, "POSITIVE");

		assertEquals(1L, dto.getId());
		assertEquals("Great Event", dto.getTitle());
		assertEquals("Amazing experience", dto.getDescription());
		assertEquals(5L, dto.getScore());
		assertEquals(2L, dto.getIdConsumer());
		assertEquals(3L, dto.getIdEvent());
		assertEquals("John", dto.getAccountName());
		assertEquals("CONSUMER", dto.getAccountReferenceType());
		assertEquals(now, dto.getCreationTime());
		assertFalse(dto.isAnonymous());
		assertEquals("POSITIVE", dto.getTag());
	}

	@Test
	void testSetters() {
		ReviewDTO dto = new ReviewDTO();
		LocalDateTime now = LocalDateTime.now();

		dto.setId(1L);
		dto.setTitle("Great Event");
		dto.setDescription("Amazing experience");
		dto.setScore(5L);
		dto.setIdConsumer(2L);
		dto.setIdEvent(3L);
		dto.setAccountName("John");
		dto.setAccountReferenceType("CONSUMER");
		dto.setCreationTime(now);
		dto.setAnonymous(false);
		dto.setTag("POSITIVE");

		assertEquals(1L, dto.getId());
		assertEquals("Great Event", dto.getTitle());
		assertEquals("Amazing experience", dto.getDescription());
		assertEquals(5L, dto.getScore());
		assertEquals(2L, dto.getIdConsumer());
		assertEquals(3L, dto.getIdEvent());
		assertEquals("John", dto.getAccountName());
		assertEquals("CONSUMER", dto.getAccountReferenceType());
		assertEquals(now, dto.getCreationTime());
		assertFalse(dto.isAnonymous());
		assertEquals("POSITIVE", dto.getTag());
	}

	@Test
	void testEqualsAndHashCode() {
		LocalDateTime now = LocalDateTime.now();
		ReviewDTO dto1 = new ReviewDTO(1L, "Great Event", "Amazing experience", 5L,
			2L, 3L, "John", "CONSUMER", now, false, "POSITIVE");

		ReviewDTO dto2 = new ReviewDTO(1L, "Great Event", "Amazing experience", 5L,
			2L, 3L, "John", "CONSUMER", now, false, "POSITIVE");

		assertEquals(dto1, dto2);
		assertEquals(dto1.hashCode(), dto2.hashCode());
	}

	@Test
	void testToString() {
		LocalDateTime now = LocalDateTime.now();
		ReviewDTO dto = new ReviewDTO(1L, "Great Event", "Amazing experience", 5L,
			2L, 3L, "John", "CONSUMER", now, false, "POSITIVE");

		String toString = dto.toString();
		assertNotNull(toString);
		assertTrue(toString.contains("id=1"));
		assertTrue(toString.contains("title=Great Event"));
		assertTrue(toString.contains("description=Amazing experience"));
		assertTrue(toString.contains("score=5"));
		assertTrue(toString.contains("idConsumer=2"));
		assertTrue(toString.contains("idEvent=3"));
		assertTrue(toString.contains("accountName=John"));
		assertTrue(toString.contains("accountReferenceType=CONSUMER"));
		assertTrue(toString.contains("isAnonymous=false"));
		assertTrue(toString.contains("tag=POSITIVE"));
	}
} 