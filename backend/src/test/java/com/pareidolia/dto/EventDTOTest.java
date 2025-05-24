package com.pareidolia.dto;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EventDTOTest {

	@Test
	void testConstructorAndGetters() {
		EventDTO dto = new EventDTO();
		assertNotNull(dto);

		LocalDate date = LocalDate.now();
		LocalTime time = LocalTime.now();
		LocalDateTime now = LocalDateTime.now();
		Duration duration = Duration.ofHours(2);
		PromoterDTO promoter = new PromoterDTO();

		dto = new EventDTO(1L, "Concert", "Amazing concert", "image.jpg", "Stadium",
			date, time, duration, List.of(promoter), 1000L, 500L, "ACTIVE",
			4.5, true, now);

		assertEquals(1L, dto.getId());
		assertEquals("Concert", dto.getTitle());
		assertEquals("Amazing concert", dto.getDescription());
		assertEquals("image.jpg", dto.getImage());
		assertEquals("Stadium", dto.getPlace());
		assertEquals(date, dto.getDate());
		assertEquals(time, dto.getTime());
		assertEquals(duration, dto.getDuration());
		assertEquals(1, dto.getPromoters().size());
		assertEquals(1000L, dto.getMaxNumberOfParticipants());
		assertEquals(500L, dto.getCurrentParticipants());
		assertEquals("ACTIVE", dto.getState());
		assertEquals(4.5, dto.getScore());
		assertTrue(dto.getBooked());
		assertEquals(now, dto.getCreationTime());
	}

	@Test
	void testSetters() {
		EventDTO dto = new EventDTO();
		LocalDate date = LocalDate.now();
		LocalTime time = LocalTime.now();
		LocalDateTime now = LocalDateTime.now();
		Duration duration = Duration.ofHours(2);
		PromoterDTO promoter = new PromoterDTO();

		dto.setId(1L);
		dto.setTitle("Concert");
		dto.setDescription("Amazing concert");
		dto.setImage("image.jpg");
		dto.setPlace("Stadium");
		dto.setDate(date);
		dto.setTime(time);
		dto.setDuration(duration);
		dto.setPromoters(List.of(promoter));
		dto.setMaxNumberOfParticipants(1000L);
		dto.setCurrentParticipants(500L);
		dto.setState("ACTIVE");
		dto.setScore(4.5);
		dto.setBooked(true);
		dto.setCreationTime(now);

		assertEquals(1L, dto.getId());
		assertEquals("Concert", dto.getTitle());
		assertEquals("Amazing concert", dto.getDescription());
		assertEquals("image.jpg", dto.getImage());
		assertEquals("Stadium", dto.getPlace());
		assertEquals(date, dto.getDate());
		assertEquals(time, dto.getTime());
		assertEquals(duration, dto.getDuration());
		assertEquals(1, dto.getPromoters().size());
		assertEquals(1000L, dto.getMaxNumberOfParticipants());
		assertEquals(500L, dto.getCurrentParticipants());
		assertEquals("ACTIVE", dto.getState());
		assertEquals(4.5, dto.getScore());
		assertTrue(dto.getBooked());
		assertEquals(now, dto.getCreationTime());
	}

	@Test
	void testEqualsAndHashCode() {
		LocalDate date = LocalDate.now();
		LocalTime time = LocalTime.now();
		LocalDateTime now = LocalDateTime.now();
		Duration duration = Duration.ofHours(2);
		PromoterDTO promoter = new PromoterDTO();

		EventDTO dto1 = new EventDTO(1L, "Concert", "Amazing concert", "image.jpg", "Stadium",
			date, time, duration, List.of(promoter), 1000L, 500L, "ACTIVE",
			4.5, true, now);

		EventDTO dto2 = new EventDTO(1L, "Concert", "Amazing concert", "image.jpg", "Stadium",
			date, time, duration, List.of(promoter), 1000L, 500L, "ACTIVE",
			4.5, true, now);

		assertEquals(dto1, dto2);
		assertEquals(dto1.hashCode(), dto2.hashCode());
	}

	@Test
	void testToString() {
		LocalDate date = LocalDate.now();
		LocalTime time = LocalTime.now();
		LocalDateTime now = LocalDateTime.now();
		Duration duration = Duration.ofHours(2);
		PromoterDTO promoter = new PromoterDTO();

		EventDTO dto = new EventDTO(1L, "Concert", "Amazing concert", "image.jpg", "Stadium",
			date, time, duration, List.of(promoter), 1000L, 500L, "ACTIVE",
			4.5, true, now);

		String toString = dto.toString();
		assertNotNull(toString);
		assertTrue(toString.contains("id=1"));
		assertTrue(toString.contains("title=Concert"));
		assertTrue(toString.contains("description=Amazing concert"));
		assertTrue(toString.contains("image=image.jpg"));
		assertTrue(toString.contains("place=Stadium"));
		assertTrue(toString.contains("maxNumberOfParticipants=1000"));
		assertTrue(toString.contains("currentParticipants=500"));
		assertTrue(toString.contains("state=ACTIVE"));
		assertTrue(toString.contains("score=4.5"));
		assertTrue(toString.contains("booked=true"));
	}
} 