package com.pareidolia.repository.model;

import com.pareidolia.entity.Event;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EventWithInfoTest {

	@Test
	void testNoArgsConstructor() {
		EventWithInfo eventWithInfo = new EventWithInfo();
		assertNull(eventWithInfo.getEvent());
		assertNull(eventWithInfo.getCurrentParticipants());
	}

	@Test
	void testAllArgsConstructor() {
		Event event = new Event();
		Long participants = 10L;

		EventWithInfo eventWithInfo = new EventWithInfo(event, participants);

		assertEquals(event, eventWithInfo.getEvent());
		assertEquals(participants, eventWithInfo.getCurrentParticipants());
	}

	@Test
	void testConstructorWithInteger() {
		Event event = new Event();
		Integer participants = 10;

		EventWithInfo eventWithInfo = new EventWithInfo(event, participants);

		assertEquals(event, eventWithInfo.getEvent());
		assertEquals(Long.valueOf(participants), eventWithInfo.getCurrentParticipants());
	}

	@Test
	void testConstructorWithNullParticipants() {
		Event event = new Event();
		Integer participants = null;

		EventWithInfo eventWithInfo = new EventWithInfo(event, participants);

		assertEquals(event, eventWithInfo.getEvent());
		assertNull(eventWithInfo.getCurrentParticipants());
	}

	@Test
	void testSettersAndGetters() {
		EventWithInfo eventWithInfo = new EventWithInfo();

		Event event = new Event();
		Long participants = 20L;

		eventWithInfo.setEvent(event);
		eventWithInfo.setCurrentParticipants(participants);

		assertEquals(event, eventWithInfo.getEvent());
		assertEquals(participants, eventWithInfo.getCurrentParticipants());
	}

	@Test
	void testEqualsAndHashCode() {
		Event event = new Event();
		Long participants = 10L;

		EventWithInfo eventWithInfo1 = new EventWithInfo(event, participants);
		EventWithInfo eventWithInfo2 = new EventWithInfo(event, participants);
		EventWithInfo eventWithInfo3 = new EventWithInfo(new Event(), 20L);

		assertEquals(eventWithInfo1, eventWithInfo2);
		assertEquals(eventWithInfo1.hashCode(), eventWithInfo2.hashCode());

		assertNotEquals(eventWithInfo1, eventWithInfo3);
		assertNotEquals(eventWithInfo1.hashCode(), eventWithInfo3.hashCode());
	}

	@Test
	void testToString() {
		Event event = new Event();
		Long participants = 10L;

		EventWithInfo eventWithInfo = new EventWithInfo(event, participants);
		String toString = eventWithInfo.toString();

		assertTrue(toString.contains("event=" + event));
		assertTrue(toString.contains("currentParticipants=" + participants));
	}
} 